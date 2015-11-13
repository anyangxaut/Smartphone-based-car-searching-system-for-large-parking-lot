package edu.xaut.easypark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class ThreeDimensionPark extends Activity {

	private ImageView uplayer = null;
	private ImageView downlayer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去掉标题栏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_threedimensionpark);
		
		uplayer = (ImageView)findViewById(R.id.uplayer);
		downlayer = (ImageView)findViewById(R.id.downlayer);
		
		/**
		 * ImageView点击事件监听器
		 */
		uplayer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ThreeDimensionPark.this, TwoDimensionPark.class);
				Bundle bundle = new Bundle();
				bundle.putString("rootmapfolder", "uplayerpark");
				bundle.putString("floor", "0");
				intent.putExtra("bundle", bundle);
				startActivity(intent);
			}
			
		});
		
		downlayer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ThreeDimensionPark.this, TwoDimensionPark.class);
				Bundle bundle = new Bundle();
				bundle.putString("rootmapfolder", "downlayerpark");
				bundle.putString("floor", "1");
				intent.putExtra("bundle", bundle);				
				startActivity(intent);
			}
			
		});
	}
}
