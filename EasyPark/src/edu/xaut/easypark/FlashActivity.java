package edu.xaut.easypark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
/**
 * 默认启动的第一个页面：显示App的Logo和Version信息
 * @author anyang
 *
 */
public class FlashActivity extends Activity {

    // 动画时间间隔设定1.5秒
    private final int SPLASH_DISPLAY_LENGHT = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 加载布局文件
        setContentView(R.layout.activity_flash);
        // 显示动画效果，这里可以更新UI是因为该Runnable运行在UI线程中（主线程），如果此处需要执行比较耗时的操作，最好重新new一个线程
        // 否则会出现ANR错误(Application Not Responding)
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent();
                intent.setClass(FlashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGHT);
    }
}
