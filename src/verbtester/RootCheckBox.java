package verbtester;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;

public class RootCheckBox extends JCheckBox {

	public RootCheckBox(Action a) {
		super(a);
		init();
	}

	public RootCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
		init();
	}

	public RootCheckBox(Icon icon) {
		super(icon);
		init();
	}

	public RootCheckBox(String text, boolean selected) {
		super(text, selected);
		init();
	}

	public RootCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		init();
	}

	public RootCheckBox(String text, Icon icon) {
		super(text, icon);
		init();
	}

	public RootCheckBox(String text) {
		super(text);
		init();
	}

	private static final long serialVersionUID = 7420502854359388037L;

	private List<SlaveCheckBox> slaves;
	boolean stateChangedFromSlave;
	
	public RootCheckBox() {
		super();
		init();
	}

	private void init() {
		stateChangedFromSlave = false;
		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!stateChangedFromSlave && slaves != null) {
					for(SlaveCheckBox s: slaves) {
						s.changeStateFromRoot(isSelected());
					}
				}
			}
		});
	}
	
	public void setSlaves(SlaveCheckBox[] theSlaves) {
		this.slaves = new ArrayList<SlaveCheckBox>();
		for(SlaveCheckBox s: theSlaves) {
			slaves.add(s);
		}
	}
	
	public void addSlave(SlaveCheckBox s) {
		slaves.add(s);
	}
	
	public void changeStateFromSlave(boolean isChecked) {
		stateChangedFromSlave = true;
		boolean allChecked = true;
		for(JCheckBox s : slaves) {
			if(!s.isSelected()) {
				allChecked = false;
			}
		}
		if(!allChecked) {
			setSelected(false);
		}
		stateChangedFromSlave = false;
	}
}
