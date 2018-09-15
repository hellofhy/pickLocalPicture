# pickLocalPicture
选择本地图片
调用方法：

在acitivity中直接调用



           AlbumPreviewActivity.startIntentForResult(this, INTENT_REQUEST_ALBUM_CODE);
      
    
    
     
并重写onActivityResult方法拿到数据，list包含图片信息，isOriPicture为是否上传原图



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENT_REQUEST_ALBUM_CODE:
                        ArrayList<AlbumImageEntity> albumImageList = (ArrayList<AlbumImageEntity>) data.getSerializableExtra        (PickAlbumConfig.INTENT_ALBUM_RESULT_IMAGE_LIST);
                        boolean isOriPicture = data.getBooleanExtra(PickAlbumConfig.INTENT_ALBUM_RESULT_ORI_PICTURE, false);
                        imageResultList.clear();
                        if (resultCode == PickAlbumConfig.INTENT_ALBUM_RESULT_SEND_CODE) {//发送刷新数据
                            imageResultList.addAll(albumImageList);
                            Toast.makeText(mContext, "直接返回，albumImageList.size=" + albumImageList.size() +
                                    (isOriPicture ? "上传原图" : "不上传原图"), Toast.LENGTH_SHORT).show();
                        } else if (resultCode == PickAlbumConfig.INTENT_ALBUM_RESULT_RETURN_CODE) {//直接返回 doNothing
                            Toast.makeText(mContext, "直接返回，albumImageList.size=" + albumImageList.size() +
                                    (isOriPicture ? "上传原图" : "不上传原图"), Toast.LENGTH_SHORT).show();
                        }
                break;
            default:
                break;
        }
    }
