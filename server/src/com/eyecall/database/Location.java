package com.eyecall.database;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "location")
public class Location {
	
	@ManyToOne
	@JoinColumn(name = "volunteer_id")
	private Volunteer volunteer;
	
	@GeneratedValue
	@Id
	@Column(name = "id")
	private int id;
	
	@Column(name="latitude")
	private double latitude;
	
	@Column(name="longitude")
	private double longitude;
	
	@Column(name="preferred")
	private boolean preferred;
	
	@Column(name="radius")
	private int radius;
	
	private Date created;

	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
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
	
	public Date getCreated() {
		return created;
	}
	
	@PrePersist
	protected void onCreate(){
		this.created = new Date();
	}

	@Override
	public String toString() {
		return "Location [volunteer=" + volunteer + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", preferred=" + preferred
				+ ", radius=" + radius + "]";
	}
}
