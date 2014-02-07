package verbtester;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SlaveCheckBox extends JCheckBox {

	private static final long serialVersionUID = -7448966512962552212L;
	
	private int id;
	private RootCheckBox root;
	
	private boolean changedFromRoot;

	private void init() {
		changedFromRoot = false;
		addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(!changedFromRoot) {
					root.changeStateFromSlave(isSelected());
				}
			}
		});
	}

	public SlaveCheckBox(RootCheckBox r, int _id) {
		super();
		setId(_id);
		root = r;
		init();
	}
	
	public void setRoot(RootCheckBox r) {
		root = r;
	}
	
	public void changeStateFromRoot(boolean s) {
		changedFromRoot = true;
		setSelected(s);
		changedFromRoot = false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	// Legenrált konstruktorok
//	public SlaveCheckBox() {
//		super();
//		init();
//	}
//
//	public SlaveCheckBox(Action a) {
//		super(a);
//		init();
//	}
//
//	public SlaveCheckBox(Icon icon, boolean selected) {
//		super(icon, selected);
//		init();
//	}
//
//	public SlaveCheckBox(Icon icon) {
//		super(icon);
//		init();
//	}
//
//	public SlaveCheckBox(String text, boolean selected) {
//		super(text, selected);
//		init();
//	}
//
//	public SlaveCheckBox(String text, Icon icon, boolean selected) {
//		super(text, icon, selected);
//		init();
//	}
//
//	public SlaveCheckBox(String text, Icon icon) {
//		super(text, icon);
//		init();
//	}
//
//	public SlaveCheckBox(String text) {
//		super(text);
//		init();
//	}

}
