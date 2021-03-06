package com.ctc.email;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;


//import java.util.logging.Logger;
import org.apache.log4j.Logger;

import com.ctc.email.entity.*;
import com.ctc.email.util.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.ByteArrayInputStream;
import java.sql.*;


public class ScheduledEmailBatchSender extends Thread{
	static Logger loger = Logger.getLogger(ScheduledEmailBatchSender.class.getName());
	private Connection conn = null;

	private String JDBC_DRIVER=null;
	private String DB_URL=null;
	private String USER=null;
	private String PASS=null;
	
	public static Auth auth=null;
	
	//private boolean connect(String JDBC_DRIVER , String DB_URL,String USER,String PASS ) {
	private boolean connect() {
		boolean ret=false;
		try{
			Class.forName(this.JDBC_DRIVER);
			loger.debug("Connecting to a selected database...");
			this.conn = DriverManager.getConnection(this.DB_URL, this.USER, PASS);
			loger.debug("Connected database successfully...");	
			ret=true;
		}catch(Exception e) {
			loger.error(e);
		}
		return ret;
	}

	private boolean disConnect() {
		boolean ret=false;
		loger.debug("From disConnect Method");
		try {
			if (this.conn != null) {
				loger.debug("closing conn...");
				this.conn.close();
				loger.debug("closed conn");
				ret=true;
				this.conn=null;
			}
		} catch (SQLException se) {
			loger.error(se);
		} 
		return ret;
	}	
	
	public ScheduledEmailBatchSender(String jdbc_driver , String db_url,String user,String pass){
		this.JDBC_DRIVER=jdbc_driver;
		this.DB_URL=db_url;
		this.USER=user;
		this.PASS=pass;
	}
	
	public void run(){  
		loger.info("ScheduledEmailBatchSender thread is running...");  
		doAllEmailSendingJobTasks();
		loger.info("ScheduledEmailBatchSender thread finished..."); 
	}  
	
	private int doAllEmailSendingJobTasks() {
		int ret=0;
		if (!connect()) {
			loger.error("Failed to connect database");
			return ret;
		}
		
		if ( this.conn==null) {
			loger.error("Error, conn is null");
			return ret;
		}
		
		Statement stmt = null;
		try {
			List<EmailTemplate> emailTempaltes=getEmailTemplates(conn);
			loger.debug("Tatal email-sending tasks=" + emailTempaltes.size());
			for (EmailTemplate emailTempalte : emailTempaltes) {
				if (needTriggerSending(conn,emailTempalte)) {
					updateEmailJobScheduleStatus("last_send_at",emailTempalte);
					ret += processEmailTemplate(emailTempalte);
					updateEmailJobScheduleStatus("last_finish_at",emailTempalte);
				}
			}
		}catch(Exception e) {
			//e.printStackTrace();
			loger.error(e);
		}finally{
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
				loger.error(se);
			}
		}
		
		disConnect();
		
