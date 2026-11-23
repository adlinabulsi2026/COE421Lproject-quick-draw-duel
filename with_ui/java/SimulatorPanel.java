import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// Buttons that pretend to be the two guns (used to test without hardware).
// Each button builds the same line the Arduino would send,
// e.g. "1,0,1,0,0", and gives it to the ZigBeeReader.
public class SimulatorPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private ZigBeeReader zigbee;
	private int[] holster = {0, 0, 0};   // index 0 is not used

	public SimulatorPanel(ZigBeeReader zigbee) {
		this.zigbee = zigbee;
		setLayout(new GridLayout(2, 1, 0, 4));
		setBackground(new Color(240, 222, 186));
		setBorder(BorderFactory.createTitledBorder("Simulated guns"));
		add(makeRow(1));
		add(makeRow(2));
	}

	private JPanel makeRow(final int id) {
		JPanel row = new JPanel(new GridLayout(1, 5, 6, 0));
		row.setOpaque(false);

		JLabel name = new JLabel("Player " + id, SwingConstants.CENTER);
		name.setFont(new Font("SansSerif", Font.BOLD, 14));

		JButton holsterButton = new JButton("Holster");
		JButton drawButton = new JButton("Draw");
		JButton hitButton = new JButton("Shoot (hit)");
		JButton missButton = new JButton("Shoot (miss)");

		holsterButton.addActionListener(e -> holsterGun(id));
		drawButton.addActionListener(e -> drawGun(id));
		hitButton.addActionListener(e -> shoot(id, true));
		missButton.addActionListener(e -> shoot(id, false));

		row.add(name);
		row.add(holsterButton);
		row.add(drawButton);
		row.add(hitButton);
		row.add(missButton);
		return row;
	}

	// line format: player,holster,trigger,hit
	private void send(int id, int trigger, int hit) {
		zigbee.inject(id + "," + holster[id] + "," + trigger + "," + hit);
	}

	private void holsterGun(int id) {
		holster[id] = 1;
		send(id, 0, 0);
	}

	private void drawGun(int id) {
		holster[id] = 0;
		send(id, 0, 0);   // gun leaves the holster
	}

	private void shoot(int id, boolean hitsTarget) {
		int other = 3 - id;
		send(id, 1, 0);       // trigger pressed
		send(id, 0, 0);       // trigger released
		if (hitsTarget) {
			send(other, 0, 1);   // the other player's receiver detects the IR
			send(other, 0, 0);
		}
	}
}
