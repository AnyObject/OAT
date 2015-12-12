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

package OAT.sql;

import java.text.ParseException;
import java.util.Calendar;
import OAT.util.DateUtil;

/**
 * Static methods for SQL.
 *
 * @author Antonio Yip
 */
public class SqlUtil {

    /**
     * Substitute SQL script.
     * 
     * @param script
     * @param variables
     * @return
     */
    public static String substitute(String script, String[] variables) {
        String newScript = script.trim();

        if (variables != null) {
            for (int i = 0; i < variables.length; i++) {
                newScript = newScript.replaceAll("#" + i + "#", variables[i].replaceAll("'", ""));
            }
        }

        return newScript;
    }

    /**
     * Return the proper format for SQL statement.
     * 
     * @param values
     * @return
     */
    public static String formatValues(Object... values) {
        if (values == null) {
            return "NULL";
        }

        String dateFormat = DateUtil.DATETIME_FORMAT;
        StringBuilder sb = new StringBuilder();

        for (Object value : values) {
            if (sb.length() > 0) {
                sb.append(", ");
            }

            if (value == null) {
                sb.append("NULL");

            } else if (Boolean.class.isInstance(value)) {
                sb.append((Boolean) value ? "1" : "0");

            } else if (String.class.isInstance(value)) {
                if (((String) value).matches("\\d{8}\\s{1,}\\d{2}:\\d{2}:\\d{2}")) {
                    try {
                        sb.append("'").append(DateUtil.convertDateString((String) value, "yyyyMMdd HH:mm:ss", dateFormat)).append("'");
                    } catch (ParseException ex) {
                        sb.append("'").append(value).append("'");
                    }
                } else if (((String) value).equalsIgnoreCase("null")) {
                    sb.append("NULL");
                } else {
                    sb.append("'").append(value).append("'");
                }

            } else if (Calendar.class.isInstance(value)) {
                sb.append("'").append(DateUtil.getTimeStamp((Calendar) value, dateFormat)).append("'");

            } else if (Long.class.isInstance(value)) {
                sb.append((Long) value == Long.MAX_VALUE ? "NULL" : value);

            } else if (Integer.class.isInstance(value)) {
                sb.append((Integer) value == Integer.MAX_VALUE ? "NULL" : value);

            } else if (Number.class.isInstance(value)) {
                sb.append((Double) value == Double.MAX_VALUE
                        || Double.isNaN((Double) value)
                        ? "NULL" : value);
            } else {
                sb.append("'").append(value).append("'");
            }
        }

        return sb.toString();
    }
}
