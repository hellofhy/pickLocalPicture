package com.example.fanhy.myapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.fanhy.myapplication.adapter.AlbumPreviewImageAdapter;
import com.example.fanhy.myapplication.bean.AlbumImageBean;
import com.example.fanhy.myapplication.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private Activity mContext;
    private Button btnAlbum;
    private RecyclerView mRecyclerView;
    private ArrayList<AlbumImageBean> imageResultList;
    private AlbumPreviewImageAdapter mAlbumPreviewImageAdapter;
    public final static int INTENT_REQUEST_ALBUM_CODE = 1;//请求参数
    public final static int INTENT_FOR_RESULT_CODE = 1;//返回code 固定值（后续页面对应），请勿修改
    private final static String INTENT_RESULT_IMAGE_LIST = "INTENT_RESULT_IMAGE_LIST";//携带数据返回的参数，固定值（后续页面对应），请勿修改
    private final static String INTENT_RESULT_ORI_PICTURE = "INTENT_RESULT_ORI_PICTURE";//携带数据返回的参数，固定值（后续页面对应），请勿修改

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        mContext = this;
        btnAlbum = findViewById(R.id.btn_album);
        mRecyclerView = findViewById(R.id.rv_album_result);
    }

    private void initData() {
        imageResultList = new ArrayList<>();
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mAlbumPreviewImageAdapter = new AlbumPreviewImageAdapter(this, imageResultList, null, 9, 4);
        mRecyclerView.setAdapter(mAlbumPreviewImageAdapter);
        btnAlbum.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_album:
                AlbumPreviewActivity.startIntentForResult(this, INTENT_REQUEST_ALBUM_CODE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENT_REQUEST_ALBUM_CODE:
                if (resultCode == INTENT_FOR_RESULT_CODE) {
                    ArrayList<AlbumImageBean> albumImageList = (ArrayList<AlbumImageBean>) data.getSerializableExtra(INTENT_RESULT_IMAGE_LIST);
                    boolean isOriPicture = data.getBooleanExtra(INTENT_RESULT_ORI_PICTURE, false);
                    imageResultList.clear();
                    imageResultList.addAll(albumImageList);
                    mAlbumPreviewImageAdapter.notifyDataSetChanged();
                    Toast.makeText(mContext, isOriPicture ? "上传原图" : "不上传原图", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
