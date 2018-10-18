package com.example.mechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public Handler handler;
    private TextView tv;
    public MyThread thread;
    private ImageView iv;
    private boolean isClickGG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        isClickGG = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);
        iv = findViewById(R.id.iv_);
        handler = new MyHandler();

        thread = new MyThread(this);
        thread.start();
        tv.setOnClickListener(this);
        iv.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isClickGG) {
            Log.e("WWS"," onResume isClickGG = " + isClickGG);
            tv.callOnClick();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv:
                thread.stopRun();
                this.startActivity(new Intent(MainActivity.this,LoginActivity.class));
                this.finish();
                break;

            case R.id.iv_:
                thread.stopRun();
                isClickGG = true;
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://chaoshi.detail.tmall.com/item.htm?id=522072066498&ali_refid=a3_430673_1006:1104060239:N:%E3%F2%D6%DD%C0%CF%BD%D152%B6%C8:e5d8b60e7384f2c0c5cc69c1c0eb708e&ali_trackid=1_e5d8b60e7384f2c0c5cc69c1c0eb708e&spm=a2e15.8261149.07626516002.1");
                intent.setData(content_url);
                startActivity(intent);
                break;
        }
    }

    static class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == 0){
                MainActivity a = ((MainActivity)msg.obj);
                a.startActivity(new Intent(a,LoginActivity.class));
                a.finish();
            }else{
                ((MainActivity)msg.obj).tv.setText("跳转 "+ msg.arg1 +" s");
            }
        }
    }

    static class MyThread extends Thread{
        MainActivity a;
        boolean isRun;
        public MyThread(MainActivity a) {
            this.a = a;
            isRun = true;
        }

        public void stopRun(){
            isRun = false;
        }

        @Override
        public void run() {
            for (int i = 3; i >= 0;--i)
            {
                if(!isRun) break;
                Message msg = new Message();
                msg.obj = a;
                msg.arg1 = i;
                a.handler.sendMessage(msg);
                try {Thread.sleep(1000); } catch (InterruptedException e) { }
            }
        }
    }
}
