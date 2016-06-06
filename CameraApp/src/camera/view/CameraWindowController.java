package camera.view;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import com.github.sarxos.webcam.Webcam;


import camera.util.Logger;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

public class CameraWindowController {

	@FXML
	private TilePane tilePane;

	@FXML
	private TextArea textArea;
	
	@FXML
	private Button start;
	
	@FXML
	private Button stop;

	private ImageView currentFrame;
	
	private Logger logger;
	
	

	
	static Image image = new Image("image.jpg");
	
	private ScheduledExecutorService camExecutor;

	public static boolean stopCamera;
	
	public void init() throws Exception {
		camExecutor = Executors.newScheduledThreadPool(2,
				new FirstLineThreadFactory("cam"));
	}
	
	
	
	public void stop() throws InterruptedException {
		camExecutor.shutdown();
		camExecutor.awaitTermination(3, TimeUnit.SECONDS);
		
		
	}
	
	public void startWebCamCamera() {
		stopCamera = false;
		InitCamera();
		
	}

	public void stopWebCamCamera() throws InterruptedException {
		stopCamera = true;
		tilePane.getChildren().clear();
		
		
	}


	@FXML
	private void initialize() {
		logger = new Logger(textArea);
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.log("init");

		InitCamera();

	}
	
	
	
	private void InitCamera() {
	
		
		ObservableList<WebCamInfo> options = FXCollections.observableArrayList();
		Integer webCamCounter = 0;

		
		camStart(new VBox(), camExecutor);
		
		
	}
	
	

	/*protected void startCamera(ImageView image, WebCamInfo cam) {
		
		ScheduledExecutorService timer = null;
		VideoCapture capture = new VideoCapture();
		
		boolean cameraActive = false;
		
		final CamLabel label = new CamLabel();
		
		tilePane.getChildren().add(image);

		
		if (!cameraActive) {
			// index kamery
			capture.open(cam.getWebCamIndex());

	
			if (capture.isOpened()) {
				cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						Image imageToShow = grabFrame(capture);
						image.setImage(imageToShow);
					}
				};

				timer = Executors.newSingleThreadScheduledExecutor();
				timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

			} else {
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			cameraActive = false;
			// update again the button content

			// stop the timer
			try {
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log the exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}

			
			capture.release();
		
			this.currentFrame.setImage(null);
		}
	}*/

	
	
	/**
	

	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private static Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	
	private class WebCamInfo {

		private String webCamName;
		private Integer webCamIndex;

		public String getWebCamName() {
			return webCamName;
		}

		public void setWebCamName(String webCamName) {
			this.webCamName = webCamName;
		}

		public int getWebCamIndex() {
			return webCamIndex;
		}

		public void setWebCamIndex(Integer index) {
			this.webCamIndex = index;
		}

		@Override
		public String toString() {
			return webCamName + " - nr: " + webCamIndex;
		}
	}

	
	
	
	public void camStart(final VBox monitoredLabel, ScheduledExecutorService executorService) {
		int webCamCounter = 0;
		for (Webcam webcam : Webcam.getWebcams()) {
			final CameraService service = new CameraService();
			final VideoCapture capture= new VideoCapture(webCamCounter);
			service.setExecutor(executorService);
			service.setPeriod(Duration.millis(200));
			service.setCapture(capture);
			
			
			final CamLabel progressMonitoredLabel = new CamLabel();
			progressMonitoredLabel.label.setText(Integer.toString(webCamCounter));
			progressMonitoredLabel.imageView.imageProperty().bind(service.imageProperty());
			tilePane.getChildren().add(progressMonitoredLabel);
			
			
		
			service.start();
		webCamCounter++;
		}
	}
	
	private class CamLabel extends VBox {
		final ImageView imageView;
		final Label label;
		
		
		
		public CamLabel() {
			super(0);
			imageView = new ImageView();
			label = new Label("label");
			
			imageView.setFitHeight(300);
			imageView.setFitWidth(350);
			
			getChildren().addAll(imageView, label);
		}
	}
	
	public static class CameraService extends ScheduledService<Void> {
		private VideoCapture capture;
		private ObjectProperty<Image> imageView = new SimpleObjectProperty<Image>();
		public final void setImage(Image value) {imageView.set(value);}
		public final Image getImage() {return imageView.get(); }
		public final ObjectProperty<Image> imageProperty() {return imageView; }
		public final void setCapture(VideoCapture capture) {this.capture = capture;}
		
		@Override
		protected Task<Void> createTask() {
		
			
			return new Task<Void>() {
			
				@Override
				protected Void call() throws Exception {
					
					if (!stopCamera) {
					
						if (capture.isOpened()) {
							
							Image imageToShow = grabFrame(capture);
							imageView.set(imageToShow);
							System.out.println("1");
						
						}} else {
							capture.release();
							this.cancel();
						}
					
					return null;
				}

			};
			
			
		}
		
	}
	
	
	 /* Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private static Image grabFrame(VideoCapture capture) {
	
		Image imageToShow = null;
		Mat frame = new Mat();

	
		if (capture.isOpened()) {
			try {
				
				capture.read(frame);

				if (!frame.empty()) {
					imageToShow = mat2Image(frame);
				}

			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return imageToShow;
	}


	static class FirstLineThreadFactory implements ThreadFactory {
	static final AtomicInteger poolNumber = new AtomicInteger(1);
	private final String type;
	
	public FirstLineThreadFactory(String type) {
		this.type = type;
	}
	
	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, "LineService" + poolNumber.getAndIncrement()+ "-thread-" + type);
		 thread.setDaemon(true);
		 
	      return thread;

	}
	
}
	
	
}
