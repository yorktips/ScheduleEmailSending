package com.ctc.email.entity;


import java.sql.Date;

public class EmailTemplate {
	private int id;
	private String smtp_host;
	private String smtp_port;
	private String smtp_username;
	private String smtp_password;
	private String email_body_type;
	private String task_name;
	private String send_from;
	private String send_cc;
	private String send_bcc;
	private String schedule_type;
	private String schedule_time;
	private String send_to_type;
	private String send_to_list;
	private String send_to_sql;
	private boolean enabled;
	private String email_title;
	private String email_template;
	private Date last_send_at;
	private Date last_finish_at;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSmtp_host() {
		return smtp_host;
	}
	public void setSmtp_host(String smtp_host) {
		this.smtp_host = smtp_host;
	}
	public String getSmtp_port() {
		return smtp_port;
	}
	public void setSmtp_port(String smtp_port) {
		this.smtp_port = smtp_port;
	}
	public String getSmtp_username() {
		return smtp_username;
	}
	public void setSmtp_username(String smtp_username) {
		this.smtp_username = smtp_username;
	}
	public String getSmtp_password() {
		return smtp_password;
	}
	public void setSmtp_password(String smtp_password) {
		this.smtp_password = smtp_password;
	}
	public String getEmail_body_type() {
		return email_body_type;
	}
	public void setEmail_body_type(String email_body_type) {
		this.email_body_type = email_body_type;
	}
	public String getTask_name() {
		return task_name;
	}
	public void setTask_name(String task_name) {
		this.task_name = task_name;
	}
	public String getSend_from() {
		return send_from;
	}
	public void setSend_from(String send_from) {
		this.send_from = send_from;
	}
	public String getSend_cc() {
		return send_cc;
	}
	public void setSend_cc(String send_cc) {
		this.send_cc = send_cc;
	}
	public String getSend_bcc() {
		return send_bcc;
	}
	public void setSend_bcc(String send_bcc) {
		this.send_bcc = send_bcc;
	}
	public String getSchedule_type() {
		return schedule_type;
	}
	public void setSchedule_type(String schedule_type) {
		this.schedule_type = schedule_type;
	}
	public String getSchedule_time() {
		return schedule_time;
	}
	public void setSchedule_time(String schedule_time) {
		this.schedule_time = schedule_time;
	}
	public String getSend_to_type() {
		return send_to_type;
	}
	public void setSend_to_type(String send_to_type) {
		this.send_to_type = send_to_type;
	}
	public String getSend_to_list() {
		return send_to_list;
	}
	public void setSend_to_list(String send_to_list) {
		this.send_to_list = send_to_list;
	}
	public String getSend_to_sql() {
		return send_to_sql;
	}
	public void setSend_to_sql(String send_to_sql) {
		this.send_to_sql = send_to_sql;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getEmail_title() {
		return email_title;
	}
	public void setEmail_title(String email_title) {
		this.email_title = email_title;
	}
	public String getEmail_template() {
		return email_template;
	}
	public void setEmail_template(String email_template) {
		this.email_template = email_template;
	}
	public Date getLast_send_at() {
		return last_send_at;
	}
	public void setLast_send_at(Date last_send_at) {
		this.last_send_at = last_send_at;
	}
	public Date getLast_finish_at() {
		return last_finish_at;
	}
	public void setLast_finish_at(Date last_finish_at) {
		this.last_finish_at = last_finish_at;
	}
	@Override
	public String toString() {
		return "EmailTemplate [id=" + id + ", smtp_host=" + smtp_host
				+ ", smtp_port=" + smtp_port + ", smtp_username="
				+ smtp_username + ", smtp_password=" + smtp_password
				+ ", email_body_type=" + email_body_type + ", task_name="
				+ task_name + ", send_from=" + send_from + ", send_cc="
				+ send_cc + ", send_bcc=" + send_bcc + ", schedule_type="
				+ schedule_type + ", schedule_time=" + schedule_time
				+ ", send_to_type=" + send_to_type + ", send_to_list="
				+ send_to_list + ", send_to_sql=" + send_to_sql + ", enabled="
				+ enabled + ", email_title=" + email_title
				+ ", email_template=" + email_template + ", last_send_at="
				+ last_send_at + ", last_finish_at=" + last_finish_at + "]";
	}


	
}