package com.eyecall.volunteer;

public class Location {

	private int id;

	private double latitude;
	
	private double longitude;
	
	private boolean preferred;
	
	private int radius;
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}

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

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
}
