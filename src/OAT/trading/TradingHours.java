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

package OAT.trading;

import com.ib.client.ContractDetails;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import OAT.util.DateUtil;
import OAT.util.OrderedList;
import OAT.util.TextUtil;

/**
 * A class contains information of the trading hours of an exchange.
 *
 * @author Antonio Yip
 */
public class TradingHours implements Serializable {

    private static final int OPEN_TIME_FIELD = 0;
    private static final int CLOSE_TIME_FIELD = 1;
    private String exchange;
    private TimeZone timeZone;
    private OrderedList<Session> sessions = new OrderedList<Session>();

    public TradingHours() {
    }

    public void addTradingSession(List<Calendar[]> sessions) {
        for (Calendar[] calendars : sessions) {
            if (calendars.length > 0) {
                addTradingSession(new Session(calendars[0], calendars[1]));
            }
        }
    }

    public void addTradingSession(long open, long close, TimeZone timeZone) {
        addTradingSession(new Session(open, close, timeZone));
    }

    public void addTradingSession(Session session) {
        int openHour = session.open.get(Calendar.HOUR_OF_DAY);
        boolean isDemo = Main.getAccount() == Account.DEMO;

        if (timeZone == null) {
            timeZone = session.open.getTimeZone();
        }

        if (!isDemo && (openHour < 7 || openHour > 14)) {
            return;
        }

        if (session.open == null) {
            return;
        }

        if (!isDemo && DateUtil.isWeekend(session.open)) {
            return;
        }
        
        this.sessions.add(session);
    }

