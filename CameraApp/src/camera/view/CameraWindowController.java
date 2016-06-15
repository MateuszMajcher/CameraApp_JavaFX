package camera.view;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opencv.core.Core;
import org.opencv.core.CvType;
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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.imgproc.Imgproc;
public class CameraWindowController {

	/*panel kamer*/
	@FXML
	private TilePane tilePane;
	
	/*panel powiadomien*/
	@FXML
	private TextArea textArea;
	
	/*start kamery*/
	@FXML
	private Button start;
	
	/*zatrzymanie kamery*/
	@FXML
	private Button stop;

	
	/*Logger*/
	private Logger logger;
	
	/*ilosc klatek na sekunde*/
	private int FPS = 25;
	/*czas dla scheduleservice*/
	private int miliSecond = 1000/FPS;

	
	static Image image = new Image("image.jpg");
	
	/*executor dla watkow kamer*/
	private ScheduledExecutorService camExecutor;
	
	/*warunek dla zatrzymania watku*/
	public static boolean stopCamera;
	
	
	/*init executora*/
	public void init() throws Exception {
		camExecutor = Executors.newScheduledThreadPool(2,
				new FirstLineThreadFactory("cam"));
	}
	
	
	/*zatrzymanie watków*/
	public void stop() throws InterruptedException {
		stopCamera = true;
		Thread.sleep(miliSecond);
	}
	
	public void startWebCamCamera() {
		//stopCamera = false;
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
			service.setPeriod(Duration.millis(miliSecond));
			service.setCapture(capture);
			
			
			final CamLabel progressMonitoredLabel = new CamLabel();
			progressMonitoredLabel.label.setText(Integer.toString(webCamCounter));
			progressMonitoredLabel.imageView.imageProperty().bind(service.imageProperty());
			tilePane.getChildren().add(progressMonitoredLabel);
			
			
		
			service.start();
		webCamCounter++;
		}
	}
	
	/*Okno dla kamerki*/
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
		@Override
		protected void cancelled() {
			System.out.println("canceled");
			super.cancelled();
		}

		private VideoCapture capture;
		private ObjectProperty<Image> imageView = new SimpleObjectProperty<Image>();
		public final void setImage(Image value) {imageView.set(value);}
		public final Image getImage() {return imageView.get(); }
		public final ObjectProperty<Image> imageProperty() {return imageView; }
		public final void setCapture(VideoCapture capture) {this.capture = capture;}
		
		/*wyjsciowy obraz*/
		 Mat imag = null;
		 /*wejsciowy obraz*/
		 Mat frame = new Mat();
		 /*blur*/
		 Mat outerBox = new Mat();
		 Mat diff_frame = null;
		 Mat tempon_frame = null;
		 ArrayList<Rect> array = new ArrayList<Rect>();
		 Size sz = new Size(640, 480);
		 
		 int i= 0;
		
		
		
		@Override
		protected Task<Void> createTask() {
		
			
			return new Task<Void>() {
			
				@Override
				protected Void call() throws Exception {
					
					if (!stopCamera) {
					
						if (capture.isOpened()) {
							
							frame = grabFrame(capture);
							
							Imgproc.resize(frame, frame, sz);
			                imag = frame.clone();
			                outerBox = new Mat(frame.size(), CvType.CV_8UC1);
			                Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2GRAY);
			                Imgproc.GaussianBlur(outerBox, outerBox, new Size(3, 3), 0);
			                
			                if (i == 0) {
			                    diff_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
			                    tempon_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
			                    diff_frame = outerBox.clone();
			                }
					
			                if (i == 1) {
			                    Core.subtract(outerBox, tempon_frame, diff_frame);
			                    Imgproc.adaptiveThreshold(diff_frame, diff_frame, 255,
			                            Imgproc.ADAPTIVE_THRESH_MEAN_C,
			                            Imgproc.THRESH_BINARY_INV, 5, 2);
			                    array = detection_contours(diff_frame);
			                    if (array.size() > 0) {
			 
			                        Iterator<Rect> it2 = array.iterator();
			                        while (it2.hasNext()) {
			                            Rect obj = it2.next();
			                            Imgproc.rectangle(imag, obj.br(), obj.tl(),
			                                    new Scalar(0, 255, 0), 1);
			                        }
			 
			                    }
			                }
			                i = 1;
						
			               
							
							imageView.set(mat2Image(imag)); 
							tempon_frame = outerBox.clone();
						
						}} else {
							capture.release();
							this.cancel();
							System.out.println("wyjscie kamery " );
						}
					
					return null;
				}

			};
			
			
		}
		
		
		  public synchronized ArrayList<Rect> detection_contours(Mat outmat) {
		        Mat v = new Mat();
		        Mat vv = outmat.clone();
		        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		        Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST,
		                Imgproc.CHAIN_APPROX_SIMPLE);
		 
		        double maxArea = 100;
		        int maxAreaIdx = -1;
		        Rect r = null;
		        ArrayList<Rect> rect_array = new ArrayList<Rect>();
		 
		        for (int idx = 0; idx < contours.size(); idx++) { Mat contour = contours.get(idx); double contourarea = Imgproc.contourArea(contour); if (contourarea > maxArea) {
		                // maxArea = contourarea;
		                maxAreaIdx = idx;
		                r = Imgproc.boundingRect(contours.get(maxAreaIdx));
		                rect_array.add(r);
		                Imgproc.drawContours(imag, contours, maxAreaIdx, new Scalar(0,0, 255));
		            }
		 
		        }
		 
		        v.release();
		 
		        return rect_array;
		 
		    }
		
	}
	
	 /* Pobranie klatki z kamerki
	 * 
	 * @return the {@link Image} to show
	 */
	private static synchronized Mat grabFrame(VideoCapture capture) {
	
		Mat imageToShow = null;
		Mat frame = new Mat();

	
		if (capture.isOpened()) {
			try {
				
				capture.read(frame);

				if (!frame.empty()) {

					 
					imageToShow = frame;
					
				}

			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return imageToShow;
	}

	
	
	/**/
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
