package ohrrpgce.adapter.applet;

import java.applet.Applet;

import ohrrpgce.web.Slackulator;

public class InputAdapter implements ohrrpgce.adapter.InputAdapter {

	//private int keyStates;
	private Slackulator parent;
	
	public InputAdapter(Slackulator parent) {
		this.parent = parent;
	}
	
	/**
	 * Call to set the key states; this will be returned whenever getKeyStates() requests so.
	 * @param combinedState The new key state(s)
	 */
	/*public void setKeyStates(int combinedState) {
		keyStates = combinedState;
	}*/
	
	public int getKeyStates() {
		return parent.getKeysAndFlush();
	}

}
