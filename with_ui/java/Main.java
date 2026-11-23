import java.util.Scanner;

// Starts the game.
public class Main {
	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		System.out.println("WILD WEST QUICK-DRAW DUEL");
		System.out.println("Choose game mode:");
		System.out.println("1 = Duel (first hit wins)");
		System.out.println("2 = Reaction (fastest draw wins)");
		int mode = scanner.nextInt();

		System.out.println("Run in simulation mode without hardware? (y/n)");
		boolean simulation = scanner.next().equalsIgnoreCase("y");

		WinnerStrategy strategy;
		if (mode == 2) {
			strategy = new FastestDrawStrategy();
		} else {
			strategy = new FirstHitStrategy();
		}
		System.out.println("Mode: " + strategy.getName());

		ZigBeeReader zigbee;
		if (simulation) {
			zigbee = new ZigBeeReader();          // no serial port
		} else {
			zigbee = new ZigBeeReader("COM6");    // port of the receiver XBee
		}

		GameController game = new GameController(zigbee, strategy);
		GameWindow window = new GameWindow(game, simulation);
		window.setMode(strategy.getName());

		// views that hear about the game (Observer pattern)
		game.addView(new Scoreboard());
		game.addView(window);

		// observers that receive the values from the guns (Observer pattern)
		zigbee.registerObserver(game);
		zigbee.registerObserver(new EventLogger());
		zigbee.registerObserver(window);

		if (simulation) {
			window.addSimulator(new SimulatorPanel(zigbee));
		} else {
			zigbee.start();

			// Player 2's gun is connected to the PC by USB instead of ZigBee.
			// Put its COM port here, or null when both ZigBees work.
			String usbGunPort = "COM7";
			if (usbGunPort != null) {
				ZigBeeReader usbGun = new ZigBeeReader(usbGunPort);
				usbGun.registerObserver(game);
				usbGun.registerObserver(new EventLogger());
				usbGun.registerObserver(window);
				game.addUsbPort(usbGun);
				usbGun.start();
			}
		}

		Thread gameThread = new Thread(game);
		gameThread.start();
	}
}
