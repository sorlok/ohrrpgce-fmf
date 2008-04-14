package ohrrpgce.henceforth;

import ohrrpgce.menu.Action;

/**
 * Wrapper for the "int" primitive that allows setting.
 * @author Seth N. Hetu
 */
public class Int {
	private int val;
	private Action onValueChanged;
	
	public Int(int value) {
		this(value, null);
	}
	
	public Int(int value, Action onValueChangedAction) {
		this.val = value;
		this.onValueChanged = onValueChangedAction;
	}
	
	public int getValue() {
		return this.val;
	}
	
	public void setValue(int value) {
		this.val = value;
		if (onValueChanged!=null)
			onValueChanged.perform(this);
	}
	
	public void plusOne() {
		this.val++;
		if (onValueChanged!=null)
			onValueChanged.perform(this);
	}
	
	public void minusOne() {
		this.val--;
		if (onValueChanged!=null)
			onValueChanged.perform(this);
	}

}
