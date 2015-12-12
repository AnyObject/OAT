/*
    Open Auto Trading : A fully automatic equities trading platform with machine learning capabilities
    Copyright (C) 2015 AnyObject Ltd.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package OAT.util;

/**
 *
 * @author Antonio Yip
 */
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import OAT.trading.Main;

/**
 *
 * @author Antonio Yip
 */
public class DateUtil {

    public static final long SECOND_TIME = 1000;
    public static final long HALF_MINUTE_TIME = 30 * SECOND_TIME;
    public static final long MINUTE_TIME = 60 * SECOND_TIME;
    public static final long FIVE_MINUTE_TIME = 5 * MINUTE_TIME;
    public static final long TEN_MINUTE_TIME = 10 * MINUTE_TIME;
    public static final long FIFTEEN_MINUTE_TIME = 15 * MINUTE_TIME;
    public static final long HOUR_TIME = 60 * MINUTE_TIME;
    public static final long DAY_TIME = 24 * HOUR_TIME;
    public static final long WEEK_TIME = 7 * DAY_TIME;
    public static final long MONTH_TIME = 30 * DAY_TIME;
    public static final long QUARTER_TIME = 91 * DAY_TIME;
    public static final long YEAR_TIME = 365 * DAY_TIME;
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String SIMPLE_DATE_FORMAT = "yyyyMMdd";
    public static final String MINUTE_TIME_FORMAT = "HH:mm";
    public static final String SECOND_TIME_FORMAT = MINUTE_TIME_FORMAT + ":ss";
    public static final String LONG_TIME_FORMAT = SECOND_TIME_FORMAT + ".SSS";
    public static final String WEEKDAY_TIME_FORMAT = "EEE " + SECOND_TIME_FORMAT;
    public static final String DATETIME_FORMAT = DATE_FORMAT + " " + SECOND_TIME_FORMAT;
    public static final String LONG_DATETIME_FORMAT = DATE_FORMAT + " " + LONG_TIME_FORMAT;
    public static final String DATETIME_TZ_FORMAT = DATETIME_FORMAT + " z";
    public static final String SIMPLE_DATETIME_FORMAT = "yy-MM-dd " + MINUTE_TIME_FORMAT;
    public static final String EXECUTION_DATETIME_FORMAT = SIMPLE_DATE_FORMAT + "  " + SECOND_TIME_FORMAT;
    public static final String TRADE_DATETIME_FORMAT = "MMM dd, " + SECOND_TIME_FORMAT + " z";

//  G	Era	Text	“GG” -> “AD”
//  y	Year	Number	“yy” -> “03″
//                      “yyyy” -> “2003″
//  M	Month	Text or Number	“M” -> “7″
//                              “M” -> “12″
//                              “MM” -> “07″
//                              “MMM” -> “Jul”
//                              “MMMM” -> “December”
//  d	Day in month	Number	“d” -> “3″
//                              “dd” -> “03″
//  h	Hour (1-12, AM/PM)	Number	“h” -> “3″
//                                      “hh” -> “03″
//  H	Hour (0-23)	Number	“H” -> “15″
//                              “HH” -> “15″
//  k	Hour (1-24)	Number	“k” -> “3″
//                               “kk” -> “03″
//  K	Hour (0-11 AM/PM)	Number	“K” -> “15″
//                                      “KK” -> “15″
//  m	Minute	Number	“m” -> “7″
//                      “m” -> “15″
//                      “mm” -> “15″
//  s	Second	Number	“s” -> “15″
//                      “ss” -> “15″
//  S	Millisecond (0-999)	Number	“SSS” -> “007″
//  E	Day in week	Text	“EEE” -> “Tue”
//                              “EEEE” -> “Tuesday”
//  D	Day in year (1-365 or 1-364)	Number	“D” -> “65″
//                                              “DDD” -> “065″
//  F	Day of week in month (1-5)	Number	“F” -> “1″
//  w	Week in year (1-53)	Number	“w” -> “7″
//  W	Week in month (1-5)	Number	“W” -> “3″
//  a	AM/PM	Text	“a” -> “AM”
//                      “aa” -> “AM”
//  z	Time zone	Text	“z” -> “EST”
//                              “zzz” -> “EST”
//                              “zzzz” -> “Eastern Standard Time”
//  ‘	Excape for text	Delimiter	“‘hour’ h” -> “hour 9″
//  ”	Single quote	Literal	“ss”SSS” -> “45′876″
    /**
     * Get Java standard time zone with day light saving functionality.
     *
     * @param timeZoneId currently supports: <br> <BLOCKQUOTE> HKT -
     * Asia/Hong_Kong, <br> PST/PDT - America/Los_Angeles, <br> EST/ EDT -
     * America/New_York </BLOCKQUOTE>
     * @return
     */
    public static TimeZone getTimeZone(String timeZoneId) {
        if (timeZoneId == null) {
            return TimeZone.getDefault();
        }

        if (timeZoneId.contains("New South Wales")) {
            timeZoneId = "AET";

        } else if (timeZoneId.toUpperCase().matches("E[DS]T")
                || timeZoneId.equalsIgnoreCase("NYT")) {
            timeZoneId = "America/New_York";

        } else if (timeZoneId.toUpperCase().matches("P[DS]T")) {
            timeZoneId = "America/Los_Angeles";

        } else if (timeZoneId.toUpperCase().matches("HKT")
                || timeZoneId.equalsIgnoreCase("Hong Kong Time")) {
            timeZoneId = "Asia/Hong_Kong";

        } else if (timeZoneId.toUpperCase().matches("JST")
                || timeZoneId.equalsIgnoreCase("Japan Standard Time")) {
            timeZoneId = "Japan";

        } else if (timeZoneId.contains("America/Belize")
                || timeZoneId.equalsIgnoreCase("Central Standard Time")) {
            timeZoneId = "US/Central";

        } else if (timeZoneId.equalsIgnoreCase("Middle Europe Time")) {
            timeZoneId = "MET";

        } else if (timeZoneId.equalsIgnoreCase("India Standard Time")) {
            timeZoneId = "IST";

        } else if (timeZoneId.equalsIgnoreCase("Greenwich Mean Time")) {
            timeZoneId = "GMT";

        } else if (timeZoneId.toUpperCase().matches("NZ[DS]T")
                || timeZoneId.contains("New Zealand")) {
            timeZoneId = "NZ";
        }

        return TimeZone.getTimeZone(timeZoneId);
    }

