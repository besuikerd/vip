package com.eyecall.database;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="volunteer")
public class Volunteer {
	@Id
	@Column(name="id")
	private String id;
	
	public Volunteer() {
		this.id = new BigInteger(256, new SecureRandom()).toString(16);
	}
	
	public Volunteer(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Volunteer [id=" + id + "]";
	}
}
