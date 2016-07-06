package camera;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import camera.util.MailUtil;
import camera.view.CameraWindowController;
import camera.view.SetupWindowController;
import camera.view.WindowController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class Main extends Application {

	private Stage stage;
	private BorderPane rootLayout;
	private CameraWindowController controller;
	
	

	
	
	@Override
	public void start(Stage primaryStage) {
		this.stage = primaryStage;
		initRootLayout();
		MailUtil.init();
		showCameraPanel();
		
	
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
		  {
		      public void handle(WindowEvent e){
		          System.out.println("test");  
		          try {
		        	  //sto cam
		        	   controller.stop();
		               Platform.exit();
		          } 
		          catch (Exception e1) {
		               e1.printStackTrace();
		          }
		      }
		   });
	}

	public void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/Window.fxml"));

			rootLayout = (BorderPane) loader.load();
			WindowController controller = loader.getController();
			controller.setMainApp(this);
			Scene scene = new Scene(rootLayout, 800, 600);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean showSetupDialog() {
    	try {
    		FXMLLoader loader = new FXMLLoader();
    		loader.setLocation(Main.class.getResource("view/SetupWindow.fxml"));
    		AnchorPane page = (AnchorPane) loader.load();
    		
    		Stage dialogStage = new Stage();
    		dialogStage.setTitle("edit person");
    		dialogStage.initModality(Modality.WINDOW_MODAL);
    		dialogStage.initOwner(stage);
    		Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
           SetupWindowController controller = loader.getController();
            controller.setDialogStage(dialogStage);
         
            
            
            dialogStage.showAndWait();
            
            return true;
    	} catch (IOException e) {
    		e.printStackTrace();
    		return false;
    	}
    		
    		
    	}
	
	private void showCameraPanel() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/CameraWindow.fxml"));
			AnchorPane drawPanel;

			drawPanel = (AnchorPane) loader.load();
			controller = loader.getController();
			

			rootLayout.setCenter(drawPanel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
		System.out.println("mat = " + mat.dump());
		launch(args);
		
	}
	
	
	
}
