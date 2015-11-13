package edu.xaut.easypark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class NavigationActivity extends Activity {

	 // 定位
 	public BDLocationListener myListener = new MyLocationListener();
 	private LocationClient mLocClient = null;
 	private boolean mIsStart = false;
 	/**
 	 * MapView 是地图主控件
 	 */
 	private MapView mMapView = null;
 	/**
 	 * 用MapController完成地图控制
 	 */
 	private MapController mMapController = null;
 	private MyOverlay mOverlay = null;
 	private BaiDuMapRegister app;
 	private BDLocation lastLocation = null;
 	private Map<String, Double> targetLocation = null;
 	private MKSearch mKSearch = null;
 	private MySearchListener mySearchListener = new MySearchListener();
 	private GeoPoint currentPt = null;
 	private GeoPoint targetPt = null; 	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去掉标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        /**
		 * 使用地图sdk前需先初始化BMapManager. BMapManager是全局的，可为多个MapView共用，它需要地图模块创建前创建，
		 * 并在地图地图模块销毁后销毁，只要还有地图模块在使用，BMapManager就不应该销毁
		 */
		app = (BaiDuMapRegister)this.getApplication();
		
		if (app.mBMapManager == null) {	
			app.mBMapManager = new BMapManager(this);
			/**
			 * 如果BMapManager没有初始化则初始化BMapManager
			 */
			app.mBMapManager.init(BaiDuMapRegister.strKey,		
					new BaiDuMapRegister.MyGeneralListener());
		}
		/**
		 * 由于MapView在setContentView()中初始化,所以它需要在BMapManager初始化之后
		 */
		setContentView(R.layout.activity_navigation);
		/**
		 * 获取目标停车场的地理坐标信息
		 */
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		targetLocation = new HashMap<String, Double>();
		targetLocation.put("lat", bundle.getDouble("lat"));
		targetLocation.put("lng", bundle.getDouble("lng"));
		
		mMapView = (MapView)findViewById(R.id.bmapView);
		/**
		 * 获取地图控制器
		 */
		mMapController = mMapView.getController();
		/**
		 * 设置地图是否响应点击事件 .
		 */
		mMapController.enableClick(true);
		/**
		 * 设置地图缩放级别
		 */
		mMapController.setZoom(16);
		/**
		 * 显示内置缩放控件
		 */
		mMapView.setBuiltInZoomControls(true);
		mLocClient = new LocationClient(this); // 声明LocationClient类
		mLocClient.setAK(BaiDuMapRegister.strKey);
		mLocClient.registerLocationListener(myListener); // 注册监听函数
		
		setLocationOption();

		if (!mIsStart) {
			mLocClient.start();
			if (mLocClient != null && mLocClient.isStarted()) {
				mLocClient.requestLocation(); // 发起定位请求。请求过程是异步的，定位结果在监听函数onReceiveLocation中获取。
				mIsStart = true;
			} else
				Log.d("LocSDK3", "locClient is null or not started");
		}	
		/**
		 * 通过gps进行定位，并在当前位置和目标位置之间进行路径导航
		 */
		//初始化搜索模块
		mKSearch = new MKSearch();
        //设置路线策略为最短距离
		mKSearch.setDrivingPolicy(MKSearch.ECAR_DIS_FIRST);
		mKSearch.init(app.mBMapManager, mySearchListener); 
	}
	
	public class MySearchListener implements MKSearchListener{

		@Override
		public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
			// TODO Auto-generated method stub
			/* 
             * 返回地址信息搜索结果。 参数： arg0 - 搜索结果 arg1 - 错误号，0表示结果正确，result中有相关结果信息；100表示结果正确，无相关地址信息 
             */  
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			/* 
             * 返回驾乘路线搜索结果。 参数： arg0 - 搜索结果 arg1 - 错误号，0表示正确返回 
             */  
			// 错误号可参考MKEvent中的定义
            if (arg1 != 0 || arg0 == null) {
                Toast.makeText(NavigationActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                return;
            }
            RouteOverlay routeOverlay = new RouteOverlay(NavigationActivity.this, mMapView);           
            // 此处仅展示一个方案作为示例
            MKRoute route = arg0.getPlan(0).getRoute(0);
//            int distanceM = route.getDistance();
//            String distanceKm = String.valueOf(distanceM / 1000) +"."+String.valueOf(distanceM % 1000);
//            System.out.println("距离:"+distanceKm+"公里---节点数量:"+route.getNumSteps());
//            for (int i = 0; i < route.getNumSteps(); i++) {
//                MKStep step = route.getStep(i);
//                System.out.println("节点信息："+step.getContent());
//            }
            routeOverlay.setData(route);
            mMapView.getOverlays().clear();
            mMapView.getOverlays().add(routeOverlay);
            mMapView.invalidate();
            mMapController.animateTo(arg0.getStart().pt);
    		/**
    		 * 创建自定义overlay
    		 */
    		mOverlay = new MyOverlay(getResources().getDrawable(
    				R.drawable.poi), mMapView);
    		OverlayItem item1 = new OverlayItem(currentPt, "currentLocation", "");
    		OverlayItem item2 = new OverlayItem(targetPt, "targetLocation", "");
//    		MKRoute route = arg0.getPlan(0).getRoute(0);
//    		for(ArrayList<GeoPoint> item : route.getArrayPoints()){
//    			Log.d("导航：", item.toString());
//    		}
    		/**
    		 * 设定地图中心点
    		 */
    		mMapController.setCenter(currentPt);
    		/**
    		 * 设置overlay图标，如不设置，则使用创建ItemizedOverlay时的默认图标.
    		 */
    		item1.setMarker(getResources().getDrawable(R.drawable.start));	
    		item2.setMarker(getResources().getDrawable(R.drawable.target));
    		/**
    		 * 清楚原来的pop点，重新绘制pop点	
    		 */
    		mOverlay.removeAll();
//          mMapView.getOverlays().clear();
    		/**
    		* 将item 添加到overlay中 注意： 同一个itme只能add一次
    		*/
    		mOverlay.addItem(item1);
    		mOverlay.addItem(item2);
    		/**
    		 * 将overlay 添加至MapView中
    		 */
    		mMapView.getOverlays().add(mOverlay);
    		/**
    		 * 刷新地图
    		 */
    		mMapView.refresh();
		}

		@Override
		public void onGetPoiDetailSearchResult(int arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			/* 
             * 返回poi搜索结果。 参数： arg0 - 搜索结果 arg1 - 返回结果类型: MKSearch.TYPE_POI_LIST MKSearch.TYPE_AREA_POI_LIST MKSearch.TYPE_CITY_LIST arg2 - 错误号，0表示正确返回 
             */ 
		}

		@Override
		public void onGetShareUrlResult(MKShareUrlResult arg0, int arg1,
				int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			/* 
             * 返回公交搜索结果。 参数： arg0 - 搜索结果 arg1 - 错误号，0表示正确返回， 当返回MKEvent.ERROR_ROUTE_ADDR时，表示起点或终点有歧义， 调用MKTransitRouteResult的getAddrResult方法获取推荐的起点或终点信息 
             */  
		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			/* 
             * 返回步行路线搜索结果。 参数： arg0 - 搜索结果 arg1 - 错误号，0表示正确返回 
             */
		}
		
	}

	// 设置相关参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开gps
		option.setAddrType("all"); // 返回的定位结果包含地址信息
		option.setScanSpan(10000); // 设置发起定位请求的间隔时间为10000ms
		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
		option.setPriority(LocationClientOption.GpsFirst); // 设置Gps优先
		option.disableCache(true); // 禁止启用缓存定位
