package com.example.mechat;

import android.graphics.Bitmap;

public class Info{
    public boolean isme;
    public String path;
    public Bitmap head;
    public String userName;
    public float img_kuan;
    public Info(String path,float img_kuan,String userName) {
        this.isme = true;
        this.path = path;
        this.img_kuan = img_kuan;
        this.userName = userName;
    }
    public Info( String path,Bitmap head,String userName,float img_kuan) {
        this.isme = false;
        this.path = path;
        this.head = head;
        this.userName = userName;
        this.img_kuan = img_kuan;
    }


}
