package com.chuanglan.alive.demo.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.chuanglan.sdk.tools.LogTool;

import java.io.IOException;

/**
 * 媒体播放工具类
 *
 * @author x-sir :)
 * @date 4/20/21
 */
public class MediaPlayerUtils {

    private static final String TAG = "MediaPlayerUtils<TAG->";
    private static volatile MediaPlayerUtils INSTANCE = null;
    private MediaPlayer mPlayer = new MediaPlayer();

    private MediaPlayerUtils() {
    }

    public static MediaPlayerUtils getInstance() {
        if (INSTANCE == null) {
            synchronized (MediaPlayerUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MediaPlayerUtils();
                }
            }
        }
        return INSTANCE;
    }

    public void play(Context context, String assetName) {
        AssetFileDescriptor fileDescriptor = getAssetFileDescriptor(context, assetName);
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }

        try {
            mPlayer.reset();
            mPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(), fileDescriptor.getLength()
            );
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            LogTool.e(TAG, "playSound error" + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            if (mPlayer != null) {
                //mPlayer.reset();
                mPlayer.release();
                mPlayer = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AssetFileDescriptor getAssetFileDescriptor(Context context, String assetName) {
        try {
            Context appContext = context.getApplicationContext();
            return appContext.getAssets().openFd(assetName);
        } catch (IOException e) {
            e.printStackTrace();
            LogTool.e(TAG, "IOException:" + e.toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
            LogTool.e(TAG, "NullPointerException:" + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            LogTool.e(TAG, "Exception:" + e.toString());
        }

        return null;
    }
}