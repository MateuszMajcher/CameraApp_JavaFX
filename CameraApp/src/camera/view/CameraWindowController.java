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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

public class CameraWindowController {

	@FXML
	private TilePane tilePane;

	@FXML
	private TextArea textArea;

	private ImageView currentFrame;
	
	private Logger logger;
	
	
	
	/*private static final String[] URLs = {
		    "http://www.google.com", 
		    "http://www.yahoo.com"
		  };*/
	
	Image image = new Image("image.jpg");
	
	private ScheduledExecutorService parFirstLineExecutor;
	
	public void init() throws Exception {
		parFirstLineExecutor = Executors.newScheduledThreadPool(URLs.length,
				new FirstLineThreadFactory("cam"));
	}
	
	
	
	public void stop() throws InterruptedException {
		parFirstLineExecutor.shutdown();
		parFirstLineExecutor.awaitTermination(3, TimeUnit.SECONDS);
		
		
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
		System.out.println(webCamCounter);
		for (Webcam webcam : Webcam.getWebcams()) {
			WebCamInfo webCamInfo = new WebCamInfo();
			webCamInfo.setWebCamIndex(webCamCounter);
			webCamInfo.setWebCamName(webcam.getName());
			options.add(webCamInfo);
			logger.log(webcam.getName());
			startCamera(createCameraWindow(), webCamInfo);
			webCamCounter++;
		}
		
		fetchFirstLine(new VBox(), parFirstLineExecutor);
		
		
	}
	
	
	void start(ImageView image, WebCamInfo cam) {
		boolean cameraActive = false;

		tilePane.getChildren().add(createCameraPane(cam.getWebCamName(), image));
	}

	

	protected void startCamera(ImageView image, WebCamInfo cam) {
		
		ScheduledExecutorService timer = null;
		VideoCapture capture = new VideoCapture();
		
		boolean cameraActive = false;
		
		
		
		tilePane.getChildren().add(createCameraPane(cam.getWebCamName(), image));

		
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

				//timer = Executors.newSingleThreadScheduledExecutor();
				//timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

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
	}

	
	
	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Image grabFrame(VideoCapture capture) {
	
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

	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	
	private ImageView createCameraWindow() {
		
		ImageView c = new ImageView();
		c.setFitHeight(300);
		c.setFitWidth(350);
		return c;
	}
	
	private AnchorPane createCameraPane(String name, ImageView image) {
		AnchorPane pane = new AnchorPane();
		pane.getChildren().addAll(image, new Label(name));
		pane.setMaxWidth(350);
		pane.setMaxHeight(320);
		return pane;
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

	
	
	
	public void fetchFirstLine(final VBox monitoredLabel, ScheduledExecutorService executorService) {
		for (Webcam webcam : Webcam.getWebcams()) {
			final FirstLineService service = new FirstLineService();
		
			service.setExecutor(executorService);

			service.setPeriod(Duration.millis(100));
			service.setUrl(webcam.getName());
			
			service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				
				@Override
				public void handle(WorkerStateEvent arg0) {
				
					
				}
			});
			
			service.start();
		}
	}
	
	public static class FirstLineService extends ScheduledService<Void> {
		private StringProperty url = new SimpleStringProperty(this, "url");
		public final void setUrl(String value) {url.set(value);}
		public final String getUrl() {return url.get(); }
		public final StringProperty urlProperty() {return url; }
		
		
		@Override
		protected Task<Void> createTask() {
			
			return new Task<Void>() {
				
				@Override
				protected Void call() throws Exception {
					updateMessage("Called on thread: " + Thread.currentThread().getName());
					
					
					String result = "";
					
				
						System.out.println(url);
					
						
					
					return null;
				}
			};
			
			
		}
		
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
