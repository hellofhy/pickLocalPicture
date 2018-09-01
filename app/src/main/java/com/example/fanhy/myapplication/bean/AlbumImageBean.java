package com.example.fanhy.myapplication.bean;

import java.io.Serializable;

/**
 * @anthor FanHY
 * @time 2018/8/27
 * @describe
 */
public class AlbumImageBean implements Serializable {
    public String path;
    public String name;
    public long time;
    public int id;

    public AlbumImageBean(String path, String name, long time, int id) {
        this.path = path;
        this.name = name;
        this.time = time;
        this.id = id;
    }

    //list.contains()调用的是list.indexOf()方法，仅使用equals做比较 set.contains()方法则还需要重写hashcode方法
    //相同属性的对象即视为equals
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AlbumImageBean)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        AlbumImageBean albumImage = (AlbumImageBean) obj;
        if (this.id == albumImage.id && this.time == albumImage.time
                && this.path.equals(albumImage.path) && this.name.equals(albumImage.name)) {
            return true;
        }
        return false;
    }
}