    /**
     * Get time difference in milliseconds at the provided time between the
     * provide time zone and the current time zone.
     *
     * @param date
     * @param timeZone
     * @return
     */
    public static long getTimeDiff(Calendar date, TimeZone timeZone) {
        return getTimeDiff(date.getTimeInMillis(), timeZone);
    }

    /**
     *
     * @param date
     * @return
     */
    public static long getTimeDiff(Calendar date) {
        return getTimeDiff(date.getTimeInMillis(), date.getTimeZone());
    }

    /**
     *
     * @param timeInMillis
     * @param timeZone
     * @return
     */
    public static long getTimeDiff(long timeInMillis, TimeZone timeZone) {
        if (timeZone != null) {
            return timeZone.getOffset(timeInMillis) - TimeZone.getDefault().getOffset(timeInMillis);
        } else {
            return 0;
        }
    }

    /**
     * Get current time difference from current time zone.
     *
     * @param timeZone
     * @return
     */
    public static long getTimeDiff(TimeZone timeZone) {
        return getTimeDiff(getCalendarDate(), timeZone);
    }

    /**
     * Get time difference at the provided time from GMT.
     *
     * @param date
     * @return
     */
    public static long getTimeDiffFromGMT(Calendar date) {
        return date.getTimeZone().getOffset(date.getTimeInMillis());
    }

    /**
     * Get current time difference of the default time zone from GMT.
     *
     * @return
     */
    public static long getTimeDiffFromGMT() {
        return getTimeDiffFromGMT(getCalendarDate());
    }

    /**
     * Get UNIX time in milliseconds for now.
     *
     * @return milliseconds
     */
    public static long getTimeNow() {
        return System.currentTimeMillis();
    }

    /**
     * Get Calendar date from string in specified format in specified time zone.
     *
     * @param dateString
     * @param format e.g. yyyy-MM-dd HH:mm:ss. Refer to {@link java.text.SimpleDateFormat}
     * @param timeZone
     * @return {@link Calendar}
     * @throws ParseException
     *
     * @see SimpleDateFormat
     */
    public static Calendar getCalendarDate(String dateString, String format, TimeZone timeZone) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        Calendar calendar = getCalendarDate(0);

        //TimeZone tz = getTimeZone(timeZone);
        long theTime = dateFormat.parse(dateString).getTime();
        long timeDiff = getTimeDiff(theTime, timeZone);

