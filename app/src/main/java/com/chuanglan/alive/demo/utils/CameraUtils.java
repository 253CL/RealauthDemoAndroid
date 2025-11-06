package com.chuanglan.alive.demo.utils;

import android.hardware.Camera;

import java.util.List;

public class CameraUtils {

    public static Camera.Size getOptimalSize(List<Camera.Size> previewSizes, int minWidth, int minHeight) {
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        double optimalRatio = 2; //max ratio
        boolean hasOptimalRatio = false;

        for (Camera.Size size : previewSizes) {
            double ratio = (double) size.width / size.height;
            if (ratio > optimalRatio || (hasOptimalRatio && ratio > 1)) {
                continue;
            }
            if (Math.abs(size.height - minHeight) < minDiff && previewSizes.contains(size)) {
                optimalSize = size;
                minDiff = Math.abs(size.height - minHeight);
                optimalRatio = ratio;
            }
            if (Math.abs(size.width - minWidth) < minDiff && previewSizes.contains(size)) {
                optimalSize = size;
                minDiff = Math.abs(size.width - minWidth);
                optimalRatio = ratio;
            }
            if (ratio == 1 && !hasOptimalRatio) {
                hasOptimalRatio = true;
                optimalSize = size;
                minDiff = Math.abs(size.height - minHeight);
                optimalRatio = ratio;
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : previewSizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(size.height - minHeight) < minDiff && previewSizes.contains(size)) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - minHeight);
                }
                if (Math.abs(size.width - minWidth) < minDiff && previewSizes.contains(size)) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - minWidth);
                }
            }
        }

        return optimalSize;
    }
}
