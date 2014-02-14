package verbtester;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;

public class VerbTesterWindow extends JFrame {

	public static enum GradeLimits {
		ONE(0), TWO(60), THREE(70), FOUR(80), FIVE(90);

		private final int val;

		private GradeLimits(int value) {
			val = value;
		}

		public int getValue() {
			return val;
		}

	}

	private static final long serialVersionUID = 2240584624816230669L;

	private VerbTester verbTester = null;
	private Verb[] currentVerbs = new Verb[10];
	private int[] shownFields = new int[10];

	private int curTestScore;
	private int curTestMaxScore = 0;

	private int gameScore = 0;
	private int gameMaxScore = 0;

	private JPanel topPanel;
	private JPanel rightPanel;
	private JPanel inputsPanel;
	// topPanelben 5*4 input, tömbbe rakjuk
	private JTextField[] inputs;

	private RootCheckBox checkAllCheckBox;
	private SlaveCheckBox[] skippers;
	// topPanel vége

	// legalul
	private JPanel bottomPanel;
	// bottomPanelben baloldalt
	private JPanel actionBtnsPanel;
	// actionBtnsPanel-ben balról-jobbra:
	private JButton gameControlBtn;
	private JButton hintBtn;
	// bottomPanelben középen kitöltve
	private JLabel infoLabel;

	// Menük
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu gameMenu;
	private JMenuItem openMenuItem;
	private JMenuItem quitMenuItem;
	private JMenuItem newGameMenuItem;
	private JMenuItem verbLimitsMenuItem;

	protected boolean currentGuessesChecked = false;
	protected boolean gameEnded = false;

	private ArrayList<Integer> grades;

	private int curVerbNum = 0;

	// bottomPanelben jobboldalt
	// private JButton helpBtn;

	private static enum GameControlState {
		NEED_CHECK, CAN_HAVE_NEXT, GAME_ENDED
	}

	private GameControlState gameState = GameControlState.NEED_CHECK;

	private String verbsFileName = "verbs.csv";

