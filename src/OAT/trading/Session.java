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

import java.util.Calendar;
import java.util.TimeZone;
import OAT.data.Chartable;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class Session implements Comparable<Session> {

    public static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy EEE HH:mm";
    public Calendar open, close;

    public Session(Calendar open, Calendar close) {
        open.set(Calendar.SECOND, 0);
        close.set(Calendar.SECOND, 0);

        this.open = open;
        this.close = close;
    }

    public Session(long open, long close, TimeZone timeZone) {
        this(DateUtil.getCalendarDate(open, timeZone),
                DateUtil.getCalendarDate(close, timeZone));
    }

    @Override
    public int compareTo(Session o) {
        int openDiff = open.compareTo(o.open);

//        if (openDiff == 0) {
//            return close.compareTo(o.close);
//        } else {
            return openDiff;
//        }
    }

    public Calendar[] getArray() {
        return new Calendar[]{open, close};
    }

    public Calendar getClose() {
        return close;
    }

    public Calendar getOpen() {
        return open;
    }

    public long getCloseTime() {
        return DateUtil.roundTime(close.getTimeInMillis(), DateUtil.MINUTE_TIME);
//        return close.getTimeInMillis();
    }

    public long getOpenTime() {
        return DateUtil.roundTime(open.getTimeInMillis(), DateUtil.MINUTE_TIME);
//        return open.getTimeInMillis();
    }

    public boolean isIn(Chartable o) {
        if (o == null) {
            return false;
        }
        
        return isIn((long) o.getX());
    }

//    public boolean isIn(Price o) {
//        if (o == null) {
//            return false;
//        }
//        
//        return isIn(o.getTime());
//    }

    public boolean isIn(Calendar o) {
        if (o == null) {
            return false;
        }
        
        return isIn(o.getTimeInMillis());
    }

    public boolean isIn(long o) {
        return isIn((double) o);
    }

    public boolean isIn(double o) {
        return o >= open.getTimeInMillis()
                && o < close.getTimeInMillis() + DateUtil.MINUTE_TIME;
    }

    public boolean isInNow() {
        return isIn(DateUtil.getTimeNow());
    }

    public boolean isSameDay(double o) {
        return o >= open.getTimeInMillis() - DateUtil.HOUR_TIME * 12
                && o < close.getTimeInMillis() + DateUtil.MINUTE_TIME;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        TimeZone timeZone = close.getTimeZone();

        if (!timeZone.hasSameRules(TimeZone.getDefault())) {
            sb.append("\n\t").
                    append(DateUtil.getForeignTimeStamp(open, DEFAULT_DATE_FORMAT)).
                    append(" - ").
                    append(DateUtil.getForeignTimeStamp(close, DEFAULT_DATE_FORMAT)).
                    append(" ").
                    append(timeZone.getDisplayName());
        }

        sb.append("\n\t").
                append(DateUtil.getTimeStamp(open, DEFAULT_DATE_FORMAT)).
                append(" - ").
                append(DateUtil.getTimeStamp(close, DEFAULT_DATE_FORMAT)).
                append(" ").
                append(TimeZone.getDefault().getDisplayName());


        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof Session)) {
            return false;
        }

        Session theOther = (Session) obj;

        if (this.getClass() != theOther.getClass()) {
            return false;
        }

        if (this.open == null) {
            if (theOther.open != null) {
                return false;
            }
        } else if (!this.open.equals(theOther.open)) {
            return false;
        }

        if (this.close == null) {
            if (theOther.close != null) {
                return false;
            }
        } else if (!this.close.equals(theOther.close)) {
            return false;
        }

        return true;
    }
}
