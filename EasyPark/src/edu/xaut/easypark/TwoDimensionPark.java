package edu.xaut.easypark;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ls.widgets.map.MapWidget;
import com.ls.widgets.map.config.OfflineMapConfig;
import com.ls.widgets.map.interfaces.Layer;
import com.ls.widgets.map.model.MapObject;
/*
 * mAppWidget是一个代码库，使用它可以很方便的为android手机开发自定义地图的应用。
 * 这个类库提供了很多服务，方便android开发者集成地图到自己的项目，同时支持放大，缩小，气泡，图层等功能。
 * http://blog.csdn.net/wangyuetingtao/article/details/10174455
 * 1.数据库对应关系
 * 2.路径导航
 * 3.计步器
 */
public class TwoDimensionPark extends Activity {
	// 加载的地图根路径，也就是assets文件夹下的首个文件夹名称
	private String rootmapfolder;
	// 调试TAG标识
	private static final String TAG = "TwoDimensionPark";
	// 图层id标识
	private static final int PARKING_LAYER = 1;
	// 地图对象实例
	private MapWidget map = null;
	// 创建或获取布局实例
	private LinearLayout layout = null;
	// 图层实例
	private Layer layer = null;
	// 接收上一个activity传递的数据
	private Bundle bundle = null;
	// 标识楼层信息
	private String floor = ""; 
	// 创建定时器
	private Timer timer = null;
	// Handler消息标识
	private static final int TIMERTASK = 1;
	// 计步器坐标点
	private List<Integer> listPix = new ArrayList<Integer>();
	private List<Integer> listPiy = new ArrayList<Integer>();
	private int id = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去掉标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_twodimensionpark);
		/**
		 * 获取上一个Activity传来的数据
		 */
		Intent intent = getIntent();
		bundle = intent.getBundleExtra("bundle");
		rootmapfolder = bundle.getString("rootmapfolder");
		floor = bundle.getString("floor");
		/**
		 *  初始化地图实例
		 */
		initMap();
		/**
		 *  创建地图图层
		 */
		layer = map.createLayer(PARKING_LAYER);
