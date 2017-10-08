package me.leefeng.money;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import me.leefeng.imageselector.ImageLoaderActivity;
import me.leefeng.imageselector.ImgSelConfig;
import me.leefeng.library.utils.FileUtils;
import me.leefeng.library.utils.SharedPreferencesUtil;
import me.leefeng.library.utils.ToastUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private String currentKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImgSelConfig.maxNum = 0;

        MainActivityPermissionsDispatcher.showWithCheck(this);
    }

    public void weixin(View view){
        String weixinSrc = SharedPreferencesUtil.getStringData(this,"weixin",null);
        if (weixinSrc == null){
//            FileUtils.copyFile();
            ToastUtils.showLong(this,"选择一张图片作为微信收钱码");
            currentKey = "weixin";
            ImageLoaderActivity.startActivityForResult(this, null);
            return;
        }
        Intent intent = new Intent(this,ShowActivity.class);
        intent.putExtra("imageurl",weixinSrc);
        startActivity(intent);

    }
    public void alipay(View view){

        String src = SharedPreferencesUtil.getStringData(this,"alipay",null);
        if (src == null) {
            currentKey = "alipay";
            ImageLoaderActivity.startActivityForResult(this, null);
            return;
        }
        Intent intent = new Intent(this,ShowActivity.class);
        intent.putExtra("imageurl",src);
        startActivity(intent);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void show() {
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void goout() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == me.leefeng.imageselector.ImgSelConfig.REQUEST_CODE && data != null) {
            ArrayList<String> list = data.getStringArrayListExtra("array");
            String path = data.getStringExtra("path");
            String copyPath = FileUtils.getCacheDir() + "/"+currentKey;
            if(FileUtils.copyFile(path,copyPath,false)) {
                SharedPreferencesUtil.saveStringData(this, currentKey, copyPath);
                ToastUtils.showShort(this, "设置成功");
            }else{
                ToastUtils.showShort(this, "设置失败");
            }
//                Glide.with(this).load(list.get(0)).into(image);

        }
    }
}
