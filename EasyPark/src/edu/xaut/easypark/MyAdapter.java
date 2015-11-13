package edu.xaut.easypark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * gridview内部布局，控制gridview显示，属于自定义布局，这个有图片背景
 * @author anyang
 *
 */
public class MyAdapter extends BaseAdapter { 
	
  private Context _ct;
  private String[] _items;
  private int[] _icons;


  public MyAdapter(Context ct,String[] items,int[] icons) 
  {
    _ct=ct;
    _items=items;
    _icons=icons;
  }
  public int getCount()
  {
    return _items.length;
  }

  public Object getItem(int arg0)
  {
    return _items[arg0];
  }

  public long getItemId(int position)
  {
    return position;
  }
  /**
   * 获得 LayoutInflater 实例的三种方式：
   * 1. LayoutInflater inflater = getLayoutInflater();  //调用Activity的getLayoutInflater()  
  
     2. LayoutInflater localinflater =  (LayoutInflater)context.getSystemService  
  
                                                 (Context.LAYOUT_INFLATER_SERVICE);  
  
    3. LayoutInflater inflater = LayoutInflater.from(context);    
    所以这三种方式最终本质是都是调用的Context.getSystemService(). 
   */
  public View getView(int position, View convertView, ViewGroup parent)
  {
    LayoutInflater factory = LayoutInflater.from(_ct);
    View v = (View)factory.inflate(R.layout.myadapter, null);
    ImageView iv = (ImageView)v.findViewById(R.id.imageview);
    TextView tv = (TextView)v.findViewById(R.id.textview);
    iv.setImageResource(_icons[position]);
    tv.setText(_items[position]);
    return v;
  } 
} 

