package verbtester;

public class Verb {
	private String[] alakok;
	private boolean skipped;
	
	public Verb(String[] a) throws Exception {
		if(a.length < 5) {
			throw new Exception("Hiányos az ige...");
		}
		alakok = new String[5];
		for(int i = 0; i<5;++i) {
			alakok[i] = a[i];
		}
		skipped = false;
	}
	
	public String alak(int i) {
		return alakok[i];
	}
	
	public boolean isSkipped() {
		return skipped;
	}
	
	public void setSkipped(boolean skipped) {
		this.skipped = skipped;
	}
	
	@Override
	public String toString() {
		String ret = "";
		for(String s: alakok) {
			ret += s+" ";
		}
		return ret;
	}
}
