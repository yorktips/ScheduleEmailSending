package com.fan.email;

import com.fan.email.ScheduledEmailBatchSender;

public class EmailSending {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/demodb";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "";

	public static void main(String[] args) {
		try {
			System.out.println("main start" );
			ScheduledEmailBatchSender scheduledEmailBatchSender= new ScheduledEmailBatchSender(JDBC_DRIVER,DB_URL,USER,PASS);
			System.out.println("thread start" );
			scheduledEmailBatchSender.start();
			System.out.println("main end" );
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
