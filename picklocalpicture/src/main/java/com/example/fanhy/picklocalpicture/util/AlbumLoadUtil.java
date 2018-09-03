package com.example.fanhy.picklocalpicture.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.example.fanhy.picklocalpicture.bean.AlbumFolderBean;
import com.example.fanhy.picklocalpicture.bean.AlbumImageBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @anthor FanHY
 * @time 2018/8/27
 * @describe
 */
public class AlbumLoadUtil {
    private final static String FIRST_FOLD_NAME = "全部图片";

    /**
     * 加载本地图片
     *
     * @param context
     * @param dataCallback 读取到的图片直接以ArrayList<AlbumFolderBean>回调 ,该list首项为全部图片且按时间从近到远排序
     */
    public static void loadImageFromSDCard(final Context context, final LoadLocalImageCallback dataCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                //todo
                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = resolver.query(imageUri, new String[]{
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media._ID
                }, null, null, MediaStore.Images.Media.DATE_ADDED);//照片的add时间递增排序，从远到近
                ArrayList<AlbumImageBean> images = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    images.add(new AlbumImageBean(path, name, time, id));
                }
                cursor.close();
                Collections.reverse(images);//照片的add时间从近到远 倒序
                dataCallback.onSuccess(splitFolder(images));
            }
        }).start();
    }

    /**
     * 把照片按folder组合成新集合 新集合第一个为全部图片数据
     *
     * @param images
     * @return
     */
    private static ArrayList<AlbumFolderBean> splitFolder(ArrayList<AlbumImageBean> images) {
        ArrayList<AlbumFolderBean> folders = new ArrayList<>();
        folders.add(new AlbumFolderBean(FIRST_FOLD_NAME, images));
        if (images != null && !images.isEmpty()) {
            int size = images.size();
            for (int i = 0; i < size; i++) {
                String path = images.get(i).path;
                String name = getFolderName(path);
                if (!TextUtils.isEmpty(name)) {
                    AlbumFolderBean albumFolder = getFolder(name, folders);
                    albumFolder.addImage(images.get(i));
                }
            }
        }
        return folders;
    }

    /**
     * 通过文件夹名字找到folder
     *
     * @param name
     * @param folders
     * @return
     */
    private static AlbumFolderBean getFolder(String name, ArrayList<AlbumFolderBean> folders) {
        if (folders != null && !folders.isEmpty()) {
            int size = folders.size();
            for (int i = 0; i < size; i++) {
                AlbumFolderBean folder = folders.get(i);
                if (name.equals(folder.name)) {
                    return folder;
                }
            }
        }
        AlbumFolderBean folder = new AlbumFolderBean(name);
        folders.add(folder);
        return folder;
    }

    /**
     * 通过path返回folder的name
     *
     * @param path
     * @return
     */
    private static String getFolderName(String path) {
        if (!TextUtils.isEmpty(path)) {
            String[] strings = path.split(File.separator);
            if (strings.length >= 2) {
                return strings[strings.length - 2];//返回文件夹名称
            }
        }
        return "";
    }


    public interface LoadLocalImageCallback {
        void onSuccess(ArrayList<AlbumFolderBean> list);
    }
}
