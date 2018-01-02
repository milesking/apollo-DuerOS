package com.baidu.carlifevehicle.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.baidu.carlife.protobuf.CarlifeVideoFrameRateProto.CarlifeVideoFrameRate;
import com.baidu.carlifevehicle.CarlifeActivity;
import com.baidu.carlifevehicle.CommonParams;
import com.baidu.carlifevehicle.connect.CarlifeCmdMessage;
import com.baidu.carlifevehicle.connect.ConnectClient;
import com.baidu.carlifevehicle.connect.ConnectManager;
import com.baidu.carlifevehicle.encryption.AESManager;
import com.baidu.carlifevehicle.encryption.EncryptSetupManager;
import com.baidu.carlifevehicle.message.MsgHandlerCenter;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Message;
import android.os.SystemClock;
import android.view.Surface;

public class DecodeUtil {
    private static final String TAG = "DecodeUtil";
    private static final String TAG_QA = "--QA-TEST--";
    private static final String KEY_AVC_REORDER = "disreorder";
    private static final int MAX_FRAME_LENGTH = 1920 * 1080 * 3 / 2;
    private static final int STATIS_PERIOD = 3;
    private static final int FRAME_RATE = 30;
    private static final int MID_FRAME_RATE = 25;
    private static final int MIN_WAIT_TIME = 100;
    private static final int MAX_WAIT_TIME = 400;
    private static DecodeUtil mInstance = null;
    private MediaCodec mDecoder = null;
    private Surface mSurface;
    private DecoderThread mThread;
    private DisplayThread mDisplayThread;
    /**
     * first frame received
     */
    private boolean isFirstFrame = true;
    private boolean hasReset = false;
    /**
     * first frame decoded
     */
    private boolean isFirstDecode = true;
    /**
     * spspps to solve decoder reset bug
     */
    private byte[] mSpsPps;
    /**
     * decoder can not obtain input buffer times
     */
    private int mInputMissCount = 0;
    /**
     * input frame count
     */
    private int mInputCount = 0;
    /**
     * output frame count
     */
    private int mOutputCount = 0;
    private int mEncWidth = 768;
    private int mEncHeight = 480;
    /**
     * used for statistic the total count of frame which is received in one second
     */
    private int mFrameCount = 0;
    /**
     * frame rate
     */
    private int mTargetFrameRate = FRAME_RATE;
    /**
     * used for statistic the total time which receive frame in one second
     */
    private long mFrameCountTime = 0;
    /**
     * used for test
     */
    private long timeFlag = 0;
    /**
     * the total wait time for waiting data
     */
    private long mWaitDataTimeAdd = 0;
    /**
     * the time to wait for input2Decoder
     */
    private long mWaitTime = (1000 - MAX_WAIT_TIME) * 1000;
    private int statisticTimes = 0;
    private int mFrameTotalCount = 0;
    private int mThrowFrameCount = 0;
    private Object mLock = new Object();
    private boolean mIsDecorderReady = false;
    private long mFirstDecodeTime = 0;

    MediaCodec.BufferInfo inputBufferInfo = new MediaCodec.BufferInfo();
    MediaCodec.BufferInfo outputBufferInfo = new MediaCodec.BufferInfo();

    private AESManager mAESManager = new AESManager();

    public static DecodeUtil getInstance() {
        if (mInstance == null) {
            mInstance = new DecodeUtil();
        }
        return mInstance;
    }

    private DecodeUtil() {
        if (CarlifeConfUtil.getInstance().getBooleanProperty(CarlifeConfUtil.KEY_BOOL_NEED_MORE_DECODE_TIME)) {
            mWaitTime = 1000000;
        }
    }

    /**
     * start decode thread
     */
    public void startThread() {
        if (mThread == null) {
            mThread = new DecoderThread();
            mThread.start();
        }
    }

    /**
     * stop decode thread
     */
    public void stopThread() {
        ReleaseDecoderThread releaseThread = new ReleaseDecoderThread();
        releaseThread.start();
    }

    /**
     * pause decode thread, will reset the decoder but will not set reset command message to
     * mobile device.
     */
    public void pauseThread() {
        if (mThread != null) {
            mThread.pauseThread();
        } else {
            synchronized(mLock) {
                unInitDecoder();
            }
        }
    }

