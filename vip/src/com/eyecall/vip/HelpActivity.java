package com.eyecall.vip;

import java.util.zip.Deflater;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.eyecall.android.PreviewView;
import com.eyecall.connection.Connection;
import com.eyecall.event.VideoFrameEvent;

import de.greenrobot.event.EventBus;

public class HelpActivity extends Activity{
    private Connection connection;
    
    private Camera camera;
    private Deflater deflater;
    
    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_help);
		
		//setupCamera();
	}
    
    @Override
    public void onPause(){
    	super.onPause();
    	if(camera!=null){
    		try {
                camera.stopPreview();
            } catch (Exception e){
              // ignore: tried to stop a non-existent preview
            }
    	}
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
	public void onResume(){
		super.onResume();
		
		setupCamera();
	}
    
    private void setupCamera(){
    	// Step 1 : Check hardware
		if(checkCameraHardware()){
			// Step 2 : Access camera
			// Create an instance of Camera
			obtainCamera();
	        
	        // Step 3 : Create a preview class
	        setupPreview();
	        
	        // Keep screen on
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}else{
			Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
		}
    }

	private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }
    
    private void obtainCamera(){
    	if(camera==null){
	        camera = getCameraInstance();
    	}
    }
    
    private void setupPreview(){
    	// Create our Preview view and set it as the content of our activity.
    	PreviewView previewView = new PreviewView(this, camera, new CameraCallback());
        FrameLayout previewFrame = (FrameLayout) findViewById(R.id.camera_preview_frame);
        previewFrame.removeAllViews();
        previewFrame.addView(previewView);
    }

    
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
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
			Log.d(MainActivity.TAG, "Framesize: " + String.valueOf(data.length) + " bytes");
			deflater = new Deflater();
			deflater.setInput(data);
			deflater.finish();
			
			byte[] result = new byte[data.length];
			while (!deflater.finished()) {
		        int byteCount = deflater.deflate(result);
		        Log.d(MainActivity.TAG, "Compressed: " + byteCount);
		    }
			EventBus.getDefault().post(new VideoFrameEvent(result));
		}
    	
    }
}
