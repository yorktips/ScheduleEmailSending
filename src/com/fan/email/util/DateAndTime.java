package com.fan.email.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAndTime {

	static final long ONE_HOUR = 60 * 60 * 1000L;

	public static long daysBetween(Date d1, Date d2) {
		if (d1==null && d2==null)
			return 0;
		
		return hoursBetween(d1,d2) / 24;
	}
	
	public static String getCurrentHHMM(){
		Format formatter = new SimpleDateFormat( "hhmm" );
		//System.out.println( formatter.format( new Date() ) );
		long time = System.currentTimeMillis();
		return formatter.format( new Date( time ) );
	}
	
	public static long hoursBetween(Date d1, Date d2) {
		if (d1==null && d2==null)
			return 0;
		
		return minutesBetween(d1, d2)/60;
	}

	public static long minutesBetween(Date d1, Date d2) {
		if (d1==null)
			return ((d2.getTime()  + ONE_HOUR) / (ONE_HOUR / 60));
		if (d2==null)
			return (( - d1.getTime() + ONE_HOUR) / (ONE_HOUR / 60));
		
		return ((d2.getTime() - d1.getTime() + ONE_HOUR) / (ONE_HOUR / 60));
	}
	
	public static Date toDate(Object o){
		if (o==null)
			return null;
		if (o instanceof Date)
			return (Date)o;
		
		try{
			StringToDateUtil util= new StringToDateUtil();
			return util.stringToDate(o.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
