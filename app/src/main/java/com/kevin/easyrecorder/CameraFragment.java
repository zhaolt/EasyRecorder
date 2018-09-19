package com.kevin.easyrecorder;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.kevin.easyrecorder.render.Renderer;
import com.kevin.easyrecorder.render.TextureController;
import com.kevin.easyrecorder.widget.CameraLayout;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhaoliangtai on 2018/9/18.
 */

public class CameraFragment extends Fragment implements TextureView.SurfaceTextureListener {

    private static final String TAG = CameraFragment.class.getSimpleName();
    private CameraLayout mCameraLayout;
    private TextureView mTextureView;
    private TextureController mTextureController;
    private CameraRenderer mCameraRenderer;
    private CameraThread.CameraHandler mCameraHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        mCameraLayout = view.findViewById(R.id.camera_layout);
        mTextureView = mCameraLayout.getTextureView();
        mTextureView.setSurfaceTextureListener(this);
        mTextureController = new TextureController(getContext());
        mCameraRenderer = new CameraRenderer(this);
        mTextureController.setRenderer(mCameraRenderer);
        return view;
    }

    private void startPreview() {
        if (mCameraHandler == null) {
            CameraThread thread = new CameraThread(mTextureController);
            thread.start();
            mCameraHandler = thread.getHandler();
        }
        mCameraHandler.startPreview(1280, 720);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mTextureController.surfaceCreated(surface);
        mTextureController.surfaceChanged(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mTextureController.surfaceCreated(surface);
        mTextureController.surfaceChanged(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureUpdated");
    }

    private final class CameraRenderer implements Renderer {

        private WeakReference<CameraFragment> mCameraFragmentWRef;

        public CameraRenderer(CameraFragment fragment) {
            mCameraFragmentWRef = new WeakReference<>(fragment);
        }

        @Override
        public void onSurfaceDestroy() {

        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            CameraFragment fragment = mCameraFragmentWRef.get();
            if (null == fragment) return;
            fragment.startPreview();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {
        }
    }


}
