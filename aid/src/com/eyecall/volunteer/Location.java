package com.eyecall.volunteer;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {

	public Location() {
	}

	private Location(Parcel in) {
		//Location l = new Location();
		//l.id = in.readInt();
		this.latitude = in.readDouble();
		this.longitude = in.readDouble();
		this.preferred = in.readInt()==1;
		this.radius = in.readDouble();
	}

	//private int id;

	private double latitude;

	private double longitude;

	private boolean preferred;

	private double radius;

	private int id;

	//public void setId(int id) {
	//	this.id = id;
	//}

	//public int getId() {
	//	return id;
	//}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public boolean isPreferred() {
		return preferred;
	}

	public void setPreferred(boolean preferred) {
		this.preferred = preferred;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double d) {
		this.radius = d;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		//out.writeInt(id);
		out.writeDouble(latitude);
		out.writeDouble(longitude);
		out.writeInt(preferred ? 1 : 0);
		out.writeDouble(radius);
	}

	public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
		public Location createFromParcel(Parcel in) {
			return new Location(in);
		}

		@Override
		public Location[] newArray(int size) {
			return new Location[size];
		}
	};
	
	@Override
	public String toString() {
		return "lat=" + latitude + " long=" + longitude + " pref=" + (preferred ? "true": "false") + " radius=" + radius;
	}

	public String getTag() {
		return latitude + ";" + longitude + ";" + (preferred ? "T": "F") + ";" + radius;
	}

	public void setId(int id) {
		this.id = id;
	};
	
	public int getId(){
		return this.id;
	}

}
