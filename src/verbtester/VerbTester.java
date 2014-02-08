package verbtester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
			verbsFileCanonicalPath = new File("verbs.csv").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readVerbsIn();
		curIndex = 0;
	}

	public VerbTester(String verbsFileName) {
		try {
			verbsFileCanonicalPath = new File(verbsFileName).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readVerbsIn();
		curIndex = 0;
	}

	public Verb getNext() {
		Verb ret = null;
		if (curIndex < /* 10 */verbs.size() && curIndex >= 0) {
			ret = verbs.get(curIndex);
			++curIndex;
		}
		return ret;
	}

	public void skip(Verb v) {
		verbs.remove(v);
		v.setSkipped(true);
		curIndex = (curIndex + 1) % verbs.size();
		verbs.add(v);
	}

	public void remove(int index) {
		verbs.remove(index);
		if (index < curIndex) {
			curIndex -= 1;
		}
	}

	public void remove(Verb v) {
		if (verbs.indexOf(v) < curIndex) {
			curIndex -= 1;
		}
		verbs.remove(v);
	}

	public void add(Verb v) {
		verbs.add(v);
	}

	public boolean contains(Verb v) {
		return verbs.contains(v);
	}

	public int verbMatchScore(Verb v, List<String> hints) {
		int ret = 0;
		if (contains(v)) {
			ret = 5;
			if (hints != null) {
				ret -= hints.size();
			}
		} else {
			for (Verb s : verbs) {
				int c = 0;
				boolean containsHint = false;
				for (int i = 0; i < 5; ++i) {
					if (s.alak(i).equals(v.alak(i))) {
						c += 1;
					}
					if (!containsHint && hints != null
							&& hints.contains(v.alak(i))) {
						c -= 1;
						containsHint = true;
					}
				}
				if (c > ret) {
					ret = c;
				}
			}
		}
		return ret;
	}

	private void readVerbsIn() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					verbsFileCanonicalPath));
			int i = 0;
			while (true && i < 50) {
				++i;
				String line = br.readLine();
				if (line == null)
					break;
				if (line.equals("") || line.startsWith("#"))
					continue;
				line = line.replaceAll("#.*", "");
				String[] v = line.split(";", 5);
				// Ha nincs meg mind az 5 alak,
				// 5 másodpercig megjelenítünk
				// egy panaszkodó ablakot.
				if (v.length != 5) {
					JOptionPane pane = new JOptionPane(
							"<html>A(z) "
									+ verbsFileCanonicalPath
									+ " fájl "
									+ i
									+ ". sora hiányos:<br>"
									+ line
									+ "<br>Az ott szereplő igét nem fogom használni.</html>",
							JOptionPane.INFORMATION_MESSAGE);
					final JDialog d = pane.createDialog("Hiányos ige");
					// 5 másodpercig megy jelenleg. Lehet ezt módosítani még,
					// ha esetleg ez is túl sok idő.
					new Timer().schedule(new TimerTask() {

						@Override
						public void run() {
							if (d != null) {
								d.setVisible(false);
							}
						}
					}, 5000); // <- ezt a számot kell módosítani :)
					d.setVisible(true);
				}
				// Eltároljuk jól
				verbs.add(new Verb(v));
			}
			br.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
