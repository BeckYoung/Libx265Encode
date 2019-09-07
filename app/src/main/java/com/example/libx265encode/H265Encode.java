package com.example.libx265encode;

import java.nio.ByteBuffer;

public class H265Encode {
    private static volatile H265Encode instance;

    private ByteBuffer mVideobuffer;

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("x265");
    }

    public static H265Encode getInstance() {
        if (instance == null) {
            synchronized (H265Encode.class) {
                if (instance == null) {
                    instance = new H265Encode();
                }
            }
        }
        return instance;
    }

    private H265Encode() {

    }

    public void encodeAndSave(byte[] buffer, int length, long time) {
        if (mVideobuffer == null || mVideobuffer.capacity() < length) {
            mVideobuffer = ByteBuffer.allocateDirect(((length / 1024) + 1) * 1024);
        }
        mVideobuffer.rewind();
        mVideobuffer.put(buffer, 0, length);
        encodeH265(buffer, length);
    }

    public native void initX265Encode(int width, int height, int fps, int bite);

    public native byte[] encodeH265(byte[] inBuffer, int length);
    public native String stringFromJNI();
}
