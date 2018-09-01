package com.example.fanhy.myapplication.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fanhy.myapplication.adapter.AlbumPreviewImageAdapter;
import com.example.fanhy.myapplication.bean.AlbumFolderBean;
import com.example.fanhy.myapplication.bean.AlbumImageBean;
import com.example.fanhy.myapplication.adapter.AlbumPreviewItemDecoration;
import com.example.fanhy.myapplication.util.AlbumLoadUtil;
import com.example.fanhy.myapplication.adapter.AlbumPreviewFolderAdapter;
import com.example.fanhy.myapplication.R;

import java.util.ArrayList;

/**
 * 相册选择页面
 */
public class AlbumPreviewActivity extends AppCompatActivity implements View.OnClickListener, AlbumPreviewImageAdapter.OnImageItemClickListener, AlbumPreviewFolderAdapter.OnFolderItemClickListener, CompoundButton.OnCheckedChangeListener {

    private final String TAG = getClass().getSimpleName();
    private ImageView ivBack, ivMask;
    private TextView tvTitle, tvSubmit;
    private Button btnSwitchFolder, btnPreview;
    private CheckBox cbxOriPicture;
    private RecyclerView rvImage, rvFolder;//所展示image的rv  切换图库folder的rv

    private final static String INTENT_PARAM_MAX_COUNT = "INTENT_PARAM_MAX_COUNT";//传入该页面所需参数
    private final static int INTENT_AS_RESULT_CODE = 1;//回传数据固定值（前续页面对应），请勿随意修改
    private final static int INTENT_REQUEST_IMAGE_CODE = 2;//跳转preview的requestCode
    private final static int INTENT_FOR_RESULT_CODE = 1;//返回code 固定值（后续页面对应），请勿随意修改
    public final static int INTENT_FOR_RESULT_SEND_CODE = 2;//返回code 固定值（后续页面对应），请勿随意修改
    private final static String INTENT_RESULT_IMAGE_LIST = "INTENT_RESULT_IMAGE_LIST";//携带数据返回的参数，固定参数，请勿随意修改
    private final static String INTENT_RESULT_ORI_PICTURE = "INTENT_RESULT_ORI_PICTURE";//携带数据返回的参数，固定参数，请勿随意修改

    private final static int REQUEST_READ_PERMISSION = 1;//READ_PERMISSION权限
    private final static int MAX_CHOOSE_COUNT = 9;//默认图片的最大限制数量，传入的值不能比该值大，否则设置为该值，暂定九张
    private final static int COLUMN_COUNT = 4;//图库的显示column count
    private final static int ANIM_FOLDER_DURATION = 300 * 1;//单位:ms
    private final static float HEIGHT_RATE = 7 / 8f;//folderList所能填充的最大高度（去除head 和foot剩余区域所占比例）

    private Context mContext;
    private AlbumPreviewImageAdapter mAlbumPreviewImageAdapter;
    private AlbumPreviewFolderAdapter mAlbumPreviewFolderAdapter;
    private ArrayList<AlbumImageBean> mImageList;//当前folder的图片list
    private ArrayList<AlbumFolderBean> mFolderList;//总folder的List
    private ArrayList<AlbumImageBean> curChooseImageList;//当前选中的图片list
    private TranslateAnimation folderShowAnimation, folderHideAnimation;

    private boolean folderRvIsShow;//切换图库的popup是否展示
    private boolean isOriPicture;//是否原图
    private int maxChooseCount;//设置的图片最大选择个数 实参
    private boolean folderAnimIsRunning;
//    private int curFolderPosition;//当前folder的位置

    public static void startIntentForResult(Activity activity, int requestCode) {
        startIntentForResult(activity, requestCode, MAX_CHOOSE_COUNT);
    }

