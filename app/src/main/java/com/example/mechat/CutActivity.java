package com.example.mechat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;

public class CutActivity extends AppCompatActivity {
    ImageView tv_choo;
    ImageView iv;
    int statusBarHeight;
    Bitmap bitmap;
    Bitmap new_bitmap;
    long last_back_sjc;

    float x1,y1,x2,y2;
    float tv_choo_scale = 1.0f;
    int tv_choo_w = 128;

    float two_tvc_x,two_tvc_y;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);

        iv = findViewById(R.id.iv);
        tv_choo = findViewById(R.id.tv_cho);

        Intent intent = getIntent();
        Uri u = intent.getData();
        statusBarHeight = getStatusBarHeight(this);

        ContentResolver resolver = getContentResolver();

        try {
            InputStream inputStream = resolver.openInputStream(u);

            bitmap = BitmapFactory.decodeStream(inputStream);

            iv.setImageBitmap(bitmap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static int getStatusBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getPointerCount() == 1){
            switch (event.getAction())
            {
                case 0:
                    //tv_choo.setX(event.getX() - (float) (tv_choo.getWidth()/2) );
                    //tv_choo.setY(event.getY() - (float) (tv_choo.getHeight()/2) - statusBarHeight);
                    break;
                case 1:
                    two_tvc_x = tv_choo.getX() + tv_choo.getWidth() / 2 ;
                    two_tvc_y = tv_choo.getY() + tv_choo.getHeight() / 2 + statusBarHeight;
                    break;
                case 2:
                    tv_choo.setX(event.getX() - (float) (tv_choo.getWidth()/2));
                    tv_choo.setY(event.getY() - (float) (tv_choo.getHeight()/2) - statusBarHeight);
                    break;
            }
        }else if (event.getPointerCount() == 2)
        {
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_POINTER_DOWN:
                    x1 = event.getX(0);
                    y1 = event.getY(0);
                    x2 = event.getX(1);
                    y2 = event.getY(1);
                    //Log.e("WWS", "x1 = " + x1 + " y1 = " + y1 + " x2 = "+ x2 +" y2 = " + y2);


                    break;
                case 1:

                    break;
                case 2:

                    float tx1 = event.getX(0);
                    float ty1 = event.getY(0);
                    float tx2 = event.getX(1);
                    float ty2 = event.getY(1);

                    float m_now = calM(tx2 - tx1,ty2 - ty1);
                    float m_last = calM(x2 - x1,y2 - y1);
                    float zl = (m_now - m_last) / 100.0f;

                    x1 = event.getX(0);
                    y1 = event.getY(0);
                    x2 = event.getX(1);
                    y2 = event.getY(1);

                    //Log.e("WWS","zl = " +zl +" m_now = "+m_now+ " mlast = "+m_last);
                    if(tv_choo_scale + zl >= 1.0f)
                    {
                        tv_choo_scale += zl;
                        tv_choo_w = (int)(128f * tv_choo_scale);
                        ViewGroup.LayoutParams lp = tv_choo.getLayoutParams();
                        lp.width = tv_choo_w;
                        lp.height = tv_choo_w;
                        tv_choo.setLayoutParams(lp);
                        //tv_choo.setScaleX(tv_choo_scale);
                        //tv_choo.setScaleY(tv_choo_scale);

                    }else{
                        tv_choo_scale = 1.0f;
                        tv_choo_w = 128;
                        ViewGroup.LayoutParams lp = tv_choo.getLayoutParams();
                        lp.width = 128;
                        lp.height = 128;
                        tv_choo.setLayoutParams(lp);

                    }
                    tv_choo.setX(two_tvc_x - (float) (tv_choo.getWidth()/2));
                    tv_choo.setY(two_tvc_y - (float) (tv_choo.getHeight()/2) - statusBarHeight);
                    tv_choo.invalidate();


                    break;


            }
        }

        return super.onTouchEvent(event);
    }

    float calM(float b1,float b2)
    {
        return (float) Math.sqrt(b1 * b1 + b2 * b2);
    }

    @Override
    public void onBackPressed() {

        int x = (int)tv_choo.getX();
        int y = (int)tv_choo.getY();

        if (x < 0 || y < 0 || x + tv_choo_w > bitmap.getWidth() || y + tv_choo_w > bitmap.getHeight()){
            long now = System.currentTimeMillis();
            if(now - last_back_sjc <= 300)
            {
                Intent intent = new Intent();
                intent.putExtra("new_bitmap", new_bitmap);
                setResult(RESULT_OK, intent);
                super.onBackPressed();
            }else{
                Toast.makeText(this,"选择的不对,双击退出！！！" ,Toast.LENGTH_SHORT).show();
                last_back_sjc = System.currentTimeMillis();
                return;
            }
        }else{
            /*if(Math.abs(tv_choo_scale - 1.0f) < 0.001)
            {
                new_bitmap = Bitmap.createBitmap(bitmap,x,y,128,128);
                Log.e("WWS","x = " + x + " y = " + y );
            }else */{
                Bitmap temp = Bitmap.createBitmap(bitmap, x, y, tv_choo_w, tv_choo_w);
                Log.e("WWS","x = " + x + " y = " + y + " w = " + tv_choo.getWidth() + " h = "+tv_choo.getHeight());
               // new_bitmap = Bitmap.createBitmap(bitmap, x, y, tv_choo.getWidth(), tv_choo.getHeight());
                new_bitmap = resizeImage(temp,128,128);
            }

            Intent intent = new Intent();
            intent.putExtra("new_bitmap", new_bitmap);
            setResult(RESULT_OK, intent);

            super.onBackPressed();
        }
    }
    public Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }
}
