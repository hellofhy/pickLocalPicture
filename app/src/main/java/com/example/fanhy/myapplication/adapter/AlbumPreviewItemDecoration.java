package com.example.fanhy.myapplication.adapter;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @anthor FanHY
 * @time 2018/8/27
 * @describe
 */
public class AlbumPreviewItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public AlbumPreviewItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = space;
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;
    }
}
