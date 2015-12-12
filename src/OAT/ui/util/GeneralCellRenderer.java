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

import java.util.Calendar;
import java.util.Date;
import javax.swing.table.DefaultTableCellRenderer;
import OAT.util.DateUtil;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class GeneralCellRenderer extends DefaultTableCellRenderer {

    @Override
    public void setValue(Object value) {
        setHorizontalAlignment(getAlignment());

        if (value == null) {
            setText("");
            return;
        }

        long timeL = 0;

        if (value instanceof Long) {
            timeL = ((Number) value).longValue();

        } else if (value instanceof Calendar) {
            timeL = ((Calendar) value).getTimeInMillis();

        } else if (value instanceof Date) {
            timeL = ((Date) value).getTime();

        } else if (value instanceof Number) {
            if (Double.isNaN(((Number) value).doubleValue())) {
                setText("");
                return;
            } else {
                setText(TextUtil.SIMPLE_FORMATTER.format(value));
                return;
            }

        } else {
            setText(value.toString());
            return;
        }

        if (timeL > 0) {
            String dateFormat = getDateFormat();

            if (timeL % 1000 != 0) {
                dateFormat += ".SSS";
            }

            setText(DateUtil.getTimeStamp(timeL, dateFormat));
        } else {
            setText("");
        }
    }

    protected String getDateFormat() {
        return DateUtil.WEEKDAY_TIME_FORMAT;
    }

    protected int getAlignment() {
        return LEFT;
    }
//     @Override
//    public void setValue(Object value) {
//        if (value == null) {
//            setText("");
//            return;
//        }
//
//        if (value instanceof Date) {
//            setText(DateUtil.getTimeStamp(((Date) value).getTime(),
//                    DateUtil.EXECUTION_DATETIME_FORMAT));
//
//        } else if (value instanceof Number) {
//            if (Double.isNaN(((Number) value).doubleValue())) {
//                setText("");
//            } else {
//                setText(TextUtil.SIMPLE_FORMATTER.format(value));
//                setHorizontalAlignment(RIGHT);
//            }
//
//        } else {
//            setText(value.toString());
//        }
//
//        if (value.getClass() == String.class) {
//            setToolTipText(getText());
//        }
//    }
}
