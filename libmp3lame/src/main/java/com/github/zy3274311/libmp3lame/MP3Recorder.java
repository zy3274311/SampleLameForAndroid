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
    private int sampleRateInHz = 44100;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int channelConfigIn = AudioFormat.CHANNEL_IN_MONO;

    //LameEncoder MP3编码输出参数
    private int sampleRateOutHz = 44100;
    private int bitrate = 32;
    private int channelConfigOut = AudioFormat.CHANNEL_IN_MONO;

    private OnDataCaptureListener mOnDataCaptureListener;
    private int rate;

    public MP3Recorder() {
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
        LameFormat format = new LameFormat();
        format.setSampleRateInHz(sampleRateInHz);
        format.setSampleRateOutHz(sampleRateOutHz);
        format.setBitrate(bitrate);
        format.setAudioFormat(audioFormat);
        format.setChannelConfigIn(channelConfigIn);
        format.setChannelConfigOut(channelConfigOut);

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
                long v = 0;
                // 将 buffer 内容取出，进行平方和运算
                for (short audioDatum : audioData) {
                    v += Math.abs(audioDatum);
                }
                // 平方和除以数据总长度，得到音量大小。
                double mean = v / (double) size;
                double volume = 0;
                if (mean > 0) {
                    volume = 20 * Math.log10(0);
                }
                lastCaptureTimeMillis = currentTimeMillis;
                mOnDataCaptureListener.onDBDataCapture(volume);
            }
        }
    }

    public interface OnDataCaptureListener {
        void onDBDataCapture(double volume);
    }
}
