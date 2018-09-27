package com.example.fanhy.picklocalpicture.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fanhy.picklocalpicture.R;
import com.example.fanhy.picklocalpicture.adapter.ImagePreviewRvAdapter;
import com.example.fanhy.picklocalpicture.adapter.ImagePreviewVpAdapter;
import com.example.fanhy.picklocalpicture.bean.AlbumImageBean;
import com.example.fanhy.picklocalpicture.fragment.ImagePreviewFragment;

import java.util.ArrayList;

/**
 * 选中图片预览页面
 */
public class ImagePreviewActivity extends AppCompatActivity implements View.OnClickListener, ImagePreviewRvAdapter
        .OnRvItemClickListener, ViewPager.OnPageChangeListener, ImagePreviewFragment.OnImageFragmentClickListener,
        CompoundButton.OnCheckedChangeListener {

    private final String TAG = getClass().getSimpleName();
    private ImageView ivBack;
    private TextView tvTitle, tvSubmit;
    private ViewPager mViewPager;
    private RecyclerView mRecyclerView;
    private LinearLayout llFoot, llHead;//底部栏（含mRecyclerView） 做整体动画用
    private CheckBox cbxOriPicture, cbxChoose;

    private final static String INTENT_PARAM_MAX_COUNT = "INTENT_PARAM_MAX_COUNT";//传入该页面所需参数
    private final static String INTENT_PARAM_CUR_POSITION = "INTENT_PARAM_CUR_POSITION";//传入该页面所需参数
    private final static String INTENT_PARAM_ORI_PIC = "INTENT_PARAM_ORI_PIC";//传入该页面所需参数
    private final static String INTENT_PARAM_IMAGE_LIST = "INTENT_IMAGE_LIST";//对应vp 传入该页面所需参数
    private final static String INTENT_PARAM_CHOOSE_LIST = "INTENT_CHOOSE_LIST";//对应rv 传入该页面所需参数
    private final static String INTENT_RESULT_IMAGE_LIST = "INTENT_RESULT_IMAGE_LIST";//携带数据返回的参数，固定值（后续页面对应），请勿修改
    private final static String INTENT_RESULT_ORI_PICTURE = "INTENT_RESULT_ORI_PICTURE";//携带数据返回的参数，固定值（后续页面对应），请勿修改
    public final static int INTENT_AS_RESULT_CODE = 1;//返回code 固定值（前序页面对应），请勿修改
    public final static int INTENT_AS_RESULT_SEND_CODE = 2;//返回code 固定值（前序页面对应），请勿修改

    private final static int ANIM_FOOT_DURATION = 800 * 1;//单位:ms
    private final static int ANIM_HEAD_DURATION = 800 * 1;//单位:ms

    private Context mContext;
    private ImagePreviewVpAdapter vpAdapter;
    private ImagePreviewRvAdapter rvAdapter;
    private ArrayList<AlbumImageBean> mImageList;//当前浏览的total图片列表 对应vp的数据源
    private ArrayList<AlbumImageBean> curChooseImageList;//当前选中的图片列表 对应rv
    private ArrayList<Fragment> mFragmentList;//对应vp
    private AlphaAnimation footShowAnimation, footHideAnimation;
    private TranslateAnimation headShowAnimation, headHideAnimation;

    private boolean footAnimIsRunning, headAnimIsRunning;
    private int curVpPosition;//当前vp的位置
    private int maxChooseCount;//最大选中个数
    private boolean isOriPicture;

    public static void startIntentForResult (Activity activity, int requestCode, ArrayList<AlbumImageBean> imageList,
                                             ArrayList<AlbumImageBean> curChooseImageList, int maxChooseCount, int curPosition,
                                             boolean isOriPicture) {
        if (imageList == null || imageList.size() <= 0 || curChooseImageList == null || curPosition >= imageList.size()) {//参数不合法
            return;
        }
        if (curPosition < 0) {
            curPosition = 0;
        }
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        intent.putExtra(INTENT_PARAM_IMAGE_LIST, imageList);
        intent.putExtra(INTENT_PARAM_CHOOSE_LIST, curChooseImageList);
        intent.putExtra(INTENT_PARAM_MAX_COUNT, maxChooseCount);
        intent.putExtra(INTENT_PARAM_CUR_POSITION, curPosition);
        intent.putExtra(INTENT_PARAM_ORI_PIC, isOriPicture);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        initIntent();
        initView();
        Log.d(TAG, "onCreate: initView");
        initData();
        Log.d(TAG, "onCreate: " + rvAdapter == null ? "null" : "object");
    }

    private void initIntent () {
        mImageList = (ArrayList<AlbumImageBean>) getIntent().getSerializableExtra(INTENT_PARAM_IMAGE_LIST);
        curChooseImageList = (ArrayList<AlbumImageBean>) getIntent().getSerializableExtra(INTENT_PARAM_CHOOSE_LIST);
        maxChooseCount = getIntent().getIntExtra(INTENT_PARAM_MAX_COUNT, 0);
        curVpPosition = getIntent().getIntExtra(INTENT_PARAM_CUR_POSITION, 0);
        isOriPicture = getIntent().getBooleanExtra(INTENT_PARAM_ORI_PIC, false);
    }

    private void initView () {
        mContext = this;
        mViewPager = findViewById(R.id.vp_preview);
        llHead = findViewById(R.id.ll_head);
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvSubmit = findViewById(R.id.tv_submit);
        mRecyclerView = findViewById(R.id.rv_preview);
        llFoot = findViewById(R.id.ll_foot_preview);
        cbxOriPicture = findViewById(R.id.cbx_ori_picture);
        cbxChoose = findViewById(R.id.cbx_choose);
    }

    private void initData () {
        initHeadAndFootAnimation();

        updateTvSubmit();
        updateTvTitle();
        updateCbxOriPicture();
        updateCbxChoose();

        rvAdapter = new ImagePreviewRvAdapter(mContext, curChooseImageList, mImageList.get(curVpPosition));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(rvAdapter);
        rvAdapter.setOnRvItemClickListener(this);
        checkRvIsEmpty();

        mFragmentList = new ArrayList<>();
        for (int i = 0; i < mImageList.size(); i++) {
            ImagePreviewFragment fragment = ImagePreviewFragment.newInstance(mImageList.get(i));
            fragment.setOnImageFragmentClickListener(this);
            mFragmentList.add(fragment);
        }
        vpAdapter = new ImagePreviewVpAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(vpAdapter);
        mViewPager.addOnPageChangeListener(this);
        if (curVpPosition > 0) {
            mViewPager.setCurrentItem(curVpPosition, false);
        }

        ivBack.setOnClickListener(this);
        tvSubmit.setOnClickListener(this);
        cbxChoose.setOnClickListener(this);//onCheckListener只改变drawable，因为进入页面需要修改选中的state 所以只有点击的时候触发事件，
        cbxOriPicture.setOnCheckedChangeListener(this);
    }

    /**
     * 初始化head foot 动画
     */
    private void initHeadAndFootAnimation () {
        footShowAnimation = new AlphaAnimation(0f, 1f);
        footShowAnimation.setDuration(ANIM_FOOT_DURATION);
        footShowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart (Animation animation) {
                footAnimIsRunning = true;
            }

            @Override
            public void onAnimationEnd (Animation animation) {
                llFoot.setVisibility(View.VISIBLE);
                footAnimIsRunning = false;
            }

            @Override
            public void onAnimationRepeat (Animation animation) {

            }
        });

        footHideAnimation = new AlphaAnimation(1f, 0f);
        footHideAnimation.setDuration(ANIM_FOOT_DURATION);
        footHideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart (Animation animation) {
                footAnimIsRunning = true;
            }

            @Override
            public void onAnimationEnd (Animation animation) {
                llFoot.setVisibility(View.INVISIBLE);
                footAnimIsRunning = false;
            }

            @Override
            public void onAnimationRepeat (Animation animation) {

            }
        });

        headShowAnimation = new TranslateAnimation(
                //X轴初始位置
                Animation.RELATIVE_TO_SELF, 0f,
                //X轴移动的结束位置
                Animation.RELATIVE_TO_SELF, 0f,
                //y轴开始位置
                Animation.RELATIVE_TO_SELF, - 1f,
                //y轴移动后的结束位置
                Animation.RELATIVE_TO_SELF, 0f);
        headShowAnimation.setDuration(ANIM_HEAD_DURATION);
        headShowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart (Animation animation) {
                headAnimIsRunning = true;
            }

            @Override
            public void onAnimationEnd (Animation animation) {
                llHead.setVisibility(View.VISIBLE);
                headAnimIsRunning = false;
            }

            @Override
            public void onAnimationRepeat (Animation animation) {

            }
        });

        headHideAnimation = new TranslateAnimation(
                //X轴初始位置
                Animation.RELATIVE_TO_SELF, 0f,
                //X轴移动的结束位置
                Animation.RELATIVE_TO_SELF, 0f,
                //y轴开始位置
                Animation.RELATIVE_TO_SELF, 0f,
                //y轴移动后的结束位置
                Animation.RELATIVE_TO_SELF, - 1f);
        headHideAnimation.setDuration(ANIM_HEAD_DURATION);
        headHideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart (Animation animation) {
                headAnimIsRunning = true;
            }

            @Override
            public void onAnimationEnd (Animation animation) {
                llHead.setVisibility(View.INVISIBLE);
                headAnimIsRunning = false;
            }

            @Override
            public void onAnimationRepeat (Animation animation) {

            }
        });
    }

    /**
     * 选中或取消选中图片个数时调用
     */
    private void updateTvSubmit () {
        int curChooseCount = curChooseImageList == null ? 0 : curChooseImageList.size();
        tvSubmit.setText(mContext.getString(R.string.send) + "(" + curChooseCount + "/" + maxChooseCount + ")");
        tvSubmit.setTextColor(curChooseCount <= 0 ? mContext.getResources().getColor(R.color.red) : mContext.getResources()
                .getColor(R.color.green));
    }

    /**
     * vp切换位置时，更新标题 eg: 12/208
     */
    private void updateTvTitle () {
        tvTitle.setText((curVpPosition + 1) + "/" + mImageList.size());
    }

    /**
     * 传入时设置原图按钮的 选择与否
     */
    private void updateCbxOriPicture () {
        cbxOriPicture.setChecked(isOriPicture);
    }

    /**
     * vp滑动和点击选择按钮时调用
     */
    private void updateCbxChoose () {
        cbxChoose.setChecked(curChooseImageList.contains(mImageList.get(curVpPosition)));
    }

    @Override
    public void onClick (View v) {
        if (v == null) {
            return;
        }

        int viewId = v.getId();
        if (viewId == R.id.iv_back) {//返回
            returnActivityResult();
        } else if (viewId == R.id.tv_submit) {//发送
            if (curChooseImageList.size() == 0) {
                cbxChoose.performClick();
            }
            returnActivitySendResult();
        } else if (viewId == R.id.cbx_choose) {//选择
            cbxChooseClickEvent();
        }
    }

    /**
     * 返回数据有修改
     */
    private void returnActivityResult () {
        Intent intent = new Intent();
        intent.putExtra(INTENT_RESULT_IMAGE_LIST, curChooseImageList);
        intent.putExtra(INTENT_RESULT_ORI_PICTURE, isOriPicture);
        setResult(INTENT_AS_RESULT_CODE, intent);
        finish();
    }

    /**
     * 点击send返回数据
     */
    private void returnActivitySendResult () {
        Intent intent = new Intent();
        intent.putExtra(INTENT_RESULT_IMAGE_LIST, curChooseImageList);
        intent.putExtra(INTENT_RESULT_ORI_PICTURE, isOriPicture);
        setResult(INTENT_AS_RESULT_SEND_CODE, intent);
        finish();
    }

    /**
     * 有选中/取消操作时调用， 用于显示和隐藏rv布局
     */
    private void checkRvIsEmpty () {
        if (curChooseImageList.size() > 0) {
            if (mRecyclerView.getVisibility() != View.VISIBLE) {
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mRecyclerView.getVisibility() == View.VISIBLE) {
                mRecyclerView.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * cbxChoose的点击事件
     */
    private void cbxChooseClickEvent () {
        AlbumImageBean curImage = mImageList.get(curVpPosition);
        if (curChooseImageList.contains(curImage)) {//直接删除
            curChooseImageList.remove(curImage);
            rvAdapter.notifyChooseChanged(null);
            updateTvSubmit();
            checkRvIsEmpty();
        } else {
            if (curChooseImageList.size() < maxChooseCount) {//当前已选中的未达到指定值可继续添加
                curChooseImageList.add(curImage);
                rvAdapter.notifyChooseChanged(curImage);
                mRecyclerView.smoothScrollToPosition(curChooseImageList.size() - 1);
                updateTvSubmit();
                checkRvIsEmpty();
            } else {
                String limitStr = String.format(mContext.getString(R.string.choose_pic_limit), maxChooseCount + "");
                Toast.makeText(mContext, limitStr, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * rv点击事件  将vp跳转至指定位置
     *
     * @param position
     * @param view
     */
    @Override
    public void onRvItemClick (int position, View view) {
        AlbumImageBean curAlbumImage = curChooseImageList.get(position);
        if (mImageList.contains(curAlbumImage)) {
            mViewPager.setCurrentItem(mImageList.indexOf(curAlbumImage), false);
        }
    }

    @Override
    public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * vp滑动事件  将rv转至指定位置
     *
     * @param position
     */
    @Override
    public void onPageSelected (int position) {
        curVpPosition = position;
        updateTvTitle();
        updateCbxChoose();
        AlbumImageBean curAlbumImage = mImageList.get(position);
        rvAdapter.notifyChooseChanged(curAlbumImage);//只要当前imageView改变，就需要修改rv的mask
        if (curChooseImageList.contains(curAlbumImage)) {
            mRecyclerView.smoothScrollToPosition(curChooseImageList.indexOf(curAlbumImage));
        }
    }

    @Override
    public void onPageScrollStateChanged (int state) {

    }

    /**
     * vp fragment中的iv点击事件
     *
     * @param view
     * @param albumImage 区分来自哪个fragment 可用ImageList.indexOf(albumImage) 来获取该fg的位置
     */
    @Override
    public void onImageFragmentClick (View view, AlbumImageBean albumImage) {
        Log.d(TAG, "onImageFragmentClick: " + mImageList.indexOf(albumImage));
        showAndHideHeadFootView();
    }

    /**
     * 展示或隐藏head/foot栏
     */
    private void showAndHideHeadFootView () {
        if (llFoot.getVisibility() == View.VISIBLE || llHead.getVisibility() == View.VISIBLE) {//llFoot llHead 任一可见时 隐藏
            if (! footAnimIsRunning && ! headAnimIsRunning) {
                llFoot.startAnimation(footHideAnimation);
                llHead.startAnimation(headHideAnimation);
            }
        } else {//llFoot llHead 都不可见时 显示
            if (! footAnimIsRunning && ! headAnimIsRunning) {
                llFoot.startAnimation(footShowAnimation);
                llHead.startAnimation(headShowAnimation);
            }
        }
    }

    /**
     * 原图的点击事件
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cbx_ori_picture) {

                isOriPicture = isChecked;
        }
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            returnActivityResult();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
