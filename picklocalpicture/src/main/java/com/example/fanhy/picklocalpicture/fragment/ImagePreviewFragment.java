package com.example.fanhy.picklocalpicture.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.fanhy.picklocalpicture.R;
import com.example.fanhy.picklocalpicture.bean.AlbumImageBean;

/**
 * @anthor FanHY
 * @time 2018/8/29
 * @describe
 */
public class ImagePreviewFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private static final String INTENT_PARAM_IMAGE_BEAN = "INTENT_PARAM_IMAGE_BEAN";
    private AlbumImageBean mAlbumImage;
    private ImageView mImageView;
    private OnImageFragmentClickListener mOnImageFragmentClickListener;

    public static ImagePreviewFragment newInstance(AlbumImageBean mAlbumImage) {
        ImagePreviewFragment fragment = new ImagePreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_PARAM_IMAGE_BEAN, mAlbumImage);
        fragment.setArguments(bundle); //设置参数
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mAlbumImage = (AlbumImageBean) getArguments().getSerializable(INTENT_PARAM_IMAGE_BEAN);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_image_preview, container, false);
        mImageView = view.findViewById(R.id.iv);
        Glide.with(mContext).load(mAlbumImage.path).into(mImageView);
        view.setOnClickListener(this);
        return view;
    }

    public void setOnImageFragmentClickListener(OnImageFragmentClickListener onImageFragmentClickListener) {
        mOnImageFragmentClickListener = onImageFragmentClickListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnImageFragmentClickListener != null) {
            mOnImageFragmentClickListener.onImageFragmentClick(v, mAlbumImage);
        }
    }

    public interface OnImageFragmentClickListener {
        void onImageFragmentClick(View view, AlbumImageBean albumImage);
    }
}
