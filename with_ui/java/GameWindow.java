import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

// The game window (scoreboard on the screen).
// It is an Observer of the guns (shows each sensor live)
// and a GameView of the GameController (shows the rounds and the score).
public class GameWindow extends JFrame implements Observer, GameView {

	private static final long serialVersionUID = 1L;

	// colors of the Wild West theme
	private Color sand = new Color(240, 222, 186);
	private Color wood = new Color(92, 58, 30);
	private Color red = new Color(170, 30, 30);
	private Color green = new Color(40, 120, 50);
	private Color gray = new Color(190, 175, 150);

	private GameController game;

	private JLabel modeLabel = new JLabel("", SwingConstants.CENTER);
	private JLabel statusLabel = new JLabel("Starting...", SwingConstants.CENTER);
	private JTextArea log = new JTextArea(6, 40);

	// one of each for player 1 and player 2 (index 0 is not used)
	private JLabel[] winsLabel = new JLabel[3];
	private JLabel[] holsterLamp = new JLabel[3];
	private JLabel[] triggerLamp = new JLabel[3];
	private JLabel[] hitLamp = new JLabel[3];
	private JLabel[] lastLabel = new JLabel[3];
	private JLabel[] averageLabel = new JLabel[3];
	private JLabel[] historyLabel = new JLabel[3];

	private JPanel bottom = new JPanel(new BorderLayout());

	public GameWindow(GameController game, boolean simulation) {
		this.game = game;
		setTitle("Wild West Quick-Draw Duel");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout(10, 10));
		getContentPane().setBackground(sand);

		// top: title, mode and the big status text
		JPanel top = new JPanel(new GridLayout(3, 1));
		top.setBackground(wood);
		JLabel title = new JLabel("WILD WEST QUICK-DRAW DUEL", SwingConstants.CENTER);
		title.setFont(new Font("Serif", Font.BOLD, 30));
		title.setForeground(sand);
		modeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
		modeLabel.setForeground(sand);
		if (simulation) {
			modeLabel.setText("SIMULATION MODE (no hardware)");
		}
		statusLabel.setFont(new Font("Serif", Font.BOLD, 40));
		statusLabel.setOpaque(true);
		statusLabel.setBackground(sand);
		statusLabel.setForeground(wood);
		top.add(title);
		top.add(modeLabel);
		top.add(statusLabel);
		add(top, BorderLayout.NORTH);

		// middle: one panel per player
		JPanel players = new JPanel(new GridLayout(1, 2, 15, 0));
		players.setBackground(sand);
		players.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
		players.add(makePlayerPanel(1));
		players.add(makePlayerPanel(2));
		add(players, BorderLayout.CENTER);

		// bottom: mode buttons, round history (and simulator if used)
		JPanel modes = new JPanel();
		modes.setBackground(sand);
		JButton duelButton = new JButton("Duel mode (first hit wins)");
		JButton reactionButton = new JButton("Reaction mode (fastest draw wins)");
		duelButton.addActionListener(e -> changeMode(new FirstHitStrategy()));
		reactionButton.addActionListener(e -> changeMode(new FastestDrawStrategy()));
		modes.add(duelButton);
		modes.add(reactionButton);

		log.setEditable(false);
		log.setFont(new Font("Monospaced", Font.PLAIN, 13));
		log.setBackground(new Color(250, 242, 225));

		bottom.setBackground(sand);
		bottom.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
		bottom.add(modes, BorderLayout.NORTH);
		bottom.add(new JScrollPane(log), BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		setSize(new Dimension(900, 720));
		setLocationRelativeTo(null);
		setVisible(true);
	}

	// adds the buttons that pretend to be the guns
	public void addSimulator(SimulatorPanel simulator) {
		bottom.add(simulator, BorderLayout.SOUTH);
		revalidate();
	}

	// Strategy pattern: the game mode can be changed while the program runs
	private void changeMode(WinnerStrategy strategy) {
		game.setStrategy(strategy);
		setMode(strategy.getName());
	}

	public void setMode(String name) {
		String text = modeLabel.getText();
		if (text.startsWith("SIMULATION")) {
			modeLabel.setText("SIMULATION MODE (no hardware)  |  Mode: " + name);
		} else {
			modeLabel.setText("Mode: " + name);
		}
	}