        calendar.setTimeZone(timeZone);
        calendar.setTimeInMillis(theTime - timeDiff);

        return calendar;
    }

    /**
     * Get Calendar date from string in specified format in default time zone.
     *
     * @param dateString
     * @param format e.g. yyyy-MM-dd HH:mm:ss. Refer to {@link java.text.SimpleDateFormat}
     * @return {@link Calendar}
     * @throws ParseException
     *
     * @see SimpleDateFormat
     */
    public static Calendar getCalendarDate(String dateString, String format) throws ParseException {
        return getCalendarDate(dateString, format, TimeZone.getDefault());
    }

    /**
     * Get Calendar date from milliseconds from Epoch in the given time zone.
     *
     * @param milliseconds
     * @param timeZone
     * @return  {@link Calendar}
     */
    public static Calendar getCalendarDate(long milliseconds, TimeZone timeZone) {
        Calendar date = getCalendarDate(timeZone);
        date.setTimeInMillis(milliseconds);
        return date;
    }

    /**
     * Get Calendar date from milliseconds from Epoch in default time zone.
     *
     * @param milliseconds
     * @return  {@link Calendar}
     */
    public static Calendar getCalendarDate(long milliseconds) {
        return getCalendarDate(milliseconds, TimeZone.getDefault());
    }

    /**
     * Get Calendar date Epoch in given time zone.
     *
     * @param timeZone
     * @return  {@link Calendar}
     */
    public static Calendar getCalendarDate(TimeZone timeZone) {
        if (timeZone == null) {
            return Calendar.getInstance();
        }

        return Calendar.getInstance(timeZone);
    }

    /**
     * Get current time instance.
     *
     * @return  {@link Calendar}
     */
    public static Calendar getCalendarDate() {
        return Calendar.getInstance();
    }

    private static String getDurationStr(long milliseconds, String... units) {
        StringBuilder sb = new StringBuilder();

        double[] values = {
            (int) milliseconds / DAY_TIME,
            (int) milliseconds % DAY_TIME / HOUR_TIME,
            (int) milliseconds % HOUR_TIME / MINUTE_TIME,
            (double) milliseconds % MINUTE_TIME / SECOND_TIME};

        for (int i = 0; i < units.length; i++) {
            if (values[i] > 0) {
                sb.append(sb.length() > 0 ? " " : "").
                        append(TextUtil.SIMPLE_FORMATTER.format(values[i])).
                        append(" ").
                        append(units[i]).
                        append(values[i] > 1 && units[i].length() > 1 ? "s" : "");
            }
        }

        return sb.toString();
    }

    /**
     * Get displayable duration string.
     *
     * @param milliseconds
     * @return time string in format "D day(s) H hour(s) m minute(s) s
     * second(s)"
     */
    public static String getDurationStr(long milliseconds) {
        return getDurationStr(milliseconds, "day", "hour", "minute", "second");
    }

    /**
     * Get displayable duration string since the provided time.
     *
     * @param since UNIX time in milliseconds
     * @return time string in format "D day(s) H hour(s) m minute(s) s
     * second(s)"
     */
    public static String getDurationStrSince(long since) {
        return getDurationStr(getTimeNow() - since);
    }

    /**
     *
     * @param milliseconds
     * @return
     */
    public static String getSimpleDurationStr(long milliseconds) {
        return getDurationStr(milliseconds, "D", "h", "m", "s");
    }

    /**
     *
     * @param date
     * @return
     */
    public static String getForeignTimeStamp(Calendar date) {
        return getForeignTimeStamp(date, DATETIME_FORMAT);
    }

    /**
     *
     * @param date
     * @param format
     * @return
     */
    public static String getForeignTimeStamp(Calendar date, String format) {
        return getTimeStamp(addDate(date, getTimeDiff(date, date.getTimeZone())), format);
    }

    /**
     *
     * @param date
     * @param timeZone
     * @param format
     * @return
     */
    public static String getForeignTimeStamp(Long date, TimeZone timeZone, String format) {
        return getForeignTimeStamp(getCalendarDate(date, timeZone), format);
    }

    /**
     *
     * @param date
     * @param format
     * @return
     */
    public static String getTimeStamp(Calendar date, String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date.getTime());
    }

    /**
     * Get time stamp for input date down to milliseconds.
     *
     * @param date
     * @return HH:mm:ss.SSS
     */
    public static String getTimeStamp(Calendar date) {
        return getTimeStamp(date, LONG_TIME_FORMAT);
    }

    /**
     * Get time stamp for milliseconds after 1st Jan 1970 GMT.
     *
     * @param milliseconds
     * @return HH:mm:ss.SSS
     */
    public static String getTimeStamp(long milliseconds) {
        if (milliseconds <= 0) {
            return "";
        }

        return getTimeStamp(getCalendarDate(milliseconds));
    }

    /**
     * Get simple time stamp for milliseconds after 1st Jan 1970 GMT.
     *
     * @param milliseconds
     * @return HH:mm:ss
     */
    public static String getSimpleTimeStamp(long milliseconds) {
        if (milliseconds <= 0) {
            return "";
        }

        return getTimeStamp(getCalendarDate(milliseconds), MINUTE_TIME_FORMAT);
    }

    /**
     * Get time stamp for milliseconds after 1st Jan 1970 GMT in provided
     * format.
     *
     * @param milliseconds
     * @param format
     * @return
     *
     * @see SimpleDateFormat
     */
    public static String getTimeStamp(long milliseconds, String format) {
        if (milliseconds <= 0) {
            return "";
        }

        return getTimeStamp(getCalendarDate(milliseconds), format);
    }

    /**
     * Get current time stamp in provided format.
     *
     * @param format
     * @return
     *
     * @see SimpleDateFormat
     */
    public static String getTimeStamp(String format) {
        return getTimeStamp(getCalendarDate(), format);
    }

    /**
     * Get current time stamp down to milliseconds.
     *
     * @return HH:mm:ss.SSS
     */
    public static String getTimeStamp() {
        return getTimeStamp(getCalendarDate());
    }

    /**
     *
     * @param string
     * @return milliseconds
     */
    public static long getTime(String string) {
        if (string == null || string.isEmpty()) {
            return 0;// getTimeNow();
        }

        try {
            return getCalendarDate(
                    string, EXECUTION_DATETIME_FORMAT).getTimeInMillis();
        } catch (ParseException ex) {
            try {
                return getCalendarDate(
                        string, DATETIME_FORMAT).getTimeInMillis();
            } catch (ParseException ex1) {
                try {
                    return getCalendarDate(
                            string, MINUTE_TIME_FORMAT).getTimeInMillis();
                } catch (ParseException ex2) {
                    try {
                        return Long.parseLong(string);
                    } catch (Exception ex3) {
                        return 0;
                    }
                }
            }
        }
    }

    /**
     * Get date string.
     *
     * @param time
     * @return yyyy-MM-dd
     */
    public static String getDateString(long time) {
        if (time <= 0) {
            return "";
        }

        return getTimeStamp(time, DATE_FORMAT);
    }

    /**
     * Get current full date string.
     *
     * @param time
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeString(long time) {
        if (time <= 0) {
            return "";
        }

        return getTimeStamp(time, DATETIME_FORMAT);
    }

    /**
     * Get current full date string.
     *
     * @return yyyy-MM-dd
     */
    public static String getDateString() {
        return getTimeStamp(DATE_FORMAT);
    }

    /**
     * Get current full date string.
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTimeString() {
        return getTimeStamp(DATETIME_FORMAT);
    }

    /**
     *
     * @param calendar
     * @return yyyy-MM-dd
     */
    public static String getForeignDateString(Calendar calendar) {
        return getForeignTimeStamp(calendar, DATE_FORMAT);
    }

    /**
     * Get the number of seconds starting from midnight of the day to now.
     *
     * @return seconds
     */
    public static double getSecondsOfDay() {
        return getSecondsOfDay(getCalendarDate());
    }

    /**
     * Get the number of seconds starting from midnight of the specified date.
     *
     * @param calendar
     * @return seconds
     */
    public static double getSecondsOfDay(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) * 3600
                + calendar.get(Calendar.MINUTE) * 60
                + calendar.get(Calendar.SECOND)
                + calendar.get(Calendar.MILLISECOND) / 1000;
    }

    /**
     * Get the number of seconds starting from midnight of the specified time
     * string.
     *
     * @param hours in format HH:mm or HH:mm:ss
     * @return seconds
     */
    public static double getSecondsOfDay(String hours) {
        StringTokenizer st = new StringTokenizer(hours, ":");
        double[] time = new double[3];
        int i = 0;
        while (st.hasMoreElements()) {
            time[i++] = Double.parseDouble(st.nextToken());
        }

        return time[0] * 3600 + time[1] * 60 + time[2];
    }

    /**
     * Get an clone instance of the given date.
     *
     * @param calendar
     * @return the clone
     */
    public static Calendar clone(Calendar calendar) {
        if (calendar == null) {
            return null;
        }

        return (Calendar) calendar.clone();
    }

    /**
     * Convert date string format
     *
     * @param dateString
     * @param oldFormat
     * @param NewFormat
     * @return date string in the new format
     * @throws ParseException
     */
    public static String convertDateString(String dateString, String oldFormat, String NewFormat) throws ParseException {
        return getTimeStamp(getCalendarDate(dateString.replaceAll("\\b\\s{1,}\\b", " "), oldFormat), NewFormat);
    }

    /**
     * Calculate the next soonest interval time in milliseconds of the clock.
     * e.g. 900000 (15 minutes), return the next soonest time of {hh:00, hh:15,
     * hh:30, hh:45} in Calendar class
     *
     * @param interval
     * @return Calendar class
     */
    public static Calendar nextSchedule(long interval) {
        return getCalendarDate(((long) (getTimeNow() / interval) + 1) * interval);
    }
    
    /**
     * Calculate the next soonest interval time in milliseconds of the clock.
     * e.g. 900000 (15 minutes), return the next soonest time of {hh:00, hh:15,
     * hh:30, hh:45}
     *
     * @param interval
     * @return UNIX time
     */
    public static long nextScheduleTime(long interval) {
        return ((long) (getTimeNow() / interval) + 1) * interval;
    }

    /**
     * Get an instance of Calendar date before/after milliseconds of the input
     * date.
     *
     * @param date
     * @param milliseconds
     * @return
     */
    public static Calendar addDate(Calendar date, long milliseconds) {
        if (date == null) {
            return null;
        }

        Calendar newDate = clone(date);
        newDate.add(Calendar.MILLISECOND, (int) milliseconds);

        return newDate;
    }

    /**
     * Test if the provided date before the time string in a day.
     *
     * @param calendar
     * @param hours in format HH:mm or HH:mm:ss
     * @return true or false
     */
    public static boolean isBeforeHours(Calendar calendar, String hours) {
        return getSecondsOfDay(calendar) < getSecondsOfDay(hours);
    }

    /**
     * Test if now is before the time string in a day.
     *
     * @param hours in format HH:mm or HH:mm:ss
     * @return true or false
     */
    public static boolean isBeforeHours(String hours) {
        return isBeforeHours(getCalendarDate(), hours);
    }

    /**
     * Test if the provided date after the time string in a day.
     *
     * @param calendar
     * @param hours in format HH:mm or HH:mm:ss
     * @return true or false
     */
    public static boolean isAfterHours(Calendar calendar, String hours) {
        return getSecondsOfDay(calendar) >= getSecondsOfDay(hours);
    }

    /**
     * Test if now is after the time string in a day.
     *
     * @param hours in format HH:mm or HH:mm:ss
     * @return true or false
     */
    public static boolean isAfterHours(String hours) {
        return isAfterHours(getCalendarDate(), hours);
    }

    /**
     * Test if the provided date equals the given day of week.
     *
     * @param date
     * @param dayOfWeek
     * @return true or false
     */
    public static boolean isDayOfWeek(Calendar date, int dayOfWeek) {
        return date.get(Calendar.DAY_OF_WEEK) == dayOfWeek;
    }

    /**
     * Test if the today equals the given day of week.
     *
     * @param dayOfWeek
     * @return true or false
     */
    public static boolean isDayOfWeek(int dayOfWeek) {
        return isDayOfWeek(getCalendarDate(), dayOfWeek);
    }

    /**
     * Test if the provided date is Monday.
     *
     * @param date
     * @return true or false
     */
    public static boolean isMonday(Calendar date) {
        return isDayOfWeek(date, Calendar.MONDAY);
    }

    /**
     * Test if today is Monday.
     *
     * @return true or false
     */
    public static boolean isMonday() {
        return isMonday(getCalendarDate());
    }

    /**
     * Test if the provided date is Sunday.
     *
     * @param date
     * @return true or false
     */
    public static boolean isSunday(Calendar date) {
        return isDayOfWeek(date, Calendar.SUNDAY);
    }

    /**
     * Test if today is Sunday.
     *
     * @return true or false
     */
    public static boolean isSunday() {
        return isSunday(getCalendarDate());
    }

    /**
     * Test if now is within the provide UNIX time plus a margin of {@link Main.p_Task_Interval}.
     *
     * @param time UNIX time in milliseconds
     * @return true or false
     */
    public static boolean isNow(long time) {
        return isNow(time, Main.p_Task_Interval);
    }

    /**
     * Test if now is within the provide UNIX time plus a given margin.
     *
     * @param time UNIX time in milliseconds
     * @param margin milliseconds
     * @return true or false
     */
    public static boolean isNow(long time, long margin) {
        long timeNow = getTimeNow();

        return timeNow >= time && timeNow < time + margin;
    }

    /**
     * Test if now is within the provide date plus a given margin.
     *
     * @param date
     * @param margin milliseconds
     * @return true or false
     */
    public static boolean isNow(Calendar date, long margin) {
        return isNow(date.getTimeInMillis(), margin);
    }

    /**
     *
     * @param timeString
     * @return
     */
    public static boolean isSameTimeOfDay(String timeString) {
        return isSameTimeOfDay(getCalendarDate(), timeString);
    }