    /**
     * resume decode thread
     */
    public void resumeThread() {
        if (mThread != null) {
            mThread.resumeThread();
        }
    }

    /**
     * called when disconnecting
     */
    public void disconnectedReset() {
        hasReset = false;
        isFirstFrame = true;
        isFirstDecode = true;
        mThread = null;
        // used to solve some blurred screen and black screen bugs
        mSpsPps = null;
        mIsDecorderReady = false;
    }

    public byte[] getSpsPps() {
        return mSpsPps;
    }

    public void initDecoder(Surface surface) {
        initDecoder(surface, mEncWidth, mEncHeight);
    }

    public void initDecoder(Surface surface, int width, int height) {
        if ((mDecoder != null || surface == null) && mEncWidth == width && mEncHeight == height) {
            LogUtil.e(TAG, "mDecoder != null || surface == null");
            return;
        }
        // use mlock to avoid uninit while initializing
        synchronized(mLock) {
            if ((mDecoder != null || surface == null) && mEncWidth == width && mEncHeight == height) {
                LogUtil.e(TAG, "mDecoder != null || surface == null");
                return;
            }
            unInitDecoder();
            LogUtil.i(TAG, "initDecoder start + width = " + width + ", height = " + height);
            mSurface = surface;
            mEncWidth = width;
            mEncHeight = height;
            if (mSurface != null && !mSurface.isValid()) {
                LogUtil.i(TAG, "surface has been released");
                return;
            }
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mEncWidth, mEncHeight);
            try {
                mDecoder = MediaCodec.createDecoderByType("video/avc");
                if (CommonParams.VEHICLE_CHANNEL.startsWith(CommonParams.VEHICLE_CHANNEL_CHANGAN.substring(0, 4))) {
                    // default is 0 recorder，set to 1 disable Recorder
                    mediaFormat.setInteger(KEY_AVC_REORDER, 1);
                }
                mDecoder.configure(mediaFormat, mSurface, null, 0);
            } catch (Exception e) {
                if (mDecoder != null) {
                    mDecoder.release();
                    mDecoder = null;
                }
                e.printStackTrace();
                return;
            }
            mDecoder.start();
            LogUtil.i(TAG, "initDecoder done");
            if (mDisplayThread != null) {
                mDisplayThread.stopThread();
            }
            mDisplayThread = new DisplayThread();
            mDisplayThread.start();
        }
    }

    private void unInitDecoder() {
        if (mDecoder != null) {
            try {
                mDecoder.stop();
                mDecoder.release();
                LogUtil.i(TAG, "unInitDecoder OK~");
            } catch (IllegalStateException e) {
                e.printStackTrace();
                LogUtil.i(TAG, "unInitDecoder IllegalStateException~");
            }
            if (mDisplayThread != null) {
                mDisplayThread.stopThread();
            }
            mDisplayThread = null;
            mDecoder = null;
        }
    }

    private class ReleaseDecoderThread extends Thread {

        @Override
        public void run() {
            if (mThread != null) {
                mThread.stopThread();
            }
        }
    }

    private class DecoderThread extends Thread {

        private boolean isRunning = true;
        private boolean isPausing = false;

        public void stopThread() {
            isRunning = false;
            mThread = null;
            isPausing = false;
            if (mDecoder != null) {
                synchronized(mLock) {
                    unInitDecoder();
                }
            }
        }

        public void pauseThread() {
            if (isPausing == false) {
                synchronized(mLock) {
                    isPausing = true;
                    unInitDecoder();
                }
            }
        }

        public void resumeThread() {
            if (isPausing == true) {
                if (mDecoder == null) {
                    return;
                }
                synchronized(mLock) {
                    isPausing = false;
                    hasReset = true;
                }
            }
        }

        @Override
        public void run() {
            int flag = -1;
            int dataLength = 0;
            long timeDValue = 0;
            byte inputData[] = new byte[1000000];
            mFrameCountTime = System.currentTimeMillis();
            mTargetFrameRate = FRAME_RATE;
            while (isRunning) {
                timeFlag = System.currentTimeMillis();
                flag = ConnectManager.getInstance().readVideoData(inputData, 4);
                if (flag != 4) {
                    LogUtil.e(TAG, "decoderThread get data length failed");
                    break;
                }
                dataLength = ((inputData[0] << 24) & 0xff000000)
                        + ((inputData[1] << 16) & 0xff0000) + ((inputData[2] << 8) & 0xff00)
                        + ((inputData[3] << 0) & 0xff);
                if (dataLength > MAX_FRAME_LENGTH) {
                    LogUtil.e(TAG, "dataLength > MAX_FRAME_LENGTH; dataLength = " + dataLength);
                    dataLength = -1;
                } else if (inputData.length < dataLength) {
                    inputData = new byte[dataLength];
                }
                flag = ConnectManager.getInstance().readVideoData(inputData, 4);
                if (flag != 4) {
                    LogUtil.e(TAG, "decoderThread get timeStamp failed");
                    break;
                }
                flag = ConnectManager.getInstance().readVideoData(inputData, 4);
                if (flag != 4) {
                    LogUtil.e(TAG, "decoderThread get service_type failed");
                    break;
                }
                if (dataLength == 0) {
                    // empty package, used for heart beat
                    continue;
                }
                timeDValue = timeFlag - mFrameCountTime;
                mWaitDataTimeAdd += System.currentTimeMillis() - timeFlag;
                if (timeDValue > 980) {
                    LogUtil.d(TAG, mFrameCount + " frameCounts in " + timeDValue + " ms, wait for " + mWaitDataTimeAdd);
                    mFrameCountTime = timeFlag;
                    if (statisticTimes < STATIS_PERIOD) {
                        mFrameTotalCount += mFrameCount;
                        statisticTimes++;
                    } else {
                        processThrowFrame();
                        mFrameTotalCount = mFrameCount;
                        statisticTimes = 1;
                        mThrowFrameCount = 0;
                    }
                    if (mWaitDataTimeAdd < MIN_WAIT_TIME && mTargetFrameRate > 3) {
                        mTargetFrameRate--;
                        sendFrameRateMsg(mTargetFrameRate);
                    } else if (mFrameCount < MID_FRAME_RATE && mWaitDataTimeAdd > MAX_WAIT_TIME
                            && mTargetFrameRate < FRAME_RATE) {
                        mTargetFrameRate++;
                        sendFrameRateMsg(mTargetFrameRate);
                    }
                    mFrameCount = 1;
                    mWaitDataTimeAdd = 0;
                } else {
                    mFrameCount++;
                }
                flag = ConnectManager.getInstance().readVideoData(inputData, dataLength);

                if (dataLength > 0) {
                    // decipher
                    if (EncryptSetupManager.getInstance().isEncryptEnable() && dataLength > 0) {
                        inputData = mAESManager.decrypt(inputData, dataLength);
                        if (inputData == null) {
                            LogUtil.e(TAG, "decrypt failed!");
                            return;
                        }
                        dataLength = inputData.length;
                    }
                }

                if (flag < 0) {
                    mThread = null;
                    break;
                }
                synchronized(mLock) {
                    if (isPausing && !isFirstFrame) {
                        // close current thread, when resume from pause start another thread
                        continue;
                    }
                    // if mSpsPps is null, means that this frame can be used as the first frame to parse spspps
                    if (hasReset && (mSpsPps != null)) {
                        LogUtil.i(TAG, "decoder reset");

                        if (isIFrame(inputData, 0)) {
                            try {
                                if (inputData.length < dataLength + mSpsPps.length) {
                                    byte[] tmp = inputData;
                                    inputData = new byte[dataLength + mSpsPps.length];
                                    System.arraycopy(tmp, 0, inputData, mSpsPps.length, dataLength);
                                    System.arraycopy(mSpsPps, 0, inputData, 0, mSpsPps.length);
                                } else {
                                    System.arraycopy(inputData, 0, inputData, mSpsPps.length, dataLength);
                                    System.arraycopy(mSpsPps, 0, inputData, 0, mSpsPps.length);
                                }
                                dataLength = dataLength + mSpsPps.length;
                            } catch (NullPointerException e) {
                                // set spspps to null when disconnecting will cause the NPE
                                return;
                            }
                        } else {
                            continue;
                        }
                    }
                    try {
                        input2Decoder(inputData, 0, dataLength);
                    } catch (IllegalStateException e) {
                        // if surface is not available, an IllegalStateException will be caught
                        if (mSurface != null) {
                            resetDecoder();
                        }
                        e.printStackTrace();
                    }

                }

            }
            if (isRunning) {
                // a data read exception is caught, so unInitDecoder
                mThread = null;
                synchronized(mLock) {
                    unInitDecoder();
                }
            }
        }
    }

    /**
     * created when decoder is initializing, stopped when mDecoder is null or stopThread() is called
     *
     * @author songye
     */
    private class DisplayThread extends Thread {

        private boolean isRunning = true;

        public void stopThread() {
            isRunning = false;
        }

        @Override
        public void run() {
            try {
                while (isRunning) {
                    try {
                        int outputBufferIndex = mDecoder.dequeueOutputBuffer(outputBufferInfo, 50000);
                        outputFromDecoder(outputBufferIndex);
                    } catch (IllegalStateException e) {
                        Thread.sleep(250);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                // input thread is resetting the decoder, mDecoder is null,
                e.printStackTrace();
                this.stopThread();
            }
        }
    }

    private boolean isIFrame(byte[] frameData, int offset) {
        if (frameData == null || frameData.length < 5) {
            return false;
        }
        if ((frameData[4 + offset] & 0x1F) == 5) {
            return true;
        }
        return false;
    }

    private int findFlag2(byte[] frameDate, int offset) {
        int value = -1;
        int byteCount = 0;
        int index;
        int length = frameDate.length;
        for (int i = offset; i < length; ) {
            value = frameDate[i++];
            if (value == 0) {
                index = 1;
                while (value == 0 && i < length) {
                    value = frameDate[i++];
                    index++;
                }
                if (value == 1 && index >= 4) {
                    byteCount = i - 5 + 1;
                    break;
                }
            }
        }
        return byteCount;
    }

    private void input2Decoder(byte[] buf, int offset, int length) {
        if (isFirstFrame) {
            mInputCount = 0;
            mOutputCount = 0;
            mIsDecorderReady = false;
            // save the spspps of first frame
            int byteCount = 0;
            if ((buf[4] & 0x1F) == 7) {
                for (int i = 0; i < 2; i++) {
                    byteCount = findFlag2(buf, byteCount + 5);
                    LogUtil.i(TAG, "findFlag2 byteCount = " + byteCount);
                }
            }
            LogUtil.d(TAG, "byteCount = " + byteCount);
            if (byteCount == 0) {
                // the first frame is not consist of SPS+PPS+I
                return;
            }
            mSpsPps = new byte[byteCount];
            System.arraycopy(buf, 0, mSpsPps, 0, byteCount);
            LogUtil.d(TAG, "mSpsPps = " + Arrays.toString(mSpsPps));
            isFirstFrame = false;
            if (!isIFrame(buf, byteCount) || (mThread != null && mThread.isPausing)) {
                // if not I frame, save the spspps, find I frame in the later frames
                hasReset = true;
                return;
            }
        }
        if (mDecoder == null) {
            // only reach this when reconnect or head unit exit carlife
            LogUtil.e(TAG, "decoder null");
            if (mThread != null) {
                // an exception occur
                resetDecoder();
                if (mDecoder == null) {
                    // if carlife is in the background, decoder will not be initialized
                    return;
                }
            } else {
                return;
            }
        }
        ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
        int inputBufferIndex = mDecoder.dequeueInputBuffer(mWaitTime);
        // the first I frame after reset must be decoded
        if (hasReset) {
            while (inputBufferIndex < 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int outputBufferIndex = mDecoder.dequeueOutputBuffer(inputBufferInfo, 0);
                while (outputBufferIndex >= 0) {
                    LogUtil.w(TAG,
                            "hasReset && inputBufferIndex < 0 ,outputBufferIndex >= 0， mInputCount - mOutputCount = "
                                    + (mInputCount - mOutputCount));
                    try {
                        mDecoder.releaseOutputBuffer(outputBufferIndex, true);
                        mOutputCount++;
                        if (isFirstDecode) {
                            mFirstDecodeTime = SystemClock.elapsedRealtime();
                            LogUtil.d(
                                    TAG,
                                    "-QA_Test- FirstDecodeTime = "
                                            + (mFirstDecodeTime - CarlifeActivity.mTimeConnectFinish));
                            isFirstDecode = false;
                        }
                        if (mOutputCount >= 16 && !mIsDecorderReady) {
                            LogUtil.d(TAG, "-QA_Test- SendDisplayMessage = "
                                    + (SystemClock.elapsedRealtime() - mFirstDecodeTime));
                            MsgHandlerCenter.dispatchMessage(CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT);
                            mIsDecorderReady = true;
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        break;
                    }
                    outputBufferIndex = mDecoder.dequeueOutputBuffer(inputBufferInfo, 0);
                }
                LogUtil.e(TAG, "没取到input缓冲");
                inputBufferIndex = mDecoder.dequeueInputBuffer(mWaitTime);
            }
            hasReset = false;
        }
        if (inputBufferIndex >= 0) {
            mInputMissCount = 0;
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            if (length > inputBuffer.remaining()) {
                inputBuffer.put(buf, offset, inputBuffer.remaining());
            } else {
                inputBuffer.put(buf, offset, length);
            }
            try {
                mDecoder.queueInputBuffer(inputBufferIndex, 0, length, 1, 0);
                mInputCount++;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        } else {
            mThrowFrameCount++;
            LogUtil.d(TAG,
                    "throwCount " + mThrowFrameCount + " totalFrame " + (mFrameTotalCount + mFrameCount) + " times "
                            + statisticTimes);
            LogUtil.e(TAG, "inputBufferIndex < 0");
            mInputMissCount++;
            if (mInputMissCount > 10) {
                resetDecoder();
            }
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void outputFromDecoder(int outputBufferIndex) throws NullPointerException {
        while (outputBufferIndex >= 0) {
            try {
                mDecoder.releaseOutputBuffer(outputBufferIndex, true);
                mOutputCount++;
                if (isFirstDecode) {
                    mFirstDecodeTime = SystemClock.elapsedRealtime();
                    LogUtil.d(TAG, "-QA_Test- FirstDecodeTime = "
                            + (mFirstDecodeTime - CarlifeActivity.mTimeConnectFinish));
                    isFirstDecode = false;
                }
                if (mOutputCount >= 16 && !mIsDecorderReady) {
                    LogUtil.d(TAG, "-QA_Test- SendDisplayMessage = "
                            + (SystemClock.elapsedRealtime() - mFirstDecodeTime));
                    MsgHandlerCenter.dispatchMessage(CommonParams.MSG_MAIN_DISPLAY_TOUCH_FRAGMENT);
                    mIsDecorderReady = true;
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                break;
            }
            outputBufferIndex = mDecoder.dequeueOutputBuffer(outputBufferInfo, 5000);
        }
    }

    private void resetDecoder() {
        unInitDecoder();
        initDecoder(mSurface);
        hasReset = true;
    }

    private void processThrowFrame() {
        if (mThrowFrameCount == 0 || mFrameTotalCount <= 0) {
            return;
        }
        int averageRate = (int) (mFrameTotalCount * 1.0 / statisticTimes);
        float throwRate = (mThrowFrameCount * 1.0f / (mFrameTotalCount + mFrameCount));
        if (throwRate > 0.08) {
            if (averageRate > MID_FRAME_RATE) {
                mTargetFrameRate = mTargetFrameRate - (averageRate - MID_FRAME_RATE);
            } else {
                mTargetFrameRate = mTargetFrameRate - 4;
            }
            if (mTargetFrameRate < 0) {
                mTargetFrameRate = 3;
            }
            sendFrameRateMsg(mTargetFrameRate);
        }
    }

    public void sendFrameRateMsg(int frameRate) {
        LogUtil.d(TAG, "current set target " + frameRate);
        CarlifeCmdMessage command = new CarlifeCmdMessage(true);
        command.setServiceType(CommonParams.MSG_CMD_VIDEO_ENCODER_FRAME_RATE_CHANGE);
        CarlifeVideoFrameRate.Builder builder = CarlifeVideoFrameRate.newBuilder();
        builder.setFrameRate(frameRate);
        CarlifeVideoFrameRate videoInfo = builder.build();
        command.setData(videoInfo.toByteArray());
        command.setLength(videoInfo.getSerializedSize());
        Message msgTmp =
                Message.obtain(null, command.getServiceType(), CommonParams.MSG_CMD_PROTOCOL_VERSION, 0, command);
        ConnectClient.getInstance().sendMsgToService(msgTmp);
    }

    public boolean isDecoderReady() {
        return mIsDecorderReady;
    }
}
