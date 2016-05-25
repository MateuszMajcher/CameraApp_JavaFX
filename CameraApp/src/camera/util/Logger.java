package camera.util;

import javafx.scene.control.TextArea;

public class Logger {
	
	private TextArea text;
	
	public Logger(){}
	public Logger(TextArea text) {
		this.text = text;
	}
	
	public void log(String message) {
		text.appendText(message + "\n");
	}
	
	public void clearLog() {
		text.clear();
	}

}
