"# ScheduleEmailSending" 

-- 1. Create tables
-- Create one if not exist
create table student(
   id INT NOT NULL AUTO_INCREMENT,
   firstname VARCHAR(100) NOT NULL,
   lastname VARCHAR(100) NOT NULL,
   sex      VARCHAR(1) NULL,
   dateOfBirth     DATE NULL,
   email VARCHAR(100) NOT NULL,
   studyPermExpire  DATE,
   VisaExpire  DATE,
   title VARCHAR(250) NULL,
   code VARCHAR(50) NULL,
   PRIMARY KEY ( id )
);

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
    
   
 -- 2. Prepare data for email sending
 
--select to_char(id) as id, first_name, last_name, sex from student order by id;
--schedule_type: hourly, daily, weekly, monthly, yearly
--email_body_type: text or html
--send_to_list: fan2@gmail;[[email]]
-- If you use a variable , such as [[email]], send_to_type must be "sql"
-- if send_to_type is "sql", you must have send_to_sql
--Need to set Windows task schedule run every minute if schedule_time is set. Example 1130
--run "java -jar emailsending.jar" to start the email sending task

-- 2.1. StudyPermExpire Check
select CONVERT(id, CHAR) as id, title, firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(studyPermExpire, char) as studyPermExpire , TIMESTAMPDIFF(DAY,  sysdate(),studyPermExpire) as expiredWithin  
	from tblstudent
	  where studyPermExpire is not null 
		and studyPermExpire >'0000-00-00' 
        -- and TIMESTAMPDIFF(MONTH,  sysdate(),studyPermExpire) <3
		and  TIMESTAMPDIFF(DAY,  sysdate(),studyPermExpire) <90
		and email not in (select email from email_sent_history
					where task_name='StudyPermExpire-check-email'
					and  TIMESTAMPDIFF(MONTH,  send_at, sysdate()) <=90 );

insert into email_template (smtp_host,smtp_port,smtp_username,smtp_password,task_name, send_from,schedule_type,
        schedule_time,send_to_type,send_to_list,send_cc,send_to_sql,
	   email_title,email_body_type,email_template,last_send_at)
  values(
    'smtp.gmail.com',
    null,
    'fan@gmail.com',
    'password',	
    'StudyPermExpire-check-email',
	'infor@ctc.ca',
	'daily',
	'2300',
	'sql',
	'[[email]];fan@gmail.com',
	'fan2@gmail.com',
 'select CONVERT(id, CHAR) as id, title, firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(studyPermExpire, char) as studyPermExpire , TIMESTAMPDIFF(DAY,  sysdate(),studyPermExpire) as expiredWithin  from tblstudent where studyPermExpire is not null and studyPermExpire >0 and  TIMESTAMPDIFF(DAY,  sysdate(),studyPermExpire) <90 and email not in (select email from email_sent_history where task_name=''StudyPermExpire-check-email'' and  TIMESTAMPDIFF(MONTH,  send_at, sysdate()) <=90 )',
	'Your Study Perm Expired at [[studyPermExpire]]!',
	'html',
	'Dear [[first_name]] [[last_name]], Your Study Perm Expired at [[studyPermExpire]]!',
	sysdate());
commit;


-- 2.2. VisaExpire Check
select CONVERT(id, CHAR) as id, title, firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(VisaExpire, char) as VisaExpire , TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) as VisaExpire  
	from tblstudent
	  where VisaExpire is not null 
		and VisaExpire >'0000-00-00' 
        -- and TIMESTAMPDIFF(MONTH,  sysdate(),VisaExpire) <3
		and  TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) <3
		and email not in (select email from email_sent_history
					where task_name='VisaExpire-check-email'
					and  TIMESTAMPDIFF(MONTH,  send_at, sysdate()) <=3 );


insert into email_template (smtp_host,smtp_port,smtp_username,smtp_password,task_name, send_from,schedule_type,
        schedule_time,send_to_type,send_to_list,send_cc,send_to_sql,
	   email_title,email_body_type,email_template,last_send_at)
  values(
    'smtp.gmail.com',
    null,
    'fan@gmail.com',
    'password',	
    'VisaExpire-check-email',
	'infor@ctc.ca',
	'daily',
	'2300',
	'sql',
	'[[email]];fan@gmail.com',
	'fan2@gmail.com',
 'select CONVERT(id, CHAR) as id, title, firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(VisaExpire, char) as VisaExpire , TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) as expiredWithin  from tblstudent where VisaExpire is not null and VisaExpire >0 and  TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) <90 and email not in (select email from email_sent_history where task_name=''VisaExpire-check-email'' and  TIMESTAMPDIFF(MONTH,  send_at, sysdate()) <=90 )',
	'Your Visa Perm Expired at [[VisaExpire]]!',
	'html',
	'Dear [[first_name]] [[last_name]], Your Visa Perm Expired at [[VisaExpire]]!',
	sysdate());
    
commit;


-- 2.3. Birthday Check
select CONVERT(id, CHAR) as id, title, firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(VisaExpire, char) as VisaExpire , TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) as VisaExpire  
	from tblstudent
	  where dateOfBirth is not null 
		and dateOfBirth >'0000-00-00' 
		and  TIMESTAMPDIFF(DAY,  sysdate(),dateOfBirth) =1
		and email not in (select email from email_sent_history
					where task_name='happy-birthday-email'
					and  TIMESTAMPDIFF(DAY,  send_at, sysdate()) <369 );

insert into email_template (smtp_host,smtp_port,smtp_username,smtp_password,task_name, send_from,schedule_type,
        schedule_time,send_to_type,send_to_list,send_cc,send_to_sql,
	   email_title,email_body_type,email_template,last_send_at)
  values(
    'smtp.gmail.com',
    null,
    'fan@gmail.com',
    'password',	
    'happy-birthday-email',
	'infor@ctc.ca',
	'daily',
	'2300',
	'sql',
	'[[email]];fan@gmail.com',
	'fan2@gmail.com',
    'select CONVERT(id, CHAR) as id, title, firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email from tblstudent where dateOfBirth is not null and dateOfBirth >0 and  TIMESTAMPDIFF(DAY,  sysdate(),dateOfBirth) =1 and email not in (select email from email_sent_history where task_name=''happy-birthday-email'' and  TIMESTAMPDIFF(DAY,  send_at, sysdate()) < 360)',
	'Happy Birthday [[first_name]]!',
	'html',
	'Dear [[first_name]] [[last_name]], Wish you have a happy birthday!',
	sysdate());
commit;
