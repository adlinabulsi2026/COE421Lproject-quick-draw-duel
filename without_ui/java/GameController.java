import java.awt.Toolkit;
import java.util.Random;

// Controls the whole game.
// It is an Observer of the ZigBeeReader (gets the sensor values)
// and runs the rounds on its own thread.
public class GameController implements Observer, Runnable {

	// game settings
	private final int ROUNDS_TO_WIN = 3;          // best of 5
	private final int MIN_COUNTDOWN = 3000;       // random countdown between
	private final int MAX_COUNTDOWN = 10000;      // 3 and 10 seconds
	private final int ROUND_TIME = 5000;          // 5 s after DRAW to finish the round
	private final int RESPONSE_WINDOW = 500;      // time for the other player to shoot back

	private Player p1 = new Player(1);
	private Player p2 = new Player(2);
	private ZigBeeReader zigbee;
	private ZigBeeReader usbPort = null;   // extra port for a gun connected by USB (testing)
	private WinnerStrategy strategy;
	private Scoreboard scoreboard = new Scoreboard();
	private Random random = new Random();

	private String phase = "WAITING";   // WAITING, COUNTDOWN, DRAW, OVER
	private long drawStart = 0;         // PC time when DRAW was sent
	private int foulPlayer = 0;         // player who drew too early
	private int round = 1;

	public GameController(ZigBeeReader zigbee, WinnerStrategy strategy) {
		this.zigbee = zigbee;
		this.strategy = strategy;
	}

	// used when one gun is connected by USB instead of ZigBee
	public void addUsbPort(ZigBeeReader usbPort) {
		this.usbPort = usbPort;
	}

	// sends a command to the guns through ZigBee (and USB if used)
	private void send(char c) {
		zigbee.send(c);
		if (usbPort != null) {
			usbPort.send(c);
		}
	}

	public void setStrategy(WinnerStrategy strategy) {
		this.strategy = strategy;
	}

	// called by the ZigBeeReader thread every time a gun sends a line
	public synchronized void update(int id, int holster, int trigger, int hit, long time) {
		Player p = getPlayer(id);
		Player other = getOther(p);
		boolean wasHolstered = p.isHolstered();
		p.setHolstered(holster == 1);

		if (phase.equals("COUNTDOWN")) {
			// drawing before DRAW is a foul
			if (wasHolstered && holster == 0 && foulPlayer == 0) {
				foulPlayer = id;
			}
		}

		else if (phase.equals("DRAW")) {
			long t = time - drawStart;

			// reaction time = DRAW until the gun leaves the holster
			if (wasHolstered && holster == 0 && p.getDrawTime() < 0) {
				p.setDrawTime(t);
				System.out.println("Player " + id + " drew after " + t + " ms");
			}

			// this player got hit, so the other player is the shooter
			if (hit == 1 && p.getHitTime() < 0) {
				if (other.canShoot()) {
					p.setHitTime(t);
					System.out.println("Player " + other.getId() + " hit player " + id + " at " + t + " ms");
				} else {
					System.out.println("Hit on player " + id + " does not count (shooter did not draw)");
				}
			}
		}
	}

	// the game loop, runs on its own thread
	public void run() {
		while (true) {
			waitForHolsters();

			// countdown
			startCountdown();
			int countdown = MIN_COUNTDOWN + random.nextInt(MAX_COUNTDOWN - MIN_COUNTDOWN + 1);
			int passed = 0;
			while (passed < countdown && getFoulPlayer() == 0) {
				sleep(10);
				passed = passed + 10;
			}

			if (getFoulPlayer() != 0) {
				int cheater = getFoulPlayer();
				int winner = 3 - cheater;
				setPhase("OVER");
				finishRound(winner, "Player " + cheater + " drew too early - FOUL! Player " + winner + " wins");
				continue;
			}

			// DRAW!
			startDraw();
			while (System.currentTimeMillis() - drawStart < ROUND_TIME && !strategy.isRoundDone(p1, p2)) {
				sleep(10);
			}

			// give the other player 500 ms to shoot back
			if (strategy.isRoundDone(p1, p2)) {
				sleep(RESPONSE_WINDOW);
			}

			setPhase("OVER");
			int winner = strategy.pickWinner(p1, p2);
			if (winner == 0) {
				finishRound(0, "Tie - no winner");
			} else {
				finishRound(winner, "Player " + winner + " wins");
			}
		}
	}

	// waits until both guns are in the holster for 1 second
	private void waitForHolsters() {
		setPhase("WAITING");
		System.out.println("Round " + round + ": holster both guns to start");

		int waited = 0;
		while (true) {
			if (waited % 2000 == 0) {
				send('P');   // ask both guns to send their values
			}
			if (p1.isHolstered() && p2.isHolstered()) {
				sleep(1000);
				if (p1.isHolstered() && p2.isHolstered()) {
					return;
				}
			}
			sleep(100);
			waited = waited + 100;
		}
	}

	private synchronized void startCountdown() {
		p1.newRound();
		p2.newRound();
		foulPlayer = 0;
		phase = "COUNTDOWN";
		System.out.println("Steady...");
		Toolkit.getDefaultToolkit().beep();
	}

	private synchronized void startDraw() {
		drawStart = System.currentTimeMillis();
		phase = "DRAW";
		send('D');
		Toolkit.getDefaultToolkit().beep();
		System.out.println("DRAW!!!");
	}

	private void finishRound(int winner, String result) {
		// save reaction times
		if (p1.getDrawTime() >= 0) {
			p1.addReaction(p1.getDrawTime());
		}
		if (p2.getDrawTime() >= 0) {
			p2.addReaction(p2.getDrawTime());
		}

		if (winner != 0) {
			getPlayer(winner).addWin();
		}

		// tell the guns: '1' or '2' = winner, '0' = tie
		send((char) ('0' + winner));

		scoreboard.printRound(round, result, p1, p2);
		round++;

		// check if someone won the match
		Player champion = null;
		if (p1.getWins() >= ROUNDS_TO_WIN) {
			champion = p1;
		}
		if (p2.getWins() >= ROUNDS_TO_WIN) {
			champion = p2;
		}

		if (champion != null) {
			scoreboard.printChampion(champion, p1, p2);
			Toolkit.getDefaultToolkit().beep();
			p1.resetWins();
			p2.resetWins();
			round = 1;
			System.out.println("New match starting...");
		}

		sleep(3000);
	}

	private synchronized void setPhase(String newPhase) {
		phase = newPhase;
	}

	private synchronized int getFoulPlayer() {
		return foulPlayer;
	}

	private Player getPlayer(int id) {
		if (id == 1) {
			return p1;
		}
		return p2;
	}

	private Player getOther(Player p) {
		if (p == p1) {
			return p2;
		}
		return p1;
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
