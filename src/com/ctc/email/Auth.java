package com.ctc.email;

import com.ctc.email.entity.EmailTemplate;

public class Auth {
	private String userName=null;
	private String password=null;
	public Auth(EmailTemplate template ) {
		if (template!=null) {
			userName=template.getSmtp_username();
			password=template.getSmtp_password();
		}
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
