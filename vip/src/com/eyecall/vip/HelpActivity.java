package com.eyecall.vip;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.eyecall.android.ConnectionInstance;
import com.eyecall.android.PreviewView;
import com.eyecall.android.VideoBuffer;
import com.eyecall.android.VideoPipe;
import com.eyecall.connection.Connection;
import com.eyecall.connection.Message;
import com.eyecall.event.SurfaceCreatedEvent;
import com.eyecall.eventbus.Event;
import com.eyecall.eventbus.EventBus;
import com.eyecall.eventbus.EventListener;
import com.eyecall.protocol.ProtocolField;
import com.eyecall.protocol.ProtocolName;
public class HelpActivity extends Activity implements EventListener, LocationListener{
	private static final Logger logger = LoggerFactory.getLogger(HelpActivity.class);
	
    private static final String LOCAL_SOCKET_ADDRESS = "eyecall.vip";
	private VideoBuffer videoBuffer;
    private Camera camera;
    private PreviewView previewView;
	private VideoPipe videoPipe;
    
    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_help);
		this.videoPipe = new VideoPipe(LOCAL_SOCKET_ADDRESS);
		EventBus.getInstance().subscribe(this);
	}
    
    /**
     * If the activity pauses, the camera should be released for other apps
     */
    @Override
    public void onPause(){
    	super.onPause();
    	// Stop streaming
    	logger.debug("onpause called");
    	logger.debug("1");
    	if(camera!=null && previewView.isStreaming()){
    		try {
				previewView.stopStreaming();
				logger.debug("2");
    		} catch (IOException e) {
				logger.warn("Error stopping streaming: {}", e.getMessage());
			} catch(RuntimeException e){
				logger.warn("for some reason android camera API fails and throws an undocumented RuntimeException: {}", e.getMessage());
			}
    	}
    	// Close local socket
    	try {
			stopVideoPipe();
			logger.debug("2");
		} catch (IOException e) {
			logger.warn("Error closing local socket: {}", e.getMessage());
		}
    	// Stop preview if it exists
    	if(camera!= null) {
    		try {
    			logger.debug("3");
                camera.stopPreview();
            } catch (Exception e){
              // ignore: tried to stop a non-existent preview
            	logger.warn("error stopping camera preview: {}", e.getMessage());
            }
    	}
    	// release camera for other apps
        releaseCamera(); // release the camera immediately on pause event
    }

    @Override
	public void onResume(){
		super.onResume();
		
		// (re)enable streaming
		setupCamera();
		
		// Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
    
    public void stopVideoPipe() throws IOException{
    	if(videoPipe!=null){
    		videoPipe.close();
    	}
    }

	private void setupCamera(){
    	// Step 1 : Check hardware
		if(checkCameraHardware()){
			// Step 2 : Access camera
			// Create an instance of Camera
			obtainCamera();
	        
	        // Step 3 : Create a preview class
	        try {
				setupPreview();
			} catch (IOException e) {
				Log.d(MainActivity.TAG, "Error setting camera preview: " + e.getMessage());
				return;
			}
	        
	        // Voor de andere stappen moet eerst een Surface (camerapreview) bestaan. 
	        // Het wachten is dus op Android tot de Surface gemaakt is. previewView krijgt
	        // een melding als deze is gemaakt, waarna een SurfaceCreatedEvent wordt gepost.
		}else{
			//TODO no hardcoded strings
			Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
		}
	}
	
	private void setupStreaming(){
        // Step 4 : Init MediaRecorder
        previewView.prepareMediaRecorder(100, 200);
        
        // Step 5 : Init local socket
        try {
        	videoBuffer = new VideoBuffer(videoPipe);
			videoPipe.setup();
		} catch (IOException e) {
			Log.d(MainActivity.TAG, "Error opening video pipe: " + e.getMessage());
			return;
		}
        
        // Step 6 : Start streaming
        try {
        	// Start streaming and send the frames into the pipe
        	videoBuffer.start();
			previewView.startStreaming(videoPipe.getInputFileDescriptor());
		} catch (IllegalStateException e) {
			Log.d(MainActivity.TAG, "Error starting streaming (IllegalState): " + e.getMessage());
			return;
		} catch (IOException e) {
			Log.d(MainActivity.TAG, "Error starting streaming (IOException): " + e.getMessage());
			return;
		}
    }

	private void releaseCamera(){
        if (camera != null){
        	logger.debug("releasing camera..");
            camera.release();        // release the camera for other applications
            camera = null;
        } else{
        	logger.warn("camera was null");
        }
    }
    
    private void obtainCamera(){
    	if(camera==null){
	        camera = getCameraInstance();
    	}
    }
    
    private void setupPreview() throws IOException{
    	// Create our Preview view and set it as the content of our activity.
    	previewView = new PreviewView(this, camera, new CameraCallback());
        FrameLayout previewFrame = (FrameLayout) findViewById(R.id.camera_preview_frame);
        previewFrame.removeAllViews();
        previewFrame.addView(previewView);
        previewView.startPreview();
    }

    
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        
        /*Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    	for(int i = 0; i<Camera.getNumberOfCameras(); i++){
    		Camera.getCameraInfo(i, cameraInfo);
    		if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
    			cameraId = i;
    		}
    	}*/

    	try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    
    /** Check if this device has a camera */
    private boolean checkCameraHardware() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            return false;
        }
    }
    
    class CameraCallback implements Camera.PreviewCallback{

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			/*Log.d(MainActivity.TAG, "Framesize: " + String.valueOf(data.length) + " bytes");
			deflater = new Deflater();
			deflater.setInput(data);
			deflater.finish();
			
			byte[] result = new byte[data.length];
			while (!deflater.finished()) {
		        int byteCount = deflater.deflate(result);
		        Log.d(MainActivity.TAG, "Compressed: " + byteCount);
		    }
			EventBus.getInstance().post(new VideoFrameEvent(EventTag.VIDEO_FRAME.getName(), result));*/
		}
    	
    }

	@Override
	public void onEvent(Event e) {
		if(e instanceof SurfaceCreatedEvent){
			setupStreaming();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
