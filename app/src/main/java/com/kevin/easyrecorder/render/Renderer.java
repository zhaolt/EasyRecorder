package com.kevin.easyrecorder.render;

import android.opengl.GLSurfaceView;

/**
 * Created by zhaoliangtai on 2018/9/19.
 */

public interface Renderer extends GLSurfaceView.Renderer {
    void onSurfaceDestroy();
}
