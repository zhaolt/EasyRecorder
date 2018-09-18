package com.kevin.easyrecorder.render;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.kevin.easyrecorder.util.GlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by zhaoliangtai on 2018/9/18.
 */

public class GLDrawer2D {

    private static final String TAG = GLDrawer2D.class.getSimpleName();

    private static final String VERTEX_SOURCE
            = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uTexMatrix;\n"
            + "attribute vec4 aPostion;\n"
            + "attribute vec4 aTexCoord;\n"
            + "varying vec2 vTexCoord;\n"
            + "\n"
            + "void main() {\n"
            + "    gl_Position = uMVPMatrix * aPosition;\n"
            + "    vTexCoord = (uTexMatrix * aTexCoord).xy;\n"
            + "}";

    private static final String FRAGMENT_SOURCE
            = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTexCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "void main() {\n"
            + "    gl_FragColor = texture2D(sTexture, vTexCoord);\n"
            + "}";

    private static final float[] VERTICES = { 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f };
    private static final float[] TEXCOORD = { 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f };

    private FloatBuffer mVertexBuf;
    private FloatBuffer mTexCoordBuf;

    private int mProgram;
    private int mPositionLoc;
    private int mTexCoordLoc;
    private int mMVPMatrixLoc;
    private int mTexMatrixLoc;

    private float[] mMVPMatrix = new float[16];


    public GLDrawer2D() {
        mVertexBuf = ByteBuffer.allocateDirect(VERTICES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTICES);
        mVertexBuf.position(0);
        mTexCoordBuf = ByteBuffer.allocateDirect(TEXCOORD.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXCOORD);
        mTexCoordBuf.position(0);
        mProgram = GlUtils.createProgram(VERTEX_SOURCE, FRAGMENT_SOURCE);
        GLES20.glUseProgram(mProgram);
        mPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mTexMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mTexMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES20.glVertexAttribPointer(mPositionLoc, 2, GLES20.GL_FLOAT, false, 8, mVertexBuf);
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 8, mTexCoordBuf);
        GLES20.glEnableVertexAttribArray(mPositionLoc);
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
    }

    public void release() {
        if (mProgram >= 0)
            GLES20.glDeleteProgram(mProgram);
        mProgram = -1;
    }

    public void draw(final int texId, final float[] stMatrix) {
        GLES20.glUseProgram(mProgram);
        if (stMatrix != null)
            GLES20.glUniformMatrix4fv(mTexMatrixLoc, 1, false, stMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
    }


    public static int initTex() {
        Log.i(TAG, "initTex:");
        final int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        return tex[0];
    }

    public static void deleteTex(final int hTex) {
        Log.i(TAG, "deleteTex:");
        final int[] tex = new int[] {hTex};
        GLES20.glDeleteTextures(1, tex, 0);
    }

}
