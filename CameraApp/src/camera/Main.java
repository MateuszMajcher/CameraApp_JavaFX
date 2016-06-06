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

import camera.view.CameraWindowController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
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
		showCameraPanel();
		
	
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() 
		  {
		      public void handle(WindowEvent e){
		          System.out.println("test");  
		          try {
		        	  controller.stopCamera = true;
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

			Scene scene = new Scene(rootLayout, 800, 600);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
