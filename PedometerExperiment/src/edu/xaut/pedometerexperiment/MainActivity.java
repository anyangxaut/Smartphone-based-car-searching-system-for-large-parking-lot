package edu.xaut.pedometerexperiment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {
	// 调试标签
	private static final String TAG = "MainActivity";
	// 步数显示控件
	private TextView stepsValueText = null;
	// 开始、结束、重置按钮控件
	private ImageButton startImage = null;
	private ImageButton endImage = null;
	private ImageButton resetImage = null;
	// 初始化数值变量
	private int stepValue;
	private SharedPreferences settings;
	private PedometerSettings pedometerSettings;
	private boolean isRunning;
	private static final int STEPS_MSG = 1;
	// 计步器服务
	private PedometerService pedometerService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//初始化步数
		stepValue = 0;
		
		// 获取TextView控件对象
		stepsValueText = (TextView)findViewById(R.id.steps_value_text);
		// 获取ImageButton控件对象
		startImage = (ImageButton)findViewById(R.id.start_image);
		endImage = (ImageButton)findViewById(R.id.end_image);
		resetImage = (ImageButton)findViewById(R.id.reset_image);
		
		// 开始计步按钮监听事件
		startImage.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// 启动service
				startStepService();	
		        bindStepService();		       	            
				// 修改服务运行信息
		        pedometerSettings.clearServiceRunning();
		        // 重置计步器
		        resetValues(true);
			}			
		});
		
		// 结束计步按钮监听事件
		endImage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
                unbindStepService();
                stopStepService();
			}
			
		});
		
		// 重置计步按钮监听事件
		resetImage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				resetValues(true);
			}
			
		});
				
	}
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		 Log.i(TAG, "[ACTIVITY] onDestroy");
	        if (isRunning) {
	            unbindStepService();
	            stopStepService();
	        }
	        pedometerSettings.saveServiceRunning(isRunning);
	}



	@Override
	protected void onPause() {
		Log.i(TAG, "[ACTIVITY] onPause");
        super.onPause();
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// 获取当前activity的SharedPreferences
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		// 创建PedometerSettings对象
		pedometerSettings = new PedometerSettings(settings);
		// 读取SharedPreferences中的信息，判断计步器服务是否正在运行
        isRunning = pedometerSettings.isServiceRunning();           
	}
	
	 // 重置步数变量
    private void resetValues(boolean updateDisplay) {
        if (pedometerService != null && isRunning) {
        	pedometerService.resetValues();                    
        }
        else {
        	stepsValueText.setText("0");
            SharedPreferences state = getSharedPreferences("state", 0);
            SharedPreferences.Editor stateEditor = state.edit();
            if (updateDisplay) {
                stateEditor.putInt("steps", 0);
                stateEditor.commit();
            }
        }
    }
    
    // 回调接口
    private PedometerService.ICallback mCallback = new PedometerService.ICallback() {
    	public void stepsChanged(int value) {
    		Log.d(TAG, "PedometerService.ICallback");
    		mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
    	}
    };
    
    
    // 非UI线程与UI线程通信处理方法
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                	// 获取步数
                    stepValue = (int)msg.arg1;
                    // 更新UI界面
                    stepsValueText.setText("" + stepValue);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
	    
	    /*
	     *  service与activity建立联系
	     *  intent是跳转到service的intent，如 Intent intent = new Intent(); intent.setClass(this,MyService.class);
        	conn则是一个代表与service连接状态的类，当我们连接service成功或失败时，
        	会主动触发其内部的onServiceConnected或onServiceDisconnected方法。
        	如果我们想要访问service中的数据，可以在onServiceConnected()方法中进行实现.
	     */
	    private ServiceConnection mConnection = new ServiceConnection() {
	        public void onServiceConnected(ComponentName className, IBinder service) {
	        	pedometerService = ((PedometerService.StepBinder)service).getService();
	        	pedometerService.registerCallback(mCallback);
	        	pedometerService.reloadSettings();	            
	        }

	        public void onServiceDisconnected(ComponentName className) {
	        	pedometerService = null;
	        }
	    };

	    
	// 启动服务
    private void startStepService() {
        if (!isRunning) {
            Log.i(TAG, "[SERVICE] Start");
            isRunning = true;
            startService(new Intent(MainActivity.this, PedometerService.class));
        }
    }
    
    // 停止服务运行
    private void stopStepService() {
        Log.i(TAG, "[SERVICE] Stop");
        stopService(new Intent(MainActivity.this, PedometerService.class));
        isRunning = false;
    }
    
    // 绑定服务到activity
    private void bindStepService() {
        Log.i(TAG, "[SERVICE] Bind");
        bindService(new Intent(MainActivity.this, 
        		PedometerService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }
    
    // 解除service与activity之间的绑定关系
    private void unbindStepService() {
        Log.i(TAG, "[SERVICE] Unbind");
        unbindService(mConnection);
    }    

}
