package com.kevin.easyrecorder.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.kevin.easyrecorder.R;

/**
 * Created by zhaoliangtai on 2018/9/19.
 */

public class CameraLayout extends FrameLayout {

    private TextureView mTextureView;

    public CameraLayout(@NonNull Context context) {
        this(context, null);
    }

    public CameraLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View root = inflate(context, R.layout.layout_camera, this);
        mTextureView = root.findViewById(R.id.texture_view);
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public TextureView getTextureView() {
        return mTextureView;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