//		option.setPoiNumber(20); //最多返回POI个数
//		option.setPoiDistance(5000); //poi查询距离
//		option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息
		mLocClient.setLocOption(option);
	}

	/**
	 * 监听函数，有更新位置的时候，格式化成字符串，输出到屏幕中 BDLocationListener接口有2个方法需要实现：
	 * 1.接收异步返回的定位结果，参数是BDLocation类型参数。 2.接收异步返回的POI查询结果，参数是BDLocation类型参数。
	 */
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if(location == null) {
				return;
			}else if(getDistanceFromXtoY(location) < 20){
				/**
				 * 当用户进入停车场后，显示该停车场三维地图
				 */
				startVoice();
				Intent intent = new Intent(NavigationActivity.this, ThreeDimensionPark.class);
				startActivity(intent);
			}else if(!(lastLocation == null)){
				if(!(location.getLatitude() == lastLocation.getLatitude())){
					lastLocation = location;
					initOverlay(location);
				}
			}else{
				lastLocation = location;
				initOverlay(location);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	public void initOverlay(BDLocation location) {
		/**
		 * 准备overlay 数据
		 */
		GeoPoint p1 = new GeoPoint((int) (location.getLatitude() * 1E6),
				(int) (location.getLongitude() * 1E6));

		GeoPoint p2 = new GeoPoint((int) (targetLocation.get("lat") * 1E6),
				(int) (targetLocation.get("lng") * 1E6));
		currentPt = p1;
		targetPt = p2;
		/**
		 * 设置导航起点和终点
		 */
		//设置起始地（当前位置）
        MKPlanNode startNode = new MKPlanNode();
        startNode.pt = p1;
      //设置目的地
        MKPlanNode endNode = new MKPlanNode();
        endNode.pt = p2;
		mKSearch.drivingSearch(null, startNode, null, endNode);

	}

	@Override
	public void onPause() {
		/**
		 * MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		 */
		mMapView.onPause();  
		super.onPause();
		if (mLocClient != null) {
			mLocClient.stop();
		}
		mIsStart = false;
	}

	@Override
	public void onResume() {
		/**
		 * MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		/**
		 * MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		 */
		mMapView.destroy();
		super.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {	
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@SuppressWarnings("rawtypes")
	public class MyOverlay extends ItemizedOverlay {
		public MyOverlay(Drawable defaultMarker, MapView mapView) {
			super(defaultMarker, mapView);
		}
	}	
	
	/**
	 * 语音提醒
	 */
	public void startVoice(){
		 /* 建立MediaPlayer对象 */ 
		MediaPlayer mMediaPlayer = new MediaPlayer();  
	    /* 将音乐以Import的方式保存res/raw/floorerror.mp3 */ 
	    mMediaPlayer = MediaPlayer.create(NavigationActivity.this, R.raw.voice); 
	    try {
			mMediaPlayer.prepare();
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    mMediaPlayer.start();
	}
	
	/**
	 * 计算两个BDLocation点之间的距离
	 * @param x
	 * @param y
	 * @return
	 */
	private int getDistanceFromXtoY(BDLocation x){
		double latx = x.getLatitude();
		double lngx = x.getLongitude();
		double laty = targetLocation.get("lat");
		double lngy = targetLocation.get("lng");
		
	    double pk = 180 / 3.14169;
	    double t1 = Math.cos(latx / pk) * Math.cos(lngx / pk) * Math.cos(laty / pk) * Math.cos(lngy / pk);
	    double t2 = Math.cos(latx / pk) * Math.sin(lngx / pk) * Math.cos(laty / pk) * Math.sin(lngy / pk);
	    double t3 = Math.sin(latx / pk) * Math.sin(laty / pk);
	    double tt = Math.acos(t1 + t2 + t3);
	    return (int)Math.round(6366000 * tt);
	}
}
