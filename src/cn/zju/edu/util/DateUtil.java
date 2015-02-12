package cn.zju.edu.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.jaret.util.date.JaretDate;

public class DateUtil {
	static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs
	
	public static Date formatTime(String time, String format)  throws ParseException
	{
			SimpleDateFormat df = new SimpleDateFormat(format);
			return df.parse(time);
	}
	
	public static Date formatTime(String time)  throws ParseException
	{
			return formatTime(time, "yyyy-MM-dd HH:mm:ss.SSS");
	}	
	
	public static String fromDate(Date date, String format)
	{
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}
	
	public static double calcInterval(String t1, String t2, String format) throws Exception
	{
		return (formatTime(t2, format).getTime() - formatTime(t1, format).getTime()) * 1.0 / 1000;
	}
	
	public static double calcInterval(String t1, String t2) throws Exception
	{
		return calcInterval(t1, t2, "yyyy-MM-dd HH:mm:ss.SSS");
	}

	public static double compareNow(String t) throws Exception
	{
		Date now = new Date();
		return (now.getTime() - formatTime(t).getTime() * 1.0) / 1000;
	}
	
	@SuppressWarnings("deprecation")
	public static JaretDate toJaretDate(String t, String format) throws Exception
	{
		return new JaretDate(formatTime(t, format));
		//Date d = formatTime(t, format);
		//jdate.setDateTime(d.getYear(), d.getMonth(), d.getDay(), d.getHours(), d.getMinutes(), d.getSeconds());
	}
	
	@SuppressWarnings("deprecation")
	public static JaretDate toJaretDate(String t) throws Exception
	{
		return toJaretDate(t, "yyyy-MM-dd HH:mm:ss.SSS");
	}
	
	public static String getBeforeTime(int minute)
	{
		Date d = new Date();
		long t = d.getTime();
		Date before = new Date(t - (minute * ONE_MINUTE_IN_MILLIS));
		
		return fromDate(before, "yyyy-MM-dd HH:mm:ss.SSS");
	}
	
	public static void main(String args[]) throws Exception
	{
		System.out.println(calcInterval("2015-02-02 19:56:14.210", "2015-02-02 19:57:30.110", "yyyy-MM-dd HH:mm:ss.SSS"));
		//System.out.println(dbTime2RFCTime("2015-02-02 15:23:41.323"));
	}
}
