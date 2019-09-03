"# ScheduleEmailSending" 

-- 0. Create function FUN_THIS_YEAR_BIRTHDAY
drop function     FUN_THIS_YEAR_BIRTHDAY;

DELIMITER $$
CREATE FUNCTION FUN_THIS_YEAR_BIRTHDAY(dateOfBirth Date)
    RETURNS DATE
    BEGIN
        DECLARE sDate CHAR(25);
		if dateOfBirth is null then
			return convert('0000-00-00',DATE);
		END IF;
		if CONVERT(dateOfBirth, CHAR) ='0000-00-00' then
			return convert('0000-00-00',DATE);
		END IF;
		
		SET sDate=concat(SUBSTRING(convert(sysdate(),char), 1, 5),  SUBSTRING(convert(dateOfBirth,char), 6, 5));
		return convert(sDate,DATE);
    END $$

DELIMITER ; 

DROP FUNCTION checkEmailTask;

DELIMITER $$
CREATE FUNCTION checkEmailTask(taskType CHAR(12), schedule_time VARCHAR(20),last_send_at DATETIME ) RETURNS VARCHAR(1)
BEGIN
        DECLARE sDate VARCHAR(25);
	IF LOWER(taskType)='hourly' THEN
		IF (last_send_at IS NULL OR TIMESTAMPDIFF(HOUR,  last_send_at, SYSDATE()) >=1)
			-- AND (schedule_time is null 
			--	or CONVERT(DATE_FORMAT(SYSDATE(), "%H%i"),UNSIGNED) >= CONVERT(schedule_time,UNSIGNED))  
		THEN
			RETURN 'Y';
		END IF;
	END IF;
		
	IF LOWER(taskType)='daily' THEN
		IF (last_send_at IS NULL OR TIMESTAMPDIFF(DAY,  last_send_at, SYSDATE()) >=1) 
		THEN
			RETURN 'Y';
		END IF;
	ELSEIF (LOWER(taskType)='weekly') THEN
		IF (last_send_at IS NULL OR TIMESTAMPDIFF(DAY,  last_send_at, SYSDATE()) >=7) 
		THEN
			RETURN 'Y';
		END IF;
	ELSEIF LOWER(taskType)='monthly' THEN
		IF (last_send_at IS NULL OR TIMESTAMPDIFF(MONTH,  last_send_at, SYSDATE()) >=1) 
		THEN
			RETURN 'Y';
		END IF;
	ELSEIF LOWER(taskType)='yearly' THEN
		IF (last_send_at IS NULL OR TIMESTAMPDIFF(YEAR,  last_send_at, SYSDATE()) >=1) 
		THEN
			RETURN 'Y';
		END IF;
	ELSEIF LOWER(taskType)='onetime' THEN
		IF last_send_at IS NULL  
			-- AND (schedule_time IS NULL 
			-- 	OR CONVERT(DATE_FORMAT(SYSDATE(), "%H%i"),UNSIGNED) >= CONVERT(schedule_time,UNSIGNED))  
		THEN
			RETURN 'Y';
		END IF;
	END IF;
		
	RETURN 'N';
END $$

DELIMITER ; 


DROP FUNCTION FUN_BEGEIN_WITH_NUMBER;

DELIMITER $$
CREATE FUNCTION FUN_BEGEIN_WITH_NUMBER(val VARCHAR(250))
    RETURNS VARCHAR(250)    
BEGIN
        DECLARE sRet VARCHAR(250);
        SET sRet=val;
	IF (val NOT REGEXP '^[0-9]+$') THEN
		RETURN '';
	ELSE
		RETURN val;
	END IF;
END $$

DELIMITER ; 

-- 1. Create tables
-- Create one if not exist
create table student(
   id INT NOT NULL AUTO_INCREMENT,
   firstname VARCHAR(100) NOT NULL,
   lastname VARCHAR(100) NOT NULL,
   sex      VARCHAR(1) NULL,
   oen VARCHAR(50) NULL,
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
   task_name VARCHAR(100) NOT NULL,  -- email task name, it will be shown as the job name
   send_from VARCHAR(100) NOT NULL, -- usually it is same as smtp_username,depends on the SMTP configuration. 
   send_cc VARCHAR(100)  NULL,
   send_bcc VARCHAR(100) NULL,
   email_body_type VARCHAR(10) NULL,  -- html or text
   schedule_type VARCHAR(20) NOT NULL,  -- hourly, daily, weekly, monthly, yearly, onetime 
   schedule_time VARCHAR(20) NOT NULL, -- when to send the email. 
   send_to_type VARCHAR(20) NOT NULL,  -- how to get the email list. "sql" or just leave empty 
   send_to_list VARCHAR(4000) NULL, -- use email(fanw@gmail.com) directly or variable<#email#> 
   send_to_sql VARCHAR(4000) NULL,  -- you must include "email" in a sql select 
   enabled TINYINT(1) null default 1, 
   email_title VARCHAR(250) NOT NULL,
   email_template VARCHAR(4000) NOT NULL,
   last_send_at  DATETIME null,
   last_finish_at  DATETIME null,
   PRIMARY KEY ( id )
);

drop table email_sent_history;
create table email_sent_history( 
	id INT NOT NULL AUTO_INCREMENT, 
	email VARCHAR(100) NOT NULL, 
    email_template_id INT ,
	task_name VARCHAR(100) NOT NULL, -- happy-birthday-email
	schedule_type VARCHAR(20) NOT NULL, -- daily
	email_title VARCHAR(100) NOT NULL, -- Happy Birthday Fan!
	send_at DATETIME null, 
    PRIMARY KEY ( id ));
    
   
 -- 2. Prepare data for email sending
 -- delete from email_sent_history;
