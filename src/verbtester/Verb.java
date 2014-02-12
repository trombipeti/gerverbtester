package verbtester;

public class Verb {
	private String[] alakok;
	private boolean skipped;
	private boolean asked;
	
	public Verb() {
		skipped = false;
		asked = false;
		alakok = new String[5];
	}
	
	public Verb(String[] a) throws Exception {
		if(a.length < 5) {
			throw new Exception("Hiányos az ige...");
		}
		alakok = new String[5];
		for(int i = 0; i<5;++i) {
			alakok[i] = a[i];
		}
		skipped = false;
		asked = false;
	}
	
	public String alak(int i) {
		return alakok[i];
	}
	
	public void setAlak(int i, String alak) {
		alakok[i] = alak;
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

	public boolean isAsked() {
		return asked;
	}
	

	public void setAsked(boolean asked) {
		this.asked = asked;
	}
}
