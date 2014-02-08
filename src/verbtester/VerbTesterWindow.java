package verbtester;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

	private VerbTester verbs;
	private Verb[] currentVerbs = new Verb[10];
	private int[] shownFields = new int[10];

	private int score;
	private int maxscore = 0;

	private JLabel gameLabel;
	private JPanel topPanel;
	// topPanelben 5*4 input, tömbbe rakjuk
	private JTextField[] inputs;
	// Ha nem aktív a játék, akkor pedig egy nagy "Start" gomb van
	private JButton startBtn;

	private RootCheckBox checkAllCheckBox;
	private SlaveCheckBox[] skippers;
	// topPanel vége

	// topPanel alatt
	private JLabel eredmenyLabel;

	// legalul
	private JPanel bottomPanel;
	// bottomPanelben baloldalt
	private JPanel actionBtnsPanel;
	// actionBtnsPanel-ben balról-jobbra:
	private JButton gameControlBtn;
	private JButton hintBtn;
	// bottomPanelben középen kitöltve
	private JLabel infoLabel;

	protected boolean currentGuessesChecked = false;
	protected boolean gameEnded = false;

	private int curVerbNum = 0;

	// bottomPanelben jobboldalt
	// private JButton helpBtn;

	private static enum GameControlState {
		NEED_CHECK, CAN_HAVE_NEXT, GAME_ENDED
	}

	private GameControlState gameState = GameControlState.NEED_CHECK;

	static class GameConstants {
		public static String START = "Start";
		public static String NEXT = "Következő";
		public static String RESTART = "Újrakezdés";
		public static String CHECK = "Ellenőrzés";
		public static String HINT = "Segítségkérés";
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		// Legfelső panel
		gameLabel = new JLabel("Klikk a Start gombra :)");
		gameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
		gameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		gameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		topPanel = new JPanel();
		startBtn = new JButton(GameConstants.START);
		startBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				initGameComponents();
				startNewGame();
			}
		});
		topPanel.add(startBtn);
		JPanel vmi = new JPanel(new BorderLayout());
		JPanel topp = new JPanel();
		topp.setLayout(new BoxLayout(topp, BoxLayout.PAGE_AXIS));
		topp.add(gameLabel);
		topp.add(topPanel);
		vmi.add(topp, BorderLayout.NORTH);

		// Középre nagyban az ellenőrzés eredménye
		eredmenyLabel = new JLabel();
		vmi.add(eredmenyLabel, BorderLayout.CENTER);

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
					gameState = GameControlState.CAN_HAVE_NEXT;
					gameControlBtn.setText(GameConstants.NEXT);
					break;
				case CAN_HAVE_NEXT:
					getNewVerbs();
					if (gameEnded) {
						hintBtn.setVisible(false);
						gameState = GameControlState.GAME_ENDED;
						infoLabel.setText("Játék vége. Jegy: " + getGrade()
								+ " (" + (score / (double) maxscore) * 100.0
								+ "%)");
						gameControlBtn.setText(GameConstants.RESTART);
					} else {
						gameState = GameControlState.NEED_CHECK;
						gameControlBtn.setText(GameConstants.CHECK);
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
		actionBtnsPanel.setVisible(false);
		bottomPanel.add(actionBtnsPanel, BorderLayout.CENTER);
		infoLabel = new JLabel("", SwingConstants.CENTER);
		infoLabel.setHorizontalTextPosition(JLabel.CENTER);
		infoLabel.setVerticalTextPosition(JLabel.CENTER);
		infoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		bottomPanel.add(infoLabel, BorderLayout.NORTH);
		// helpBtn = new JButton("Súgó");
		// JPanel p = new JPanel();
		// p.add(helpBtn);
		// bottomPanel.add(p,BorderLayout.EAST);
		vmi.add(bottomPanel, BorderLayout.SOUTH);
		add(vmi, BorderLayout.CENTER);

		addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent arg0) {
				resizeFontsToFit();
			}

			@Override
			public void componentResized(ComponentEvent arg0) {
				resizeFontsToFit();
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
		});
	}

	protected void initGameComponents() {
		// currentVerb = verbs.getNext();
		topPanel.remove(startBtn);
		// Felső panel és a benne lévő inputok
		topPanel.setLayout(new BorderLayout(5, 5));
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		inputs = new JTextField[5 * 10];
		JPanel rightPanel = new JPanel(new GridLayout(11, 1, 5, 5));
		checkAllCheckBox = new RootCheckBox();
		rightPanel.add(checkAllCheckBox);
		skippers = new SlaveCheckBox[10];
		for (int j = 0; j < 10; ++j) {
			skippers[j] = new SlaveCheckBox(checkAllCheckBox, j);
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

		JPanel inputsPanel = new JPanel(new GridLayout(11, 5, 5, 5));
		// TODO ezeknek kell a normális oszlopnév!
		String[] colNames = { "Infinitive", "Második", "Harmadik", "Negyedik",
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
			// topPanel.add(Box.createRigidArea(new Dimension(0,5)));
		}
		topPanel.add(inputsPanel, BorderLayout.CENTER);
		gameLabel.setText("");
		actionBtnsPanel.setVisible(true);
	}

	protected void getNewVerbs() {
		/* Skipped verbs go to the end */
		for(int i = 0;i<skippers.length;++i) {
			if (skippers[i].isSelected()) {
				currentVerbs[i].setSkipped(true);
				verbs.add(currentVerbs[i]);
				continue;
			}
		}
		curVerbNum = 0;
		for (int i = 0; i < currentVerbs.length; ++i) {
			Verb v = verbs.getNext();
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
			}
			for (SlaveCheckBox s : skippers) {
				s.setVisible(true);
				s.setEnabled(true);
				s.setSelected(false);
			}
			Random randgen = new Random(new Date().getTime());
			for (int i = 0; i < 10; ++i) {
				// If there are no more verbs, we disable their input fields
				if (i >= curVerbNum) {
					skippers[i].setVisible(false);
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
			maxscore += 1;
			if (verbs.contains(v)) {
				curScore += 1;
			} else {
				// Ami meg volt adva azt nem számoljuk.
				// TODO Ha itt a segítségként megadottak stimmelnek, akkor
				// annyit
				// le kéne vonni.
				// Ez lehet 0 is, de 3 is.
				List<String> shown = new ArrayList<String>();
				shown.add(v.alak(shownFields[i]));
				if( verbs.verbMatchScore(v, shown) == 4) {
					curScore += 1;
				}
			}
		}
		score += curScore;
		infoLabel.setText("Pontszám: " + score + "/" + maxscore + " ("
				+ String.format("%.2f", (score / (double) maxscore) * 100.0)
				+ "%)");
		currentGuessesChecked = false;
	}

	protected void resizeFontsToFit() {
		// int h = getSize().height;
		// if (gameLabel != null) {
		// Font glFont = gameLabel.getFont();
		// gameLabel.setFont(new Font(glFont.getName(), glFont.getStyle(),
		// h / 10));
		// }
		// if (inputs == null)
		// return;
		// for (JTextField f : inputs) {
		// if (f != null) {
		// Font ff = f.getFont();
		// f.setFont(new Font(ff.getName(), ff.getStyle(), h / 20));
		// }
		// }
	}

	private int getGrade() {
		int grade = 0;
		double percent = (score / (double) maxscore) * 100.0;
		for (GradeLimits g : GradeLimits.values()) {
			if (percent >= g.getValue()) {
				++grade;
			} else if (grade == 0 && percent < g.getValue()) {
				break;
			}
		}
		return grade;
	}

	protected void startNewGame() {
		verbs = new VerbTester();
		score = 0;
		maxscore = 0;
		gameEnded = false;
		getNewVerbs();
		gameState = GameControlState.NEED_CHECK;
	}

	public VerbTesterWindow() {
		super("Német rendhagyóige-kikérdező");
		initComponents();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(500, 270));
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
}
