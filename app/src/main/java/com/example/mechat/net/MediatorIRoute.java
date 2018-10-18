package com.example.mechat.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;




public class MediatorIRoute extends IRoute {
    public final static int
            WHAT_LOGIN = 1,   //登录
            WHAT_CAT = 2,     //聊天信息
            WHAT_GET_HEAD = 3;  //头像



    public MediatorIRoute(Handler handler, Communicator communicator) {
        super(handler, communicator);
    }

    @Override
    void readMessage(PHead pHead) {
        String taget = pHead.getHead(PHead.keys.TARGET);
        Log.e("WWS","target begin "+ taget);
        if(taget == null)
            return;
        if (taget.equals("login_response")) {
            if (pHead.getHead(PHead.keys.RESULT_CODE).equals("1")) {
                Integer integer = Integer.parseInt(pHead.getHead(PHead.keys.CONTEXT_LENGTH));
                if (integer > 0) {
                    byte[] byte1 = new byte[integer];
                    InputStream is = getCommunicator().getIs();
                    int len = 0;
                    try {
                        len += is.read(byte1);
                        while (len < integer)
                        {
                            len += is.read(byte1,len,integer - len);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String st1 = new String(byte1);
                    String[] st2 = st1.split("\\n");
                    List<longininfo> list = new ArrayList<longininfo>();
                    for (int i = 0; i < st2.length; i++) {
                        if (st2[i] == null) {
                            continue;
                        }
                        longininfo longin = new longininfo();
                        String[] st3 = st2[i].split(":");
                        longin.username = st3[0];
                        longin.ip = st3[1];
                        list.add(longin);
                    }
                    Message message = new Message();
                    message.what = WHAT_LOGIN;
                    message.arg1 = 1;
                    message.obj = list;
                    getHandler().sendMessage(message);
                }
            }

        } else if (taget.equals("cat_response")) {
            if (pHead.getHead(PHead.keys.STATE).equals("ok")) {
                String uname = pHead.getHead("userName");
                Integer integer = Integer.parseInt(pHead.getHead(PHead.keys.CONTEXT_LENGTH));
                if (integer > 0) {
                    Message message = new Message();
                    message.what = WHAT_CAT;
                    InputStream is = getCommunicator().getIs();
                    byte[] byte1 = new byte[integer];
                    int len = 0;
                    try {
                        len += is.read(byte1);
                        while (len < integer)
                        {
                            len += is.read(byte1,len,integer - len);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    catinfo cat = new catinfo();
                    cat.username = uname;
                    cat.bytes = byte1;
                    message.obj = cat;
                    getHandler().sendMessage(message);
                }
            }
        } else if (taget.equals("gethead_response")) {
            if (pHead.getHead(PHead.keys.STATE).equals("ok")) {
                String uname = pHead.getHead("userName");
                Integer integer = Integer.parseInt(pHead.getHead(PHead.keys.CONTEXT_LENGTH));
                Log.e("WWS","GetHead Content_len "+integer);
                if (integer > 0) {
                    Message message = new Message();
                    message.what = WHAT_GET_HEAD;
                    InputStream is = getCommunicator().getIs();
                    byte[] byte1 = new byte[integer];
                    int len = 0;
                    try {
                        len += is.read(byte1);
                        while (len < integer)
                        {
                            len += is.read(byte1,len,integer - len);
                        }
                    } catch (Exception e) {
                       Log.e("WWS","" + e.getMessage());
                    }
                    headinfo head = new headinfo();
                    head.username = uname;
                    head.bytes = byte1;
                    message.obj = head;
                    getHandler().sendMessage(message);
                }
            }
        }
        Log.e("WWS","target end "+ taget);
    }

    public class longininfo {
        String username;
        String ip;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }

    public class catinfo {
        String username;
        byte[] bytes;

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public class headinfo {
        String username;
        byte[] bytes;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }


}
