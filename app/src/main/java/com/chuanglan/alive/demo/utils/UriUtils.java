package com.chuanglan.alive.demo.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.File;

import androidx.core.content.FileProvider;


public class UriUtils {

    public static Uri getUriByPath(Context context, String path) {
        Uri picUri = Uri.parse(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String subPath = picUri.getPath().substring(1);
            picUri = getImageContentUri(context, subPath);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = context.getPackageName() + ".FileProvider";
            picUri = FileProvider.getUriForFile(context, authority, new File(picUri.getPath()));
        }

        return picUri;
    }

    /**
     * 图片路径转uri
     *
     * @param path
     * @return
     */
    public static Uri getImageContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
