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

package OAT.ui.util;

import java.util.ArrayList;
import java.util.TimeZone;
import org.jfree.chart.axis.SegmentedTimeline;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class Timeline extends SegmentedTimeline {

//    public static final long FIVE_MINUTE_SEGMENT_SIZE = 5 * MINUTE_SEGMENT_SIZE;
    public static final int TICK_TIME_SEGMENT = 1000;
    public TimeZone timeZone;

//    public Timeline(long segmentSize, int segmentsIncluded, int segmentsExcluded) {
//        super(segmentSize, segmentsIncluded, segmentsExcluded);
//    }
    public Timeline(int segmentSize, TimeZone timeZone) {
        super(segmentSize, (int) DAY_SEGMENT_SIZE / segmentSize, 0);
        this.timeZone = timeZone;

//            setBaseTimeline(newMondayThroughFridayTimeline(timeZone));
    }

//    public static Timeline newNineToFiveTimeline(int barSize, TimeZone timeZone) {
//        return newDailyTimeLine(barSize, timeZone, "09:00", "17:00");
//    }
//    public static Timeline newDefaultDailyTimeline(int barSize, TimeZone timeZone) {
//        return newDailyTimeLine(barSize, timeZone, "00:00", "24:00");
//    }
//    public static Timeline newContinuousTimeline(int barSize, TimeZone timeZone) {
//        Timeline timeline = new Timeline(barSize, (int) (DAY_SEGMENT_SIZE / barSize), 0);
//        timeline.setStartTime(-DateUtil.getTimeDiff(timeZone));
//        timeline.setTimeZone(timeZone);
//        return timeline;
//    }
//
//    public static Timeline newMondayThroughFridayTimeline(int barSize, TimeZone timeZone) {
//        Timeline timeline = new Timeline(barSize, (int) (DAY_SEGMENT_SIZE / barSize), 0);
//        timeline.setStartTime(-DateUtil.getTimeDiff(timeZone));
//        timeline.setTimeZone(timeZone);
//        timeline.setBaseTimeline(newMondayThroughFridayTimeline(timeZone));
//        return timeline;
//    }
//
    public static SegmentedTimeline newMondayThroughFridayTimeline(TimeZone timeZone) {
        SegmentedTimeline timeline = newMondayThroughFridayTimeline();
        timeline.setStartTime(timeline.getStartTime() - DateUtil.getTimeDiff(timeZone));
        return timeline;
    }

//    public static Timeline newDailyTimeLine(int barSize, TimeZone timeZone, String open, String close) {
//        Calendar timeOpen = null;
//        Calendar timeClose = null;
//        try {
//            timeOpen = DateUtil.getCalendarDate(open, "HH:mm", timeZone);
//            timeClose = DateUtil.getCalendarDate(close, "HH:mm", timeZone);
//        } catch (ParseException ex) {
//        }
//
//        long msOpen = timeOpen.getTimeInMillis() - timeZone.getDSTSavings();
//        long msClose = timeClose.getTimeInMillis();
//
//        long msIncl = (msClose > msOpen ? 0 : DAY_SEGMENT_SIZE) + msClose - msOpen;
//        int segIncl = (int) msIncl / barSize;
//        int segExcl = (int) (DAY_SEGMENT_SIZE - msIncl) / barSize;
//
//        Timeline timeline = new Timeline(barSize, segIncl, segExcl);
//        timeline.setTimeZone(timeZone);
//
//        timeline.setBaseTimeline(newMondayThroughFridayTimeline(timeZone));
//        timeline.setStartTime(msOpen);
//
//        return timeline;
//    }
    public void clearExceptionSegments() {
        setExceptionSegments(new ArrayList());
    }

//    public void setTimeZone(TimeZone timeZone) {
//        this.timeZone = timeZone;
//    }
    public TimeZone getTimeZone() {
        return timeZone;
    }
}
