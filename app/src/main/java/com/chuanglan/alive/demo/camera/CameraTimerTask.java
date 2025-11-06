package com.chuanglan.alive.demo.camera;

import java.util.Timer;
import java.util.TimerTask;

public class CameraTimerTask {

    private static Timer timerFocus = null;
    private static final long CAMERA_SCAN_INTERVAL = 2000;

    /**
     * 创建一个定时对焦的timer任务
     *
     * @param runnable 对焦代码
     * @return Timer Timer对象，用来终止自动对焦
     */
    public static Timer createAutoFocusTimerTask(final Runnable runnable) {
        if (timerFocus != null) {
            return timerFocus;
        }
        timerFocus = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
        timerFocus.scheduleAtFixedRate(task, 0, CAMERA_SCAN_INTERVAL);
        return timerFocus;
    }

    /**
     * 终止自动对焦任务，实际调用了cancel方法并且清空对象
     * 但是无法终止执行中的任务，需额外处理
     */
    public static void cancelAutoFocusTimer() {
        if (timerFocus != null) {
            timerFocus.cancel();
            timerFocus = null;
        }
    }
}
