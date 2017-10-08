package me.leefeng.money;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.ArrayList;
import java.util.Hashtable;

import me.leefeng.imageselector.ImageLoaderActivity;
import me.leefeng.imageselector.ImgSelConfig;
import me.leefeng.library.utils.FileUtils;
import me.leefeng.library.utils.LogUtils;
import me.leefeng.library.utils.SharedPreferencesUtil;
import me.leefeng.library.utils.ToastUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static int WEIXIN = 8001;
    private static int ALIPAY = 8002;

    private int currentKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImgSelConfig.maxNum = 0;

        MainActivityPermissionsDispatcher.showWithCheck(this);
    }

    public void weixin(View view) {
        String weixinSrc = SharedPreferencesUtil.getStringData(this, ""+WEIXIN, null);
        if (weixinSrc == null) {
//            FileUtils.copyFile();
            ToastUtils.showLong(this, "选择一张图片作为微信收钱码");
            currentKey = WEIXIN;
            ImageLoaderActivity.startActivityForResult(this, null);
            return;
        }
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("imageurl", weixinSrc);
        startActivity(intent);

    }

    public void alipay(View view) {

        String src = SharedPreferencesUtil.getStringData(this, ""+ALIPAY, null);
        if (src == null) {
            currentKey = ALIPAY;
            ToastUtils.showLong(this, "选择一张图片作为支付宝收钱码");
            ImageLoaderActivity.startActivityForResult(this, null);
            return;
        }
        Intent intent = new Intent(this, ShowActivity.class);
        intent.putExtra("imageurl", src);
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
            Result result = scanningImage(path);
            if (result==null){
                ToastUtils.showLong(this,"没有识别到二维码");
                return;
            }
            String text = result.getText();
            switch (currentKey){
                case 8002:
                    if (!text.startsWith("HTTPS://QR.ALIPAY.COM")){
                        ToastUtils.showLong(this,"当前不是支付宝收款码");
                        return;
                    }
                    break;
                case 8001:
                    if (!text.startsWith("wxp://")){
                        ToastUtils.showLong(this,"当前不是微信收款码");
                        return;
                    }
                    break;
                default:
                    break;
            }

            String copyPath = FileUtils.getCacheDir() + "/" + currentKey;
            if (FileUtils.copyFile(path, copyPath, false)) {
                SharedPreferencesUtil.saveStringData(this, ""+currentKey, copyPath);
                ToastUtils.showShort(this, "设置成功");
            } else {
                ToastUtils.showShort(this, "设置失败");
            }
        }
    }


    /**
     * 扫描图片返回结果
     * @param path
     * @return
     */
    protected Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        // DecodeHintType 和EncodeHintType
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码

        Bitmap scanBitmap = BitmapFactory.decodeFile(path);
        int width = scanBitmap.getWidth();
        int height = scanBitmap.getHeight();
        int[] data = new int[width * height];
        scanBitmap.getPixels(data, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {

            e.printStackTrace();

        } catch (FormatException e) {

            e.printStackTrace();

        }

        return null;

    }
}
