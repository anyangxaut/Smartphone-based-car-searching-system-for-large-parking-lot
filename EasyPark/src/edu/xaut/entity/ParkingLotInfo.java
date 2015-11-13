package edu.xaut.entity;

import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * 存储查询到的附近停车场信息
 * @author anyang
 *
 */
public class ParkingLotInfo {

	private String name;
	private String address;
	private int distance;
	private GeoPoint pt;
	
	public ParkingLotInfo(String name, String address, int distance, GeoPoint pt) {
		super();
		this.name = name;
		this.address = address;
		this.distance = distance;
		this.pt = pt;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	public GeoPoint getPt() {
		return pt;
	}
	public void setPt(GeoPoint pt) {
		this.pt = pt;
	}
}
