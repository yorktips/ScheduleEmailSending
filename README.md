"# ScheduleEmailSending" 


drop table email_template;


create table student(
   id INT NOT NULL AUTO_INCREMENT,
   first_name VARCHAR(100) NOT NULL,
   last_name VARCHAR(100) NOT NULL,
   sex      VARCHAR(1) NULL,
   birthday     DATE NULL,
   enteringDate  DATE,
   nationality VARCHAR(250) NULL,
   code VARCHAR(50) NULL,
   PRIMARY KEY ( id )
);



--select to_char(id) as id, first_name, last_name, sex from student order by id;
--schedule_type: hourly, daily, weekly, monthly, yearly
--email_body_type: text or html
--send_to_list: fan2@gmail;[[email]]
-- If you use a variable , such as [[email]], send_to_type must be "sql"
-- if send_to_type is "sql", you must have send_to_sql
--Need to set Windows task schedule run every minute if schedule_time is set. Example 1130
--run "java -jar emailsending.jar" to start the email sending task

select CONVERT(id, CHAR) as id, title, firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(studyPermExpire, char) as studyPermExpire , TIMESTAMPDIFF(DAY,  sysdate(),studyPermExpire) as expiredWithin  
	from tblstudent
	  where studyPermExpire is not null 
		and studyPermExpire >'0000-00-00' 
        -- and TIMESTAMPDIFF(MONTH,  sysdate(),studyPermExpire) <3
		and  TIMESTAMPDIFF(DAY,  sysdate(),studyPermExpire) <90;
		
drop table email_template;
create table email_template(
   id INT NOT NULL AUTO_INCREMENT,
   smtp_host VARCHAR(100) NULL,
   smtp_port VARCHAR(6)  NULL,
   smtp_username VARCHAR(24) NULL,
   smtp_password VARCHAR(24) NULL,
   task_name VARCHAR(100) NOT NULL,
   send_from VARCHAR(100) NOT NULL,
   send_cc VARCHAR(100)  NULL,
   send_bcc VARCHAR(100) NULL,
   email_body_type VARCHAR(10) NULL,
   schedule_type VARCHAR(20) NOT NULL, 
   schedule_time VARCHAR(20) NOT NULL,
   send_to_type VARCHAR(20) NOT NULL,
   send_to_list VARCHAR(4000) NULL,
   send_to_sql VARCHAR(4000) NULL,
   enabled TINYINT(1) null default 1,
   email_title VARCHAR(100) NOT NULL,
   email_template VARCHAR(4000) NOT NULL,
   last_send_at  DATE null,
   last_finish_at  DATE null,
   PRIMARY KEY ( id )
);



drop table email_sent_history;
create table email_sent_history( 
	id INT NOT NULL AUTO_INCREMENT, 
	eamil VARCHAR(100) NOT NULL, 
    email_template_id INT ,
	task_name VARCHAR(100) NOT NULL, -- happy-birthday-email
	schedule_type VARCHAR(20) NOT NULL, -- daily
	email_title VARCHAR(100) NOT NULL, -- Happy Birthday Fan!
	send_at DATE null, 
    PRIMARY KEY ( id ));
    

insert into email_template (smtp_host,smtp_port,smtp_username,smtp_password,task_name, send_from,schedule_type,
                           schedule_time,send_to_type,send_to_list,send_cc,send_to_sql,
						   email_title,email_body_type,email_template,last_send_at)
  values(
    'smtp.gmail.com',
    null,
    'fan@gmail.com',
    'password',	
    'happy-birthday-email2',
	'infor@ctc.ca',
	'daily',
	'2300',
	'sql',
	'[[email]];fan@gmail.com',
	'fan2@gmail.com',
	'select * from student where email is not null  and DATE_FORMAT(birthday,''%m%d'') = DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 2 DAY),''%m%d'' )'
	'Happy Birthday [[first_name]]!',
	'html',
	'Dear [[first_name]] [[last_name]], Wish you have a happy birthday!',
	sysdate());
	
commit;
