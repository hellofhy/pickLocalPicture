package com.example.fanhy.picklocalpicture.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.fanhy.picklocalpicture.R;
import com.example.fanhy.picklocalpicture.bean.AlbumImageBean;

import java.util.ArrayList;

/**
 * @anthor FanHY
 * @time 2018/8/29
 * @describe image预览 选中列表的adapter
 */
public class ImagePreviewRvAdapter extends RecyclerView.Adapter<ImagePreviewRvAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<AlbumImageBean> mImageList;
    private AlbumImageBean curChooseImage;//当前选中的Image
    private OnRvItemClickListener mOnRvItemClickListener;//图片的自身点击事件

    public ImagePreviewRvAdapter(Context context, ArrayList<AlbumImageBean> imageList, AlbumImageBean curChooseImage) {
        mContext = context;
        mImageList = imageList;
        this.curChooseImage = curChooseImage;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_image_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(mContext).load(mImageList.get(position).path).into(holder.ivBody);
        holder.ivBorder.setVisibility(curChooseImage != null && mImageList.contains(curChooseImage) &&
                position == mImageList.indexOf(curChooseImage) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mImageList == null ? 0 : mImageList.size();
    }

    /**
     * @param albumImage
     */
    public void notifyChooseChanged(AlbumImageBean albumImage) {
        curChooseImage = albumImage;
        notifyDataSetChanged();
    }

    public void setOnRvItemClickListener(OnRvItemClickListener onRvItemClickListener) {
        mOnRvItemClickListener = onRvItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView ivBody, ivBorder;

        public ViewHolder(View itemView) {
            super(itemView);
            ivBody = itemView.findViewById(R.id.iv_body);
            ivBorder = itemView.findViewById(R.id.iv_border);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == null) {
                return;
            }
            int position = getLayoutPosition();
            curChooseImage = mImageList.get(position);
            notifyDataSetChanged();
            if (mOnRvItemClickListener != null) {
                mOnRvItemClickListener.onRvItemClick(position, v);
            }
        }
    }

    public interface OnRvItemClickListener {
        void onRvItemClick(int position, View view);
    }
}
