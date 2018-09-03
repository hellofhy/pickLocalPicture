# pickLocalPicture
选择本地图片
调用方法：

在acitivity中设置参数
private final static int INTENT_FOR_RESULT_CODE = 1;//返回code 固定值（后续页面对应），请勿修改
private final static String INTENT_RESULT_IMAGE_LIST = "INTENT_RESULT_IMAGE_LIST";//携带数据返回的参数，固定值（后续页面对应），请勿修改
private final static String INTENT_RESULT_ORI_PICTURE = "INTENT_RESULT_ORI_PICTURE";//携带数据返回的参数，固定值（后续页面对应），请勿修改
然后直接调用
    AlbumPreviewActivity.startIntentForResult(this, INTENT_REQUEST_ALBUM_CODE);
并重写onActivity方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENT_REQUEST_ALBUM_CODE:
                if (resultCode == INTENT_FOR_RESULT_CODE) {
                    //拿到选中的image列表
                    ArrayList<AlbumImageBean> albumImageList = (ArrayList<AlbumImageBean>) data.getSerializableExtra(INTENT_RESULT_IMAGE_LIST);
                    //拿到上传照片时的boolean是否上传原图
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
