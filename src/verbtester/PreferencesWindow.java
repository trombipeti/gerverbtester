package verbtester;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PreferencesWindow extends JDialog {

	private static final long serialVersionUID = 4655825826795199940L;

	private VerbTesterWindow win;

	private JPanel panel;
	private JPanel prefPanel;
	private JPanel bottomPanel;

	private JLabel startText;
	private JComboBox<Integer> startCombo;
	private JLabel endText;
	private JComboBox<Integer> endCombo;

	private JButton saveButton;
	private JButton cancelButton;

	public PreferencesWindow(VerbTesterWindow aWin, String title,
			boolean modal) {
		super((JFrame) aWin, title, modal);
		win = aWin;
		initComponents();
		setResizable(false);
		pack();
	}

	public void showDialog() {
		setVisible(true);
	}

	private void initComponents() {
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		panel = new JPanel(new BorderLayout());

		prefPanel = new JPanel();

		startText = new JLabel("Első ige száma: ", SwingConstants.CENTER);
		startText.setVerticalTextPosition(SwingConstants.CENTER);
		prefPanel.add(startText);

		Vector<Integer> items1 = new Vector<>();
		int first = win.getFirstVerbIndex();

		for (int i = 0; i < win.getVerbNum(); ++i) {
			items1.add(i + 1);
		}
		startCombo = new JComboBox<Integer>(items1);
		startCombo.setSelectedIndex(first);
		startCombo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Vector<Integer> items = new Vector<Integer>();
				for (int i = startCombo.getSelectedIndex(); i < win
						.getVerbNum(); ++i) {
					items.add(i + 1);
				}
				endCombo.setModel(new DefaultComboBoxModel<Integer>(items));
				endCombo.setSelectedIndex(items.size() - 1);
			}
		});
		prefPanel.add(startCombo);

		endText = new JLabel("Utolsó ige száma: ", SwingConstants.CENTER);
		endText.setVerticalTextPosition(SwingConstants.CENTER);
		prefPanel.add(endText);

		Vector<Integer> items2 = new Vector<Integer>();
		for (int i = first; i < win.getVerbNum(); ++i) {
			items2.add(i + 1);
		}
		endCombo = new JComboBox<Integer>(items2);
		endCombo.setSelectedIndex(items2.size() - 1);
		prefPanel.add(endCombo);

		panel.add(prefPanel, BorderLayout.NORTH);
		
		JPanel p = new JPanel();
		// Középre
		p.setAlignmentY(0.5f);
		JLabel info = new JLabel("Az Mentés gombra kattintva újrakezdődik a teszt!");
		p.add(info);
		panel.add(p,BorderLayout.CENTER);

		bottomPanel = new JPanel(new GridLayout(1, 2, 5, 5));

		cancelButton = new JButton("Mégse");
		cancelButton.setMnemonic(KeyEvent.VK_G);
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		bottomPanel.add(cancelButton);

		saveButton = new JButton("Mentés");
		saveButton.setMnemonic(KeyEvent.VK_S);
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				applyChanges();
				setVisible(false);

			}
		});
		bottomPanel.add(saveButton);

		panel.add(bottomPanel, BorderLayout.SOUTH);

		add(panel);
	}

	protected void applyChanges() {
		win.setFirstVerbIndex((Integer) startCombo.getSelectedItem() - 1);
		win.setNumVerbsToAsk((Integer) endCombo.getSelectedItem()
				- (Integer) startCombo.getSelectedItem() + 1);
		win.startNewGame();
	}

}