    public static void startIntentForResult(Activity activity, int requestCode, int maxChooseCount) {
        Intent intent = new Intent(activity, AlbumPreviewActivity.class);
        intent.putExtra(INTENT_PARAM_MAX_COUNT, maxChooseCount);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_preview);
        initIntent();
        initView();
        initData();
    }

    private void initIntent() {
        maxChooseCount = getIntent().getIntExtra(INTENT_PARAM_MAX_COUNT, MAX_CHOOSE_COUNT);//图片的最大选择数量,默认为9
        if (maxChooseCount < 1 || maxChooseCount > MAX_CHOOSE_COUNT) {
            maxChooseCount = MAX_CHOOSE_COUNT;
        }
    }

    private void initView() {
        mContext = this;
        ivBack = findViewById(R.id.iv_back);
        ivMask = findViewById(R.id.iv_mask);
        tvTitle = findViewById(R.id.tv_title);
        tvSubmit = findViewById(R.id.tv_submit);
        btnSwitchFolder = findViewById(R.id.btn_switch_folder);
        cbxOriPicture = findViewById(R.id.cbx_ori_picture);
        btnPreview = findViewById(R.id.btn_preview);
        rvImage = findViewById(R.id.rv_choose);
        rvFolder = findViewById(R.id.rv_switch_folder);
    }

    private void initData() {
        initFolderRvAnimation();
        updateSubmitAndPreview();

        folderRvIsShow = false;
        tvSubmit.setClickable(false);
        tvTitle.setText(getString(R.string.choose_picture));

        mImageList = new ArrayList<>();
        mFolderList = new ArrayList<>();
        curChooseImageList = new ArrayList<>();

        rvImage.setLayoutManager(new GridLayoutManager(mContext, COLUMN_COUNT));
        rvImage.addItemDecoration(new AlbumPreviewItemDecoration((int) getResources().getDimension(R.dimen.album_space)));
        mAlbumPreviewImageAdapter = new AlbumPreviewImageAdapter(mContext, mImageList, curChooseImageList, maxChooseCount, COLUMN_COUNT);
        mAlbumPreviewImageAdapter.setImageItemClickListener(this);
        rvImage.setAdapter(mAlbumPreviewImageAdapter);

        rvFolder.setLayoutManager(new LinearLayoutManager(mContext));
        mAlbumPreviewFolderAdapter = new AlbumPreviewFolderAdapter(mContext, mFolderList, 0);
        mAlbumPreviewFolderAdapter.setOnFolderItemClickListener(this);
        rvFolder.setAdapter(mAlbumPreviewFolderAdapter);

        ivBack.setOnClickListener(this);
        ivMask.setOnClickListener(this);
        tvSubmit.setOnClickListener(this);
        btnSwitchFolder.setOnClickListener(this);
        btnPreview.setOnClickListener(this);
        cbxOriPicture.setOnCheckedChangeListener(this);

        requestPermission();
    }

    /**
     * 初始化folder的show hide动画
     */
    private void initFolderRvAnimation() {
        folderShowAnimation = new TranslateAnimation(
                //X轴初始位置
                Animation.RELATIVE_TO_SELF, 0f,
                //X轴移动的结束位置
                Animation.RELATIVE_TO_SELF, 0f,
                //y轴开始位置
                Animation.RELATIVE_TO_SELF, 1f,
                //y轴移动后的结束位置
                Animation.RELATIVE_TO_SELF, 0f);
        folderShowAnimation.setDuration(ANIM_FOLDER_DURATION);
        folderShowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                folderAnimIsRunning = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rvFolder.setVisibility(View.VISIBLE);
                folderRvIsShow = true;
                folderAnimIsRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        folderHideAnimation = new TranslateAnimation(
                //X轴初始位置
                Animation.RELATIVE_TO_SELF, 0f,
                //X轴移动的结束位置
                Animation.RELATIVE_TO_SELF, 0f,
                //y轴开始位置
                Animation.RELATIVE_TO_SELF, 0f,
                //y轴移动后的结束位置
                Animation.RELATIVE_TO_SELF, 1f);
        folderHideAnimation.setDuration(ANIM_FOLDER_DURATION);
        folderHideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                folderAnimIsRunning = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rvFolder.setVisibility(View.INVISIBLE);
                folderRvIsShow = false;
                folderAnimIsRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 请求READ_EXTERNAL_STORAGE 权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //没有权限去申请
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
            } else {
                loadLocalImage();
            }
        }
    }

    /**
     * 加载本地album图片
     */
    private void loadLocalImage() {
        AlbumLoadUtil.loadImageFromSDCard(mContext, new AlbumLoadUtil.LoadLocalImageCallback() {
            @Override
            public void onSuccess(ArrayList<AlbumFolderBean> list) {
                if (list == null || list.size() <= 0) {
                    Toast.makeText(mContext, "当前手机内暂无任何图片，请添加图片后重试", Toast.LENGTH_SHORT).show();
                    //todo 简单粗暴，后续再议
                    finish();
                }
//                curFolderPosition = 0;
                mFolderList.clear();
                mFolderList.addAll(list);
                mImageList.clear();
//                mImageList.addAll(list.get(curFolderPosition).imageList);
                mImageList.addAll(list.get(0).imageList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAlbumPreviewImageAdapter.notifyDataSetChanged();
                        updateBtnSwitchFolder();
                        adjustFolderRvHeight();
                    }
                });
            }
        });
    }

    /**
     * 根据albumFolder的size调整rv的高度
     */
    private void adjustFolderRvHeight() {
        float itemHeightPx = getResources().getDimension(R.dimen.album_switch_item_height);
        float rvNeedHeightPx = itemHeightPx * mFolderList.size();
        int rvFolderHeight;
        if (rvNeedHeightPx < rvImage.getHeight() * HEIGHT_RATE) {//未达到指定高度，需要多少给多少
            rvFolderHeight = (int) rvNeedHeightPx;
        } else {//否则设置为指定高度
            rvFolderHeight = (int) (rvImage.getHeight() * HEIGHT_RATE);
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) rvFolder.getLayoutParams();
        params.height = rvFolderHeight;
        rvFolder.setLayoutParams(params);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PERMISSION) {
            if (grantResults.length > 0) {
                loadLocalImage();
            } else {
                //todo 请求权限失败
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        if (mFolderList == null || mFolderList.size() == 0) {//还未初始化完毕/数据错误时不响应
            return;
        }
        switch (v.getId()) {
            case R.id.iv_back://返回
                if (folderRvIsShow) {
                    hideAlbumSwitchFolderPopup();
                } else {
                    returnActivityResult();
                }
                break;
            case R.id.iv_mask://rv的蒙板
                hideAlbumSwitchFolderPopup();
                break;
            case R.id.tv_submit://发送
                hideAlbumSwitchFolderPopup();
                returnActivityResult();
                break;
            case R.id.btn_switch_folder://选择图库
                if (folderRvIsShow) {
                    hideAlbumSwitchFolderPopup();
                } else {
                    showAlbumSwitchFolderPopup();
                }
                break;
            case R.id.btn_preview://预览
                hideAlbumSwitchFolderPopup();
                jumpToPreviewActivityForResult(curChooseImageList, 0);
                break;
            default:
                break;
        }
    }

    /**
     * 原图的点击事件
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cbx_ori_picture:
                isOriPicture = isChecked;
                break;
            default:
                break;
        }
    }

    /**
     * 展示左下角Popup
     */
    private void showAlbumSwitchFolderPopup() {
        if (!folderRvIsShow && !folderAnimIsRunning) {
            rvFolder.startAnimation(folderShowAnimation);
            ivMask.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏左下角Popup
     */
    private void hideAlbumSwitchFolderPopup() {
        if (folderRvIsShow && !folderAnimIsRunning) {
            rvFolder.startAnimation(folderHideAnimation);
            ivMask.setVisibility(View.GONE);
        }
    }

    /**
     * 图片item的body点击事件
     *
     * @param position
     * @param view
     */
    @Override
    public void onImageItemBodyClick(int position, View view) {
        jumpToPreviewActivityForResult(mImageList, position);
    }

    /**
     * 跳转到预览页面 需要重写onActivityResult
     *
     * @param imageList
     * @param curPosition 默认选中位置
     */
    private void jumpToPreviewActivityForResult(ArrayList<AlbumImageBean> imageList, int curPosition) {
        ImagePreviewActivity.startIntentForResult(this, INTENT_REQUEST_IMAGE_CODE, imageList, curChooseImageList,
                maxChooseCount, curPosition, isOriPicture);
    }


    /**
     * 图片item的choose点击事件
     *
     * @param position
     * @param view
     */
    @Override
    public void onImageItemChooseClick(int position, View view) {
        updateSubmitAndPreview();
    }

    /**
     * 选中图片list变化时调用 更新相关ui：tvSubmit,btnPreview
     */
    private void updateSubmitAndPreview() {
        int count = curChooseImageList == null ? 0 : curChooseImageList.size();
        tvSubmit.setText(getString(R.string.send) + "(" + count + "/" + maxChooseCount + ")");
        if (count == 0) {//对比条件运算符， 减少判断次数
            tvSubmit.setClickable(false);
            tvSubmit.setTextColor(getResources().getColor(R.color.red));
            btnPreview.setClickable(false);
        } else {
            tvSubmit.setClickable(true);
            tvSubmit.setTextColor(getResources().getColor(R.color.green));
            btnPreview.setClickable(true);
        }
    }

    /**
     * folder的popItem的点击事件
     *
     * @param position
     * @param view
     */
    @Override
    public void onFolderItemClick(int position, View view) {
//        curFolderPosition = position;
        updateBtnSwitchFolder();
        mImageList.clear();
//        mImageList.addAll(mFolderList.get(curFolderPosition).imageList);
        mImageList.addAll(mFolderList.get(position).imageList);
        mAlbumPreviewImageAdapter.notifyDataSetChanged();
        hideAlbumSwitchFolderPopup();
    }

    /**
     * 当前folder改变时刷新Ui
     */
    private void updateBtnSwitchFolder() {
//        btnSwitchFolder.setText(mFolderList.get(curFolderPosition).name);
        btnSwitchFolder.setText(mFolderList.get(mAlbumPreviewFolderAdapter.getCurChoosePosition()).name);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (folderRvIsShow) {
                hideAlbumSwitchFolderPopup();
                return true;
            } else {
                returnActivityResult();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENT_REQUEST_IMAGE_CODE:
                boolean isOriPicture = data.getBooleanExtra(INTENT_RESULT_ORI_PICTURE, false);
                ArrayList<AlbumImageBean> albumImageList = (ArrayList<AlbumImageBean>) data.getSerializableExtra(INTENT_RESULT_IMAGE_LIST);
                switch (resultCode) {
                    case INTENT_FOR_RESULT_CODE:
                        cbxOriPicture.setChecked(isOriPicture);
                        curChooseImageList.clear();
                        curChooseImageList.addAll(albumImageList);
                        mAlbumPreviewImageAdapter.notifyDataSetChanged();
                        updateSubmitAndPreview();
                        break;
                    case INTENT_FOR_RESULT_SEND_CODE:
                        this.isOriPicture = isOriPicture;
                        curChooseImageList.clear();
                        curChooseImageList.addAll(albumImageList);
                        returnActivityResult();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    /**
     * 返回数据有修改
     */
    private void returnActivityResult() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_RESULT_IMAGE_LIST, curChooseImageList);
        intent.putExtra(INTENT_RESULT_ORI_PICTURE, isOriPicture);
        setResult(INTENT_AS_RESULT_CODE, intent);
        finish();
    }
}
