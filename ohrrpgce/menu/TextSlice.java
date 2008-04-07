package ohrrpgce.menu;

public class TextSlice extends MenuSlice {
	
	//Text properties
	private String rawText;
	private boolean autoDiet;
	
	//Delayed drawing of text
	private boolean calcdText;
	
	
	public TextSlice(MenuFormatArgs mForm, String text, boolean autoDiet) {
		super(mForm);
		this.autoDiet = autoDiet;
		
		setText(text);
	}

	
	
	private void calculateText() {
		//Necessary?
		if (getWidth()==0 || getHeight()==0)
			return;
		
		//Figure out... how?
		????????
		
		//Done... for now!
		this.calcdText = true;
	}
	
	
	
	
	public void setText(String text) {
		this.rawText = text;
		calculateText();
	}

	
	protected void setWidth(int newWidth) {
		super.setWidth(newWidth);
		calculateText();
	}
	
	protected void setHeight(int newHeight) {
		super.setHeight(newHeight);
		calculateText();
	}


}