	static class GameConstants {
		public static String START = "Start";
		public static String NEXT = "Következő";
		public static String RESTART = "Újrakezdés";
		public static String CHECK = "Ellenőrzés";
		public static String HINT = "Segítségkérés";
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		// Alulra az ellenőriz/segítségkérés/következő gombok balra,
		// az infók középre, a súgógomb jobbra
		bottomPanel = new JPanel(new BorderLayout());
		actionBtnsPanel = new JPanel();
		// Az ellenőriz+segítségkérés+következő gombok egy panelbe balra
		gameControlBtn = new JButton(GameConstants.CHECK);
		gameControlBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (gameState) {
				case NEED_CHECK:
					checkGuesses();
					int curGrade = getGrade(curTestScore, curTestMaxScore);
					grades.add(curGrade);
					infoLabel.setText("Pontszám: " + curTestScore + "/"
							+ curTestMaxScore + " Jegy: " + curGrade);
					currentGuessesChecked = false;
					gameState = GameControlState.CAN_HAVE_NEXT;
					gameControlBtn.setText(GameConstants.NEXT);
					break;
				case CAN_HAVE_NEXT:
					getNewVerbs();
					curTestScore = 0;
					curTestMaxScore = 0;
					if (gameEnded) {
						hintBtn.setVisible(false);
						gameState = GameControlState.GAME_ENDED;
						infoLabel.setText("Teszt vége. Pontszám: "
								+ gameScore
								+ "/"
								+ gameMaxScore
								+ " ("
								+ String.format(
										"%.2f",
										(gameScore / (double) gameMaxScore) * 100.0)
								+ "%)");
						gameControlBtn.setText(GameConstants.RESTART);
					} else {
						gameState = GameControlState.NEED_CHECK;
						gameControlBtn.setText(GameConstants.CHECK);
						infoLabel.setText("");
					}
					break;
				case GAME_ENDED:
					startNewGame();
					hintBtn.setVisible(true);
					gameControlBtn.setText(GameConstants.CHECK);
					infoLabel.setText("");
					break;
				default:
					break;
				}
			}
		});
		hintBtn = new JButton(GameConstants.HINT);
		// TODO write listener for hintBtn

		actionBtnsPanel.add(gameControlBtn);
		actionBtnsPanel.add(hintBtn);
		bottomPanel.add(actionBtnsPanel, BorderLayout.CENTER);
		infoLabel = new JLabel("", SwingConstants.CENTER);
		infoLabel.setHorizontalTextPosition(JLabel.CENTER);
		infoLabel.setVerticalTextPosition(JLabel.CENTER);
		infoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		bottomPanel.add(infoLabel, BorderLayout.NORTH);

		initMenus();

		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout(5, 5));
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		inputs = new JTextField[5 * 10];
		rightPanel = new JPanel(new GridLayout(11, 1, 5, 5));

		// initCheckBoxes();

		inputsPanel = new JPanel(new GridLayout(11, 5, 5, 5));

		initInputs();

		topPanel.add(inputsPanel, BorderLayout.CENTER);
		add(topPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

	}

	private void initInputs() {
		String[] colNames = { "Infinitiv", "<html><p align='center'>Präsens<br/>E/3</p></html>", "Präteritum", "Perfekt",
				"Magyar" };
		for (String s : colNames) {
			inputsPanel.add(new JLabel(s, SwingConstants.CENTER));
		}
		for (int i = 0; i < 5 * 10; ++i) {
			inputs[i] = new JTextField();
			inputs[i].setHorizontalAlignment(SwingConstants.CENTER);
			inputs[i].setFocusable(true);
			inputs[i].setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			inputs[i].addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent arg0) {
					JTextComponent t = ((JTextComponent) arg0.getComponent());
					if (t != null) {
						t.setSelectionEnd(0);
					}
				}

				@Override
				public void focusGained(FocusEvent arg0) {
					JTextComponent t = ((JTextComponent) arg0.getComponent());
					if (t.getCaret() != null) {
						t.getCaret().setVisible(true);
					}
					t.setHighlighter(new DefaultHighlighter());
					t.setSelectionStart(0);
					t.setSelectionEnd(t.getText().length());
				}
			});
			inputsPanel.add(inputs[i]);
		}
	}

	private void initCheckBoxes() {
		checkAllCheckBox = new RootCheckBox("Kihagyás");
		checkAllCheckBox.setHorizontalTextPosition(SwingConstants.RIGHT);
		rightPanel.add(checkAllCheckBox);
		skippers = new SlaveCheckBox[10];
		for (int j = 0; j < 10; ++j) {
			skippers[j] = new SlaveCheckBox(checkAllCheckBox, j);
			// skippers[j].setText("Kihagyás");
			rightPanel.add(skippers[j]);
		}
		checkAllCheckBox.setSlaves(skippers);
		checkAllCheckBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (checkAllCheckBox.isSelected()) {
					gameState = GameControlState.CAN_HAVE_NEXT;
					gameControlBtn.setText(GameConstants.NEXT);
				} else {
					gameState = GameControlState.NEED_CHECK;
					gameControlBtn.setText(GameConstants.CHECK);
				}
			}
		});
		topPanel.add(rightPanel, BorderLayout.EAST);
	}

	private void initMenus() {
		menuBar = new JMenuBar();

		fileMenu = new JMenu("Fájl");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		openMenuItem = new JMenuItem("Megnyitás...");
		openMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (openFileChooser() == JFileChooser.APPROVE_OPTION) {
					startNewGame();
				}
			}
		});
		openMenuItem.setToolTipText("Igéket tartalmazó fájl megnyitása");
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));

		fileMenu.add(openMenuItem);
		fileMenu.addSeparator();

		quitMenuItem = new JMenuItem("Kilépés");
		quitMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selected = JOptionPane.showConfirmDialog(getRootPane(),
						"Tényleg abbahagyod a tanulást????", "NELUSTÁKOGGYÁ!!!!",
						JOptionPane.YES_NO_OPTION);
				if (selected == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				ActionEvent.CTRL_MASK));

		fileMenu.add(quitMenuItem);

		gameMenu = new JMenu("Játék");
		gameMenu.setMnemonic(KeyEvent.VK_J);
		menuBar.add(gameMenu);

		newGameMenuItem = new JMenuItem("Újrakezdés");
		newGameMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startNewGame();
			}
		});
		newGameMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));

		gameMenu.add(newGameMenuItem);

		verbLimitsMenuItem = new JMenuItem("Kérdezendő igék megadása");
		verbLimitsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				ActionEvent.CTRL_MASK));
		verbLimitsMenuItem
				.setToolTipText("Itt tudod megadni, hogy melyik igéket kérdezze ki");
		verbLimitsMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVerbLimits();
			}
		});

		gameMenu.add(verbLimitsMenuItem);

		getRootPane().setJMenuBar(menuBar);
	}

	protected void setVerbLimits() {
		VerbLimitsPrefWindow prefWin = new VerbLimitsPrefWindow(this,
				"Igék beállítása", true);
		prefWin.showDialog();
	}

	protected int openFileChooser() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"CSV kiterjesztésű fájlok", "csv");
		chooser.setFileFilter(filter);
		int retVal = chooser.showOpenDialog(this);
		if (retVal == JFileChooser.APPROVE_OPTION) {
			try {
				verbsFileName = chooser.getSelectedFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return retVal;
	}

	protected void getNewVerbs() {
		// Currently no skip option!
		// /* Skipped verbs go to the end */
		// for (int i = 0; i < skippers.length; ++i) {
		// if (skippers[i].isSelected()) {
		// currentVerbs[i].setSkipped(true);
		// getVerbTester().add(currentVerbs[i]);
		// continue;
		// }
		// }
		curVerbNum = 0;
		for (int i = 0; i < currentVerbs.length; ++i) {
			Verb v = getVerbTester().getRandom();
			if (v == null) {
				break;
			} else {
				++curVerbNum;
				currentVerbs[i] = v;
			}
		}
		if (curVerbNum == 0) {
			gameEnded = true;
		} else {
			for (JTextField f : inputs) {
				f.setVisible(true);
				f.setEditable(true);
				f.setEnabled(true);
				f.setText("");
				f.setForeground(UIManager.getColor("TextField.foreground"));
				f.setToolTipText(null);
			}
			// No checkboxes for now
			// for (SlaveCheckBox s : skippers) {
			// s.setVisible(true);
			// s.setEnabled(true);
			// s.setSelected(false);
			// }
			Random randgen = new Random(new Date().getTime());
			for (int i = 0; i < 10; ++i) {
				// If there are no more verbs, we disable their input fields
				if (i >= curVerbNum) {
					// No checkboxes
					// skippers[i].setVisible(false);
					for (int j = 0; j < 5; ++j) {
						inputs[i * 5 + j].setVisible(false);
					}
					continue;
				}
				int shown = randgen.nextInt(5);
				shownFields[i] = shown;
				inputs[i * 5 + shown].setEnabled(false);
				inputs[i * 5 + shown].setDisabledTextColor(UIManager
						.getColor("TextField.foreground"));
				inputs[i * 5 + shown].setText(currentVerbs[i].alak(shown));
			}
		}
		currentGuessesChecked = false;
	}

	protected void checkGuesses() {
		int curScore = 0;
		for (int i = 0; i < curVerbNum; ++i) {
			Verb v = new Verb();
			for (int j = 0; j < 5; ++j) {
				v.setAlak(j, inputs[i * 5 + j].getText());
			}
			curTestMaxScore += 1;
			if (getVerbTester().contains(v)) {
				curScore += 1;
			} else {
				// Ami meg volt adva azt nem számoljuk.
				// TODO Ha itt a segítségként megadottak stimmelnek, akkor
				// annyit
				// le kéne vonni.
				// Ez lehet 0 is, de 3 is.
				List<String> shown = new ArrayList<String>();
				shown.add(v.alak(shownFields[i]));
				if (getVerbTester().verbMatchScore(v, shown) == 4) {
					curScore += 1;
				}
			}
		}
		curTestScore += curScore;

		gameScore += curScore;
		gameMaxScore += curTestMaxScore;
	}

	private int getGrade(int score, int maxScore) {
		int grade = 0;
		double percent = (curTestScore / (double) curTestMaxScore) * 100.0;
		for (GradeLimits g : GradeLimits.values()) {
			if (percent >= g.getValue()) {
				++grade;
			} else if (grade == 0 && percent < g.getValue()) {
				break;
			}
		}
		return grade;
	}

	protected double getGradeAverage() {
		double ret = 0.0;
		int sum = 0;
		for (Integer i : grades) {
			sum += i;
		}
		ret = (double) sum / grades.size();
		return ret;
	}

	public void startNewGame() {
		if (verbTester == null) {
			setVerbTester(new VerbTester(verbsFileName));
		} else {
			verbTester.reset();
		}
		curTestScore = 0;
		curTestMaxScore = 0;
		gameScore = 0;
		gameMaxScore = 0;
		gameEnded = false;
		infoLabel.setText("");
		getNewVerbs();
		grades = new ArrayList<Integer>();
		gameState = GameControlState.NEED_CHECK;
	}

	public VerbTester getVerbTester() {
		return verbTester;
	}

	public void setVerbTester(VerbTester verbTester) {
		this.verbTester = verbTester;
	}

	public VerbTesterWindow() {
		super("Német rendhagyóige-kikérdező");
		initComponents();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(640, 480));
		startNewGame();
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				VerbTesterWindow win = new VerbTesterWindow();
				win.setVisible(true);
			}
		});
	}

	public int getFirstVerbIndex() {
		return verbTester.getFirstVerbIndex();
	}

	public int getVerbNum() {
		return verbTester.getVerbNum();
	}

	public int getNumVerbsToAsk() {
		return verbTester.getNumVerbsToAsk();
	}

	public void setFirstVerbIndex(int i) {
		verbTester.setFirstVerbIndex(i);
	}

	public void setNumVerbsToAsk(int i) {
		verbTester.setNumVerbsToAsk(i);
	}
}
