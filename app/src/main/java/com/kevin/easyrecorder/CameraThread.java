package com.kevin.easyrecorder;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.kevin.easyrecorder.render.TextureController;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhaoliangtai on 2018/9/18.
 */

public class CameraThread extends Thread {
    private static final String TAG = CameraThread.class.getSimpleName();
    private CameraHandler mHandler;
    private final Object mReadyFence = new Object();
    private boolean isRunning = false;
    private Camera mCamera;
    private WeakReference<TextureController> mControllerWRef;

    public CameraThread(TextureController controller) {
        super(TAG);
        mControllerWRef = new WeakReference<>(controller);
    }

    public CameraHandler getHandler() {
        synchronized (mReadyFence) {
            try {
                mReadyFence.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mHandler;
    }

    @Override
    public void run() {
        Log.i(TAG, "Camera Thread running...");
        Looper.prepare();
        synchronized (mReadyFence) {
            mHandler = new CameraHandler(this);
            isRunning = true;
            mReadyFence.notify();
        }
        Looper.loop();
        synchronized (mReadyFence) {
            mHandler = null;
            isRunning = false;
        }
    }

    private void startPreview(int width, int height) {
        TextureController controller = mControllerWRef.get();
        if (controller != null && mCamera == null) {
            try {
                mCamera = Camera.open(0);
                final Camera.Parameters params = mCamera.getParameters();
                final List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                } else {
                    Log.i(TAG, "Camera does not support autofocus");
                }
                final List<int[]> supportedFpsRange = params.getSupportedPreviewFpsRange();
                final int[] max_fps = supportedFpsRange.get(supportedFpsRange.size() - 1);
                Log.i(TAG, String.format("fps:%d-%d", max_fps[0], max_fps[1]));
                params.setPreviewFpsRange(max_fps[0], max_fps[1]);
                params.setRecordingHint(true);
                final Camera.Size closestSize = getClosestSupportedSize(
                        params.getSupportedPreviewSizes(), width, height);
                params.setPreviewSize(closestSize.width, closestSize.height);
//            setRotation(params);
                mCamera.setParameters(params);
                final Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                final SurfaceTexture st = controller.getSurfaceTexture();
                st.setDefaultBufferSize(previewSize.width, previewSize.height);
                mCamera.setPreviewTexture(st);
            } catch (IOException e) {
                e.printStackTrace();
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }
        if (mCamera != null) {
            // start camera preview display
            mCamera.startPreview();
        }
    }

//    private final void setRotation(final Camera.Parameters params) {
//        Log.v(TAG, "setRotation:");
//        final CameraGLView parent = mWeakParent.get();
//        if (parent == null) return;
//
//        final Display display = ((WindowManager) parent.getContext()
//                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        final int rotation = display.getRotation();
//        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0: degrees = 0; break;
//            case Surface.ROTATION_90: degrees = 90; break;
//            case Surface.ROTATION_180: degrees = 180; break;
//            case Surface.ROTATION_270: degrees = 270; break;
//        }
//        // get whether the camera is front camera or back camera
//        final Camera.CameraInfo info =
//                new android.hardware.Camera.CameraInfo();
//        android.hardware.Camera.getCameraInfo(CAMERA_ID, info);
//        mIsFrontFace = (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
//        if (mIsFrontFace) {	// front camera
//            degrees = (info.orientation + degrees) % 360;
//            degrees = (360 - degrees) % 360;  // reverse
//        } else {  // back camera
//            degrees = (info.orientation - degrees + 360) % 360;
//        }
//        // apply rotation setting
//        mCamera.setDisplayOrientation(degrees);
//        parent.mRotation = degrees;
//        // XXX This method fails to call and camera stops working on some devices.
////			params.setRotation(degrees);
//    }

    private static Camera.Size getClosestSupportedSize(List<Camera.Size> supportedSizes,
                                                       final int requestedWidth,
                                                       final int requestedHeight) {
        return Collections.min(supportedSizes, new Comparator<Camera.Size>() {

            private int diff(final Camera.Size size) {
                return Math.abs(requestedWidth - size.width) + Math.abs(requestedHeight - size.height);
            }

            @Override
            public int compare(final Camera.Size lhs, final Camera.Size rhs) {
                return diff(lhs) - diff(rhs);
            }
        });

    }

    private void stopPreview() {
        Log.v(TAG, "stopPreview:");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public static final class CameraHandler extends Handler {
        private static final String TAG = CameraHandler.class.getSimpleName();
        private static final int MSG_START_PREVIEW = 1003;
        private static final int MSG_STOP_PREVIEW = 1004;


        private CameraThread mThread;

        public CameraHandler(CameraThread thread) {
            mThread = thread;
        }

        public void startPreview(int width, int height) {
            sendMessage(obtainMessage(MSG_START_PREVIEW, width, height));
        }

        public void stopPreview(boolean needWait) {
            sendEmptyMessage(MSG_STOP_PREVIEW);
            if (needWait && mThread.isRunning) {
                Log.i(TAG, "Wait for terminating of camera thread.");
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_PREVIEW:
                    mThread.startPreview(msg.arg1, msg.arg2);
                    break;
                case MSG_STOP_PREVIEW:
                    mThread.stopPreview();
                    synchronized (this) {
                        notifyAll();
                    }
                    Looper.myLooper().quit();
                    mThread = null;
                    break;
            }
        }


    }
}
