package com.example.mechat.net;

import com.example.mechat.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 头信息
 */
public class PHead {
    private HashMap<String, String> heads;
    private String headString = null;

    private PHead() {
    }

    private void setHeads(HashMap<String, String> heads) {
        this.heads = heads;
    }

    public String getHead(String key) {
        return heads.get(key);
    }

    public void setHead(String key, String value) {
        heads.put(key, value);
        headString = null;
    }

    @Override
    public String toString() {
        if (headString != null)
            return headString;
        StringBuilder stringBuilder = new StringBuilder(20);
        for (Map.Entry<String, String> v : heads.entrySet()) {
            stringBuilder.append(v.getKey() + "=" + v.getValue() + ";");
        }
        return (headString = stringBuilder.toString());
    }

    public int length() {
        return toString().length();
    }

    public long getContentLength() {
        long l = 0;
        try {
            l = Long.parseLong(getHead(keys.CONTEXT_LENGTH));
        } catch (Exception ignored) {
        }
        return l;
    }

    public static PHead create(String head) {
        HashMap<String, String> heads = new HashMap<>();
        if (head != null) {
            String sp[] = head.split(";");
            for (String s : sp) {
                if (s != null && s.length() > 0) {
                    String sp2[] = s.split("=");
                    if (sp2.length > 1) {
                        heads.put(sp2[0], sp2[1] == null ? "" : sp2[1]);
                    }
                }
            }

        }
        PHead p = new PHead();
        p.setHeads(heads);
        return p;
    }

    /**
     * 默认的头信息
     */
    public static PHead create() {
        PHead pHead = PHead.create(null);
        pHead.setHead(keys.STATE, "ok");
        pHead.setHead(keys.CONTEXT_TYPE, "stream");
        pHead.setHead(keys.CONTEXT_LENGTH, "0");
        pHead.setHead(keys.RESULT_CODE, "1");
        return pHead;
    }

    public static PHead create(String key, String value) {
        PHead pHead = create();
        pHead.setHead(key, value);
        return pHead;
    }

    public static class keys {
        public static final String
                STATE = "state",
                CONTEXT_TYPE = "context-type",
                CONTEXT_LENGTH = "context-length",
                TARGET = "target",
                RESULT_CODE = "result-code",
                RESULT_MESSAGE = "result-message";

    }

    public static void writeHead(OutputStream outputStream, PHead pHead) throws IOException {
        outputStream.write(Util.IntToByteArr(pHead.length()));
        outputStream.write(pHead.toString().getBytes());
    }
}
