package com.ctc.email;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


//Useage:
//java com.fan.email.EmailSending
public class EmailSending {
	// JDBC driver name and database URL
	static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static String DB_URL = "jdbc:mysql://localhost/central";

	// Database credentials
	static String USER = "root";
	static String PASS = "";

	public static void main(String[] args) {
		Properties prop = new Properties();
		OutputStream output = null;
		InputStream input = null;

		try {
			input = new FileInputStream("Database.properties");

			// load a properties file
			prop.load(input);
			// get the property value and print it out
			DB_URL=prop.getProperty("DBURL");
			USER=prop.getProperty("USERNAME");
			PASS=prop.getProperty("PASSWORDW");
			System.out.println("DB_URL=" + DB_URL + ";USER=" + USER + ";PASS=" + PASS);
			
			try {
				System.out.println("main start" );
				ScheduledEmailBatchSender scheduledEmailBatchSender= new ScheduledEmailBatchSender(JDBC_DRIVER,DB_URL,USER,PASS);
				System.out.println("thread start" );
				scheduledEmailBatchSender.start();
				System.out.println("main end" );
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		

	}

}