    public void addTradingSessions(ContractDetails contractDetails) throws ParseException {
//        if (contractDetails == null) {
//            return;
//        }

        String m_liquidHours = contractDetails.m_liquidHours;
        exchange = contractDetails.m_summary.m_exchange;
        timeZone = DateUtil.getTimeZone(contractDetails.m_timeZoneId);

        if (m_liquidHours == null) {
            return;
        }

        int i = m_liquidHours.lastIndexOf(" ");

        if (i > -1) {
            m_liquidHours = m_liquidHours.substring(0, i);
        }

        StringTokenizer st1 = new StringTokenizer(m_liquidHours, ";");
        while (st1.hasMoreTokens()) {
            String dayTime = st1.nextToken();
            int j = dayTime.indexOf(":");
            String day = dayTime.substring(0, j);

            if (dayTime.contains("CLOSED")) {
                continue;
            }

            StringTokenizer st2 = new StringTokenizer(dayTime.substring(j + 1, dayTime.length()), ",");
            while (st2.hasMoreTokens()) {
                List<Calendar> calendars = new ArrayList<Calendar>();

                StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "-");
                while (st3.hasMoreTokens()) {
                    String time = st3.nextToken();
                    calendars.add(DateUtil.getCalendarDate(
                            day + " " + time, "yyyyMMdd HHmm", timeZone));
                }

                Session session = new Session(calendars.get(0),
                        calendars.get(calendars.size() - 1));

//                session.close.roll(Calendar.SECOND, 59);

                addTradingSession(session);
            }
        }
    }

    public boolean isEmpty() {
        return sessions.isEmpty();
    }

    /**
     * Check whether the market is open at the provided time.
     *
     * @param date
     * @return
     */
    public boolean isOpen(Calendar date) {
        return isOpen(date.getTimeInMillis());
    }

    public boolean isOpen(long time) {
        for (Session session : sessions) {
            if (session.isIn(time)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether the market is currently open.
     *
     * @return
     */
    public boolean isOpen() {
        return isOpen(DateUtil.getTimeNow());
    }

    public boolean isIntradayBreak() {
        return isIntradayBreak(DateUtil.getCalendarDate(timeZone));
    }

    public boolean isIntradayBreak(Calendar date) {
        Calendar lastOpen = getLastOpen(date);
        Calendar lastClose = getLastClose(date);
        Calendar nextOpen = getNextOpen(date);

        if (lastOpen == null || lastClose == null || nextOpen == null) {
            return false;
        }

        return lastOpen.get(Calendar.DAY_OF_YEAR)
                == nextOpen.get(Calendar.DAY_OF_YEAR)
                && lastClose.before(date)
                && nextOpen.after(date);
    }

    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * Get the next session open time from the input time. Does not include
     * current session.
     *
     * @param date
     * @return
     */
    public Calendar getNextOpen(Calendar date) {
        return getNext(date, OPEN_TIME_FIELD);
    }

    /**
     * Get the next session open time from the input time. Does not include
     * current session.
     *
     * @param time
     * @return
     */
    public long getNextOpenTime(long time) {
        return getNextOpen(DateUtil.getCalendarDate(time, timeZone)).getTimeInMillis();
    }

    /**
     * Get the next session open time from now. Does not include current
     * session.
     *
     * @return
     */
    public Calendar getNextOpen() {
        return getNextOpen(DateUtil.getCalendarDate(timeZone));
    }

    /**
     * Get the next session open time from now. Does not include current
     * session.
     *
     * @return
     */
    public long getNextOpenTime() {
        return getNextOpen().getTimeInMillis();
    }

    /**
     * Get the next close time from the input time.
     *
     * @param date
     * @return
     */
    public Calendar getNextClose(Calendar date) {
        return getNext(date, CLOSE_TIME_FIELD);
    }

    /**
     * Get the next close time from the input time.
     *
     * @param time
     * @return
     */
    public long getNextCloseTime(long time) {
        return getNextClose(DateUtil.getCalendarDate(time, timeZone)).getTimeInMillis();
    }

    /**
     * Get the next close time from now.
     *
     * @return
     */
    public Calendar getNextClose() {
        return getNextClose(DateUtil.getCalendarDate(timeZone));
    }

    /**
     * Get the next close time from now.
     *
     * @return
     */
    public long getNextCloseTime() {
        return getNextClose().getTimeInMillis();
    }

    /**
     * Get the next field time from the input time.
     *
     * @param date
     * @param field
     * @return
     */
    public Calendar getNext(Calendar date, int field) {
//        Calendar prevC = null;
//        Calendar nextWorkingDay = null;

        for (Session session : sessions) {
            Calendar c = session.getArray()[field];

            if (date.before(c)) {
//                if (nextWorkingDay != null
//                        && date.before(nextWorkingDay)
//                        && nextWorkingDay.before(c)) {
//                    return nextWorkingDay;
//                }

                return c;
            }

//            if (!DateUtil.isSameDay(prevC, c)) {
//                nextWorkingDay = DateUtil.getNextWorkingDay(c);
//            }

//            prevC = c;
        }

        // catch if sessions is empty
        return DateUtil.getNextWorkingDay(DateUtil.getMidnight(timeZone));
    }

    /**
     * Get the next field time from now.
     *
     * @param field
     * @return
     */
    public Calendar getNext(int field) {
        return getNext(DateUtil.getCalendarDate(timeZone), field);
    }

    /**
     * Get the last session open time that nearest to the provided date.
     *
     * @param date
     * @return
     */
    public Calendar getLastOpen(Calendar date) {
        return getLast(date, OPEN_TIME_FIELD);
    }

    /**
     * Get the last session open time that nearest to the provided date.
     *
     * @param time
     * @return
     */
    public long getLastOpenTime(long time) {
        return getLastOpen(DateUtil.getCalendarDate(time, timeZone)).getTimeInMillis();
    }

    /**
     * Get the last session open time.
     *
     * @return
     */
    public long getLastOpenTime() {
        return getLastOpen().getTimeInMillis();
    }

    /**
     * Get the last session open time.
     *
     * @return
     */
    public Calendar getLastOpen() {
        return getLastOpen(DateUtil.getCalendarDate(timeZone));
    }

    /**
     * Get the last session close time that nearest to the provided date.
     *
     * @param time
     * @return
     */
    public long getLastCloseTime(long time) {
        return getLastClose(DateUtil.getCalendarDate(time, timeZone)).getTimeInMillis();
    }

    /**
     * Get the last session close time.
     *
     * @return
     */
    public long getLastCloseTime() {
        return getLastClose().getTimeInMillis();
    }

    /**
     * Get the last session close time that nearest to the provided date.
     *
     * @param date
     * @return
     */
    public Calendar getLastClose(Calendar date) {
        return getLast(date, CLOSE_TIME_FIELD);
    }

    public Calendar getLastClose() {
        return getLastClose(DateUtil.getCalendarDate(timeZone));
    }

    /**
     * Get the last session field time that nearest to the provided date.
     *
     * @param date
     * @param field
     * @return
     */
    public Calendar getLast(Calendar date, int field) {
//        if (!sessions.isEmpty()) {

        for (Iterator<Session> it = sessions.descendingIterator(); it.hasNext();) {
            Session session = it.next();

            Calendar c = session.getArray()[field];

            if (date.after(c)) {
                return c;
            }
        }

//        for (int i = sessions.size() - 1; i >= 0; i--) {
//            Calendar c = sessions.get(i).getArray()[field];
//            boolean isDayOpen = false;
//
//            if (i > 0) {
//                Calendar[] prevCalendars = sessions.get(i - 1).getArray();
//                if (c.get(Calendar.DAY_OF_YEAR)
//                        != prevCalendars[field].get(Calendar.DAY_OF_YEAR)) {
//                    isDayOpen = true;
//                }
//            } else {
//                isDayOpen = true;
//            }
//
//            if (date.after(c) && isDayOpen) {
//                return c;
//            }
//        }
//        }

        return DateUtil.getMidnight(date);
    }

    /**
     * Get the last session field time.
     *
     * @param field
     * @return
     */
    public Calendar getLast(int field) {
        return getLast(DateUtil.getCalendarDate(timeZone), field);
    }

    public String getExchange() {
        return exchange;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public OrderedList<Session> getTradingSessions() {
        return sessions;
    }

    public Session getFirstSession() {
        return sessions.getFirst();
    }

    public Session getLastSession() {
        return sessions.getLast();
    }

    public Session getCurrentSession() {
        return getCurrentSession(DateUtil.getTimeNow());
    }

    public Session getCurrentSession(long time) {
        Session currentSession = null;

        for (Session session : sessions) {
            if (session != null
                    && session.isIn(time)) {
                if (currentSession == null
                        || session.getCloseTime() > currentSession.getCloseTime()) {
                    currentSession = session;
                }
            }
        }

        return currentSession;
    }

    public OrderedList<Session> getSessions() {
        return sessions;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
//        List<String> strings = new ArrayList<String>();
//
//        for (Session session : sessions) {
//            strings.add(session.toString());
//        }

        return exchange + " trading hours: "
                + TextUtil.toString(sessions, "\n\t-----------");
    }
}
