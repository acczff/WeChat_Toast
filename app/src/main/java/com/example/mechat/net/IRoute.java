package com.example.mechat.net;

import android.os.Handler;
import android.util.Log;

import com.example.mechat.Util;

import java.io.IOException;


public abstract class IRoute implements Runnable {
    private Handler handler;
    private Communicator communicator;
    private volatile boolean isRun = false;
    private Thread thread = null;

    public IRoute(Handler handler, Communicator communicator) {
        this.handler = handler;
        this.communicator = communicator;
    }

    public Handler getHandler() {
        return handler;
    }

    public Communicator getCommunicator() {
        return communicator;
    }

    static String getHeadString(Communicator communicator) throws IOException {
        byte lenbys[] = new byte[4];
        int read_len = communicator.getIs().read(lenbys);
        Log.e("WWS", "read_len = " + read_len);
        if(read_len == -1)
            return null;
        int len = Util.ByteArrToInt(lenbys);
        Log.e("WWS", "len = " + len);
        if (len > 0) {
            byte content[] = null;
            try {
                content = new byte[len];
            }catch (OutOfMemoryError error)
            {
                Log.e("WWS","len = "+ len +" OutfMemoryError");
                return null;
            }
            communicator.getIs().read(content);
            return new String(content);
        }
        return null;
    }

    public void start() {
        if (isRun)
            return;
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this);
        }
        isRun = true;
        thread.start();
    }

    public void stop() {
        if (!isRun) return;
        isRun = false;
    }

    @Override
    public void run() {

        while (isRun) {
            try {

                String head = getHeadString(this.communicator);
                if (head == null) {
                    Log.e("WWS", "IRoute recv return");
                    handler.sendEmptyMessage(-2);
                    isRun = false;
                }
                PHead pHead = PHead.create(head);
                Log.e("WWS", "pHead = " + pHead);
                readMessage(pHead);

            } catch (IOException e) {
                //Log.e("WWS", "IRoute recv Exception : " + e.toString());
                //return;
            }
        }
    }

    abstract void readMessage(PHead pHead);
}
