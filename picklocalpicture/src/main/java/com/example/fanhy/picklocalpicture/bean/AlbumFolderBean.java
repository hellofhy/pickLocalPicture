package com.example.fanhy.picklocalpicture.bean;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @anthor FanHY
 * @time 2018/8/31
 * @describe
 */
public class AlbumFolderBean implements Serializable {
    public String name;
    public ArrayList<AlbumImageBean> imageList;

    public AlbumFolderBean(String name) {
        this.name = name;
    }

    public AlbumFolderBean(String name, ArrayList<AlbumImageBean> imageList) {
        this.name = name;
        this.imageList = imageList;
    }

    public void addImage(AlbumImageBean albumImage) {
        if (albumImage != null && !TextUtils.isEmpty(albumImage.name)) {
            if (imageList == null) {
                imageList = new ArrayList<>();
            }
            imageList.add(albumImage);
        }
    }
}
