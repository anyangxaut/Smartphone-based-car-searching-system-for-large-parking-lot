package edu.xaut.easypark;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import edu.xaut.entity.ParkingLotInfo;

public class ListAdapter extends BaseAdapter {

	private Context context;
	private List<ParkingLotInfo> listInfo;
	
	public ListAdapter(Context context, List<ParkingLotInfo> listInfo) {
		super();
		this.context = context;
		this.listInfo = listInfo;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listInfo.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return listInfo.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		LayoutInflater factory = LayoutInflater.from(context);
	    View view = (View)factory.inflate(R.layout.listitem, null);
	    
	    TextView name = (TextView)view.findViewById(R.id.name);
	    name.setText((arg0 + 1) + "." + listInfo.get(arg0).getName());
	    TextView distance = (TextView)view.findViewById(R.id.distance);
	    distance.setText(String.valueOf(listInfo.get(arg0).getDistance()) + "米");
	    TextView address = (TextView)view.findViewById(R.id.address);
	    address.setText(listInfo.get(arg0).getAddress());
	    
	    return view;
	}

}
