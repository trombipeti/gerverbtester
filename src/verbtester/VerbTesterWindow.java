package verbtester;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.Date;
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
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;

import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;

public class VerbTesterWindow extends JFrame {

	private static final long serialVersionUID = 2240584624816230669L;

	private VerbTester verbs;
	private Verb[] currentVerbs;

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
	private JButton checkNextBtn;
	private JButton hintBtn;
	// bottomPanelben középen kitöltve
	private JLabel infoLabel;

	protected boolean currentGuessesChecked = false;

	// bottomPanelben jobboldalt
	// private JButton helpBtn;

	private void initComponents() {
		setLayout(new BorderLayout());
		// Legfelső panel
		gameLabel = new JLabel("Klikk a Start gombra :)");
		gameLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
		gameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		gameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		topPanel = new JPanel();
		startBtn = new JButton("Start");
		startBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// currentVerb = verbs.getNext();
				topPanel.remove(startBtn);
				// Felső panel és a benne lévő inputok
				topPanel.setLayout(new BorderLayout(5, 5));
				topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
						10));
				inputs = new JTextField[5 * 5];
				JPanel rightPanel = new JPanel(new GridLayout(6, 1, 5, 5));
				checkAllCheckBox = new RootCheckBox();
				rightPanel.add(checkAllCheckBox);
				skippers = new SlaveCheckBox[5];
				for (int j = 0; j < 5; ++j) {
					skippers[j] = new SlaveCheckBox(checkAllCheckBox,j);
					rightPanel.add(skippers[j]);
				}
				checkAllCheckBox.setSlaves(skippers);
				topPanel.add(rightPanel, BorderLayout.EAST);

				JPanel leftPanel = new JPanel(new GridLayout(6, 5, 5, 5));
				// TODO ezeknek kell a normális oszlopnév!
				String[] colNames = { "Infinitive", "Második", "Harmadik",
						"Negyedik", "Magyar" };
				for (String s : colNames) {
					leftPanel.add(new JLabel(s, SwingConstants.CENTER));
				}
				for (int i = 0; i < 5 * 5; ++i) {
					inputs[i] = new JTextField();
					inputs[i].setHorizontalAlignment(SwingConstants.CENTER);
					inputs[i].setFocusable(true);
					inputs[i]
							.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
					inputs[i].addFocusListener(new FocusListener() {

						@Override
						public void focusLost(FocusEvent arg0) {
							JTextComponent t = ((JTextComponent) arg0
									.getComponent());
							if (t != null) {
								t.setSelectionEnd(0);
							}
						}

						@Override
						public void focusGained(FocusEvent arg0) {
							JTextComponent t = ((JTextComponent) arg0
									.getComponent());
							if (t.getCaret() != null) {
								t.getCaret().setVisible(true);
							}
							t.setHighlighter(new DefaultHighlighter());
							t.setSelectionStart(0);
							t.setSelectionEnd(t.getText().length());
						}
					});
					leftPanel.add(inputs[i]);
					// topPanel.add(Box.createRigidArea(new Dimension(0,5)));
				}
				topPanel.add(leftPanel, BorderLayout.CENTER);
				gameLabel.setText("");
				actionBtnsPanel.setVisible(true);
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
		checkNextBtn = new JButton("Ellenőrzés");
		checkNextBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentGuessesChecked) {
					getNewVerbs();
					currentGuessesChecked = false;
					checkNextBtn.setText("Ellenőrzés");
				} else {
					checkGuesses();
					currentGuessesChecked = true;
					checkNextBtn.setText("Következő");

				}
			}
		});
		hintBtn = new JButton("Segítségkérés");
		// TODO write listener for hintBtn

		actionBtnsPanel.add(checkNextBtn);
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

	protected void getNewVerbs() {
		for (int i = 0; i < currentVerbs.length; ++i) {
			currentVerbs[i] = verbs.getNext();
		}
		if (currentVerbs[0] == null) {
			// TODO end game
			System.out.println("Vége");
			actionBtnsPanel.setVisible(false);
		} else {
			for (JTextField f : inputs) {
				f.setEditable(true);
				f.setEnabled(true);
				f.setText("");
				f.setForeground(UIManager.getColor("TextField.Foreground"));
			}
			Random randgen = new Random(new Date().getTime());
			for (int i = 0; i < 5; ++i) {
				int shown = randgen.nextInt(5);
				inputs[i * 5 + shown].setEditable(false);
				inputs[i * 5 + shown].setForeground(Color.ORANGE);
				// inputs[shown].setEnabled(false);
				inputs[i * 5 + shown].setText(currentVerbs[i].alak(shown));
			}
		}
	}

	protected void checkGuesses() {
		int curscore = 0;
		for (int i = 0; i < 5; ++i) {
			Verb v = new Verb();
			for (int j = 0; j < 5; ++j) {
				v.setAlak(j, inputs[i * 5 + j].getText());
			}
			if (skippers[i].isSelected()) {
				v.setSkipped(true);
				verbs.add(v);
				continue;
			}
			maxscore += 4;
			if (verbs.contains(v)) {
				curscore += 4;
			} else {
				// Ami meg volt adva azt nem számoljuk.
				curscore += verbs.verbMatchScore(v) - 1;
			}
		}
		score += curscore;
		infoLabel.setText("Pontszám: " + score + "/" + maxscore + " ("
				+ String.format("%.2f", (score / (double) maxscore) * 100.0)
				+ "%)");
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

	protected void startNewGame() {
		verbs = new VerbTester();
		score = 0;
		maxscore = 0;
		getNewVerbs();
	}

	public VerbTesterWindow() {
		super("Német rendhagyóige-kikérdező");
		currentVerbs = new Verb[5];
		initComponents();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(500, 270));
	}

	public static void main(String[] args) {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
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
