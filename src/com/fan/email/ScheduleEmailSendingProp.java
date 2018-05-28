package com.fan.email;

import java.util.ArrayList;

import com.fan.email.entity.EmailTemplate;


public class ScheduleEmailSendingProp {
	private String JDBC_DRIVER=null;//com.mysql.jdbc.Driver
	private String DB_URL=null; //jdbc:mysql://localhost/demodb
	private String DB_USER=null;
	private String DB_PASS=null;

	private ArrayList<EmailTemplate> emailSchedules=null;

	public String getJDBC_DRIVER() {
		return JDBC_DRIVER;
	}

	public void setJDBC_DRIVER(String jDBC_DRIVER) {
		JDBC_DRIVER = jDBC_DRIVER;
	}

	public String getDB_URL() {
		return DB_URL;
	}

	public void setDB_URL(String dB_URL) {
		DB_URL = dB_URL;
	}

	public String getDB_USER() {
		return DB_USER;
	}

	public void setDB_USER(String dB_USER) {
		DB_USER = dB_USER;
	}

	public String getDB_PASS() {
		return DB_PASS;
	}

	public void setDB_PASS(String dB_PASS) {
		DB_PASS = dB_PASS;
	}

	public ArrayList<EmailTemplate> getEmailSchedules() {
		return emailSchedules;
	}

	public void setEmailSchedules(ArrayList<EmailTemplate> emailSchedules) {
		this.emailSchedules = emailSchedules;
	}
	
}