//
//    public static boolean isSameTimeOfDay(long time, String timeString) {
//        return isSameTimeOfDay(getCalendarDate(time), timeString);
//    }

    /**
     * Test if the input date is within the specified time string.
     *
     * @param date
     * @param timeString in HH:mm format, start inclusive, end exclusive. {,;}
     * for separate intervals, {-~} for range. E.g.
     * 10:00-10:30,11:00~14:00;16:00-20:00
     * @return
     */
    public static boolean isSameTimeOfDay(Calendar date, String timeString) {
        for (String interval : timeString.split("\\s*[,;]\\s*")) {

            String[] t = interval.split("\\s*[-~]\\s*");

            String t0 = t[0];
            String t1 = t[t.length - 1];

            Calendar d0 = null;
            Calendar d1 = null;

            try {
                d0 = getCalendarDate(t0, MINUTE_TIME_FORMAT);
                d1 = getCalendarDate(t1, MINUTE_TIME_FORMAT);
            } catch (ParseException ex) {
            }

            if (d0 == null || d1 == null) {
                continue;
            }

            d0.set(Calendar.YEAR, date.get(Calendar.YEAR));
            d0.set(Calendar.MONTH, date.get(Calendar.MONDAY));
            d0.set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR));

            d1.set(Calendar.YEAR, date.get(Calendar.YEAR));
            d1.set(Calendar.MONTH, date.get(Calendar.MONDAY));
            d1.set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR));

            if (d0.equals(d1)) {
                d1 = addDate(d1, Main.p_Task_Interval);
            }
            
