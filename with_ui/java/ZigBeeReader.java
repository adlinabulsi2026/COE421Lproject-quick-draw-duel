import java.util.ArrayList;

// Reads the lines sent by both guns through the XBee coordinator.
// Runs on its own thread and notifies all observers every time a line arrives.
public class ZigBeeReader implements Subject, Runnable {

	private SerialPortHandle sph = null;   // stays null in simulation mode
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

	// simulation mode: no serial port, lines come from the SimulatorPanel
	public ZigBeeReader() {
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
		if (sph == null) {
			if (c != 'P') {
				System.out.println("   (command to guns: " + c + ")");
			}
			return;
		}
		try {
			sph.writeByte((byte) c);
		} catch (Exception e) {
			System.out.println("Could not send " + c);
		}
	}

	// simulation mode: a line typed by the SimulatorPanel instead of a gun
	public void inject(String line) {
		handleLine(line);
	}

	public void run() {
		if (sph == null) {
			return;   // the port did not open, nothing to read
		}
		while (true) {
			try {
				String line = sph.readLine();
				if (line != null) {
					handleLine(line);
				}
			} catch (Exception e) {
				System.out.println("Read error: " + e.getMessage());
			}
		}
	}

	// line format: player,holster,trigger,hit
	private void handleLine(String line) {
		String[] v = line.trim().split(",");
		if (v.length != 4) {
			return;
		}

		try {
			playerId = Integer.parseInt(v[0]);
			holster = Integer.parseInt(v[1]);
			trigger = Integer.parseInt(v[2]);
			hit = Integer.parseInt(v[3]);
		} catch (NumberFormatException e) {
			return;   // broken line, ignore it
		}
		time = System.currentTimeMillis();

		if (playerId == 1 || playerId == 2) {
			notifyObservers();
		}
	}
}
