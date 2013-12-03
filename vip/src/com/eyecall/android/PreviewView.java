package com.eyecall.android;

import java.io.FileDescriptor;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.eyecall.event.SurfaceCreatedEvent;
import com.eyecall.eventbus.EventBus;
import com.eyecall.vip.EventTag;
import com.eyecall.vip.MainActivity;

/** 
 * A basic Camera preview class 
 * Source: http://developer.android.com/guide/topics/media/camera.html#camera-preview
 */
public class PreviewView extends SurfaceView implements SurfaceHolder.Callback {
	private static final Logger logger = LoggerFactory.getLogger(PreviewView.class);
	
    private SurfaceHolder surfaceHolder;
    private Camera camera;
	private PreviewCallback previewCallback;
	private MediaRecorder mediaRecorder;
	
	private StreamingEventHandler streamingEventHandler;
	
    @SuppressWarnings("deprecation")
	public PreviewView(Context context, Camera camera, PreviewCallback previewCallback) {
        super(context);
        this.camera = camera;
        this.previewCallback = previewCallback;
        this.streamingEventHandler = new StreamingEventHandler();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public void prepareMediaRecorder(int width, int height){
    	mediaRecorder = new MediaRecorder();
    	try {
            camera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }
    	// Unlock camera for recording
    	camera.lock();
        camera.unlock();
        
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        
        /*mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoFrameRate(15);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setVideoSize(width, height);
        
        mediaRecorder.setVideoEncodingBitRate(512*1024);*/
    }
    
    public void startPreview() throws IOException{
    	logger.debug("camera: {}, surfaceholder: {}", camera, surfaceHolder);
    	 camera.setPreviewDisplay(surfaceHolder);
    	 camera.setPreviewCallback(previewCallback);
         camera.startPreview();
    }
    
    public void startStreaming(FileDescriptor target) throws IllegalStateException, IOException{
    	mediaRecorder.setOutputFile(target);
        mediaRecorder.setMaxDuration(9600000);        // Set max duration 4 hours
        //myMediaRecorder.setMaxFileSize(1600000000); // Set max file size 16G
        mediaRecorder.setOnInfoListener(streamingEventHandler);
        mediaRecorder.setOnErrorListener(streamingEventHandler);
    	
    	mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
    	
    	//camera.setPreviewDisplay(surfaceHolder);
   	 	//camera.setPreviewCallback(previewCallback);
        
    	mediaRecorder.prepare();
        mediaRecorder.start();    
    }
    
    /**
     * Stops the streaming
     * If you want to restart streaming after stopping it, you need to reconfigure
     * the MediaRecorder by calling prepareMediaRecoder()
     * @throws IOException 
     * @link http://developer.android.com/reference/android/media/MediaRecorder.html
     */
    public void stopStreaming() throws IOException{
    	mediaRecorder.stop();
    	mediaRecorder.reset();
    	mediaRecorder.release();
    	mediaRecorder = null;
    	//camera.lock();
    	//startPreview();
    }

    public boolean isStreaming() {
		return mediaRecorder!=null;
	}

	public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
           startPreview();
           EventBus.getInstance().post(new SurfaceCreatedEvent(EventTag.SURFACE_CREATED));
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
    	if ( camera != null && mediaRecorder == null) {
            camera.stopPreview();
            try {
                camera.setPreviewDisplay(holder);
            } catch ( Exception ex) {
                ex.printStackTrace();
            }
            try {
                startPreview();
            } catch (Exception e){
                Log.d(MainActivity.TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }
    
    
    class StreamingEventHandler implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Log.d(MainActivity.TAG, "MediaRecorder event: " + what);    
        }

		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			Log.d(MainActivity.TAG, "MediaRecorder error: " + what);    
		}
    };

}
