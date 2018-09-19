package com.kevin.easyrecorder.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zhaoliangtai on 2018/9/18.
 */

public class TextureController implements GLSurfaceView.Renderer {

    private static final String TAG = TextureController.class.getSimpleName();

    private Object mSurface;

    private GLView mGLView;
    private SurfaceTexture mSurfaceTexture;
    private int mTextureId;
    private GLDrawer2D mDrawer;
    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];
    private Renderer mRenderer;

    public TextureController(Context context) {
        init(context);
    }

    private void init(Context context) {
        mGLView = new GLView(context);
        ViewGroup vg = new ViewGroup(context) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
            }
        };
        vg.addView(mGLView);
        vg.setVisibility(View.GONE);
    }

    public void setRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    public void surfaceCreated(Object nativeWindow) {
        mSurface = nativeWindow;
        mGLView.surfaceCreated(null);
    }

    public void surfaceChanged(int width, int height) {
        mGLView.surfaceChanged(null, 0, width, height);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        if (!extensions.contains("OES_EGL_image_external")) {
            throw new RuntimeException("This system does not support OES_EGL_image_external.");
        }
        mTextureId = GLDrawer2D.initTex();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
        mDrawer = new GLDrawer2D();
        mDrawer.setMatrix(mMVPMatrix, 0);
        if (null != mRenderer) {
            mRenderer.onSurfaceCreated(gl, config);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
        if (null != mRenderer) {
            mRenderer.onSurfaceChanged(gl, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (null != mRenderer) {
            mRenderer.onDrawFrame(gl);
        }
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mDrawer.draw(mTextureId, mSTMatrix);
    }

    private final class GLView extends GLSurfaceView {

        public GLView(Context context) {
            super(context);
            init();
        }

        private void init() {
            getHolder().addCallback(null);
            setEGLWindowSurfaceFactory(new EGLWindowSurfaceFactory() {
                @Override
                public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
                    return egl.eglCreateWindowSurface(display, config, mSurface, null);
                }

                @Override
                public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
                    egl.eglDestroySurface(display, surface);
                }
            });
            setEGLContextClientVersion(2);
            setRenderer(TextureController.this);
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }
    }
}