		return ret;
	}
	
	public List<EmailTemplate> getEmailTemplates(){
		return getEmailTemplates(conn);
	}
	
	private List<EmailTemplate> getEmailTemplates(Connection conn){
		if (conn==null) return null;
		
		List<EmailTemplate> templates= new ArrayList<EmailTemplate>();
		String sql="select * from email_template where enabled=true and checkEmailTask(schedule_type, schedule_time,last_send_at )='Y' ";
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			//getColumns(rs);
			// STEP 5: Extract data from result set
			while (rs.next()) {
				EmailTemplate template = new EmailTemplate();
				// Retrieve by column name
				template.setId(rs.getInt("id"));
				template.setSmtp_host(rs.getString("smtp_host"));
				template.setSmtp_port(rs.getString("smtp_port"));
				template.setSmtp_username(rs.getString("smtp_username"));
				template.setSmtp_password(rs.getString("smtp_password"));				
				template.setTask_name(rs.getString("task_name"));
				template.setSend_from(rs.getString("send_from"));
				template.setSend_cc(rs.getString("send_cc"));
				template.setSend_bcc(rs.getString("send_bcc"));
				template.setEmail_body_type(rs.getString("email_body_type"));
				template.setSchedule_type(rs.getString("schedule_type"));
				template.setSchedule_time(rs.getString("schedule_time"));
				template.setSend_to_type(rs.getString("send_to_type"));
				template.setSend_to_list(rs.getString("send_to_list"));
				template.setSend_to_sql(rs.getString("send_to_sql"));
				template.setEnabled(rs.getBoolean("enabled"));
				template.setEmail_title(rs.getString("email_title"));
				template.setEmail_template(rs.getString("email_template"));
				template.setLast_send_at(rs.getDate("last_send_at"));
				template.setLast_finish_at(rs.getDate("last_finish_at"));
				templates.add(template);
			}
			// STEP 6: Clean-up environment
			rs.close();
			stmt.close();
		} catch (Exception e) {
			loger.error(e);
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}			
		}
		return templates;
	}
	
	//This function is for future purpose.Currently, always return true.
	//Job schedule is supposed to set in Windows daily task schedule. 
	public static boolean needTriggerSending(Connection conn, EmailTemplate emailTempalte) {
		boolean bret=false;
		int nHoursEclipsed=0;
		String schedule_time=emailTempalte.getSchedule_time();
		
		if (schedule_time==null ||"".equalsIgnoreCase(schedule_time))
				return true;
		
		String schedule_type=emailTempalte.getSchedule_type();
		String hhmm=DateAndTime.getCurrentHHMM();
		//if (emailTempalte.getLast_send_at()==null) return true;
		if ("daily".equalsIgnoreCase(schedule_type)) {
			if (hhmm.equalsIgnoreCase(emailTempalte.getSchedule_time())) {
				//doSending(emailTempalte);
				return true;
			}
		}

		Date last_send_at=emailTempalte.getLast_send_at();
		Date last_finish_at=emailTempalte.getLast_finish_at();
		return true;
	}
	
	public int processEmailTemplate(EmailTemplate emailTempalte){
		return processEmailTemplate(conn,emailTempalte);
	}
	
	private int processEmailTemplate(Connection conn,EmailTemplate emailTempalte){
		int ret=0;
		String   send_to_sql=emailTempalte.getSend_to_sql();
		if (!emailTempalte.isEnabled())  return ret;
		
		if (conn==null) return 0;
		if (!"sql".equalsIgnoreCase(emailTempalte.getSend_to_type())) {
			return ret;
		}else{
			loger.debug( "send_to_sql= " +send_to_sql);
			List<HashMap<String,Object>> members=getAllSendToFromDb(send_to_sql);
			int nTotal=0;
			if (members !=null) {
				nTotal=members.size();
			}			
			loger.info( "Total " +members.size() + " email found for " +  emailTempalte.getTask_name() + " this time");
			for (HashMap<String,Object> member:members){
				if (sendEmail(emailTempalte,member)){
					ret++;
					loger.info( "Sent email to " + member.get("email"));
					addEmailSentHistory(emailTempalte, member);
				}else{
					loger.info( "Failed to send email to " + member.get("email"));
				}
			}
		}
		return ret;		
	}
	public List<HashMap<String,Object>> getAllSendToFromDb(String sql)  {
		return getAllSendToFromDb(conn,sql);
	}
	
	private List<HashMap<String,Object>> getAllSendToFromDb(Connection conn,String sql)  {
		
		Statement stmt = null;
		List<HashMap<String,Object>> ret=null;
		if (conn==null)
			return null;
		
		try{
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs!=null) {
				ret=convertResultSetToList(rs);
			}
		}catch (SQLException e) {			
			loger.error(e);
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			}			
		}
		return ret;
	}
	
	public static List<HashMap<String,Object>> convertResultSetToList(ResultSet rs) throws SQLException {
	    ResultSetMetaData md = rs.getMetaData();
	    int columns = md.getColumnCount();
	    List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();

	    while (rs.next()) {
	        HashMap<String,Object> row = new HashMap<String, Object>(columns);
	        for(int i=1; i<=columns; ++i) {
	        	loger.debug("columnName=" + md.getColumnName(i) + ";ColumnLabel=" + md.getColumnLabel(i)+ ",value=" +rs.getObject(i) );
	        	//System.out.println("columnName=" + md.getColumnName(i) + ";ColumnLabel=" + md.getColumnLabel(i)+ ",value=" +rs.getObject(i) );
	            row.put(md.getColumnLabel(i),rs.getObject(i));
	        }
	        list.add(row);
	    }

	    return list;
	}
	
	//member must includes "email" column name
	public boolean sendEmail(EmailTemplate emailTempalte,HashMap<String,Object> member){
		boolean ret=false;
		String   email_title=emailTempalte.getEmail_title();
		
		String   email_template=emailTempalte.getEmail_template();
		
		if (StringToDateUtil.fileExist(email_template)) {
			email_template=StringToDateUtil.templateFile2String(email_template);
		}
		
		if (email_template==null || email_template.length()<3) {
			loger.error("email-template is missing:" + emailTempalte.getEmail_template());
			return ret;
		}
			
		
		String body=email_template;
		String title=email_title;
		// Recipient's email ID needs to be mentioned.
		String to=emailTempalte.getSend_to_list();//(String)member.get("email");
		String cc=emailTempalte.getSend_cc();
		String bcc=emailTempalte.getSend_bcc();
		
		if ("sql".equalsIgnoreCase(emailTempalte.getSend_to_type())) {
			to=doRepalceEmail(to,member);
			loger.debug("to_list=" + emailTempalte.getSend_to_list());
			loger.debug("member.email=" + (String)member.get("email") + ", to=" + to);
			if (to==null || to.length()<2)
				to=member.get("email").toString();
			cc=doRepalceEmail(cc,member);
			bcc=doRepalceEmail(bcc,member);
			body=doRepalce(body,member);
			title=doRepalce(title,member);
			loger.debug("final to=" + to + ";cc=" + cc );
			
		}
		
		if (to==null || title==null) return ret;

		String username = emailTempalte.getSmtp_username();
		String password = emailTempalte.getSmtp_password();
		Properties props = new Properties();
		
		// Sender's email ID needs to be mentioned
		String from = emailTempalte.getSend_from();// change accordingly
		//Need to enable from google account first.
		//https://myaccount.google.com/lesssecureapps?utm_source=google-account&utm_medium=web
		if ("smtp.gmail.com".equalsIgnoreCase(emailTempalte.getSmtp_host())){
			// Assuming you are sending email through relay.jangosmtp.net
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", emailTempalte.getSmtp_host());
			// https://myaccount.google.com/lesssecureapps?pli=1
			//props.put("mail.smtp.port", emailTempalte.getSmtp_port()); // 465 or 587
			//props.put("mail.smtp.socketFactory.port", "587");
			//props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			//props.put("mail.smtp.port", "465");
		}else{
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", emailTempalte.getSmtp_host());
			props.put("mail.smtp.port", emailTempalte.getSmtp_port()); 
		}
		
		ScheduledEmailBatchSender.auth= new Auth(emailTempalte);
		
		// Get the Session object.
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						ScheduledEmailBatchSender.auth.getUserName();
						String username=ScheduledEmailBatchSender.auth.getUserName();
						String password=ScheduledEmailBatchSender.auth.getPassword();
						System.out.println("username=" + username);
						System.out.println("password=" + password);
						return new PasswordAuthentication(username, password);
					}
				});

		try {
			// Create a default MimeMessage object.
			Message message = new MimeMessage(session);
			// MailMessage MyMailMessage = new MailMessage();
			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));
			loger.debug("sendfrom=" + from);
			//1. Set To
			String[] toEmails=toArrays(to);
			if (to==null)
				return ret;
			
			loger.debug("toEmail=" + to);
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));;

			//2. Set CC
			String[] ccEmails=toArrays(cc);
			ccEmails=removeDuplicatedIn(ccEmails,toEmails);
			String sCCEmail="";
			if (ccEmails!=null) {
				for (String ccEmail:ccEmails) {
					if ("".equals(sCCEmail))
						sCCEmail=ccEmail;
					else
						sCCEmail += "," + ccEmail;
				}
			}
			loger.debug("ccEmail=" + sCCEmail);
			if (sCCEmail!=null && !"".equalsIgnoreCase(sCCEmail))
				message.addRecipients(Message.RecipientType.CC,InternetAddress.parse(sCCEmail));

			//3. Set BCC
			String[] bccEmails=toArrays(bcc);
			bccEmails=removeDuplicatedIn(bccEmails,toEmails);
			bccEmails=removeDuplicatedIn(bccEmails,ccEmails);
			String sBccEmails="";
			if (bccEmails!=null) {
				for (String bccEmail:bccEmails) {
					if ("".equals(sBccEmails))
						sBccEmails=bccEmail;
					else
						sBccEmails += "," + bccEmail;
				}
			}
			loger.debug("bccEmail=" + sBccEmails);
			if (sBccEmails!=null && !"".equalsIgnoreCase(sBccEmails))
						message.addRecipients(Message.RecipientType.BCC,InternetAddress.parse(sBccEmails));
			
			// Set Subject: header field
			message.setSubject(title);
			loger.debug("title=" + title);
			loger.debug("body=" + body);
			// Now set the actual message
			if ("html".equalsIgnoreCase(emailTempalte.getEmail_body_type())) {
				message.setContent(body, "text/html");
			}else{
				message.setText(body);
			}
			
			loger.info("Sending....");

			// Send message
			Transport.send(message);
			loger.info("Sent message successfully....");
			
			ret=true;
		} catch (MessagingException e) {
			loger.error(e);
			throw new RuntimeException(e);
		}
		
		return ret;
	}
	
	//Remove all items that exist in removelist from removeFrom
	//The purpose of the method is prevent from putting same email in both TO and CC/BCC 
	public static String[] removeDuplicatedIn(String[] fromArray,String[] removeArray){
		if (fromArray==null) 
			return null;
		if (removeArray==null)
			return fromArray;
		
		ArrayList<String> listFrom = new ArrayList<String>(Arrays.asList(fromArray));
		ArrayList<String> removelist = new ArrayList<String>(Arrays.asList(removeArray));
		ArrayList<String> emails= new ArrayList<String>();
		for(String email1:listFrom) {
			boolean bFind=false;
			for(String email2:removelist) {
				if (email1!=null && email1.equalsIgnoreCase(email2)) {
					bFind=true;
				}				
			}
			if (!bFind)
				emails.add(email1);
		}
		
		return emails.toArray(new String[emails.size()]);
	}
	
	
	//<#email#> -> fan8@gmail.com
	public static String doRepalce(String str, HashMap<String,Object> member){
		String ret=str;
		Set<String> columns=member.keySet();
		for (String column:columns) {
			
			Object value=member.get(column);
			//loger.debug("column=" + column + ";value=" + value);
			if (value==null) value="";
			if (value!=null && !"".equals(value)) {
				//loger.debug("replacing [[" + column + "]] -> " + member.get(column).toString());
				ret=ret.replace("<#" + column + "#>",member.get(column).toString());
			}
		}
		
		return ret;
	}

	//<#email#> -> fan8@gmail.com
	public static String doRepalceEmail(String str, HashMap<String,Object> member){
		String ret=str;
		if (str==null || str.length()<1) {
			return null;
		}			
		String[] columns={"email","EMAIL","Email","e-mail", "E-mail","e-Mail"};
		for (String column:columns) {
			Object value=member.get(column);
			if (value!=null) {
				ret=ret.replace("<#" + column + "#>",member.get("email").toString());
			}
		}
		return ret;
	}
	
	//emails="fan1@gmail.com;fan2@gmail.com"
	public static String[] toArrays(String emails) {
		if (emails==null || "".equals(emails))
			return null;
		String lists=emails.replace(";",",");
		lists=lists.replace(":",",");
        String[] ret=lists.split(";");
        return ret;
	}
	
	
	//Add the email send info to email send history table
	//So we know "happy birthday email has been send. 
	//
	public void addEmailSentHistory(EmailTemplate emailTempalte, HashMap<String,Object> member){
		String eamil=null;
	    int email_template_id=0;
		String task_name=null; //happy-birthday-email
		String schedule_type=null; // daily
		String email_title=null;//Happy Birthday Fan!
		Date send_at=null;
		
		if (emailTempalte==null ) {
			loger.error("emailTempalte is null");
			return;
		}
		if (member==null || member.get("email")==null ) {
			loger.error("column email doesn't exist or null value");
			return;
		}
		eamil=member.get("email").toString();
		email_template_id=emailTempalte.getId();
		task_name=emailTempalte.getTask_name();
		schedule_type=emailTempalte.getSchedule_type();
		email_title=emailTempalte.getEmail_title();
		
		email_title=doRepalce(email_title,member);
		
		String sql="insert into email_sent_history(email, email_template_id, task_name, schedule_type, email_title,send_at) values (";
		sql +="'" + eamil + "'";
		sql += "," + email_template_id;
		sql += ",'" + task_name + "'";
		sql += ",'" + schedule_type + "'";
		sql += ",'" + email_title + "'";
		sql += ",sysdate()) ";
		Statement stmt = null;
		loger.debug("addEmailSentHistory sql=" + sql);
		try {
			stmt = this.conn.createStatement();
			stmt.executeUpdate(sql);
			loger.info(eamil + " send " + email_title + " " +  schedule_type);
					
		} catch (SQLException se) {
			loger.error(se);
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			loger.error(e);
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
			}// do nothing
		}

	}
	
	//column=last_send_at,last_finish_at
	//public void updateEmailJobScheduleStatus(String column,EmailTemplate emailTempalte) {
	//	updateEmailJobScheduleStatus(column,emailTempalte);
	//}
	
	private void updateEmailJobScheduleStatus(String column,EmailTemplate emailTempalte) {
		Statement stmt = null;
		String sql ="update email_template set last_send_at=sysdate() where id=" + emailTempalte.getId();
		if ("last_finish_at".equalsIgnoreCase(column))
			sql ="update email_template set last_finish_at=sysdate() where id=" + emailTempalte.getId();
		
		try {
			stmt = this.conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException se) {
			loger.error(se);
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			loger.error(e);
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se) {
			}// do nothing
		}
	}
	
    @Override
    protected void finalize() throws Throwable
    {
    	//disConnect();
    }	
}
