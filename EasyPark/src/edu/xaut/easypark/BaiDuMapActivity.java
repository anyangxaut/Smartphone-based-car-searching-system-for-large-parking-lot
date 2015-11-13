package edu.xaut.easypark;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import edu.xaut.entity.ParkingLotInfo;

public class BaiDuMapActivity extends Activity {
    // 定位
 	public BDLocationListener myListener = new MyLocationListener();
 	private LocationClient mLocClient = null;
 	private boolean mIsStart = false;
 	private MKSearch mKSearch; 
 	private BaiDuMapRegister app;
 	private BDLocation lastLocation = null;
 	private MySearchListener mySearchListener = new MySearchListener();
	private List<ParkingLotInfo> nearParkingLotInfo = null; 
 	private ListView listView = null;
 	
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
		setContentView(R.layout.activity_baidumap);
		/**
		 *  存储附近停车场信息
		 */
		nearParkingLotInfo = new ArrayList<ParkingLotInfo>();
		listView = (ListView)findViewById(R.id.listView);
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BaiDuMapActivity.this, NavigationActivity.class);
				Bundle bundle = new Bundle();
				bundle.putDouble("lat", nearParkingLotInfo.get(arg2).getPt().getLatitudeE6() / 1E6);
				bundle.putDouble("lng", nearParkingLotInfo.get(arg2).getPt().getLongitudeE6() / 1E6);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			
		});
		mLocClient = new LocationClient(this); // 声明LocationClient类
		mLocClient.setAK(BaiDuMapRegister.strKey);
		mLocClient.registerLocationListener(myListener); // 注册监听函数
		/**
		 *  定位相关设置
		 */
		setLocationOption();

		if (!mIsStart) {
			mLocClient.start();
			if (mLocClient != null && mLocClient.isStarted()) {
				/**
				 *  发起定位请求。请求过程是异步的，定位结果在监听函数onReceiveLocation中获取
				 */
				mLocClient.requestLocation(); 
				mIsStart = true;
			} else
				Log.d("LocSDK3", "locClient is null or not started");
		}	
		
		/**
		 *  POI搜索之周边周边检索poiSearchNearBy---搜索附近5000m内的停车场
		 */
		/**
		 *  设置每页返回的POI数，默认为10，取值范围1-50  
		 */
        MKSearch.setPoiPageCapacity(20);  
        /**
         * 初始化
         */
		mKSearch = new MKSearch();  
		mKSearch.init(app.mBMapManager, mySearchListener);
	}
	
	/**
	 *  POI搜索之周边周边检索poiSearchNearBy---监听器
	 */
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
            if (arg0 == null)  
            {  
                return;  
            }  
            /**
             *  刷新页面，显示附近的停车场
             */
            updateListView(arg0);
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
	
	// 通过listview显示附近停车场信息
	private void updateListView(MKPoiResult result){
        /**
         *  遍历当前页返回的搜索结果（默认只返回10个）   
         */
        for (MKPoiInfo poiInfo : result.getAllPoi())  
        {  
        	ParkingLotInfo info = new ParkingLotInfo(poiInfo.name, poiInfo.address, 
        			getDistanceFromXtoY(lastLocation, poiInfo.pt), poiInfo.pt);
        	nearParkingLotInfo.add(info);
        }   
        
        listView.setAdapter(new ListAdapter(BaiDuMapActivity.this, nearParkingLotInfo));
	}
	
	/**
	 * 计算两个BDLocation点之间的距离
	 * @param x
	 * @param y
	 * @return
	 */
	private int getDistanceFromXtoY(BDLocation x, GeoPoint y){
//		GeoPoint gp = new GeoPoint((int) (x.getLatitude() * 1E6), (int) (x.getLongitude() * 1E6));
		double latx = x.getLatitude();
		double lngx = x.getLongitude();
		double laty = y.getLatitudeE6() / 1E6;
		double lngy = y.getLongitudeE6() / 1E6;
		
	    double pk = 180 / 3.14169;
	    double t1 = Math.cos(latx / pk) * Math.cos(lngx / pk) * Math.cos(laty / pk) * Math.cos(lngy / pk);
	    double t2 = Math.cos(latx / pk) * Math.sin(lngx / pk) * Math.cos(laty / pk) * Math.sin(lngy / pk);
	    double t3 = Math.sin(latx / pk) * Math.sin(laty / pk);
	    double tt = Math.acos(t1 + t2 + t3);
	    return (int)Math.round(6366000 * tt);
	}
	
	// 设置定位相关参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开gps
		option.setAddrType("all"); // 返回的定位结果包含地址信息
		option.setScanSpan(10000); // 设置发起定位请求的间隔时间为10000ms
		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
		option.setPriority(LocationClientOption.GpsFirst); // 设置Gps优先
		option.disableCache(true); // 禁止启用缓存定位
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
			}else if(!(lastLocation == null)){
				if(!(location.getLatitude() == lastLocation.getLatitude())){
					lastLocation = location;
					searchNearParkingLot(location);
				}
			}else{
				lastLocation = location;
				searchNearParkingLot(location);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	public void searchNearParkingLot(BDLocation location) {
		// 根据百度定位sdk返回的定位信息，搜索当前位置附近5000m内的所有停车场
		if (mKSearch.poiSearchNearBy("停车场", new GeoPoint(  
                (int) (location.getLatitude() * 1E6),  
                (int) (location.getLongitude() * 1E6)), 5000) == -1)  
        {  
            Log.d("BaiDuMapActivity", "mKSearch.poiSearchNearBy失败");  
        }  
        else  
        {  
        	Log.d("BaiDuMapActivity", "mKSearch.poiSearchNearBy成功"); 
        }  
	}

	@Override
	public void onPause() { 
		super.onPause();
		if (mLocClient != null) {
			mLocClient.stop();
		}
		mIsStart = false;
	}

	@Override
	public void onDestroy() {
		mLocClient = null;
		super.onDestroy();
	}
}
