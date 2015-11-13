package edu.xaut.pedometerexperiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class PedometerService extends Service implements SensorEventListener{
	// 测试标签
	private static final String TAG = "PedometerService";
	// 窗口大小
	private static final int WINDOWSIZE = 24;
    // 步数统计结果显示
    private StepDisplayer stepDisplayer;
    // 状态栏通知管理器
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notifyBuilder;
    // 通知ID
    private static final int notifyId = 100;
    // 传感器管理器
    private SensorManager sensorManager;
    // 传感器类
    private Sensor accSensor;
    private Sensor oriSensor;
    // 工具类，返回系统时间
    private Utils utils;
    // 步数变量
    private int steps;
    // 使用sharedpreferences存储配置信息
    private SharedPreferences settings;
    private PedometerSettings pedometerSettings;
    private SharedPreferences state;
    private SharedPreferences.Editor stateEditor;
 	// 定时器
 	private Timer timer = null;
    // 存储加速度传感器数据数组
    private float[] acc50 = new float[3];
    private float[] acc20 = new float[3];
    private List<Double> accList = null;
    // 存储方向传感器数据数组
    private float[] ori50 = new float[3];
    private float[] ori20 = new float[3];
    // 存储陀螺仪传感器数据数组
    private float[] gro50 = new float[3];
    private float[] gro20 = new float[3];
    // 记录起始时间和结束时间
    private long start = 0, end = 0;
    // 采样数据列表
    private List<Double> gvSample = new ArrayList<Double>();
    
    @Override
    public void onCreate() {
        Log.i(TAG, "[SERVICE] onCreate");
        super.onCreate();
        // 获取系统通知service
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 设置通知参数
        showNotification();
        // 加载设置
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        pedometerSettings = new PedometerSettings(settings);
        state = getSharedPreferences("state", 0);

        utils = Utils.getInstance();
        
        // 获取系统传感器管理器对象
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //  注册检测器
        registerDetector();

        stepDisplayer = new StepDisplayer(pedometerSettings, utils);
        stepDisplayer.setSteps(steps = state.getInt("steps", 0));
        stepDisplayer.addListener(stepListener);

        // 显示service已启动
        Toast.makeText(this, getText(R.string.start_text), Toast.LENGTH_SHORT).show();
    }
   
	@Override
	public void onDestroy() {
		super.onDestroy();		
        Log.i(TAG, "[SERVICE] onDestroy");
        
        stateEditor = state.edit();
        stateEditor.putInt("steps", steps);
        stateEditor.commit();
        // 取消通知
        notificationManager.cancel(notifyId);                     
        // 停止检测步数
        unregisterDetector();
        // 显示service已停止信息
        Toast.makeText(this, getText(R.string.end_text), Toast.LENGTH_SHORT).show();
    }

	 @Override
	    public IBinder onBind(Intent intent) {
	        Log.i(TAG, "[SERVICE] onBind");
	        return mBinder;
	    }
	 
	// 从activity中接收消息
	 private final IBinder mBinder = new StepBinder();

	public class StepBinder extends Binder {
		PedometerService getService() {
            return PedometerService.this;
        }
    }
	
	// 声明一个回调接口
	public interface ICallback {
        public void stepsChanged(int value);
    }	
	// 初始化接口变量
    private ICallback mCallback;
    // 自定义事件
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }   
    // 从PaceNotifier传递步数到activity（回调方法）
    private StepDisplayer.Listener stepListener = new StepDisplayer.Listener() {
        public void stepsChanged(int value) {
            steps = value;
            if (mCallback != null) {
                mCallback.stepsChanged(steps);
            }
        }
    };
    
    // 重新加载设置信息
    public void reloadSettings() {
        settings = PreferenceManager.getDefaultSharedPreferences(this);     
        if (stepDisplayer != null) stepDisplayer.reloadSettings();
    }
    
    // 重置步数
    public void resetValues() {
        stepDisplayer.setSteps(0);
    }    
    
    // 当service运行时，在状态栏显示通知（实现前台service运行）
    private void showNotification() {   	
    	//点击通知跳转到MainActivity
        Intent pedometerIntent = new Intent();
        pedometerIntent.setComponent(new ComponentName(this, MainActivity.class));
        pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, pedometerIntent, 0);
        
        // 设置消息格式
    	notifyBuilder = new NotificationCompat.Builder(this)
        /*设置small icon*/
        .setSmallIcon(R.drawable.ic_notification)
        /*设置title*/
        .setContentTitle(getText(R.string.app_name))
    	  /*设置发出通知的时间为发出通知时的系统时间*/
        .setWhen(System.currentTimeMillis())
        /*设置详细文本*/
        .setContentText("计步器正在运行。。。")
    	 /*设置通知数量的显示类似于QQ那种，用于同志的合并*/
        .setNumber(2)
        /*点击跳转到MainActivity*/
        .setContentIntent(contentIntent);
    	
    	// 发送通知
    	notificationManager.notify(notifyId, notifyBuilder.build());    	                   
    }
    
    private void registerDetector() {
    	// 获取指定类型的传感器对象（加速度传感器）
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        oriSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION );
        // 将传感器对象和传感器操作类绑定
        // 将Sensor实例与SensorEventListener实例相互绑定，20000microseconds采样一次,即50HZ。
        sensorManager.registerListener(this, accSensor, 20000);
        sensorManager.registerListener(this, oriSensor, 20000);
        // 初始化list，存放加速度数据，之后用于求动态阈值
        accList = new ArrayList<Double>();
        
        // 重新启动一个新的线程，以20HZ的频率读取传感器数据，并对这些数据进行处理
        timer = new Timer();  
        timer.schedule(new TimerTask() {  
            public void run() {  
            	synchronized (this) {
            	for(int i = 0; i < acc20.length; i++){
            		acc20[i] = acc50[i];
            	}
            	}
            	// 利用方向传感器将加速度数据投影到重力方向
            	double ax = Math.pow(acc20[0], 2);
            	double ay = Math.pow(acc20[1], 2);
            	double az = Math.pow(acc20[2], 2);
            	// 给gv取绝对值
            	double gv = Math.sqrt(ax + ay + az);    
            	// 数据采样
//            	sample(gv);
            	/*
            	 *  计算动态阈值
            	 *  通过观察实验数据可知：有效步数波峰值和平均值加速度值之间的差异大于1，在采样频率为20HZ的情况下，且步行最慢时间为2秒，
            	 *  所以设定长度为24的存储队列来存储加速度值.
            	 */
                if(accList.size() < WINDOWSIZE){
                	accList.add(gv);
                }else{
                	double[] standardDeviation = new double[8];
                	double[] correlation = new double[8];
                	for(int i = 1; i < 8; i++){
                		double[] aArray = new double[i+5];
                		double[] bArray = new double[i+5];               		
                		for(int j = 0; j < aArray.length; j++){
                			aArray[j] = accList.get(j);
                			bArray[j] = accList.get(j+7);
                		}
                		// 计算bArray的标准差σb
                		standardDeviation[i] = standardDeviation(bArray);
                		// 计算aArray与bArray的相关系数ρab
                		correlation[i] = correlation(aArray, bArray);               		               		
                	}   
                	// 求 出7个ρab的最大值MAX及对应该次的σb和数组bArray的长度bLength
            		int tag = maxTag(correlation);
            		Log.d(TAG, "standardDeviation[tag]:" + standardDeviation[tag]);
            		Log.d(TAG, "correlation[tag]:" + correlation[tag]);
                    // 判断是否发生一步
                    if((standardDeviation[tag] > 0.5) /*&& (correlation[tag] > 0.7)*/){
                    	// 获取系统当前时间（毫秒）
                		end= System.currentTimeMillis(); 
                		// 因为人们的反应速度最快为0.2s，因此当发生动作的时间间隔小与0.2s时，则认为是外界干扰
                		if(end - start > 500) {
                			Log.d(TAG, "step");
                			// 发送通知，通过回调方法将步数显示在ui界面上
                			stepDisplayer.onStep();
                			start= end; 
                		}
                    }                    
                    // 删除accList的前bLength(tag+5)项，剩余项依次平移至数组前端
                    accList = removeElements(accList, (tag+5));
                    
                }                                
            }  
        }, 1000, 50);  
       
    }
    
    // 计算标准差
    private double standardDeviation(double[] array){    	
    	double result = 0;
    	double sumb = 0;
    	
    	for(int m = 0; m < array.length; m++){
			sumb += array[m];
		}
    
		double avgb = sumb / array.length;
		sumb = 0;
		for(int n = 0; n < array.length; n++){
			sumb += Math.pow((array[n] - avgb), 2);
		}
		result = Math.sqrt(sumb / array.length);
    	
		return result;   	
    }
    
    // 计算相关系数
    private double correlation(double[] aArray, double[] bArray){
    	double result = 0;
    	double suma = 0;
    	double sumb = 0;
    	double avga = 0;
    	double avgb = 0;
    	double sumUp =0;
    	double sumDown1 =0;
    	double sumDown2 =0;
    	
    	for(int m = 0; m < aArray.length; m++){
			suma += aArray[m];
			sumb += bArray[m];
		}
    	avga = suma / aArray.length;
    	avgb = sumb / bArray.length;
    	
    	for(int n = 0; n < aArray.length; n++){
    		sumUp += (aArray[n] - avga) * (bArray[n] - avgb);
    		sumDown1 += Math.pow((aArray[n] - avga), 2);
    		sumDown2 += Math.pow((bArray[n] - avgb), 2);		
		}
    	
    	result = sumUp / Math.sqrt(sumDown1 * sumDown2);
    	
    	return result; 
    }
    
    // 返回数组中最大值的下标
    private int maxTag(double[] correlation){
    	int tag = 0;
    	correlation[0] = 0;
    	for(int m = 1; m < correlation.length; m++){
    		if(correlation[m] > correlation[m-1]){
    			tag = m;
    		}
    	}
    	return tag;
    }
    
    // 移除前number项元素
    private List<Double> removeElements(List<Double> list, int number){
    	List<Double> result = new ArrayList<Double>();
    	
    	for(int m = number; m < list.size(); m++){
    		result.add(list.get(m));
    	}
    	list = null;
		return result;
    	
    }
    
    // 注销步数检测服务
    private void unregisterDetector() {
        sensorManager.unregisterListener(this);
        // 停止定时器
        if(timer != null){
        timer.cancel();
        }
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	// 获取传感器数据信息并进行处理，判断是否是有效步数
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

    	// 获取传感器对象
        Sensor sensor = event.sensor; 
//        Log.d(TAG, "sensor.getType():" + sensor.getType());
        
        synchronized (this) {
        	// 判断传感器类型
            if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
//            	Log.d(TAG, "TYPE_ORIENTATION");
            	// 方向传感器
            	ori50[0] = event.values[0];
            	ori50[1] = event.values[1];
            	ori50[2] = event.values[2];
            	
            }else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER){
//            	Log.d(TAG, "TYPE_ACCELEROMETER");
            	// 加速度传感器
            	acc50[0] = event.values[0];
            	acc50[1] = event.values[1];
            	acc50[2] = event.values[2];
             }else if (sensor.getType() == Sensor.TYPE_GYROSCOPE){
//            	 Log.d(TAG, "TYPE_GYROSCOPE");
            	 // 陀螺仪传感器
            	 gro50[0] = event.values[0];
             	 gro50[1] = event.values[1];
             	 gro50[2] = event.values[2];
             }               
         }
     }
	
	// 数据采样
	private void sample(double gv) {
		if(gvSample.size() >= 20){
			//当数据达到20条，进行传输
			trans();
			//清空缓冲区
			gvSample.clear();			
		}		
		// 添加新数据
		gvSample.add(gv);	
	}
	
	// 传输传感器数据
	private String trans() {
		// 添加数据实体
		List<NameValuePair> pairList = new ArrayList<NameValuePair>();
		NameValuePair pair = new BasicNameValuePair("gv", gvSample.toString());
		pairList.add(pair);
		// 以post方式发送http请求
		String url = httpUtil.BASE_URL + "/SampleServlet";
		String information = httpUtil.queryStringForPost(url, pairList);
		return information;
	}
}
