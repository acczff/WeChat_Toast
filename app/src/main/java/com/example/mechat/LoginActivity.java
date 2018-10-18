package com.example.mechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.mechat.db.IPHistorySQLite;
import com.example.mechat.net.Communicator;
import com.example.mechat.net.PHead;
import com.example.mechat.view.TutoBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static boolean isDebug;
    ImageView iv_tx;
    EditText et_ip, et_name;
    ImageView iv_go;
    ImageView select_btn;
    String logIpPort = "39.106.197.25:9150";
    static final int REQUEST_ALBUM_OK = 1;
    static final int REQUEST_CUT_BITMAP = 2;
    String path;
    MyHandler handler;
    Communicator communicator;
    boolean isImageSelected = false;
    Bitmap tx_bitmap;
    String userName;
    SharedPreferences sharedPreferences;
    boolean isLoging = false;
    Animation normal, xz_720, xz_180, xz_0;
    PopupMenu popupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = getSharedPreferences("save_ip", MODE_PRIVATE);

        handler = new MyHandler();

        userName = sharedPreferences.getString("userName", null);
        iv_tx = findViewById(R.id.iv_tx);
        iv_go = findViewById(R.id.iv_go);
        et_ip = findViewById(R.id.et_ip);
        et_name = findViewById(R.id.name_enter);
        select_btn = findViewById(R.id.select_btn);

        normal = AnimationUtils.loadAnimation(this, R.anim.ap_in_normal);
        xz_720 = AnimationUtils.loadAnimation(this, R.anim.xuanzhuan_720);
        xz_180 = AnimationUtils.loadAnimation(this, R.anim.xuanzhuan_180);
        xz_0 = AnimationUtils.loadAnimation(this, R.anim.xuznzhuan_0);
        xz_180.setFillAfter(true);
        xz_0.setFillAfter(true);

        iv_tx.startAnimation(normal);
        et_ip.startAnimation(normal);
        iv_go.startAnimation(xz_720);
        if (userName != null)
            et_name.setText(userName);

        select_btn.setOnClickListener(this);
        iv_tx.setOnClickListener(this);
        iv_go.setOnClickListener(this);

        path = getFilesDir().getAbsolutePath() + "/TX.png";
        File tx = new File(path);
        if (tx.exists()) {
            loadTX();
        }
        et_ip.setText(sharedPreferences.getString("ip_config", ""));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_tx:
                Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
                albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(albumIntent, REQUEST_ALBUM_OK);
                break;
            case R.id.iv_go:
                if (isDebug){
                    gotoChat();
                    return;
                }
                if (isLoging) {
                    TutoBar.make(this, "请等待上一个操作完成", TutoBar.TIME_LONG).setFlag(TutoBar.FLAG_NOMOR).show();
                    return;
                }
                iv_go.startAnimation(AnimationUtils.loadAnimation(this, R.anim.xuanzhuan_720));
                if (!isImageSelected) {
                    TutoBar.make(this, "你还没选择头像！", Toast.LENGTH_SHORT).setFlag(TutoBar.FLAG_ERR).show();
                    return;
                }
                String[] ss = null;
                try {
                    String ip_str = et_ip.getText().toString();
                    String name_str = et_name.getText().toString();
                    sharedPreferences.edit().putString("ip_config", ip_str).apply();
                    if (!name_str.isEmpty()) {
                        if (!name_str.matches("^[0-9A-Za-z_\\+\\-\\$]{1,20}$"))
                            throw new Exception("昵称只能由英文和数字下划线部分符号组成");
                        userName = name_str;
                        sharedPreferences.edit().putString("userName", name_str).apply();
                    } else {
                        throw new Exception("昵称不能为空");
                    }
                    ss = ip_str.split(":");
                    connectService(ss[0], Integer.parseInt(ss[1]));
                    if (!ip_str.equals(logIpPort))
                        IPHistorySQLite.getSqLite(this).add(ss[0], Integer.parseInt(ss[1]));
                    isLoging = true;
                } catch (Exception e) {
                    TutoBar.make(this, "err:" + e.getMessage(), Toast.LENGTH_SHORT)
                            .setFlag(TutoBar.FLAG_ERR).show();
                    isLoging = false;
                    return;
                }
                break;
            case R.id.select_btn: {
                select_btn.startAnimation(xz_180);
                if (popupMenu == null) {
                    popupMenu = new PopupMenu(this, et_ip);
                    popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                        @Override
                        public void onDismiss(PopupMenu menu) {
                            select_btn.startAnimation(xz_0);
                        }
                    });
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == 0){
                                LoginActivity.isDebug = true;
                                et_ip.setText("DEBUG");
                                return false;
                            }
                            et_ip.setText(item.getTitle().toString());
                            return false;
                        }
                    });
                }
                Menu menu = popupMenu.getMenu();
                menu.clear();
                menu.add( Html.fromHtml("<font color='#00BFFF'>测试进入（忽略IP）</font>"));

                Spanned html = Html.fromHtml("<font color='#00BFFF'>" + logIpPort + "</font>");
                menu.add(html);
                ArrayList<String> sis = IPHistorySQLite.getSqLite(this).queryTop(10);
                for (String s : sis) {
                    menu.add(s);
                }
                popupMenu.show();
            }
            break;
        }
    }


    void gotoChat() {
        if (!isImageSelected) {
           tx_bitmap = ((BitmapDrawable)getResources().getDrawable(R.mipmap.debug,getTheme())).getBitmap();
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("my_head", tx_bitmap);
        intent.putExtra("userName", userName);
        startActivity(intent);
//        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ALBUM_OK:
                if (data != null) {
                    Intent intent = new Intent(this, CutActivity.class);
                    intent.setData(data.getData());
                    startActivityForResult(intent, REQUEST_CUT_BITMAP);
                } else {
                    TutoBar.make(this, "你没有选择图片", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CUT_BITMAP:
                Bitmap new_bitmap = (Bitmap) data.getExtras().get("new_bitmap");
                if (new_bitmap != null) {
                    new_bitmap = getCircleBitmap(new_bitmap);
                    iv_tx.setImageBitmap(new_bitmap);
                    tx_bitmap = new_bitmap;
                    isImageSelected = true;
                }
                break;
        }
    }

    Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circleBitmap);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight()));
        float roundPx;
        roundPx = bitmap.getWidth();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        final Rect src = new Rect(0, 0, bitmap.getWidth(),
                bitmap.getHeight());
        canvas.drawBitmap(bitmap, src, rect, paint);
        return circleBitmap;
    }

    class Handle {
        public LoginActivity activity;
        public Socket socket;

        public Handle(LoginActivity activity, Socket socket) {
            this.activity = activity;
            this.socket = socket;
        }
    }

    private static final int CHECK_KEY1 = 0xfa12c24b;
    private static final int CHECK_KEY2 = 0xa029fbcf;

    private void connectService(final String ip, final int prot) {
        new Thread() {
            @Override
            public void run() {
                Socket socket = null;
                OutputStream os = null;
                try {
                    socket = new Socket(ip, prot);
                    socket.setSoTimeout(2000);
                    socket.getOutputStream().write(Util.IntToByteArr(CHECK_KEY1));
                    socket.getOutputStream().write(Util.IntToByteArr(CHECK_KEY2));
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = new Handle(LoginActivity.this, socket);
                    handler.sendMessage(msg);

                } catch (Exception e) {
                    try {
                        if (socket != null)
                            socket.close();
                    } catch (Exception e2) {
                        Log.e("WWS", "" + e2.getMessage());
                    }

                    Log.e("WWS", "" + e.getMessage());
                    Message m = new Message();
                    m.what = -1;
                    m.obj = LoginActivity.this;
                    handler.sendMessage(m);

                }
            }
        }.start();
    }

    private void sendBitmap() {
        new Thread() {
            @Override
            public void run() {

                try {
                    OutputStream os = communicator.getOs();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    tx_bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                    PHead pHead = PHead.create();
                    pHead.setHead(PHead.keys.CONTEXT_TYPE, "png");
                    pHead.setHead(PHead.keys.TARGET, "Login");
                    pHead.setHead("userName", userName);
                    pHead.setHead(PHead.keys.CONTEXT_LENGTH, baos.size() + "");

                    String head = pHead.toString();


                    int len = head.getBytes().length;


                    os.write(Util.IntToByteArr(len), 0, 4);
                    os.flush();

                    os.write(head.getBytes(), 0, head.getBytes().length);
                    os.flush();

                    os.write(baos.toByteArray(), 0, baos.size());
                    os.flush();

                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = LoginActivity.this;
                    handler.sendMessage(msg);

                } catch (IOException e) {
                    Log.e("WWS", "" + e.getMessage());
                }

            }
        }.start();
    }

    void saveTx() {
        FileOutputStream out = null;
        try {
            File tx = new File(path);
            out = new FileOutputStream(tx);
            tx_bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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
    }

    private void loadTX() {
        tx_bitmap = BitmapFactory.decodeFile(path);
        if (tx_bitmap != null) {
            iv_tx.setImageBitmap(tx_bitmap);
            isImageSelected = true;
        }
    }

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    LoginActivity loginActivity = (LoginActivity) msg.obj;
                    loginActivity.isLoging = false;
                    TutoBar.make(loginActivity, "连接失败！！！", TutoBar.TIME_SHORT).setFlag(TutoBar.FLAG_ERR).show();
                    break;
                case 0:
                    Handle h = (Handle) msg.obj;
                    Communicator.init(h.socket);
                    h.activity.communicator = Communicator.getInstance();
                    TutoBar.make(h.activity, "连接成功！准备上传头像", TutoBar.TIME_SHORT).setBackgroundColor(
                            h.activity.getResources().getColor(android.R.color.holo_green_dark)
                    ).show();
                    h.activity.sendBitmap();
                    h.activity.isLoging = false;
                    break;
                case 1:
                    LoginActivity activity = (LoginActivity) msg.obj;
                    activity.saveTx();
                    activity.gotoChat();
                    activity.isLoging = false;
                    break;
            }
        }
    }
}
