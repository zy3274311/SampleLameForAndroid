package com.github.zy3274311.libmp3lame;

public class LameFormat {
    private int sampleRateInHz;
    private int sampleRateOutHz;
    private int bitrate;
    private int audioFormat;
    private int channelConfigIn;
    private int channelConfigOut;


    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public int getSampleRateOutHz() {
        return sampleRateOutHz;
    }

    public void setSampleRateOutHz(int sampleRateOutHz) {
        this.sampleRateOutHz = sampleRateOutHz;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
    }

    public int getChannelConfigIn() {
        return channelConfigIn;
    }

    public void setChannelConfigIn(int channelConfigIn) {
        this.channelConfigIn = channelConfigIn;
    }

    public int getChannelConfigOut() {
        return channelConfigOut;
    }

    public void setChannelConfigOut(int channelConfigOut) {
        this.channelConfigOut = channelConfigOut;
    }
}
