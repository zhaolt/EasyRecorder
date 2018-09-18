package com.kevin.easyrecorder.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
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

    private Object mSurface;

    private GLView mGLView;
    private SurfaceTexture mSurfaceTexture;

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

    public void surfaceCreated(Object nativeWindow) {
        mSurface = nativeWindow;
        mGLView.surfaceCreated(null);
    }

    public void surfaceChanged(int width, int height) {
        mGLView.surfaceChanged(null, 0, width, height);
    }

    public SurfaceTexture getSurfaceTexture() {
        return null;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

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
