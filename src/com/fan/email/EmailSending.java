package com.fan.email;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import com.fan.email.entity.*;

public class EmailSending {
	// JDBC driver name and database URL
	static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static String DB_URL = null;//"jdbc:mysql://localhost/demodb";

	// Database credentials
	static String USER = null;//"root";
	static String PASS = null;//"";

	public static ScheduleEmailSendingProp loadProperties() {
		String propertiesFile="scheduleemailsending.properties";
		Properties prop = new Properties();
		InputStream input = null;
		ScheduleEmailSendingProp properties = new ScheduleEmailSendingProp();
		ArrayList<EmailTemplate> emailSchedules= new ArrayList<EmailTemplate>();
		
		try {
			input = new FileInputStream(propertiesFile);
			// load a properties file
			prop.load(input);

			properties.setJDBC_DRIVER(prop.getProperty("JDBC_DRIVER"));
			properties.setDB_URL(prop.getProperty("DB_URL"));
			properties.setDB_USER(prop.getProperty("DB_USER"));
			properties.setDB_PASS(prop.getProperty("DB_PASS"));
			properties.setEmailSchedules(emailSchedules);
			
			String sTotalSchedules=prop.getProperty("email_template_cnt");
			int nTotalSchedules=Integer.parseInt(sTotalSchedules);
			if (nTotalSchedules<1) {
				System.out.println("No Schedule found.");
				//return properties;
			}
			
			for (int idx=1; idx<=nTotalSchedules; idx++) {
				EmailTemplate emailTemplate= new EmailTemplate();
				
				emailTemplate.setId(Integer.parseInt(prop.getProperty("email_template_" + idx + ".id")));
				emailTemplate.setSmtp_host(prop.getProperty("email_template_" + idx + ".smtp_host"));
				emailTemplate.setSmtp_port(prop.getProperty("email_template_" + idx + ".smtp_port"));
				emailTemplate.setSmtp_username(prop.getProperty("email_template_" + idx + ".smtp_username"));
				emailTemplate.setSmtp_password(prop.getProperty("email_template_" + idx + ".smtp_password"));
				emailTemplate.setTask_name(prop.getProperty("email_template_" + idx + ".task_name"));
				emailTemplate.setSend_from(prop.getProperty("email_template_" + idx + ".send_from"));
				emailTemplate.setSend_cc(prop.getProperty("email_template_" + idx + ".send_cc"));
				emailTemplate.setSend_bcc(prop.getProperty("email_template_" + idx + ".send_bcc"));
				emailTemplate.setEmail_body_type(prop.getProperty("email_template_" + idx + ".email_body_type"));
				emailTemplate.setSchedule_type(prop.getProperty("email_template_" + idx + ".schedule_type"));
				emailTemplate.setSchedule_time(prop.getProperty("email_template_" + idx + ".schedule_time"));
				emailTemplate.setSend_to_type(prop.getProperty("email_template_" + idx + ".send_to_type"));
				emailTemplate.setSend_to_list(prop.getProperty("email_template_" + idx + ".send_to_list"));
				emailTemplate.setSend_to_sql(prop.getProperty("email_template_" + idx + ".send_to_sql"));
				
				String sEnabled=prop.getProperty("email_template_" + idx + ".enabled");
				if ("true".equalsIgnoreCase(sEnabled))
					emailTemplate.setEnabled(true);
				else
					emailTemplate.setEnabled(false);
				
				emailTemplate.setEmail_title(prop.getProperty("email_template_" + idx + ".email_title"));
				emailTemplate.setEmail_template(prop.getProperty("email_template_" + idx + ".email_template"));
				
				String sLast_send_at=prop.getProperty("email_template_" + idx + ".last_send_at");
				if (sLast_send_at!=null && sLast_send_at.length()>14) {
					Date last_send_at=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sLast_send_at);
					emailTemplate.setLast_send_at(new java.sql.Date(last_send_at.getTime()));
				}

				String sLast_finish_at=prop.getProperty("email_template_" + idx + ".last_finish_at");
				if (sLast_finish_at!=null && sLast_finish_at.length()>14) {
					Date last_finish_at=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sLast_finish_at);
					emailTemplate.setLast_finish_at(new java.sql.Date(last_finish_at.getTime()));
				}

				emailSchedules.add(emailTemplate);
			}		
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					return properties;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			properties.setEmailSchedules(emailSchedules);
		}
		
		return properties;
	}
	
	
	public static void main(String[] args) {
		try {
			ScheduleEmailSendingProp properties=loadProperties();
			JDBC_DRIVER=properties.getJDBC_DRIVER();
			DB_URL=properties.getDB_URL();
			USER=properties.getDB_USER();
			PASS=properties.getDB_PASS();
			
			System.out.println("main start" );
			ScheduledEmailBatchSender scheduledEmailBatchSender= new ScheduledEmailBatchSender(JDBC_DRIVER,DB_URL,USER,PASS);
			scheduledEmailBatchSender.setEmailTempaltes(properties.getEmailSchedules());
			System.out.println("thread start" );
			scheduledEmailBatchSender.start();
			System.out.println("main end" );
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
