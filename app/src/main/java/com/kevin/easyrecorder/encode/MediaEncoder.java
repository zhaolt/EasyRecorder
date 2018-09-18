package com.kevin.easyrecorder.encode;

import android.media.MediaCodec;

/**
 * Created by zhaoliangtai on 2018/9/18.
 */

public abstract class MediaEncoder implements Runnable {

    private final Object mSync = new Object();
    private boolean isCapturing = false;
    private int mRequestDrain = -1;


    private MediaCodec mMediaCodec;
    private boolean isEOS;
    private boolean isMuxerStarted = false;
    private int mTrackIndex = -1;
    private MediaCodec.BufferInfo mBufferInfo;
    private long mPrevOutputPTSUs;



    @Override
    public void run() {

    }
}