//            System.out.println(d0.getTime());
//            System.out.println(d1.getTime());

            if (!d0.after(date) && d1.after(date)) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param minute
     * @return
     */
    public static boolean isMinuteInterval(int minute) {
        return isMinuteInterval(getCalendarDate(), minute);
    }

    /**
     *
     * @param date
     * @param minute
     * @return
     */
    public static boolean isMinuteInterval(Calendar date, int minute) {
        return date.get(Calendar.MINUTE) % minute == 0
                && getMillisecond(date) < Main.p_Task_Interval;
    }

    /**
     *
     * @return
     */
    public static long getMillisecond() {
        return getMillisecond(getCalendarDate());
    }

    /**
     *
     * @return
     */
    public static int getMinute() {
        return getMinute(getCalendarDate());
    }

    /**
     *
     * @param date
     * @return
     */
    public static long getMillisecond(Calendar date) {
        return 1000L * date.get(Calendar.SECOND) + date.get(Calendar.MILLISECOND);
    }

    /**
     *
     * @param date
     * @return
     */
    public static int getMinute(Calendar date) {
        return date.get(Calendar.MINUTE);
    }

//    public static Calendar getNextWorkingDayMidnight(Calendar calendar) {
//        return getNextWorkingDay(getMidnight(calendar));
//    }
//
//    public static Calendar getNextWorkingDayMidnight() {
//        return getNextWorkingDay(getMidnight());
//    }
//
//    public static Calendar getNextWorkingDayMidnight(TimeZone timeZone) {
//        return getNextWorkingDay(getMidnight(timeZone));
//    }
    /**
     *
     * @param calendar
     * @return
     */
    public static Calendar getLastWorkingDay(Calendar calendar) {
        Calendar lastDay = addDate(calendar, -DAY_TIME);

        while (!(!isWeekend(lastDay) && lastDay.before(calendar))) {
            lastDay.add(Calendar.DAY_OF_YEAR, -1);
        }

        return lastDay;
    }

    /**
     *
     * @return
     */
    public static Calendar getLastWorkingDay() {
        return getLastWorkingDay(getCalendarDate());
    }

    /**
     *
     * @param calendar
     * @return
     */
    public static Calendar getNextWorkingDay(Calendar calendar) {
        Calendar nextDay = addDate(calendar, DAY_TIME);

        while (!(!isWeekend(nextDay) && nextDay.after(calendar))) {
            nextDay.add(Calendar.DAY_OF_YEAR, 1);
        }

        return nextDay;
    }

    /**
     *
     * @return
     */
    public static Calendar getNextWorkingDay() {
        return getNextWorkingDay(getCalendarDate());
    }

    /**
     *
     * @param calendar
     * @return
     */
    public static Calendar getNextMonday(Calendar calendar) {
        if (calendar == null) {
            return null;
        }

        Calendar nextMonday = clone(calendar);

        while (!(isMonday(nextMonday) && nextMonday.after(calendar))) {
            nextMonday.add(Calendar.DAY_OF_YEAR, 1);
        }

        return nextMonday;
    }

    /**
     *
     * @return
     */
    public static Calendar getNextMonday() {
        return getNextMonday(getCalendarDate());
    }

