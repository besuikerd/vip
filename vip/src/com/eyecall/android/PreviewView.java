package com.eyecall.android;

import java.io.IOException;

import com.eyecall.vip.MainActivity;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** 
 * A basic Camera preview class 
 * Source: http://developer.android.com/guide/topics/media/camera.html#camera-preview
 */
public class PreviewView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
    private Camera camera;
	private PreviewCallback previewCallback;

    public PreviewView(Context context, Camera camera, PreviewCallback previewCallback) {
        super(context);
        this.camera = camera;
        this.previewCallback = previewCallback;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public void startPreview() throws IOException{
    	 camera.setPreviewDisplay(surfaceHolder);
    	 camera.setPreviewCallback(previewCallback);
         camera.startPreview();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
           startPreview();
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (surfaceHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            startPreview();
        } catch (Exception e){
            Log.d(MainActivity.TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}
