package com.example.mechat;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 这个录音帮助的最大缓存为6M
 * 使用方法--
 * 使用<code>RecordHelper.load()</code>方法加载一个录音设备，这个录音设备只能有一个。<br/>
 * <code>start()</code> 开始录音
 */
public class RecordHelper implements Runnable {
    private static ByteArrayOutputStream maxBuffer;
    static final int SOURCE = MediaRecorder.AudioSource.MIC;
    private static RecordHelper recordHelper = null;
    public static final int HZ = 44100;
    public static final int CHANNEL = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MAX_BUFFER_SIZE = 2 * 1024 * 1024;
    private ReadListen readListen;
    private Thread theThread;
    private AudioRecord record;
    public int bufferSizeInBytes;
    private boolean isRun = false;

    public static int getMinBuffer() {
        return AudioRecord.getMinBufferSize(HZ, CHANNEL, AUDIO_FORMAT);
    }

    private RecordHelper(ReadListen readListen) {
        this.readListen = readListen;
        bufferSizeInBytes = getMinBuffer();
        Log.e("-------", "buffersize" + bufferSizeInBytes);
        this.record = new AudioRecord(SOURCE, HZ, CHANNEL, AUDIO_FORMAT, bufferSizeInBytes);
    }

    /**
     * 接收一个监听器，即录制完毕后的回调
     *
     * @param bufferSize 最大缓存大小，超出此缓存将会自动停止录音
     * @param listen     回调
     * @return {@link RecordHelper}实例
     */
    public static RecordHelper load(int bufferSize, @NonNull ReadListen listen) {
        synchronized (RecordHelper.class) {
            if (bufferSize <= 0)
                throw new RuntimeException("The size of the cache can not be less than 0");
            if (bufferSize > MAX_BUFFER_SIZE)
                bufferSize = MAX_BUFFER_SIZE;
            if (maxBuffer == null)
                maxBuffer = new ByteArrayOutputStream(bufferSize);
            else {
                try {
                    maxBuffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                maxBuffer = new ByteArrayOutputStream(bufferSize);
            }
            if (recordHelper == null)
                recordHelper = new RecordHelper(listen);
            return recordHelper;
        }
    }

    /**
     * 开始录制音频
     */
    public void start() {
        if (isRun)
            return;
        if (theThread == null)
            theThread = new Thread(this);
        else if (!theThread.isAlive()) {
            theThread.interrupt();//try
            theThread = new Thread(this);
        }
        isRun = true;
        theThread.start();
        Log.e("-----", "startThread");
    }

    /**
     * 停止
     */
    public void stop() {
        isRun = false;
    }

    /**
     * 释放相关资源
     */
    public void close() {
        if (isRun)
            stop();
        try {
            maxBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        maxBuffer = null;
        recordHelper = null;
        record.stop();
        record.release();
        record = null;
    }


    @Override
    public synchronized void run() {
        Log.e("----", "begin");
        byte[] buffer = new byte[bufferSizeInBytes];
        record.startRecording();
        while (isRun) {
            int len = record.read(buffer, 0, bufferSizeInBytes);
            if (len < 0)
                break;
            maxBuffer.write(buffer, 0, len);
            if (maxBuffer.size() >= MAX_BUFFER_SIZE)
                stop();
        }
//        record.release();
        if (readListen != null)
            readListen.onRead(maxBuffer.toByteArray());
        maxBuffer.reset();
    }

    public interface ReadListen {
        void onRead(byte[] pcm);
    }
}
