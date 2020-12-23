package com.github.zy3274311.libmp3lame;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class MP3Recorder implements Runnable {
    private static String TAG = MP3Recorder.class.getSimpleName();
    public static double MAX_DB = 20 * Math.log10(Short.MAX_VALUE);

    private LameEncoder lameEncoder;
    private Thread recordThread;
    private String path;

    //AudioRecorder 录制音频参数
    private final int sampleRateInHz;
    private final int audioFormat;
    private final int channelConfigIn;
    //LameEncoder MP3编码输出参数
    private final int sampleRateOutHz;
    private final int bitrate;
    private final int channelConfigOut;

    private OnDataCaptureListener mOnDataCaptureListener;
    private int rate;
    private final LameFormat format;

    /**
     * 使用默认音频录制及编码参数
     */
    public MP3Recorder() {
        this.sampleRateInHz = 44100;
        this.audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        this.channelConfigIn = AudioFormat.CHANNEL_IN_MONO;
        this.sampleRateOutHz = 44100;
        this.bitrate= 32;
        this.channelConfigOut = AudioFormat.CHANNEL_IN_MONO;

        format = new LameFormat();
        format.setSampleRateInHz(sampleRateInHz);
        format.setSampleRateOutHz(sampleRateOutHz);
        format.setBitrate(bitrate);
        format.setAudioFormat(audioFormat);
        format.setChannelConfigIn(channelConfigIn);
        format.setChannelConfigOut(channelConfigOut);
    }

    /**
     * 初始化MP3 Recorder
     * @param format mp3录制及编码参数设置
     */
    public MP3Recorder(LameFormat format) {
        this.format = format;
        this.sampleRateInHz = format.getSampleRateInHz();
        this.audioFormat = format.getAudioFormat();
        this.channelConfigIn = format.getChannelConfigIn();
        this.sampleRateOutHz = format.getSampleRateOutHz();
        this.bitrate = format.getBitrate();
        this.channelConfigOut = format.getChannelConfigOut();
    }

    /**
     * Registers an OnDataCaptureListener interface and specifies the rate at which the capture should be updated as well as the type of capture requested.
     * Call this method with a null listener to stop receiving the capture updates.
     *
     * @param onDataCaptureListener @see OnDataCaptureListener
     * @param rate                  in milliHertz at which the capture should be updated
     */
    public void setOnDataCaptureListener(OnDataCaptureListener onDataCaptureListener, int rate) {
        this.mOnDataCaptureListener = onDataCaptureListener;
        this.rate = rate;
    }

    public void setOutputPath(String path) {
        this.path = path;
    }

    public void prepare() {
        lameEncoder = new LameEncoder();
        lameEncoder.init(format);
    }


    public void start() throws RuntimeException {
        Log.d(TAG, "start() called");
        if (recordThread != null) {
            throw new RuntimeException("start() has been called");
        }
        recordThread = new Thread(this);
        recordThread.start();
    }

    public void stop() throws RuntimeException {
        Log.d(TAG, "stop() called");
        if (recordThread == null) {
            throw new RuntimeException("start() has not been called");
        }
        recordThread.interrupt();
        try {
            recordThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        recordThread = null;
    }

    public void release() {
        if (recordThread != null) {
            recordThread.interrupt();
            recordThread = null;
        }
        lameEncoder.release();
    }


    @Override
    public void run() {
        AudioRecord audioRecord = null;
        FileOutputStream fos = null;
        try {
            int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfigIn, audioFormat);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRateInHz,
                    channelConfigIn,
                    audioFormat,
                    bufferSizeInBytes);
            audioRecord.startRecording();


            short[] audioData = new short[bufferSizeInBytes / 2];
            byte[] mp3Buffer = new byte[bufferSizeInBytes];
            fos = new FileOutputStream(path);
            while (!Thread.interrupted()) {
                int sizeShorts = audioRecord.read(audioData, 0, bufferSizeInBytes / 2);
//                Log.d(TAG, "audioData 0.value: "+audioData[0]);
//                Log.d(TAG, "audioData MAX: "+Short.MAX_VALUE);
                captureData(audioData, sizeShorts);
                int encodeSize = lameEncoder.encode(audioData, sizeShorts, mp3Buffer);
//                Log.d(TAG, "lameEncoder.encode: "+encodeSize);
                fos.write(mp3Buffer, 0, encodeSize);
            }
            int flushSize = lameEncoder.flush(mp3Buffer);
//            Log.d(TAG, "lameEncoder.flush: "+flushSize);
            fos.write(mp3Buffer, 0, flushSize);
            fos.flush();
            audioRecord.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (audioRecord != null) {
                try {
                    audioRecord.stop();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                audioRecord.release();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getOutputPath() {
        return path;
    }

    public int getSampleRateOutHz() {
        return sampleRateOutHz;
    }

    public int getChannelConfigOut() {
        return channelConfigOut;
    }

    private long lastCaptureTimeMillis;
    private void captureData(short[] audioData, int size) {
        if (mOnDataCaptureListener != null && size > 0) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - lastCaptureTimeMillis > rate) {

                long sum = 0;
                for (int i = 0; i < size; i++) {
                    short audioDatum = audioData[i];
                    sum += Math.abs(audioDatum);
                }

                double ret = sum*500f/(size * 32767);
                if (ret >= 100){
                    ret = 100;
                }
                mOnDataCaptureListener.onDBDataCapture(ret);
                lastCaptureTimeMillis = currentTimeMillis;
            }
        }
    }

    public interface OnDataCaptureListener {
        void onDBDataCapture(double volume);
    }
}
