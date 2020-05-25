package cn.horncomb.framework.util;

import cn.horncomb.framework.web.rest.errors.CustomParameterizedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static String getWeekOfDate(Date date) {
        /**
         *
         * 功能描述: 根据日期获取星期几
         *
         * @auther: lkj
         * @date: 2018/4/3 下午1:46
         * @param: [date]
         * @return: java.lang.String
         *
         */
        String[] weekDays = {"7", "1", "2", "3", "4", "5", "6"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    public static String getWeekStrOfDate(Date date) {
        /**
         *
         * 功能描述: 根据日期获取星期几
         *
         * @auther: lkj
         * @date: 2018/4/3 下午1:46
         * @param: [date]
         * @return: java.lang.String
         *
         */
        String[] weekDays = {"周天", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    public static String getDescribeOfDate(Date date){
        String timeDescribe = "";
        SimpleDateFormat df = new SimpleDateFormat("HH");
        String str = df.format(date);
        int a = Integer.parseInt(str);
        if (a >= 0 && a <= 6) {
            timeDescribe = "凌晨";
        }
        if (a > 6 && a <= 12) {
            timeDescribe = "上午";
        }
        if (a > 12 && a <= 13) {
            timeDescribe = "中午";
        }
        if (a > 13 && a <= 18) {
            timeDescribe = "下午";
        }
        if (a > 18 && a <= 24) {
            timeDescribe = "晚上";
        }
        return timeDescribe;
    }
    public static String getStrOfDate(Date date,String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static Date getDateOfStr(String dateStr,String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new CustomParameterizedException("日期格式不正确！");
        }
    }
    public static int getDifferMinutes(Date date1,Date date2){
        long diff = (date2.getTime()-date1.getTime())/1000/60;
        return Integer.valueOf(""+diff);
    }
    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     * @param date1
     * @param date2
     * @return
     */
    public static int getDifferDays(Date date1,Date date2){
        int a= (int) (date2.getTime()-date1.getTime());
        if(a<=0){
            return 0;
        }
        int b = 1000*3600*24;
        int days = a%b == 0 ? (a/b) : (a/b)+1;
        return days;
    }
    public static boolean compareTimeGT(Date time1,Date time2){
        return time1.getTime()>time2.getTime();
    }
    public static boolean compareTimeGE(Date time1,Date time2){
        return time1.getTime()>=time2.getTime();
    }
    public static boolean compareTimeNE(Date time1,Date time2){
        return time1.getTime()!=time2.getTime();
    }
    public static boolean compareTimeEQ(Date time1,Date time2){
        return time1.getTime()==time2.getTime();
    }
    public static boolean compareTimeLT(Date time1,Date time2){
        return time1.getTime()<time2.getTime();
    }
    public static boolean compareTimeLE(Date time1,Date time2){
        return time1.getTime()<=time2.getTime();
    }
    public static String getCurTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }
    public static String getDayLastTime(){
        return "23:59:59";
    }

    public static Date getYyyyMmDdDate(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int month = c.get(Calendar.MONTH)+1;
        String monthStr = month>9?""+month:"0"+month;
        String dayStr = c.get(Calendar.DATE)>9?""+c.get(Calendar.DATE):"0"+c.get(Calendar.DATE);
        String dateStr = c.get(Calendar.YEAR)+"-"+monthStr+"-"+dayStr;
        return getDateOfStr(dateStr,"yyyy-MM-dd");
    }

    public static String getAge(Date birthDay){
        if(birthDay==null){
            return null;
        }
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        int age = yearNow - yearBirth;   //计算整岁数
        int month = 0;
        if (monthBirth < monthNow) {
            month = monthNow-monthBirth;
        }else if(monthBirth>monthNow){
            month = 12+monthNow-monthBirth;
            age--;
        }
        if(month>0&&age<=14){
            return age+"岁"+month+"个月";
        }else if(age<=0){
            return "0岁";
        }else{
            return age+"岁";
        }
    }

    public static Integer getAgeInt(Date birthDay){
        int age = 0;
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        age = yearNow - yearBirth;   //计算整岁数
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;//当前日期在生日之前，年龄减一
            } else {
                age--;//当前月份在生日之前，年龄减一
            }
        }
        return age;
    }

    /**
     * 获取当前日期Cal，格式：yyyy-MM-dd
     * @return
     */
    public static Calendar getCurDateCal(){
        Calendar c = Calendar.getInstance();
        c.setTime(getYyyyMmDdDate(new Date()));
        return c;
    }
}
