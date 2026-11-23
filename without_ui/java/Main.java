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

		WinnerStrategy strategy;
		if (mode == 2) {
			strategy = new FastestDrawStrategy();
		} else {
			strategy = new FirstHitStrategy();
		}
		System.out.println("Mode: " + strategy.getName());

		// change COM6 to the port of the receiver XBee
		ZigBeeReader zigbee = new ZigBeeReader("COM6");
		GameController game = new GameController(zigbee, strategy);

		// observers that receive the values from the guns
		zigbee.registerObserver(game);
		zigbee.registerObserver(new EventLogger());

		zigbee.start();

		// TESTING ONLY: gun connected to the PC by USB instead of ZigBee.
		// Put its COM port here, or null when both ZigBees work.
		String usbGunPort = "COM7";
		if (usbGunPort != null) {
			ZigBeeReader usbGun = new ZigBeeReader(usbGunPort);
			usbGun.registerObserver(game);
			usbGun.registerObserver(new EventLogger());
			game.addUsbPort(usbGun);
			usbGun.start();
		}

		Thread gameThread = new Thread(game);
		gameThread.start();
	}
}
