package com.example.mechat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class QP_View extends View {
    private Bitmap bitmap;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    private int lor;
    private float img_width_scale;
    private Paint mPaint;

    public QP_View(Context context) {
        super(context);
        init();
    }

    public QP_View(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QP_View(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    void init()
    {
        lor = 1;
        img_width_scale = 0.0f;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void setLor(int lor) {
        this.lor = lor;
    }

    public void setImageWidthScale(float x)
    {
        this.img_width_scale = x;
    }

    void setImageBitmap(Bitmap bitmap)
    {
        this.bitmap = bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(bitmap == null) return;
        int w = (int)(img_width_scale * canvas.getWidth());
       // Log.e("WWS","QP_VIEW w = "+w +" scale_w = "+img_width_scale);
        if(lor == LEFT)
        {
            Rect dst = new Rect(0,0,w,canvas.getHeight());

            canvas.drawBitmap(bitmap,null,dst,mPaint);
        }else {
            Rect dst = new Rect(canvas.getWidth() - w,0,w,canvas.getHeight());
            Log.e("WWS","right = "+dst);
            canvas.drawBitmap(bitmap,null,dst,mPaint);

        }
    }
}
