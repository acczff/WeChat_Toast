package com.example.mechat;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class MyAdapter extends BaseAdapter {


    private List<Info> infos;
    private Bitmap my_head;
    private Context context;

    //private Bitmap my_poo;
    // private Bitmap his_poo;
    private HashMap<String, Bitmap> heads;
    private OnPlaySelect playSelect;

    public MyAdapter(List<Info> infos, Bitmap head, HashMap<String, Bitmap> heads, Context context) {
        this.infos = infos;
        this.my_head = head;
        this.context = context;
        // this.my_poo = BitmapFactory.decodeResource(context.getResources(),R.drawable.right);
        // this.his_poo = BitmapFactory.decodeResource(context.getResources(),R.drawable.left);
        this.heads = heads;
    }

    @Override
    public int getItemViewType(int position) {
        if (infos.get(position).isme) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int i) {
        return infos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        ViewHolder vh = null;
        Bitmap his_head = null;
        if (v == null) {
            vh = new ViewHolder(this);
            if (infos.get(i).isme) {
                v = View.inflate(context, R.layout.item_chat_me, null);
                vh.tag = "me";
                vh.iv_head = v.findViewById(R.id.iv_my_head);
                vh.iv_content = v.findViewById(R.id.iv_my_content);
                vh.tv_un = v.findViewById(R.id.tv_un);
                vh.iv_head.setImageBitmap(my_head);
            } else {
                v = View.inflate(context, R.layout.item_chat_there, null);
                vh.tag = "his";
                vh.iv_head = v.findViewById(R.id.iv_his_head);
                vh.iv_content = v.findViewById(R.id.iv_his_content);
                vh.tv_un = v.findViewById(R.id.tv_un);
                if (infos.get(i).head != null)
                    vh.iv_head.setImageBitmap(infos.get(i).head);
                else if ((his_head = heads.get(infos.get(i).userName)) != null)
                    vh.iv_head.setImageBitmap(his_head);

            }
            vh.iv_content.setOnClickListener(vh);
            v.setTag(vh);
        } else {
            vh = (ViewHolder) v.getTag();
            if (vh.tag.equals("me")) {
                vh.iv_head.setImageBitmap(my_head);
                //vh.iv_content.setImageBitmap(my_poo);
            } else {
                if (infos.get(i).head != null)
                    vh.iv_head.setImageBitmap(infos.get(i).head);
                else if ((his_head = heads.get(infos.get(i).userName)) != null)
                    vh.iv_head.setImageBitmap(his_head);

                //vh.iv_content.setImageBitmap(his_poo);
            }
        }
        if (i == infos.size() - 1) {
            if (vh.tag.equals("me"))
                v.startAnimation(AnimationUtils.loadAnimation(viewGroup.getContext(), R.anim.right_in));
            else
                v.startAnimation(AnimationUtils.loadAnimation(viewGroup.getContext(), R.anim.left_in));

        }
        //Log.e("WWS","infos.get(i).img_kuan = "+infos.get(i).img_kuan);
        //vh.iv_content.setImageWidthScale(infos.get(i).img_kuan);
        //vh.iv_content.setScaleX(1.0f + infos.get(i).img_kuan);
        //vh.iv_content.invalidate();
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < infos.get(i).img_kuan; ++n) {
            sb.append(' ');
        }
        vh.thePos = i;
        vh.iv_content.setText(sb.toString());
        vh.tv_un.setText(infos.get(i).userName);
        return v;
    }

    public void setPlaySelect(OnPlaySelect playSelect) {
        this.playSelect = playSelect;
    }

    public OnPlaySelect getPlaySelect() {
        return playSelect;
    }
}

interface OnPlaySelect {
    void onPlay(int pos);
}

class ViewHolder implements View.OnClickListener {
    MyAdapter myAdapter;

    public ViewHolder(MyAdapter myAdapter) {
        this.myAdapter = myAdapter;
    }

    public ImageView iv_head;
    public TextView iv_content;
    public String tag;
    public TextView tv_un;
    int thePos = 0;

    @Override
    public void onClick(View view) {
        if (myAdapter.getPlaySelect() != null)
            myAdapter.getPlaySelect().onPlay(thePos);
    }
}
