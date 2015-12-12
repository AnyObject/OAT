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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 *
 * @author Antonio Yip
 */
public class TextUtil {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final NumberFormat SIMPLE_FORMATTER = new DecimalFormat("0.###");
    public static final NumberFormat BASIC_FORMATTER = new DecimalFormat("#,##0.###");
    public static final NumberFormat PRECISE_FORMATTER = new DecimalFormat("#,##0.#####");
    public static final NumberFormat PRICE_FORMATTER = new DecimalFormat("#,##0.####");
    public static final NumberFormat PRICE_CHANGE_FORMATTER = new DecimalFormat("+#,##0.####;-#,##0.####");
    public static final NumberFormat SIZE_FORMATTER = new DecimalFormat("#,##0.##");
    public static final NumberFormat CURRENCY_FORMATTER = new DecimalFormat("+#,##0.00;-#,##0.00");
    public static final NumberFormat SIMPLE_CURRENCY_FORMATTER = new DecimalFormat("#,##0");
    public static final NumberFormat BIG_CURRENCY_FORMATTER = new DecimalFormat("+#,##0;-#,##0");
    public static final NumberFormat MILLION_CURRENCY_FORMATTER = new DecimalFormat("+#,##0.000M;-#,##0.000M");
    public static final NumberFormat TWO_DIGIT_FORMATTER = new DecimalFormat("00");
    public static final String LOG_SECTION_BREAK = "\n-----------------------------------------";

    /**
     * Return a new string that match the provided length. String with shorter
     * length will be added space at behind. String with longer length will be
     * truncated to the specified length.
     *
     *
     * @param object
     * @param symbol
     * @param length
     * @return
     */
    public static String matchLength(Object object, String symbol, int length) {
        String newString = String.valueOf(object).trim();

        if (length <= 0) {
            return newString;
        }

        if (newString.length() > length) {
            newString = newString.substring(0, length - symbol.length()) + symbol;

        } else {
            while (newString.length() < length) {
                newString += " ";
            }
        }

        return newString;
    }

    /**
     * Convert array into string in table format.
     *
     * @param collection
     * @param separator
     * @param fixedLength
     * @return
     */
    public static String toString(Collection collection, String separator, int fixedLength) {
        Iterator iter;

        if (collection == null || (!(iter = collection.iterator()).hasNext())) {
            return "";
        }

        StringBuilder sb = new StringBuilder(matchLength(String.valueOf(iter.next()), "", fixedLength));

        while (iter.hasNext()) {
            sb.append(separator).append(matchLength(String.valueOf(iter.next()), "", fixedLength));
        }

        return sb.toString();
    }

    /**
     * Convert array into string in table format.
     *
     * @param objects
     * @param separator
     * @param fixedLength
     * @return
     */
    public static String toString(Object[] objects, String separator, int fixedLength) {
        if (objects == null) {
            return "";
        }

        return toString(Arrays.asList(objects), separator, fixedLength);
    }

    /**
     * Return a table from the provided column heads and table.
     *
     * @param columnHeads
     * @param table
     * @param cellSeparator
     * @param cellwidth
     * @return
     */
    public static String toString(String[] columnHeads, Object[][] table, String cellSeparator, int cellwidth) {
        return toString(columnHeads, Arrays.asList(table), cellSeparator, cellwidth);
    }

    /**
     * Return a table from the provided column heads and table.
     *
     * @param columnHeads column names or the first row of the table
     * @param table a 2-dimensional array that holds the data
     * @param cellSeparator separator between columns, recommended "|"
     * @param cellwidth the maximum width (number of character) of each cell
     * @return
     */
    public static String toString(String[] columnHeads, List<Object[]> table, String cellSeparator, int cellwidth) {
        StringBuilder sb = new StringBuilder();

//        sb.append("\n");

        sb.append(toString(columnHeads, cellSeparator, cellwidth)).append(LINE_SEPARATOR);

        for (Object[] objects : table) {
            sb.append(toString(objects, cellSeparator, cellwidth)).append(LINE_SEPARATOR);
        }

        sb.append(table.size()).append(" row");
        if (table.size() > 1) {
            sb.append("s");
        }

        return sb.toString();
    }

