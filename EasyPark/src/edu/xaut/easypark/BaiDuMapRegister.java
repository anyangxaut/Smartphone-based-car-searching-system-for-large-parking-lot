package edu.xaut.easypark;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;

public class BaiDuMapRegister extends Application{

	  private static BaiDuMapRegister mInstance = null;
	  public boolean m_bKeyRight = true; 
	  public BMapManager mBMapManager = null;
	  public static final String strKey = "a2RoMhfQ66jGbubPeiSfmSUy";

		@Override
	    public void onCreate() {	
		    super.onCreate();

			mInstance = this;
			initEngineManager(this);
		}
			
		public void initEngineManager(Context context) {
			
	        if (mBMapManager == null) {	
	            mBMapManager = new BMapManager(context);
	        }
	        if (!mBMapManager.init(strKey,new MyGeneralListener())) {	        	
	            Toast.makeText(BaiDuMapRegister.getInstance().getApplicationContext(),             		
	                    "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
	        }
		}
		
		public static BaiDuMapRegister getInstance() {
			return mInstance;
		}
		
		// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	    public static class MyGeneralListener implements MKGeneralListener {
	        @Override
	        public void onGetNetworkState(int iError) {
	            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
	                Toast.makeText(BaiDuMapRegister.getInstance().getApplicationContext(), "您的网络出错啦！",
	                    Toast.LENGTH_LONG).show();
	            }
	            else if (iError == MKEvent.ERROR_NETWORK_DATA) {
	                Toast.makeText(BaiDuMapRegister.getInstance().getApplicationContext(), "输入正确的检索条件！",
	                        Toast.LENGTH_LONG).show();
	            } 
	        }
	        
	        @Override
	        public void onGetPermissionState(int iError) {
	            if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
	                //授权Key错误：
	                Toast.makeText(BaiDuMapRegister.getInstance().getApplicationContext(), 
	                        "请输入正确的授权Key！", Toast.LENGTH_LONG).show();
	                BaiDuMapRegister.getInstance().m_bKeyRight = false;
	            }
	        }
	    }
}
