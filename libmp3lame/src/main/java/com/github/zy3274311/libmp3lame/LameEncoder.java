package com.github.zy3274311.libmp3lame;

import android.media.AudioFormat;

public class LameEncoder {

    static {
        System.loadLibrary("libmp3lame");
    }

    private long pr;
    private LameFormat format;

    public void init(LameFormat format) {
        this.format = format;
        int channelConfigOut = format.getChannelConfigOut();
        int out_channels = channelConfigOut== AudioFormat.CHANNEL_IN_MONO?1:2;
        pr = init(format.getSampleRateInHz(), format.getSampleRateOutHz(), out_channels, format.getBitrate());
    }

    int encode(short[] audioData, int sizeInShorts, byte[] mp3buffer) {
        return encode(pr, audioData, audioData, sizeInShorts, mp3buffer);
    }

    int flush(byte[] mp3buffer) {
        return flush(pr, mp3buffer);
    }

    void release() {
        release(pr);
    }

    private native long init(int sampleRateInHz, int sampleRateOutHz, int out_channels, int bitrate);

    private native int encode(long pr, short[] leftPCM, short[] rightPCM, int sizeInShorts, byte[] mp3buffer);

    private native int flush(long pr, byte[] mp3buffer);

    private native void release(long pr);
}
