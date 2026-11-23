import java.util.ArrayList;

// Stores everything about one player.
// Methods are synchronized because two threads use the players.
public class Player {

	private int id;
	private boolean holstered = false;

	// this round (times are in ms after DRAW, -1 means it did not happen)
	private long drawTime = -1;
	private long hitTime = -1;

	// whole session
	private int wins = 0;
	private ArrayList<Long> reactions = new ArrayList<Long>();

	public Player(int id) {
		this.id = id;
	}

	public synchronized void newRound() {
		drawTime = -1;
		hitTime = -1;
	}

	// a shot from this player counts only if the gun was drawn after DRAW
	public synchronized boolean canShoot() {
		if (drawTime < 0) {
			return false;
		}
		return true;
	}

	public synchronized void addReaction(long ms) {
		reactions.add(ms);
	}

	// reaction times of the last 5 rounds
	public synchronized String getLastFive() {
		String text = "";
		int start = reactions.size() - 5;
		if (start < 0) {
			start = 0;
		}
		for (int i = start; i < reactions.size(); i++) {
			text = text + reactions.get(i) + " ";
		}
		return text;
	}

	public synchronized long getAverage() {
		if (reactions.size() == 0) {
			return 0;
		}
		long sum = 0;
		for (int i = 0; i < reactions.size(); i++) {
			sum = sum + reactions.get(i);
		}
		return sum / reactions.size();
	}

	public synchronized int getId() { return id; }
	public synchronized boolean isHolstered() { return holstered; }
	public synchronized void setHolstered(boolean h) { holstered = h; }
	public synchronized long getDrawTime() { return drawTime; }
	public synchronized void setDrawTime(long t) { drawTime = t; }
	public synchronized long getHitTime() { return hitTime; }
	public synchronized void setHitTime(long t) { hitTime = t; }
	public synchronized int getWins() { return wins; }
	public synchronized void addWin() { wins++; }
	public synchronized void resetWins() { wins = 0; }
}
