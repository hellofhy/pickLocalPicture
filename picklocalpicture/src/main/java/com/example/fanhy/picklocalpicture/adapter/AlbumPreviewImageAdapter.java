package com.example.fanhy.picklocalpicture.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fanhy.picklocalpicture.R;
import com.example.fanhy.picklocalpicture.bean.AlbumImageBean;

import java.io.File;
import java.util.ArrayList;

/**
 * @anthor FanHY
 * @time 2018/8/27
 * @describe AlbumPreviewActivity的image的adapter
 */
public class AlbumPreviewImageAdapter extends RecyclerView.Adapter<AlbumPreviewImageAdapter.ViewHolder> {
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private ArrayList<AlbumImageBean> mImageList;//list的数据源,全部图片
    private ArrayList<AlbumImageBean> curChooseImageList;//当前选中的图片列表
    private OnImageItemClickListener mOnImageItemClickListener;//图片的自身点击事件
    private int maxChooseCount;//当前能选中的最大个数
    private int columnCount;//gridview的column数量,做适配用

    public AlbumPreviewImageAdapter(Context context, ArrayList<AlbumImageBean> imageList, ArrayList<AlbumImageBean> curChooseImageList, int maxChooseCount, int columnCount) {
        mContext = context;
        mImageList = imageList;
        this.maxChooseCount = maxChooseCount;
        this.columnCount = columnCount;
        //储存选中image的list, 不可为空
        this.curChooseImageList = curChooseImageList == null ? new ArrayList<AlbumImageBean>() : curChooseImageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_choose, parent, false);
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int space = (int) mContext.getResources().getDimension(R.dimen.album_space);
        return new ViewHolder(view, width, space, columnCount);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AlbumImageBean albumImage = mImageList.get(position);
        holder.ivMask.setVisibility(curChooseImageList.contains(albumImage) ? View.VISIBLE : View.INVISIBLE);
        Glide.with(mContext).load(new File(albumImage.path)).into(holder.ivBody);
    }

    @Override
    public int getItemCount() {
        return mImageList == null ? 0 : mImageList.size();
    }

    public void setImageItemClickListener(OnImageItemClickListener onImageItemClickListener) {
        mOnImageItemClickListener = onImageItemClickListener;
    }

    /**
     * 获取当前选中图片个数，当传入curImageList为空时可用
     *
     * @return
     */
    public int getCurChooseCount() {
        return curChooseImageList == null ? 0 : curChooseImageList.size();
    }

    /**
     * 获取当前选中的list数据，当传入curImageList为空时可用
     *
     * @return
     */
    public ArrayList<AlbumImageBean> getCurChooseImageList() {
        return curChooseImageList;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView ivBody, ivChoose, ivMask;
        private FrameLayout flContainer;

        /**
         * @param itemView
         * @param screenWidth 屏幕宽度
         * @param space       gridview的条目边距
         * @param columnCount gridview的column数量,做适配用
         */
        public ViewHolder(View itemView, int screenWidth, int space, int columnCount) {
            super(itemView);
            flContainer = itemView.findViewById(R.id.fl_container);
            ivBody = itemView.findViewById(R.id.iv_body);
            ivChoose = itemView.findViewById(R.id.iv_choose);
            ivMask = itemView.findViewById(R.id.iv_mask);

            ViewGroup.LayoutParams layoutParams = flContainer.getLayoutParams();
            layoutParams.width = screenWidth / columnCount - space;
            layoutParams.height = layoutParams.width;
            flContainer.setLayoutParams(layoutParams);
            ivBody.setOnClickListener(this);
            ivMask.setOnClickListener(this);
            ivChoose.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == null) {
                return;
            }
            int viewId = v.getId();
            if (viewId == R.id.iv_choose) {
                int curPosition = getLayoutPosition();
                AlbumImageBean curImage = mImageList.get(curPosition);//获取当前点击的图片Model
                if (curChooseImageList.contains(curImage)) {//直接删除
                    curChooseImageList.remove(curImage);
                    ivMask.setVisibility(View.INVISIBLE);
                } else {
                    if (curChooseImageList.size() < maxChooseCount) {//当前已选中的未达到指定值可继续添加
                        curChooseImageList.add(curImage);
                        ivMask.setVisibility(View.VISIBLE);
                    } else {
                        String limitStr = String.format(mContext.getString(R.string.choose_pic_limit), maxChooseCount + "");
                        Toast.makeText(mContext, limitStr, Toast.LENGTH_SHORT).show();
                    }
                }
                if (mOnImageItemClickListener != null) {
                    mOnImageItemClickListener.onImageItemChooseClick(curPosition, v);
                }
            } else {
                if (mOnImageItemClickListener != null) {
                    mOnImageItemClickListener.onImageItemBodyClick(getLayoutPosition(), v);
                }
            }
//            switch (viewId) {
//                case R.id.iv_body:
//                case R.id.iv_mask:
//                    if (mOnImageItemClickListener != null) {
//                        mOnImageItemClickListener.onImageItemBodyClick(getLayoutPosition(), v);
//                    }
//                    break;
//            }
        }
    }

    public interface OnImageItemClickListener {
        //图片自身点击事件 --> 放大操作
        void onImageItemBodyClick(int position, View view);

        //图片选中的点击事件
        void onImageItemChooseClick(int position, View view);
    }
}