//		/**
//		 * 显示地图对象
//		 */
//		addPOI(0, R.drawable.map_object_start, 94, 392);
//		addPOI(2, R.drawable.map_object_start, 221, 392);		
//		addPOI(1, R.drawable.map_object_start, 348, 392);			
//		addPOI(3, R.drawable.map_object_start, 475, 392);			
//		addPOI(4, R.drawable.map_object_start, 602, 392);			
//		addPOI(5, R.drawable.map_object_start, 772, 392);		
//		addPOI(6, R.drawable.map_object_start, 899, 392);		
//		addPOI(8, R.drawable.map_object_start, 1026, 392);				
//		addPOI(7, R.drawable.map_object_start, 1153, 392);			
//		addPOI(11, R.drawable.map_object_start, 1280, 392);				
//		addPOI(9, R.drawable.map_object_start, 1544, 392);			
//		addPOI(10, R.drawable.map_object_start, 1671, 392);
//		addPOI(12, R.drawable.map_object_start, 1751, 392);		
//		addPOI(13, R.drawable.map_object_start, 1878, 392);
//		addPOI(14, R.drawable.map_object_start, 2005, 392);
	}
	
	/**
	 * 使用反射机制去掉MapWidget水印logo
	 */
	private void initMap() {
		Class<?> c = null;
		try {
			// 反射找到Resources类
			c = Class.forName("com.ls.widgets.map.utils.Resources");
			Object obj = c.newInstance();
			// 找到Logo 属性，是一个数组
			Field field = c.getDeclaredField("LOGO");
			field.setAccessible(true);
			// 将LOGO字段设置为null
			field.set(obj, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 *  初始化地图类,参数1：上下文;参数2：rootmapfolder地图根路径，也就是assets文件夹下的首个文件夹名称;参数3：地图缩放级别(根据手机横竖屏状态进行设定)
		 */
		Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
		int ori = mConfiguration.orientation ; //获取屏幕方向
		if(ori == Configuration.ORIENTATION_LANDSCAPE){
			map = new MapWidget(TwoDimensionPark.this, rootmapfolder, 11);
		}else if(ori == Configuration.ORIENTATION_PORTRAIT){
			map = new MapWidget(TwoDimensionPark.this, rootmapfolder, 10);
		}		
		/**
		 *  设置map属性参数
		 */
		OfflineMapConfig config = map.getConfig();
		config.setZoomBtnsVisible(true); // 设置缩放按钮可见
		config.setPinchZoomEnabled(true); // 设置可以通过pinch方式缩放地图
		config.setFlingEnabled(true); // 设置可以滑动地图
		config.setMapCenteringEnabled(true); // 设置map居中显示，这样就可以不显示背景
		// 获取当前地图缩放级别，默认为10
		Log.d(TAG, "getZoomLevel：" + map.getZoomLevel());
		// 获取layout布局对象
		layout = (LinearLayout) findViewById(R.id.mainLayout);
		// 添加地图实例到layout布局中
		layout.addView(map);
	}
	
	
	/**
	 * 给地图添加poi点
	 * @param qrcode
	 * @param objectId
	 * @param image
	 */
	private void addPOI(int id, int image, int x, int y) {				
		/**
		 *  创建地图对象图标
		 */
		Drawable drawable = getResources().getDrawable(image);
		/**
		 *  创建地图对象(在本地图中一个方格的长和宽均为35像素)
		 */
		MapObject mapObject = new MapObject(id, // 设置对象id
				drawable, // 设置图标
				x, y, // 像素坐标
				11, 33, // 中心点坐标
				true, // 可触摸
				true); // 可扩展	
		/**
		 *  添加地图对象到图层中(任何位置)
		 */
		layer.addMapObject(mapObject);
	}	
	
		/**
		 *  二维码扫描
		 * @param i
		 */
		private void scanQrcode(int i) {
			// 二维码扫描CaptureActivity
			Intent openCameraIntent = new Intent(TwoDimensionPark.this,
					CaptureActivity.class);
			// 返回扫描结果
			startActivityForResult(openCameraIntent, i);
		}
		
		/**
		 *  获取二维码识别结果，并在地图上进行显示
		 */
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);

			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				String result = bundle.getString("result");
				Log.d(TAG, "" + "result：" + result);
				/**
				 * 对二维码中的json字符串进行解析
				 */
				try {
					JSONObject object = new JSONObject(result);
					String parkingLotName = object.getString("ParkingLotName");
					String floorJson = object.getString("Floor");
					String qrcode = object.getString("Qrcode");
					
					if(floorJson.equals(floor)){
						if (requestCode == 0) {
					    	/**
					    	 * 汽车所在地(0代表-2层，1代表-3层)
					    	 */
					    	addPOI(5, R.drawable.map_object_start, 817, 163);
						}else if(requestCode == 1){
					    	/**
					    	 * 用户所在地
					    	 */
							if(qrcode.equals("74")){
								addPOI(74, R.drawable.map_object_end, 967, 783);
						    	startNavigationUp1();
							}else{
								layer.clearAll();
								map.refreshDrawableState();
								addPOI(5, R.drawable.map_object_start, 817, 163);
								addPOI(40, R.drawable.map_object_end, 1586, 335);
						    	startNavigationUp2();
							}				    		    	
						}
					}else{
						Toast.makeText(getApplicationContext(), "您当前所在楼层为-3层，您的爱车停放在-2层，请您至-2层寻车！", 
								Toast.LENGTH_LONG).show();
						startVoice();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * 语音提醒
		 */
		public void startVoice(){
			 /* 建立MediaPlayer对象 */ 
			MediaPlayer mMediaPlayer = new MediaPlayer();  
		    /* 将音乐以Import的方式保存res/raw/floorerror.mp3 */ 
		    mMediaPlayer = MediaPlayer.create(TwoDimensionPark.this, R.raw.floorerror); 
		    try {
				mMediaPlayer.prepare();
			} catch (IllegalStateException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    mMediaPlayer.start();
		}
	
	
	public void startNavigationUp1(){
		/**
		 * 路径规划5-74
		 */
		addPOI(100, R.drawable.poi, 1030, 800);
		addPOI(101, R.drawable.poi, 1030, 730);
		addPOI(102, R.drawable.poi, 1030, 660);
		addPOI(103, R.drawable.poi, 1093, 600);
		addPOI(104, R.drawable.poi, 1222, 600);
		addPOI(105, R.drawable.poi, 1351, 600);
		addPOI(106, R.drawable.poi, 1479, 600);
		addPOI(107, R.drawable.poi, 1609, 600);
		addPOI(108, R.drawable.poi, 1757, 600);
		addPOI(109, R.drawable.poi, 1890, 600);
		addPOI(110, R.drawable.poi, 2026, 600);
		addPOI(111, R.drawable.poi, 2026, 520);
		addPOI(112, R.drawable.poi, 2026, 450);
		addPOI(113, R.drawable.poi, 1920, 410);
		addPOI(114, R.drawable.poi, 1790, 410);
		addPOI(115, R.drawable.poi, 1760, 326);
		addPOI(116, R.drawable.poi, 1760, 252);
		addPOI(117, R.drawable.poi, 1634, 240);
		addPOI(118, R.drawable.poi, 1504, 240);
		addPOI(119, R.drawable.poi, 1376, 240);
		addPOI(120, R.drawable.poi, 1247, 240);
		addPOI(121, R.drawable.poi, 1119, 240);
		addPOI(122, R.drawable.poi, 990, 240);
		addPOI(123, R.drawable.poi, 855, 240);

		/**
		 * 计步器
		 */
		listPix.clear();
		listPiy.clear();
		id = 0;
		listPix.add(1030);	listPiy.add(800);
		listPix.add(1030);	listPiy.add(730);
		listPix.add(1030);	listPiy.add(660);
		listPix.add(1093);	listPiy.add(620);		
		listPix.add(1222);	listPiy.add(620);		
		listPix.add(1351);	listPiy.add(620);
		listPix.add(1479);	listPiy.add(620);
		listPix.add(1609);	listPiy.add(620);
		listPix.add(1757);	listPiy.add(620);
		listPix.add(1890);	listPiy.add(620);
		listPix.add(2026);	listPiy.add(620);
		listPix.add(2026);	listPiy.add(520);
		listPix.add(2026);	listPiy.add(440);
		listPix.add(1920);	listPiy.add(430);
		listPix.add(1820);	listPiy.add(430);
		listPix.add(1730);	listPiy.add(430);
		listPix.add(1650);	listPiy.add(430);
		Log.d("----------->", listPix.toString());
		timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = myHandler.obtainMessage();
				msg.what = TwoDimensionPark.TIMERTASK;
				myHandler.sendMessage(msg);
			}
			
		}, 3000, 1000);  
//		Toast.makeText(getApplicationContext(), "当前行走路线已偏离系统规划路径，您可以重新扫描二维码进行路径矫正！", 
//				Toast.LENGTH_LONG).show();
	}
	
	
	public void startNavigationUp2(){
		/**
		 * 路径规划5-74
		 */
		addPOI(98, R.drawable.poi, 1670, 410);
		addPOI(99, R.drawable.poi, 1760, 410);
		addPOI(103, R.drawable.poi, 1760, 326);
		addPOI(104, R.drawable.poi, 1760, 252);
		addPOI(105, R.drawable.poi, 1634, 240);
		addPOI(106, R.drawable.poi, 1504, 240);
		addPOI(107, R.drawable.poi, 1376, 240);
		addPOI(108, R.drawable.poi, 1247, 240);
		addPOI(109, R.drawable.poi, 1119, 240);
		addPOI(110, R.drawable.poi, 990, 240);
		addPOI(111, R.drawable.poi, 855, 240);

		/**
		 * 计步器
		 */
		listPix.clear();
		listPiy.clear();
		id = 0;
		listPix.add(1670);	listPiy.add(430);
		listPix.add(1760);	listPiy.add(410);
		listPix.add(1760);	listPiy.add(326);
		listPix.add(1760);	listPiy.add(265);		
		listPix.add(1634);	listPiy.add(265);		
		listPix.add(1504);	listPiy.add(265);
		listPix.add(1376);	listPiy.add(265);
		listPix.add(1247);	listPiy.add(265);
		listPix.add(1119);	listPiy.add(265);
		listPix.add(990);	listPiy.add(265);
		listPix.add(855);	listPiy.add(265);		
		timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = myHandler.obtainMessage();
				msg.what = TwoDimensionPark.TIMERTASK;
				myHandler.sendMessage(msg);
			}
			
		}, 3000, 1000);   
		
	}
	
	private Handler myHandler = new Handler() {  
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
                  case TwoDimensionPark.TIMERTASK:   
                  	 timerTask();
                       break;   
             }   
             super.handleMessage(msg);   
        }   
   };  
	
	/**
	 * 定时器任务类---模拟计步器
	 * @author anyang
	 *
	 */
   public void timerTask(){
	   Log.d("----------->", "id:" + id);
	    addPOI(id, R.drawable.pedometerpoi, listPix.get(id), listPiy.get(id));
	    id++;
		if(id == listPix.size()){
			timer.cancel();// 停止定时器
		}
   }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.two_dimension_park, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
	    case R.id.scanQrcodeParkingLot:
	    	scanQrcode(0);   	
	    	break;
	    case R.id.scanQrcodeUser:
	    	scanQrcode(1);
	    	break;	
	    }
	    return true;
	}
}
