package com.example.mechat.net;


import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Communicator {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private static Communicator self;

    private Communicator(Socket socket) {
        this.socket = socket;
    }

    public static void init(Socket s){
        self = new Communicator(s);
    }

    public static Communicator getInstance()
    {
        return self;
    }

    public InputStream getIs() {
        synchronized (this)
        {
            if(is == null) {
                try {
                    is = socket.getInputStream();
                }catch (Exception e){
                    Log.e("WWS",""+e.getMessage());
                }
            }
            return is;
        }
    }

    public OutputStream getOs() {
        synchronized (this)
        {
            if(is == null) {
                try {
                    os = socket.getOutputStream();
                }catch (Exception e){
                    Log.e("WWS",""+e.getMessage());
                }
            }
            return os;
        }
    }

    public void close()
    {
        try {
            if(os != null)
                os.close();
            if(is != null)
                is.close();
            if(socket != null)
                socket.close();
        }catch (Exception e)
        {
            Log.e("WWS",""+e.getMessage());
        }
    }

}
