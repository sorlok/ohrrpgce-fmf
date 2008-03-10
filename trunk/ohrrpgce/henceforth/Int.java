package ohrrpgce.henceforth;

/**
 * Wrapper for the "int" primitive that allows setting.
 * @author Seth N. Hetu
 */
public class Int {
	private int val;
	
	public Int(int value) {
		this.val = value;
	}
	
	public int getValue() {
		return this.val;
	}
	
	public void setValue(int value) {
		this.val = value;
	}
	
	public void plusOne() {
		this.val++;
	}

}
