package verbtester;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
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

public class VerbTesterWindow extends JFrame {

	private static final long serialVersionUID = 2240584624816230669L;

	private VerbTester verbs;
	private Verb currentVerb;

	private int score;
	private int maxscore = 0;

	private JLabel gameLabel;
	private JPanel topPanel;
	// topPanelben sorban 4 input, tömbbe rakjuk
	private JTextField[] inputs;
	// Ha nem aktív a játék, akkor pedig egy nagy "Start" gomb van
	private JButton startBtn;
	// topPanel vége

	// topPanel alatt
	private JLabel eredmenyLabel;

	// legalul
	private JPanel bottomPanel;
	// bottomPanelben baloldalt
	private JPanel actionBtnsPanel;
	// actionBtnsPanel-ben balról-jobbra:
	private JButton checkBtn;
	private JButton hintBtn;
	private JButton nextBtn;
	// bottomPanelben középen kitöltve
	private JLabel infoLabel;

	// bottomPanelben jobboldalt
	// private JButton helpBtn;

	private void initComponents() {
		JPanel vmi = new JPanel(new BorderLayout());
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
				// TODO Az inputok fölé kéne egy-egy label a nevével
				// Felső panel és a benne lévő inputok
				topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
				topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				inputs = new JTextField[5];
				for (int i = 0; i < 5; ++i) {
					inputs[i] = new JTextField();
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
					topPanel.add(inputs[i]);
					topPanel.add(Box.createRigidArea(new Dimension(0,5)));
				}
				gameLabel.setText("");
				actionBtnsPanel.setVisible(true);
				getNewVerb();
				repaint();
				validate();
			}
		});
		topPanel.add(startBtn);
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
		checkBtn = new JButton("Ellenőriz");
		checkBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int curscore = 0;
				int i = 0;
				for (JTextField f : inputs) {
					if (f.isEditable() == false) {
						++i;
						continue;
					}
					if (f.getText().toLowerCase()
							.equals(currentVerb.alak(i).toLowerCase())) {
						curscore += 1;
					}
					++i;
				}
				if (currentVerb.isSkipped() == false) {
					maxscore += 4;
					score += curscore;
				}
				infoLabel.setText("Pontszám: " + score + "/" + maxscore + " ("
						+ String.format("%.2f",(score / (double) maxscore) * 100.0) + "%)");
				// if (score == 1.0) {
				// verbs.remove(currentVerb);
				// } else {
				// verbs.skip(currentVerb);
				// }
				verbs.remove(currentVerb);
				getNewVerb();
			}
		});
		hintBtn = new JButton("Segítségkérés");
		nextBtn = new JButton("Átugrás");
		nextBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (currentVerb != null) {
					verbs.skip(currentVerb);
				}
				getNewVerb();
			}
		});
		actionBtnsPanel.add(checkBtn);
		actionBtnsPanel.add(hintBtn);
		actionBtnsPanel.add(nextBtn);
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

	protected void getNewVerb() {
		currentVerb = verbs.getNext();
		if (currentVerb == null) {
			// TODO end game
			System.out.println("Vége");
			actionBtnsPanel.setVisible(false);
		} else {
			Random randgen = new Random(new Date().getTime());
			int shown = randgen.nextInt(5);
			for (JTextField f : inputs) {
				f.setEditable(true);
				f.setEnabled(true);	
				f.setText("");
				f.setForeground(UIManager.getColor("TextField.Foreground"));
			}
			inputs[shown].setEditable(false);
			inputs[shown].setForeground(Color.ORANGE);
//			inputs[shown].setEnabled(false);
			inputs[shown].setText(currentVerb.alak(shown));
			if (currentVerb.isSkipped()) {
				nextBtn.setEnabled(false);
			}
		}
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

	public VerbTesterWindow() {
		super("Német rendhagyóige-kikérdező");
		verbs = new VerbTester();
		initComponents();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setSize(new Dimension(640, 480));
		setMinimumSize(new Dimension(270, 270));
		score = 0;
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
