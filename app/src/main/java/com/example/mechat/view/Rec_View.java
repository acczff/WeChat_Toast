package com.example.mechat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.mechat.R;

public class Rec_View extends View {
    private boolean isPressed = false;
    private Bitmap rec;
    private Paint paint;
    private Paint big_paint;
    int margin = 5;
    int x_r = 6;
    int space = 5;
    float big_r = 40.0f;
    double sin_big_r = 0.0;
    Rect dst;
    MyHandler handler;
    MyThread thread;
    boolean draw_two;
    boolean draw_three;
    private OnRecListener recListener;
    //Vibrator vibrator;
    public SoundPool soundPool;
    public int jd_id;
    int end_id;
    boolean can_rec;

    public Rec_View(Context context) {
        super(context);
        init();
    }

    public Rec_View(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Rec_View(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        rec = BitmapFactory.decodeResource(getResources(), R.drawable.rec);
        paint = new Paint();
        big_paint = new Paint();
        big_paint.setAntiAlias(true);
        paint.setAntiAlias(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        handler = new MyHandler();
        thread = new MyThread();
        thread.start();
        //vibrator = (Vibrator)this.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        jd_id = soundPool.load(getContext(), R.raw.jd, 12);
        end_id = soundPool.load(getContext(), R.raw.end, 12);
        can_rec = false;
    }

    public void setRecListener(OnRecListener l) {
        recListener = l;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int x0 = canvas.getWidth() / 2;
        int y0 = canvas.getHeight() / 2;

        if (dst == null)
            dst = new Rect(x0 - 64, y0 - 64, x0 + 64, y0 + 64);

        int layerID = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), paint, Canvas.ALL_SAVE_FLAG);


        //RectF oval = new RectF(x0 - 64 - 32,y0 - 64 - 32,x0 + 64 + 32,y0 + 64 + 32);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setARGB(255, 230, 230, 230);
        //paint.setAntiAlias(true);

        int bx = margin;
        int by = margin;
        while (true) {
            if (by >= canvas.getHeight() - margin) {
                break;
            }
            while (true) {
                if (bx >= canvas.getWidth() - margin) {
                    break;
                }
                canvas.drawCircle(bx, by, x_r, paint);
                bx += x_r * 2;
                bx += space;
            }
            by += x_r * 2;
            by += space;
            bx = margin;
        }


        if (isPressed) {
            big_paint.setStrokeWidth(30);
            big_paint.setARGB(255, 40, 220, 255);
            big_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            big_paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(x0, y0, big_r, big_paint);
            if (draw_two)
                canvas.drawCircle(x0, y0, big_r - 80, big_paint);
            if (draw_three)
                canvas.drawCircle(x0, y0, big_r - 180, big_paint);
            big_r += Math.cos(sin_big_r) * 30.0f;
            if (big_r * 2 >= canvas.getWidth() + 350) {
                reset();
            }
            if (sin_big_r >= Math.PI) {
                sin_big_r = 0.0;
            } else {
                sin_big_r += 0.05;
            }
            if (big_r >= 120) {
                draw_two = true;
            }
            if (big_r >= 220) {
                draw_three = true;
            }

            // Log.e("WWS"," " + big_r);

        }
        if (can_rec)
            canvas.drawBitmap(rec, null, dst, paint);

        //paint.setARGB(255,255,0,0);
        //canvas.drawArc(oval,0,180,false,paint);
        canvas.restoreToCount(layerID);

    }

    void reset() {
        big_r = 40.0f;
        sin_big_r = 0.0;
        draw_two = false;
        draw_three = false;
    }

    public void setCan_rec(boolean can_rec) {
        this.can_rec = can_rec;
    }

    public void playAnm() {
        isPressed = true;
        recListener.onPressed();
    }

    public void stopAnm() {
        isPressed = false;
        reset();
        //vibrator.vibrate(50);
        soundPool.play(end_id, 1.0f, 1.0f, 100, 0, 1);
        recListener.onUp();
    }


    class MyThread extends Thread {
        boolean isRun = true;

        public void stopRun() {
            isRun = false;
        }

        @Override
        public void run() {
            while (isRun) {
                if (isPressed) {
                    Message meg = new Message();
                    meg.obj = Rec_View.this;
                    meg.what = 0;
                    handler.sendMessage(meg);
                }
                try {
                    Thread.sleep(16);
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        thread.stopRun();
        soundPool.release();
        //Log.e("WWS","onDetachedFromWindow");
    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ((Rec_View) msg.obj).invalidate();
        }
    }

    public interface OnRecListener {
        void onPressed();

        void onUp();

        void onMoveOut();
    }
}
