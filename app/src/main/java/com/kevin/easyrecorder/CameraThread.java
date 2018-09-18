package com.kevin.easyrecorder;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by zhaoliangtai on 2018/9/18.
 */

public class CameraThread extends Thread {
    private static final String TAG = CameraThread.class.getSimpleName();
    private CameraHandler mHandler;
    private final Object mReadyFence = new Object();
    private boolean isRunning = false;
    private Camera mCamera;

    public CameraThread() {
        super(TAG);
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

    }

    private void stopPreview() {

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
