package com.example.fanhy.myapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.fanhy.myapplication.R;
import com.example.fanhy.myapplication.bean.AlbumFolderBean;

import java.io.File;
import java.util.ArrayList;

/**
 * @anthor FanHY
 * @time 2018/8/27
 * @describe AlbumPreviewActivity的folderList的adapter
 */
public class AlbumPreviewFolderAdapter extends RecyclerView.Adapter<AlbumPreviewFolderAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<AlbumFolderBean> mFolderList;
    private int curChoosePosition;//当前选中的folder位置
    private OnFolderItemClickListener mOnItemClickListener;

    public AlbumPreviewFolderAdapter(Context context, ArrayList<AlbumFolderBean> folderList, int curChoosePosition) {
        mContext = context;
        mFolderList = folderList;
        this.curChoosePosition = curChoosePosition;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_folder_switch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AlbumFolderBean folder = mFolderList.get(position);
        holder.tvTitle.setText(folder.name);
        holder.tvNum.setText(folder.imageList.size() + mContext.getString(R.string.unit_pic_of_folder));
        holder.ivMark.setVisibility(curChoosePosition == position ? View.VISIBLE : View.INVISIBLE);
        Glide.with(mContext).load(new File(folder.imageList.get(0).path)).into(holder.ivBody);
    }

    @Override
    public int getItemCount() {
        return mFolderList == null ? 0 : mFolderList.size();
    }

    public void setOnFolderItemClickListener(OnFolderItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public int getCurChoosePosition() {
        return curChoosePosition;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private FrameLayout mFrameLayout;
        private ImageView ivBody, ivMark;
        private TextView tvTitle, tvNum;

        public ViewHolder(View itemView) {
            super(itemView);
            mFrameLayout = itemView.findViewById(R.id.fl_switch_folder);
            ivBody = itemView.findViewById(R.id.iv_body);
            ivMark = itemView.findViewById(R.id.iv_mark);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvNum = itemView.findViewById(R.id.tv_num);
            mFrameLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.fl_switch_folder:
                    curChoosePosition = getLayoutPosition();
                    notifyDataSetChanged();
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onFolderItemClick(curChoosePosition, v);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public interface OnFolderItemClickListener {
        //条目的点击事件
        void onFolderItemClick(int position, View view);
    }
}