-- update email_template set send_to_list=null, send_cc=null where id>1;
-- update email_template set email_title=REPLACE(email_title, '<#last_name#>', '<#lastName#>');
-- commit;

-- select * from email_sent_history;
-- select * from email_template;
--select to_char(id) as id, first_name, last_name, sex from student order by id;
--schedule_type: hourly, daily, weekly, monthly, yearly. Or just set NULL and controled by Task Schedule time in Windows.
--email_body_type: text or html
--send_to_list: fan2@gmail;<#email#>
-- If you use a variable , such as <#email#>, send_to_type must be "sql"
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
	'[[email]],fan@gmail.com',
	'fan2@gmail.com',
 'select CONVERT(id, CHAR) as id, title, concat(firstName, LastName) as name,firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(studyPermExpire, char) as studyPermExpire , TIMESTAMPDIFF(DAY,  sysdate(),studyPermExpire) as expiredWithin  from tblstudent where studyPermExpire is not null and studyPermExpire >0 and  TIMESTAMPDIFF(DAY,  sysdate(),studyPermExpire) <90 and email not in (select email from email_sent_history where task_name=''StudyPermExpire-check-email'' and  TIMESTAMPDIFF(MONTH,  send_at, sysdate()) <=90 )',
	'Your Study Perm Expired at [[studyPermExpire]]!',
	'html',
	'Dear [[first_name]] [[last_name]], Your Study Perm Expired at [[studyPermExpire]]!',
	sysdate());
commit;


-- 2.2. VisaExpire Check
select CONVERT(id, CHAR) as id, title, concat(firstName, LastName) as name, firstName, LastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(VisaExpire, char) as VisaExpire , TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) as VisaExpire  
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
	'[[email]],fan@gmail.com',
	'fan2@gmail.com',
 'select CONVERT(id, CHAR) as id, title, firstName, lastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(VisaExpire, char) as VisaExpire , TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) as expiredWithin  from tblstudent where VisaExpire is not null and VisaExpire >0 and  TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) <90 and email not in (select email from email_sent_history where task_name=''VisaExpire-check-email'' and  TIMESTAMPDIFF(MONTH,  send_at, sysdate()) <=90 )',
	'Your Visa Perm Expired at [[VisaExpire]]!',
	'html',
	'Dear [[first_name]] [[last_name]], Your Visa Perm Expired at [[VisaExpire]]!',
	sysdate());
    
commit;


-- 2.3. Birthday Check
select CONVERT(id, CHAR) as id, title, firstName, lastName, CONVERT(dateOfBirth, char) as dateOfBirth , email, CONVERT(VisaExpire, char) as VisaExpire , TIMESTAMPDIFF(DAY,  sysdate(),VisaExpire) as VisaExpire  
	from tblstudent
	  where dateOfBirth is not null 
		and dateOfBirth >'0000-00-00' 
		and  TIMESTAMPDIFF(DAY,  FUN_THIS_YEAR_BIRTHDAY(sysdate()),FUN_THIS_YEAR_BIRTHDAY(dateOfBirth)) =1
		and email not in (select email from email_sent_history
					where task_name='happy-birthday-email'
					and  TIMESTAMPDIFF(DAY,  send_at, sysdate()) <364 );

insert into email_template (smtp_host,smtp_port,smtp_username,smtp_password,task_name, send_from,schedule_type, schedule_time,send_to_type,send_to_list,send_cc,send_to_sql, email_title,email_body_type,email_template,last_send_at) values( 'smtp.gmail.com', '456', 'fan@gmail.com', 'password', 'happy-birthday-email', 'infor@ctc.ca', 'daily', '2300', 'sql', '[[email]],fan@gmail.com', 'fan2@gmail.com', 'select CONVERT(id, CHAR) as id, title, firstName, lastName, CONVERT(dateOfBirth, char) as dateOfBirth , email from tblstudent where dateOfBirth is not null and dateOfBirth >0 and TIMESTAMPDIFF(DAY, FUN_THIS_YEAR_BIRTHDAY(sysdate()),FUN_THIS_YEAR_BIRTHDAY(dateOfBirth)) =1 and email not in (select email from email_sent_history where task_name=''happy-birthday-email'' and TIMESTAMPDIFF(DAY, send_at, sysdate()) < 360)', 'Happy Birthday [[first_name]]!', 'html', 'Dear [[first_name]] [[last_name]], Wish you have a happy birthday!', sysdate()); 
commit;


-- 2.4. About Student Password and Email
insert into email_template (smtp_host,smtp_port,smtp_username,smtp_password,task_name, send_from,schedule_type,
        schedule_time,send_to_type,send_to_list,send_cc,send_to_sql,
	   email_title,email_body_type,email_template,last_send_at)
  values(
    'smtp.gmail.com',
    '456',
    'fan@gmail.com',
    'password',	
    'happy-birthday-email',
	'infor@ctc.ca',
	'daily',
	'2300',
	'sql',
	'[[email]],fan@gmail.com',
	'fan2@gmail.com',
    'select CONVERT(id, CHAR) as studid, title, firstName || \' \'||lastName as studentname, oen as prtcode,email as password, email as email, CONVERT(dateOfBirth, char) as dateOfBirth from tblstudent where email is not null and oen is not null ',
	'About Student Password and Email',
	'html',
	'C:\\fan\\StudentPasswordAndIDEmail.html',
	sysdate());
commit;

UPDATE email_template SET send_to_sql='SELECT CONVERT(id, CHAR) AS studid, title, CONCAT(CONCAT(firstName ,\' \'),lastName) AS studentname, postalcode AS prtcode,password AS `password`, email AS `email`, CONVERT(dateOfBirth, CHAR) AS dateOfBirth FROM tblstudent WHERE email IS NOT NULL AND id=201500092 ';
COMMIT;


                   
