package com.ctc.email;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


//Useage:
//java com.fan.email.EmailSending
public class EmailSendingBean {
	static Logger loger = Logger.getLogger(EmailSendingBean.class.getName());
	
	// JDBC driver name and database URL
	static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static String DB_URL = "jdbc:mysql://localhost/tc";

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
			loger.debug("DB_URL=" + DB_URL + ";USER=" + USER + ";PASS=" + PASS);
			
			try {
				loger.debug("main start" );
				ScheduledEmailBatchSender scheduledEmailBatchSender= new ScheduledEmailBatchSender(JDBC_DRIVER,DB_URL,USER,PASS);
				loger.debug("thread start" );
				scheduledEmailBatchSender.start();
				loger.debug("main end" );
			}catch(Exception e) {
				//e.printStackTrace();
				loger.error(e);
			}
			
		} catch (IOException ex) {
			//ex.printStackTrace();
			loger.error(ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					loger.error(e);
					//e.printStackTrace();
				}
			}
		}
		

	}

}
