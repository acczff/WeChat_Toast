package com.example.mechat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mechat.anm.RotateForZ;
import com.example.mechat.net.Communicator;
import com.example.mechat.net.IRoute;
import com.example.mechat.net.MediatorIRoute;
import com.example.mechat.net.PHead;
import com.example.mechat.view.Rec_View;
import com.example.mechat.view.TutoBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnTouchListener, Animation.AnimationListener {
    Rec_View rv;
    ListView lv;
    RelativeLayout relativeLayout;
    TextView hintText;
    MyAdapter adapter;
    ArrayList<Info> infos;
    Bitmap my_head;
    Communicator communicator;
    RecordHelper recordHelper;
    boolean isRecording, isCanSend = true;//isCanSend 可否发送录音
    PlayHelper playHelper;
    PlayPcmThread playPcmThread;
    MyHandler handler;
    String userName;
    HashMap<String, Bitmap> heads;
    IRoute iRoute;
    static final float XS = 0.0001f;
    ImageView playButton;
    LinearLayout mengBan;
    Animation rv_in, rv_out, to_big, to_small, xz_z;
    float out_recor_bound = 0;

    public final static Object MUTEX_SEND = new Object();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        loadView();
        loadAnm();

        heads = new HashMap<>();


        handler = new MyHandler(new WeakReference<ChatActivity>(this));
        playHelper = PlayHelper.load();
        lodmyhead:
        {
            if (LoginActivity.isDebug) {
                userName = "DEBUG";
                break lodmyhead;
            }
            userName = getIntent().getStringExtra("userName");
            communicator = Communicator.getInstance();
            playButton.setVisibility(View.GONE);
            iRoute = new MediatorIRoute(handler, communicator);
            iRoute.start();
        }

        my_head = getIntent().getParcelableExtra("my_head");
        //my_head = BitmapFactory.decodeFile(getFilesDir().getAbsolutePath() + "/TX.png");

        rv.setVisibility(View.GONE);

        infos = new ArrayList<>();
        adapter = new MyAdapter(infos, my_head, heads, this);
        lv.setAdapter(adapter);

        playButton.setOnTouchListener(this);
        relativeLayout.post(new Runnable() {
            @Override
            public void run() {
                out_recor_bound = relativeLayout.getY();
            }
        });

        rv.setRecListener(new Rec_View.OnRecListener() {
            @Override
            public void onPressed() {
                if (!(playPcmThread == null || !playPcmThread.isRun())) {
                    TutoBar.make(ChatActivity.this, "请等待播放完毕", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isRecording) {
                    recordHelper = RecordHelper.load(RecordHelper.MAX_BUFFER_SIZE, new RecordHelper.ReadListen() {
                        @Override
                        public void onRead(byte[] pcm) {
                            if (pcm.length >= 1024) {
                                Log.e("WWS", "onRead");
                                if (isCanSend) {
                                    if (!LoginActivity.isDebug)
                                        send_pcm(pcm);
                                    save_pcm(pcm);
                                }
                                isRecording = false;
                                xz_z.setRepeatCount(0);
                                xz_z.cancel();
                                handler.sendEmptyMessage(0x10001);
                            }
                        }
                    });
                    isRecording = true;
                    recordHelper.start();
                } else {
                    TutoBar.make(ChatActivity.this, "上次录音未结束", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onUp() {
                if (isRecording) {
                    recordHelper.stop();
                }
            }

            @Override
            public void onMoveOut() {
                if (isRecording) {
                    recordHelper.stop();
                }
            }
        });


        adapter.setPlaySelect(new OnPlaySelect() {
            @Override
            public void onPlay(int pos) {
                if (playPcmThread == null || !playPcmThread.isRun()) {
                    play_pcm(infos.get(pos).path);
                } else {
                    TutoBar.make(ChatActivity.this, "请等待播放完毕", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void loadView() {
        lv = findViewById(R.id.lv);
        rv = findViewById(R.id.rv);
        hintText = findViewById(R.id.hint_text);
        playButton = findViewById(R.id.play_button);
        mengBan = findViewById(R.id.mengban_bk);
        relativeLayout = findViewById(R.id.rv_layout);
    }

    private void loadAnm() {
        rv_in = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
        rv_out = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
        to_big = AnimationUtils.loadAnimation(this, R.anim.to_big);
        to_small = AnimationUtils.loadAnimation(this, R.anim.to_small);
        xz_z = new RotateForZ();
        xz_z.setDuration(2000);
        xz_z.setRepeatCount(-1);
        rv_in.setAnimationListener(this);
        rv_out.setAnimationListener(this);
    }

    void send_pcm(final byte[] pcm) {
        new Thread() {
            @Override
            public void run() {

                try {
                    OutputStream os = communicator.getOs();
                    PHead pHead = PHead.create();
                    pHead.setHead(PHead.keys.TARGET, "Cat");
                    pHead.setHead("userName", userName);
                    pHead.setHead(PHead.keys.CONTEXT_LENGTH, pcm.length + "");
                    String head = pHead.toString();
                    int len = head.getBytes().length;

                    synchronized (MUTEX_SEND) {
                        os.write(Util.IntToByteArr(len), 0, 4);

                        os.write(head.getBytes(), 0, head.getBytes().length);

                        os.write(pcm, 0, pcm.length);
                    }


                } catch (IOException e) {
                    Log.e("WWS", "" + e.getMessage());
                }
            }
        }.start();
    }

    void request_head(final String name) {
        Log.e("WWS", "request head " + name);
        new Thread() {
            @Override
            public void run() {

                try {
                    OutputStream os = communicator.getOs();

                    PHead pHead = PHead.create();
                    pHead.setHead(PHead.keys.TARGET, "GetHead");
                    pHead.setHead("userName", name);

                    String head = pHead.toString();


                    int len = head.getBytes().length;

                    synchronized (MUTEX_SEND) {
                        os.write(Util.IntToByteArr(len), 0, 4);

                        os.write(head.getBytes(), 0, head.getBytes().length);
                    }

                } catch (IOException e) {
                    Log.e("WWS", "" + e.getMessage());
                }
            }
        }.start();
    }

    void save_pcm(byte[] pcm) {
        FileOutputStream out = null;
        File f = null;
        try {
            f = new File(getCacheDir() + "/me-" + System.currentTimeMillis() + ".pcm");
            out = new FileOutputStream(f);
            out.write(pcm);
            out.flush();

            infos.add(new Info(f.getAbsolutePath(), pcm.length * XS, userName));
            handler.sendEmptyMessage(-1);
        } catch (Exception e) {
            Log.e("WWS", "" + e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
            }
        }
    }

    void save_pcm(byte[] pcm, String userName) {
        Log.e("WWS", "save pcm for orthe " + userName + " " + pcm.length);
        FileOutputStream out = null;
        File f = null;
        try {
            f = new File(getCacheDir() + "/" + userName + "-" + System.currentTimeMillis() + ".pcm");
            out = new FileOutputStream(f);
            out.write(pcm);
            out.flush();
        } catch (Exception e) {
            Log.e("WWS", "" + e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
            }
        }
        infos.add(new Info(f.getAbsolutePath(), heads.get(userName), userName, pcm.length * XS));
        handler.sendEmptyMessage(-1);
    }

    void play_pcm(String path) {
        playPcmThread = new PlayPcmThread(path);
        playPcmThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordHelper != null)
            recordHelper.close();
        playHelper.close();
        if (!LoginActivity.isDebug) {
            iRoute.stop();
            communicator.close();
        }
    }


    boolean isOutTouch = false;
    boolean isCanRecord = true;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();
        int action = motionEvent.getAction();
        if (id == R.id.play_button) {
            if (action == MotionEvent.ACTION_DOWN &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !(isCanRecord = (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED))) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    TutoBar.make(this, "无录音权限将无法录制音频", TutoBar.TIME_SHORT).setFlag(TutoBar.FLAG_ERR).show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 201);
                }
                return false;
            }
            if (!isCanRecord)
                return false;
            float ys = motionEvent.getRawY();
            if (ys < out_recor_bound && !isOutTouch) {
                isOutTouch = true;
                isCanSend = false;
                playButton.setImageDrawable(getResources().getDrawable(R.drawable.red_record_image, getTheme()));
                hintText.setText("松手取消");
            } else if (ys >= out_recor_bound && isOutTouch) {
                isOutTouch = false;
                isCanSend = true;
                hintText.setText(getResources().getString(R.string.up_canc));
                playButton.setImageDrawable(getResources().getDrawable(R.drawable.layer_mengban_rec, getTheme()));
            }
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mengBan.setBackgroundColor(0x55aaaaaa);
                    rv.setVisibility(View.VISIBLE);
                    rv.soundPool.play(rv.jd_id, 0.5f, 0.5f, 100, 0, 1);
                    rv.startAnimation(rv_in);
                    to_big.setFillAfter(true);
                    view.startAnimation(to_big);
                    hintText.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_UP:
                    view.startAnimation(to_small);
                    rv.startAnimation(rv_out);
                    mengBan.setBackgroundColor(0x00000000);
                    playButton.setImageDrawable(getResources().getDrawable(R.drawable.layer_mengban_rec, getTheme()));
                    hintText.setVisibility(View.GONE);
                    if (!isCanSend) TutoBar.make(this, "取消发送", TutoBar.TIME_SHORT).show();
                    rv.stopAnm();
                    break;
            }
        } else return false;
        return true;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (animation == rv_in) {
            rv.playAnm();
        } else if (animation == rv_out) {
            rv.setVisibility(View.GONE);
            playButton.startAnimation(xz_z);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    class PlayPcmThread extends Thread {
        String path;
        boolean is_Run;

        public PlayPcmThread(String path) {
            this.path = path;
        }

        public boolean isRun() {
            return is_Run;
        }

        @Override
        public void run() {
            is_Run = true;
            FileInputStream in = null;
            File f = null;
            int n = 0;
            try {
                f = new File(path);
                in = new FileInputStream(f);
                int len = -1;
                byte[] buf = new byte[2048];
                while ((len = in.read(buf)) != -1) {
                    if (n == 2)
                        playHelper.ready();
                    playHelper.write(buf, 0, len);
                    ++n;
                }
            } catch (Exception e) {
                Log.e("WWS", "" + e.getMessage());
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e) {
                }
                is_Run = false;
                return;
            }
        }
    }

    static class MyHandler extends Handler {
        WeakReference<ChatActivity> activity;

        public MyHandler(WeakReference<ChatActivity> activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -2: {
                    ChatActivity chatActivity = activity.get();
                    if (chatActivity != null) {
                        TutoBar.make(chatActivity, "Recv Thread already Return", Toast.LENGTH_SHORT).setFlag(TutoBar.FLAG_ERR).show();
                        chatActivity.playButton.setVisibility(View.GONE);
                    }
                }
                case -1: {
                    ChatActivity chatActivity = activity.get();
                    if (chatActivity != null) {
                        chatActivity.adapter.notifyDataSetChanged();
                        chatActivity.lv.setSelection(chatActivity.infos.size() - 1);
                    }
                    break;
                }
                case MediatorIRoute.WHAT_LOGIN: {
                    //Log.e("WWS","SI_Login");
                    //when load over
                    List<MediatorIRoute.longininfo> list = (List<MediatorIRoute.longininfo>) msg.obj;
                    ChatActivity chatActivity = activity.get();
                    if (chatActivity != null) {
//                        chatActivity.rv.setCan_rec(true);
                        chatActivity.playButton.setVisibility(View.VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(chatActivity, R.anim.zoom_in);
                        chatActivity.playButton.startAnimation(animation);
                        for (MediatorIRoute.longininfo longininfo : list) {
                            if (!longininfo.getUsername().equals(chatActivity.userName)) {
                                activity.get().request_head(longininfo.getUsername());
                            }
                        }
                    }
                    break;
                }
                case MediatorIRoute.WHAT_CAT: {
                    //Log.e("WWS","SI_Cat");
                    MediatorIRoute.catinfo catinfo = (MediatorIRoute.catinfo) msg.obj;
                    ChatActivity chatActivity = activity.get();
                    if (chatActivity != null) {
                        chatActivity.save_pcm(catinfo.getBytes(), catinfo.getUsername());
                        if (chatActivity.heads.get(catinfo.getUsername()) == null) {
                            chatActivity.request_head(catinfo.getUsername());
                        }
                    }
                    break;
                }
                case MediatorIRoute.WHAT_GET_HEAD: {
                    //Log.e("WWS","SI_GH");
                    MediatorIRoute.headinfo headinfo = (MediatorIRoute.headinfo) msg.obj;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(headinfo.getBytes(), 0, headinfo.getBytes().length);
                    ChatActivity chatActivity = activity.get();
                    if (chatActivity != null) {
                        chatActivity.heads.put(headinfo.getUsername(), bitmap);
                        chatActivity.adapter.notifyDataSetChanged();
                    }
                    break;
                }

            }
        }
    }
}
