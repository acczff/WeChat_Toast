package com.example.mechat.view;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mechat.R;

import java.util.Objects;

public class TutoBar {
    private static Integer maxW;
    private View view;
    private int flag = -1;
    private Context context;
    public static final int TIME_SHORT = Toast.LENGTH_SHORT,
            TIME_LONG = Toast.LENGTH_LONG,
            FLAG_ERR = 0,
            FLAG_NOMOR = 1;
    private int time = TIME_SHORT;

    public TutoBar(Context context) {
        this.context = context;
        if (maxW == null) {
            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager) Objects.requireNonNull(context.getSystemService(Context.WINDOW_SERVICE)))
                    .getDefaultDisplay().getMetrics(dm);
            maxW = dm.widthPixels;
        }
        LayoutInflater inflate = LayoutInflater.from(context);
        view = inflate.inflate(R.layout.toast_view, null);
    }

    public TutoBar setBackgroundColor(int color) {
        this.view.setBackgroundColor(color);
        return this;
    }

    public TutoBar setFlag(int flag) {
        this.flag = flag;
        return this;
    }

    public TutoBar setTime(int time) {
        this.time = time;
        return this;
    }

    public void show() {
        if (this.flag == FLAG_ERR)
            setBackgroundColor(Color.RED);
        else if (this.flag == FLAG_NOMOR)
            setBackgroundColor(context.getResources().getColor(R.color.colorAccent));

        Toast toast = new Toast(this.context);
        toast.setView(view);
        toast.setDuration(time);
        toast.setMargin(0, 0);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    public static TutoBar make(Context context, CharSequence text, int time) {
        TutoBar tutoBar = new TutoBar(context);
        View view = tutoBar.view;
        TextView textView;
        (textView = view.findViewById(R.id.toast_text))
                .setText(text);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
        layoutParams.width = maxW;
        textView.setLayoutParams(layoutParams);
        tutoBar.setTime(time);
        return tutoBar;
    }

}
