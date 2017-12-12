package com.example.mylibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;


/**
 * Created by Administrator on 2017/12/11 0011.
 * Date 2017/12/11 0011
 */

public class CameraUtils {
    private static final int PHOTO_REQUEST_GALLERY = 100;
    private boolean flag=false;
    private static final int PHOTO_REQUEST_CAMERA = 101;
    private static final int PHOTO_REQUEST_CUT = 102;
    private File tempFile;
    private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
    private Context mContext;
    private static CameraUtils mCameraUtils;
    public static CameraUtils getInstence(Context ctx){
        if(mCameraUtils==null) {
            mCameraUtils = new CameraUtils(ctx);
        }
            return mCameraUtils;
    }
    private CameraUtils(Context ctx){
        mContext=ctx;
    }
    //检测权限
    private void checkpermissiom(){
        List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();
        permissionItems.add(new PermissionItem(Manifest.permission.CAMERA, "手机状态", R.drawable.permission_ic_camera));
        HiPermission.create(mContext)
                .title("亲爱的上帝")
                .permissions(permissionItems)
                .msg("为了保护世界的和平,开启这些权限吧！\n你我一起拯救世界！")
                .animStyle(R.style.PermissionAnimScale)
                .style(R.style.PermissionDefaultBlueStyle)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        Toast.makeText(mContext, "用户关闭权限申请", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFinish() {
                        flag=true;
                    }
                    @Override
                    public void onDeny(String permission, int position) {
                    }
                    @Override
                    public void onGuarantee(String permission, int position) {
                    }
                });
    }
    // 打开相册
    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        ((Activity)mContext).startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }
    /* 判断sdcard是否被挂载
*/
    private boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
    //将图片加载到ImageView
    public void initImage(int requestCode, Intent data, ImageView img){
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            if (data != null) {
                Uri uri = data.getData();
                crop(uri);
            }
        } else if (requestCode == PHOTO_REQUEST_CAMERA) {
            // 从相机返回的数据
            // 裁剪
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //  大于等于24即为7.0及以上执行内容
                crop(FileProvider.getUriForFile(mContext, "com.example.mylibrary", tempFile));
            } else {
                //  低于24即为7.0以下执行内容
                crop(Uri.fromFile(tempFile));
            }
        } else if (requestCode == PHOTO_REQUEST_CUT) {// 来自裁剪图片
            if (data != null) {
                Bitmap bitmap = data.getParcelableExtra("data");
                img.setImageBitmap(bitmap);
            }
        }
    }
    /*
* 剪切图片
*/
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //  大于等于24即为7.0及以上执行内容
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", false);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        ((Activity)mContext).startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }
    // 打开相机
    public void openCamera() {
        checkpermissiom();
        if(flag){
            Uri uri=null;
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (hasSdcard()) {
                tempFile = new File(Environment.getExternalStorageDirectory(),
                        PHOTO_FILE_NAME);
                Toast.makeText(mContext, "存储卡", Toast.LENGTH_SHORT).show();
            }else {
                tempFile = new File(mContext.getCacheDir(),
                        PHOTO_FILE_NAME);
                Toast.makeText(mContext, "内存", Toast.LENGTH_SHORT).show();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //  大于等于24即为7.0及以上执行内容
                uri = FileProvider.getUriForFile(mContext, "com.example.mylibrary", tempFile);//通过FileProvider创建一个content类型的Uri
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri
            } else {
                //  低于24即为7.0以下执行内容
                uri = Uri.fromFile(tempFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CAREMA
            ((Activity)mContext).startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
        }else {
            Toast.makeText(mContext, "请打开权限", Toast.LENGTH_SHORT).show();
        }
    }
}
