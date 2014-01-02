package verbtester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class VerbTester {

	private ArrayList<Verb> verbs = new ArrayList<Verb>();
	private String verbsFileCanonicalPath;
	private int curIndex;

	public VerbTester() {
		try {
			verbsFileCanonicalPath = (new File(System.getProperty("user.home"),
					"VerbTester")).getCanonicalPath();
			verbsFileCanonicalPath = new File(verbsFileCanonicalPath,
					"verbs.csv").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readVerbsIn();
		curIndex = 0;
	}

	public Verb getNext() {
		Verb ret = null;
		if(curIndex < verbs.size()) {
			ret = verbs.get(curIndex);
			curIndex = (curIndex + 1)%verbs.size();
		}
		return ret;
	}

	public void skip(Verb v) {
		verbs.remove(v);
		v.setSkipped(true);
		curIndex = (curIndex + 1)%verbs.size();
		verbs.add(v);
	}
	
	public void remove(int index) {
		verbs.remove(index);
		if(index < curIndex) {
			curIndex -= 1;
		}
	}
	
	public void remove(Verb v) {
		if(verbs.indexOf(v) < curIndex) {
			curIndex -= 1;
		}
		verbs.remove(v);
	}

	private void readVerbsIn() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					verbsFileCanonicalPath));
			int i = 0;
			while (true) {
				++i;
				String line = br.readLine();
				if (line == null)
					break;
				if (line.equals("") || line.startsWith("#"))
					continue;
				line = line.replaceAll("#.*", "");
				String[] v = line.split(";", 5);
				if (v.length != 5) {
					JOptionPane pane = new JOptionPane(
							"<html>A "
									+ verbsFileCanonicalPath
									+ " fájl "
									+ i
									+ ". sora hiányos:<br>"
									+ line
									+ "<br>Az ott szereplő igét nem fogom használni.</html>",
							JOptionPane.INFORMATION_MESSAGE);
					final JDialog d = pane.createDialog("Hiányos ige");
					new Timer().schedule(new TimerTask() {

						@Override
						public void run() {
							if (d != null) {
								d.setVisible(false);
							}
						}
					}, 5000);
					d.setVisible(true);
				}
				verbs.add(new Verb(v));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
