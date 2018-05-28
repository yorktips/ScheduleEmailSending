package com.fan.email.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StringToDateUtil {


	private List<SimpleDateFormat> sdfList;

	public static String readFileAsString(String fileName) { 
		String text = ""; 
		try { 
			text = new String(Files.readAllBytes(Paths.get(fileName))); 
		} catch (IOException e) { 
			e.printStackTrace(); 
		} 
		return text; 
	}

		
	public StringToDateUtil() {
		sdfList=  new ArrayList<SimpleDateFormat>();
		sdfList.add(new SimpleDateFormat("dd MMM yyyy"));
		sdfList.add(new SimpleDateFormat("M/dd/yyyy"));
		sdfList.add(new SimpleDateFormat("yyyy-MM-dd"));   
		sdfList.add(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
		sdfList.add(new SimpleDateFormat("M/dd/yyyy hh:mm:ss a"));
	}
	
	// set the List of Format's you want to use
	public StringToDateUtil(List<SimpleDateFormat> sdfList) throws Exception {
		if (sdfList == null)
			throw new Exception("sdfList can't be null");
		this.sdfList = sdfList;
	}

	public Date stringToDate(String str) throws Exception {
		if (str == null)
			return null;
		Date date = null;
		// parse the input String with list of SimpleDateFormats we have
		for (SimpleDateFormat sdf : sdfList) {
			try {
				date = sdf.parse(str);
			} catch (ParseException pe) {
				// do nothing, we need to try other format
			}
			// check if parsed successfully?
			if (date != null)
				break;
		}
		// return date if parsed successfully or else throw exception
		if (date != null)
			return date;
		throw new Exception("invalid format for String:" + str);
	}

	public static void main(String args[]) throws Exception {
		
		String fa=StringToDateUtil.readFileAsString("C:\\fa\\x.txt");
		System.out.println(fa);
		List<SimpleDateFormat> formatList = new ArrayList<SimpleDateFormat>();
		formatList.add(new SimpleDateFormat("dd MMM yyyy"));
		formatList.add(new SimpleDateFormat("M/dd/yyyy"));
		formatList.add(new SimpleDateFormat("M/dd/yyyy hh:mm:ss a"));
		
		StringToDateUtil sdUtil = new StringToDateUtil(formatList);
		// Lets format some String to Date
		
		String[] arr = { "10 NOV 2012", "10/14/2012", "10/14/2012 10:45:30", "ABC", null };
		for (String str : arr) {
			try {
				System.out.println(str + " Date Object = " + sdUtil.stringToDate(str));
			} catch (Exception e) {
				System.out.println(str + " is not a valid date");
			}
		}
	}

}