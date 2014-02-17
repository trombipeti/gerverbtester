package verbtester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class VerbTester {

	private ArrayList<Verb> verbs = new ArrayList<Verb>();
	private Set<Integer> askedVerbIDs = new TreeSet<Integer>();
	private String verbsFileCanonicalPath;
	private int curIndex;

	private int firstVerbIndex;
	private int numVerbsToAsk;

	public VerbTester() {
		try {
			verbsFileCanonicalPath = new File("verbs.csv").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readVerbsIn();
		curIndex = 0;
		firstVerbIndex = 0;
		numVerbsToAsk = verbs.size();
	}

	public VerbTester(String verbsFileName) {
		try {
			verbsFileCanonicalPath = new File(verbsFileName).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readVerbsIn();
		curIndex = 0;
		firstVerbIndex = 0;
		numVerbsToAsk = verbs.size();
	}

	public VerbTester(String verbsFileName, int _firstVerbIndex,
			int _lastVerbToAskIndex) {
		try {
			verbsFileCanonicalPath = new File(verbsFileName).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readVerbsIn();
		curIndex = _firstVerbIndex;
		this.firstVerbIndex = _firstVerbIndex;
		this.numVerbsToAsk = _lastVerbToAskIndex;
	}

	public VerbTester(int _firstVerbIndex, int _lastVerbToAskIndex) {
		try {
			verbsFileCanonicalPath = new File("verbs.csv").getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readVerbsIn();
		// TODO check for index boundaries and overflow
		curIndex = _firstVerbIndex;
		this.firstVerbIndex = _firstVerbIndex;
		this.numVerbsToAsk = _lastVerbToAskIndex;
	}

	public int getFirstVerbIndex() {
		return firstVerbIndex;
	}

	public void setFirstVerbIndex(int firstVerbIndex) {
		this.firstVerbIndex = firstVerbIndex;
	}

	public int getNumVerbsToAsk() {
		return numVerbsToAsk;
	}

	public void setNumVerbsToAsk(int n) {
		this.numVerbsToAsk = n;
	}

	public String getVerbsFileName() {
		return verbsFileCanonicalPath;
	}

	public int getVerbNum() {
		return verbs.size();
	}

	public Verb getNext() {
		Verb ret = null;
		while (true) {
			if (curIndex < firstVerbIndex + numVerbsToAsk
					&& curIndex >= firstVerbIndex) {
				ret = verbs.get(curIndex);
				++curIndex;
				// Ha már kérdeztük ezt az igét, akkor továbbmegyünk
				if (askedVerbIDs.contains(ret.getId())) {
					askedVerbIDs.add(ret.getId());
					continue;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return ret;
	}

	public Verb getRandom(boolean excludeTried) {
		Verb ret = null;
		int at = 0;
		Set<Integer> tried = new TreeSet<Integer>();
		Random randgen = new Random(System.currentTimeMillis());
		while (true) {
			at = randgen.nextInt(numVerbsToAsk) + firstVerbIndex;
			ret = verbs.get(at);
			if ((ret.isAsked() == false && !askedVerbIDs.contains(ret.getId()))
					|| excludeTried == false) {
				askedVerbIDs.add(ret.getId());
				ret.setAsked(true);
				verbs.set(at, ret);
				break;
			}
			tried.add(new Integer(at));
			// System.out.println(numVerbsToAsk+" - "+firstVerbIndex + " : " +
			// at);
			if (tried.size() >= numVerbsToAsk) {
				ret = null;
				break;
			}
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

	public void setVerbsFile(String s) throws IOException {
		verbsFileCanonicalPath = new File(s).getCanonicalPath();
		readVerbsIn();
	}

	public void reset() {
		curIndex = firstVerbIndex;
		askedVerbIDs.clear();
		verbs.clear();
		readVerbsIn();
		firstVerbIndex = (firstVerbIndex < verbs.size() ? firstVerbIndex : 0);
		numVerbsToAsk = (numVerbsToAsk <= verbs.size() ? numVerbsToAsk : verbs
				.size());
	}

	public ArrayList<Verb> getVerbsById(int id) {
		ArrayList<Verb> ret = new ArrayList<Verb>();
		for (Verb v : verbs) {
			if (v.getId() == id) {
				ret.add(v);
			}
		}
		return ret;
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
			while (true) {
				++i;
				String line = br.readLine();
				if (line == null)
					break;
				if (line.equals("") || line.startsWith("#"))
					continue;
				line = line.replaceAll("#.*", "");
				String[] v = line.split(";", 6);
				// Ha nincs meg mind az 5 alak + az ID,
				// 5 másodpercig megjelenítünk
				// egy panaszkodó ablakot.
				if (v.length != 6) {
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