    /**
     * Converts a collection of objects into a string using the provide
     * separator.
     *
     * @param collection
     * @param separator
     * @return
     */
    public static String toString(Collection collection, String separator) {
        Iterator iter;

        if (collection == null || (!(iter = collection.iterator()).hasNext())) {
            return "";
        }

        StringBuilder sb = new StringBuilder(String.valueOf(iter.next()));

        while (iter.hasNext()) {
            sb.append(separator).append(String.valueOf(iter.next()));
        }

        return sb.toString();
    }

    /**
     * Converts a collection of objects into a string using ", ".
     *
     * @param collection
     * @return
     */
    public static String toString(Collection collection) {
        return toString(collection, ", ");
    }

    /**
     * Converts an array of objects into a string using the provide separator.
     *
     * @param objects
     * @param separator
     * @return
     */
    public static String toString(Object[] objects, String separator) {
        if (objects == null) {
            return "";
        }

        return toString(Arrays.asList(objects), separator);
    }

    /**
     * Converts an array of objects into a string using ", ".
     *
     * @param objects
     * @return
     */
    public static String toString(Object[] objects) {
        return toString(objects, ", ");
    }

    /**
     * Converts a 2-D array of objects into a string using the provided line
     * separator lineSeparator and column separator colSeparator.
     *
     * @param objects
     * @param lineSeparator
     * @param colSeparator
     * @return
     */
    public static String toString(Object[][] objects, String lineSeparator, String colSeparator) {
        if (objects == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < objects.length; i++) {
            if (sb.length() > 0) {
                sb.append(lineSeparator);
            }

            sb.append(toString(objects[i], colSeparator));
        }

        return sb.toString();
    }

    /**
     * Converts a 2-D array of objects into a string using line separator "\n"
     * and column separator ": ".
     *
     * @param objects
     * @return
     */
    public static String toString(Object[][] objects) {
        return toString(objects, LINE_SEPARATOR, ": ");
    }

    /**
     * Converts a map into a string using separator ";" and column separator ":
     * ".
     *
     * @param <K> key
     * @param <V> value
     * @param map any map
     * @return
     */
    public static <K, V> String toString(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        List<String> entries = new ArrayList<String>();

        for (Map.Entry entry : map.entrySet()) {
            entries.add(entry.getKey() + ": " + entry.getValue());
        }

        return toString(entries, ";");
    }

    /**
     * Counts the occurrence of the first string in the second string.
     *
     * @param occur
     * @param string
     * @return
     */
    public static int count(String occur, String string) {
        return string.replaceAll("[^" + occur + "]", "").length();
    }

    /**
     * Replace "." and white-spaces by "_"
     *
     * @param symbol symbol text
     * @return new string
     */
    public static String convertSymbol(String symbol) {
        return symbol.replace(".", " ").replaceAll("\\b\\s{1,}\\b", "_");
    }

    /**
     * Get an unique id using time.
     *
     * @return
     */
    public static String getRandomId() {
        return "" + DateUtil.getTimeNow() + (int) (Math.random() * 1000);
    }

    /**
     * Extract the last portion of text after a ".".
     *
     * @param string
     * @return
     */
    public static String getSimpleName(String string) {
        String[] names = string.split("\\.");

        return names[names.length - 1];
    }

    /**
     * Returns a HTML formated table.
     *
     * @param headers
     * @param cells
     * @return
     */
    public static String htmlTable(Object[] headers, Object[][] cells) {
        StringBuilder sb = new StringBuilder();

        sb.append("<table><tr ALIGN=LEFT>");
        for (Object object : headers) {
            sb.append("<th>").append(object).append("</th>");
        }
        sb.append("</tr>");


        for (Object[] row : cells) {
            sb.append("<tr>");
            for (Object object : row) {
                sb.append("<td>").append(object).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }
}