	private JPanel makePlayerPanel(int id) {
		JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
		panel.setBackground(new Color(250, 242, 225));
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(wood, 4),
				BorderFactory.createEmptyBorder(8, 12, 8, 12)));

		JLabel name = new JLabel("PLAYER " + id, SwingConstants.CENTER);
		name.setFont(new Font("Serif", Font.BOLD, 26));
		name.setForeground(wood);
		panel.add(name);

		winsLabel[id] = new JLabel("Wins: 0", SwingConstants.CENTER);
		winsLabel[id].setFont(new Font("Serif", Font.BOLD, 36));
		winsLabel[id].setForeground(red);
		panel.add(winsLabel[id]);

		// three "lamps" that show the sensors live
		JPanel lamps = new JPanel(new GridLayout(1, 3, 4, 0));
		lamps.setOpaque(false);
		holsterLamp[id] = makeLamp("HOLSTER");
		triggerLamp[id] = makeLamp("TRIGGER");
		hitLamp[id] = makeLamp("HIT");
		lamps.add(holsterLamp[id]);
		lamps.add(triggerLamp[id]);
		lamps.add(hitLamp[id]);
		panel.add(lamps);

		lastLabel[id] = makeInfo("Last reaction: -");
		averageLabel[id] = makeInfo("Average reaction: -");
		historyLabel[id] = makeInfo("Last 5: -");
		panel.add(lastLabel[id]);
		panel.add(averageLabel[id]);
		panel.add(historyLabel[id]);
		return panel;
	}

	private JLabel makeLamp(String text) {
		JLabel lamp = new JLabel(text, SwingConstants.CENTER);
		lamp.setOpaque(true);
		lamp.setBackground(gray);
		lamp.setForeground(Color.WHITE);
		lamp.setFont(new Font("SansSerif", Font.BOLD, 12));
		return lamp;
	}

	private JLabel makeInfo(String text) {
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setFont(new Font("SansSerif", Font.PLAIN, 16));
		label.setForeground(wood);
		return label;
	}

	private void setLamp(JLabel lamp, boolean on, Color color) {
		if (on) {
			lamp.setBackground(color);
		} else {
			lamp.setBackground(gray);
		}
	}

	// ---------- Observer: called every time a gun sends its values ----------
	public void update(int id, int holster, int trigger, int hit, long time) {
		setLamp(holsterLamp[id], holster == 1, green);
		setLamp(triggerLamp[id], trigger == 1, wood);
		setLamp(hitLamp[id], hit == 1, red);
	}

	// ---------- GameView: called by the GameController ----------
	// long messages get a smaller font so they fit
	private void setStatusText(String text) {
		if (text.length() > 28) {
			statusLabel.setFont(new Font("Serif", Font.BOLD, 26));
		} else {
			statusLabel.setFont(new Font("Serif", Font.BOLD, 40));
		}
		statusLabel.setText(text);
	}

	public void showStatus(String phase, String message) {
		setStatusText(message);
		if (phase.equals("DRAW")) {
			statusLabel.setBackground(red);
			statusLabel.setForeground(Color.WHITE);
		} else if (phase.equals("COUNTDOWN")) {
			statusLabel.setBackground(wood);
			statusLabel.setForeground(sand);
		} else {
			statusLabel.setBackground(sand);
			statusLabel.setForeground(wood);
		}
		if (message.startsWith("New match")) {
			winsLabel[1].setText("Wins: 0");
			winsLabel[2].setText("Wins: 0");
		}
		log.append(message + "\n");
		log.setCaretPosition(log.getDocument().getLength());
	}

	public void showRound(int round, String result, Player p1, Player p2) {
		setStatusText(result);
		statusLabel.setBackground(sand);
		statusLabel.setForeground(green);
		showPlayer(p1);
		showPlayer(p2);
		log.append("---- Round " + round + ": " + result + " ----\n");
		log.setCaretPosition(log.getDocument().getLength());
	}

	public void showChampion(Player winner, Player p1, Player p2) {
		setStatusText("PLAYER " + winner.getId() + " IS THE FASTEST GUN IN THE WEST!");
		statusLabel.setBackground(new Color(212, 160, 40));
		statusLabel.setForeground(wood);
		log.append("**** PLAYER " + winner.getId() + " WINS THE MATCH "
				+ p1.getWins() + " - " + p2.getWins() + " ****\n");
		log.setCaretPosition(log.getDocument().getLength());
	}

	private void showPlayer(Player p) {
		int id = p.getId();
		winsLabel[id].setText("Wins: " + p.getWins());
		long last = p.getDrawTime();
		if (last >= 0) {
			lastLabel[id].setText("Last reaction: " + last + " ms");
		} else {
			lastLabel[id].setText("Last reaction: -");
		}
		averageLabel[id].setText("Average reaction: " + p.getAverage() + " ms");
		historyLabel[id].setText("Last 5: " + p.getLastFive() + "ms");
	}
}
