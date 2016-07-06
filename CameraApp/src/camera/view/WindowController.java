package camera.view;

import camera.Main;
import javafx.fxml.FXML;

public class WindowController {
	
	private Main mainApp;
	
	
	@FXML
	private void showSettings() {
		
		mainApp.showSetupDialog();
		System.out.println("dodano kamere");
	}
	
	@FXML
	private void showInfo() {
		System.out.println("dodano kamere");
	}
	
	@FXML
	private void close() {
		System.out.println("dodano kamere");
	}
	
	public void setMainApp(Main mainApp) {
		this.mainApp = mainApp;
	}

}
