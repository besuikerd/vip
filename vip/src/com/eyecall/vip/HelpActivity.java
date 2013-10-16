package com.eyecall.vip;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.eyecall.android.PreviewView;
import com.eyecall.connection.Connection;


public class HelpActivity extends Activity{
    private Connection connection;
    
    private Camera camera;
    
    @Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_help);
		
		// Step 1 : Check hardware
		if(checkCameraHardware()){
			// Step 2 : Access camera
			// Create an instance of Camera
	        camera = getCameraInstance();
	        
	        // Step 3 : Create a preview class
	        // Create our Preview view and set it as the content of our activity.
	        PreviewView previewView = new PreviewView(this, camera, new CameraCallback());
	        FrameLayout previewFrame = (FrameLayout) findViewById(R.id.camera_preview_frame);
	        previewFrame.addView(previewView);
		}        
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
    	releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        /*if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }*/
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    
    @Override
    public void onResume(){
    	super.onResume();
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
        	Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    
    class CameraCallback implements Camera.PreviewCallback{

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			//Log.d(MainActivity.TAG, ");
		}
    	
    }
}
