package edu.xaut.easypark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

/**
 * Created by anyang on 2015/7/28.
 */
public class HomePageActivity extends Activity {
	
	private ViewPager viewPager; // android-support-v4中的滑动组件
	private List<ImageView> imageViews; // 滑动的图片集合

	private String[] titles; // 图片标题
	private int[] imageResId; // 图片ID
	private List<View> dots; // 图片标题正文的那些点

	private TextView tv_title;
	private int currentItem = 0; // 当前图片的索引号
	
	// gridview相关变量初始化
	private GridView gridview;
	private MyAdapter adapter;
	private int[] icons = { R.drawable.nearparkinglot, R.drawable.nearparkinglot, R.drawable.nearparkinglot};
	private List<Map<String, Double>> parkingInfo = null;

	private ScheduledExecutorService scheduledExecutorService;

	// 切换当前显示的图片
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			viewPager.setCurrentItem(currentItem);// 切换当前显示的图片
		};
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_homepage);
  
        imageResId = new int[] { R.drawable.parka, R.drawable.parkb, R.drawable.parkc};
		titles = new String[imageResId.length];
		titles[0] = "立丰国际停车场";
		titles[1] = "东方家园停车场";
		titles[2] = "常春藤花园东区停车场";
		/**
		 * 添加常用的停车场的地理信息坐标
		 */
		parkingInfo = new ArrayList<Map<String, Double>>();
		Map<String, Double> map1 = new HashMap<String, Double>();
		map1.put("lat", 34.25263);
		map1.put("lng", 109.003712);
		parkingInfo.add(map1);
		Map<String, Double> map2 = new HashMap<String, Double>();
		map2.put("lat", 34.250895);
		map2.put("lng", 109.0036);
		parkingInfo.add(map2);
		Map<String, Double> map3 = new HashMap<String, Double>();
		map3.put("lat", 34.253681);
		map3.put("lng", 108.996476);
		parkingInfo.add(map3);
		
		imageViews = new ArrayList<ImageView>();

		// 初始化图片资源
		for (int i = 0; i < imageResId.length; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(imageResId[i]);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageViews.add(imageView);
		}

		dots = new ArrayList<View>();
		dots.add(findViewById(R.id.v_dot0));
		dots.add(findViewById(R.id.v_dot1));
		dots.add(findViewById(R.id.v_dot2));

		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(titles[0]);//

		viewPager = (ViewPager) findViewById(R.id.vp);
		viewPager.setAdapter(new ViewPagerAdapter());// 设置填充ViewPager页面的适配器
		// 设置一个监听器，当ViewPager中的页面改变时调用
		viewPager.setOnPageChangeListener(new MyPageChangeListener());
		
		
		adapter = new MyAdapter(this, titles, icons);
		gridview = (GridView)findViewById(R.id.gridview);
		gridview.setAdapter(adapter);
		
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
//				Intent intent = new Intent(HomePageActivity.this, NavigationActivity.class);
				Intent intent = new Intent(HomePageActivity.this, ThreeDimensionPark.class);
				Bundle bundle = new Bundle();
				bundle.putDouble("lat", parkingInfo.get(arg2).get("lat"));
				bundle.putDouble("lng", parkingInfo.get(arg2).get("lng"));
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
    }
    
    @Override
	protected void onStart() {
    	// 按指定频率周期执行某个任务
    	// 只有一个线程的线程池，因此所有提交的任务是顺序执行
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		// 当Activity显示出来后，每3秒钟切换一次图片显示
		/**
		 * new ScrollTask()：执行线程
		 * 1：初始化延时
		 * 3：两次开始执行最小间隔时间
		 * TimeUnit.SECONDS:计时单位
		 */
		scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 3, TimeUnit.SECONDS);
		super.onStart();
	}

	@Override
	protected void onStop() {
		// 当Activity不可见的时候停止切换
		scheduledExecutorService.shutdown();
		super.onStop();
	}

	/**
	 * 切换图片的任务线程
	 * 
	 * @author Administrator
	 * 
	 */
	private class ScrollTask implements Runnable {

		public void run() {
			synchronized (viewPager) {
				currentItem = (currentItem + 1) % imageViews.size();
				handler.obtainMessage().sendToTarget(); // 通过Handler切换图片
			}
		}

	}

	/**
	 * 当ViewPager中页面的状态发生改变时调用
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyPageChangeListener implements OnPageChangeListener {
		private int oldPosition = 0;

		/**
		 * This method will be invoked when a new page becomes selected.
		 * position: Position index of the new selected page.
		 */
		public void onPageSelected(int position) {
			currentItem = position;
			tv_title.setText(titles[position]);
			dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
			dots.get(position).setBackgroundResource(R.drawable.dot_focused);
			oldPosition = position;
		}

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

	/**
	 * 填充ViewPager页面的适配器
	 * 
	 * @author Administrator
	 * 
	 */
	private class ViewPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return imageResId.length;
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(imageViews.get(arg1));
			return imageViews.get(arg1);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.home_page, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
	    case R.id.near_parkinglot:
	        Intent intent = new Intent(HomePageActivity.this, BaiDuMapActivity.class);
	        startActivity(intent);
	    	break;
	    }
	    return true;
	}
	
}
