package ohrrpgce.data;

public class Passcode {
	public static final int PASSCODE_NONE = 0;
	public static final int PASSCODE_FIRST_STYLE = 1;
	public static final int PASSCODE_SECOND_SYTLE = 2;
	public static final int PASSCODE_THIRD_STYLE = 3;
	
	private int format;
	private int rotator;
	private byte[] encryptedPasscode;
	
	public Passcode(int format) {
		this.format = format;
	}
	
	public void setPasscode(byte[] encryptedCode) {
		this.encryptedPasscode = encryptedCode;
	}
	
	public void setRotator(int rotator) {
		this.rotator = rotator;
	}
	
	public boolean isProtected() {
		return format!=PASSCODE_NONE;
	}
	
	/**
	 * Check if the password a user entered is valid. Requires that rotator, format, and encryptedPasscode be set appropriately.
	 *  Although the documentation of the OHR implies an open passcode standard, for the latest version, we guarentee never
	 *  to leave the un-encrypted password in a string/char[] for easy spying. (We instead decrypt and check it letter-for-letter.) 
	 * @param pass The user-entered password
	 * @return true iff this password matches the encrypted one.
	 */
	public boolean checkPassword(String pass) {
		switch (format) {
			case PASSCODE_NONE:
				return true;
			case PASSCODE_THIRD_STYLE:
				int encrPos = 0;
				char chC;
				char pC;
				char minChar = ' ';
				for (int i=0; i<pass.length(); i++) {
					//Get characters
					pC = pass.charAt(i);
					do {
						chC = (char) (encryptedPasscode[encrPos] - rotator);
						encrPos++;
					} while (chC < minChar);
					
					//Check
					if (pC != chC)
						return false;
				}
				//Is the user-entered passcode too short?
				for (;encrPos<encryptedPasscode.length; encrPos++) { 
					if ((encryptedPasscode[encrPos] - rotator)>=minChar)
						return false;
				}
				return true;
			case PASSCODE_SECOND_SYTLE:
			case PASSCODE_FIRST_STYLE:
				throw new RuntimeException("Passcode checker not implemented for version: " + format);
			default:
				throw new RuntimeException("Invalid passcode format number: " + format);
		}
	}

}
