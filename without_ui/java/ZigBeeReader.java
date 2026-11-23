import java.util.ArrayList;

// Reads the lines sent by both guns through the XBee coordinator.
// Runs on its own thread and notifies all observers every time a line arrives.
public class ZigBeeReader implements Subject, Runnable {

	private SerialPortHandle sph;
	private ArrayList<Observer> observers = new ArrayList<Observer>();

	// values of the last line received
	private int playerId;
	private int holster;
	private int trigger;
	private int hit;
	private long time;

	public ZigBeeReader(String port) {
		try {
			sph = new SerialPortHandle(port);
		} catch (Exception e) {
			System.out.println("Could not open " + port);
		}
	}

	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	public void registerObserver(Observer o) {
		observers.add(o);
	}

	public void removeObserver(Observer o) {
		observers.remove(o);
	}

	public void notifyObservers() {
		for (int i = 0; i < observers.size(); i++) {
			Observer observer = observers.get(i);
			observer.update(playerId, holster, trigger, hit, time);
		}
	}

	// sends one command byte to both guns
	public synchronized void send(char c) {
		try {
			sph.writeByte((byte) c);
		} catch (Exception e) {
			System.out.println("Could not send " + c);
		}
	}

	public void run() {
		while (true) {
			try {
				String line = sph.readLine();
				if (line == null) {
					continue;
				}

				// line format: player,holster,trigger,hit
				String[] v = line.trim().split(",");
				if (v.length != 4) {
					continue;
				}

				playerId = Integer.parseInt(v[0]);
				holster = Integer.parseInt(v[1]);
				trigger = Integer.parseInt(v[2]);
				hit = Integer.parseInt(v[3]);
				time = System.currentTimeMillis();

				if (playerId == 1 || playerId == 2) {
					notifyObservers();
				}
			} catch (NumberFormatException e) {
				// broken line, ignore it
			} catch (Exception e) {
				System.out.println("Read error: " + e.getMessage());
			}
		}
	}
}