//    public static Calendar getNextMondayMidnight() {
//        return getNextMonday(getMidnight());
//    }
//
//    public static Calendar getNextMondayMidnight(TimeZone timeZone) {
//        return getNextMonday(getMidnight(timeZone));
//    }
    /**
     *
     * @param date1
     * @param date2
     * @return
     */
    public static Calendar getEarlier(Calendar date1, Calendar date2) {
        if (date1 == null || date2 == null) {
            throw new UnsupportedOperationException("Date can't be null.");
        }

        if (date2.before(date1)) {
            return date2;
        } else {
            return date1;
        }
    }

    /**
     *
     * @param date
     * @return
     */
    public static boolean isWeekend(Calendar date) {
        return isDayOfWeek(date, Calendar.SUNDAY)
                || isDayOfWeek(date, Calendar.SATURDAY);
    }

    /**
     *
     * @return
     */
    public static boolean isWeekend() {
        return isWeekend(getCalendarDate());
    }

    /*
     * public static boolean isTime(Calendar date, String timeString) throws
     * ParseException { Calendar time = getCalendarDate(timeString, "HH:mm");
     *
     * return date.get(Calendar.HOUR_OF_DAY) == time.get(Calendar.HOUR_OF_DAY)
     * && date.get(Calendar.MINUTE) == time.get(Calendar.MINUTE); }
     *
     * public static boolean isTime(String timeString) throws ParseException {
     * return isTime(getCalendarDate(), timeString); }
     *
     */
    /**
     * Test if the date is midnight time
     *
     * @param date
     * @return
     */
    public static boolean isMidnight(Calendar date) {
        return date.get(Calendar.HOUR_OF_DAY) == 0
                && date.get(Calendar.MINUTE) == 0
                && getMillisecond(date) < Main.p_Task_Interval;
    }

    /**
     *
     * @return
     */
    public static boolean isMidnight() {
        return isMidnight(getCalendarDate());
    }

    /**
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameDay(Calendar date1, Calendar date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.getTimeZone().hasSameRules(date2.getTimeZone())
                && date1.get(Calendar.DAY_OF_YEAR) == date2.get(Calendar.DAY_OF_YEAR)
                && date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR);
    }

//    public static boolean isSameMinute(Calendar date1, Calendar date2) {
//        if (date1 == null || date2 == null) {
//            return false;
//        }
//
//        return isSameMinute(date1.getTimeInMillis(), date2.getTimeInMillis());
//    }
//
//    public static boolean isSameMinute(long time1, long time2) {
//        return roundTime(time1, MINUTE_TIME) == roundTime(time2, MINUTE_TIME);
//    }
    /**
     * Get the midnight date of the same day of current time zone.
     *
     * @return
     */
    public static Calendar getMidnight() {
        return getMidnight(getCalendarDate());
    }

    /**
     * Get the midnight date of the same day of the given time zone.
     *
     * @param timeZone
     * @return
     */
    public static Calendar getMidnight(TimeZone timeZone) {
        return getMidnight(getCalendarDate(timeZone));
    }

    /**
     * Get the midnight date of the same day and same time zone as the given date.
     *
     * @param date
     * @return
     */
    public static Calendar getMidnight(Calendar date) {
        if (date == null) {
            return null;
        }

        Calendar newDate = clone(date);

        newDate.set(Calendar.HOUR_OF_DAY, 0);
        newDate.set(Calendar.MINUTE, 0);
        newDate.set(Calendar.SECOND, 0);
        newDate.set(Calendar.MILLISECOND, 0);

        return newDate;
    }

    /**
     * Get the midnight date of the given date and given time zone.
     *
     * @param date
     * @param timeZone
     * @return
     */
    public static Calendar getMidnight(Calendar date, TimeZone timeZone) {
        Calendar c = clone(date);

        if (date != null && timeZone != null) {
            c.setTimeZone(timeZone);
        }

        return getMidnight(c);
    }

    /**
     *
     * @param date
     * @return
     */
    public static long getMidnightTime(Calendar date) {
        if (date == null) {
            return 0;
        }

        return getMidnight(date).getTimeInMillis();
    }

    /**
     *
     * @return
     */
    public static long getMidnightTime() {
        return getMidnightTime(getCalendarDate());
    }

    /**
     *
     * @param timeZone
     * @return
     */
    public static long getMidnightTime(TimeZone timeZone) {
        return getMidnight(timeZone).getTimeInMillis();
    }

    /**
     *
     * @param time
     * @param precision
     * @return
     */
    public static long roundTime(long time, long precision) {
        return ((long) Math.floor((double) time / precision)) * precision;
    }

    /**
     * Return minimum valid duration for IB data request
     *
     * @param milliseconds
     * @return durationStr
     */
    public static String getHistDataDurationStr(long milliseconds) {
        int seconds = (int) (milliseconds / 1000);
        if (seconds < 60) {
            return "60 S";
        } else if (seconds <= 300) {
            return "300 S"; //=5 mins
        } else if (seconds <= 960) {
            return "900 S"; //=15 mins
        } else if (seconds <= 1800) {
            return "1800 S"; //=30 mins
        } else if (seconds <= 3600) {
            return "3600 S"; //=1 hr
        } else if (seconds <= 7200) {
            return "7200 S"; //=2 hrs
        } else if (seconds <= 14400) {
            return "14400 S"; //=4 hrs
        } else if (seconds <= 86400) {
            return "1 D";
        } else if (seconds <= 172800) {
            return "2 D";
        } else if (seconds <= 604800) {
            return "1 W";
        } else if (seconds <= 2592000) { //assume 30 days
            return "1 M";
        } else if (seconds <= 7776000) { //assume 90 days
            return "3 M";
        } else if (seconds <= 15552000) { //assume 180 days
            return "6 M";
        } else if (seconds <= 31104000) { //assume 360 days
            return "1 year";
        } else {
            throw new UnsupportedOperationException("Unsupported duration");
        }
    }

    /**
     *
     * @param milliseconds
     * @return
     */
    public static String getHistDataBarSizeStr(long milliseconds) {
        int seconds = (int) (milliseconds / 1000);
        if (seconds < 5) {
            return "1 secs";
        } else if (seconds <= 5) {
            return "5 secs";
        } else if (seconds <= 15) {
            return "15 secs";
        } else if (seconds <= 30) {
            return "30 secs";
        } else if (seconds <= 60) {
            return "1 min";
        } else if (seconds <= 120) {
            return "2 mins";
        } else if (seconds <= 180) {
            return "3 mins";
        } else if (seconds <= 300) {
            return "5 mins"; //=5 mins
        } else if (seconds <= 960) {
            return "15 mins"; //=15 mins
        } else if (seconds <= 1800) {
            return "30 mins"; //=30 mins
        } else if (seconds <= 3600) {
            return "1 H"; //=1 hr
        } else if (seconds <= 7200) {
            return "2 H"; //=2 hrs
        } else if (seconds <= 14400) {
            return "4 H"; //=4 hrs
        } else if (seconds <= 86400) {
            return "1 D";
        } else if (seconds <= 172800) {
            return "2 D";
        } else if (seconds <= 604800) {
            return "1 W";
        } else if (seconds <= 2592000) { //assume 30 days
            return "1 M";
        } else if (seconds <= 7776000) { //assume 90 days
            return "3 M";
        } else if (seconds <= 15552000) { //assume 180 days
            return "6 M";
        } else if (seconds <= 31104000) { //assume 360 days
            return "1 year";
        } else {
            throw new UnsupportedOperationException("Unsupported bar size");
        }
    }
}
