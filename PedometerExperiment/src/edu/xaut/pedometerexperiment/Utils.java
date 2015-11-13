package edu.xaut.pedometerexperiment;

import android.text.format.Time;
/**
 * 工具类
 * 获取系统当前时间
 * @author anyang
 *
 */
public class Utils {
	// 测试标签
//	private static final String TAG = "Utils";
    private static Utils instance = null;
	
	public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

	// 返回当前系统时间
    public static long currentTimeInMillis() {
        Time time = new Time();
        // 设置到当前时间
        time.setToNow();
        // 返回毫秒数
        return time.toMillis(false);
    }
}
