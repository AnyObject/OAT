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

import com.ib.client.ContractDetails;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import OAT.data.ChartType;
import OAT.trading.Account;
import OAT.trading.Parameters;
import OAT.trading.Side;
import OAT.trading.Trend;
import OAT.trading.client.IbClient;

/**
 *
 * @author Antonio Yip
 */
public class GeneralUtil {

    /**
     * Test if a comparable object is within the range.
     *
     * @param range upper and lower bound
     * @param object comparable object
     * @return
     */
    public static boolean isWithin(Comparable[] range, Comparable object) {
        if (range.length < 2) {
            return false;
        }

        return object.compareTo(range[0]) >= 0
                && object.compareTo(range[range.length - 1]) <= 0;
    }

    /**
     * Join two arrays.
     *
     * @param <T>
     * @param array1
     * @param array2
     * @return
     */
    public static <T> T[] join(T[] array1, T[] array2) {
        ArrayList<T> newArray = new ArrayList<T>();
        newArray.addAll(Arrays.asList(array1));
        newArray.addAll(Arrays.asList(array2));

        return newArray.toArray(Arrays.copyOf(array1, 0));
    }

    /**
     * Add a new item at the end of the array.
     *
     * @param <T>
     * @param array1
     * @param newItem
     * @return
     */
    public static <T> T[] join(T[] array1, T newItem) {
        ArrayList<T> newArray = new ArrayList<T>();
        newArray.addAll(Arrays.asList(array1));
        newArray.add(newItem);

        return newArray.toArray(Arrays.copyOf(array1, 0));
    }

    /**
     * Construct a sub array from the startIndex (inclusive) to the endIndex
     * (exclusive).
     *
     * @param <T>
     * @param array
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static <T> T[] subArray(T[] array, int startIndex, int endIndex) {
        List<T> subArray = new ArrayList<T>();

        for (int i = startIndex; i < endIndex; i++) {
            subArray.add(array[i]);
        }

        return subArray.toArray(Arrays.copyOf(array, 0));
    }

    /**
     * Used by backtest.
     *
     * @param steps
     * @return
     */
    public static Object[][] extractCombinations(List[] steps) {
        Object[][] table;
        int combinationCount = 1;

        for (List list : steps) {
            combinationCount *= list.size();
        }

        table = new Object[combinationCount][steps.length];

        int f = combinationCount;

        for (int j = 0; j < steps.length; j++) {
            if (steps[j].isEmpty()) {
                continue;
            }

            f /= steps[j].size();

            for (int i = 0; i < combinationCount; i++) {
                table[i][j] = steps[j].get(((int) i / f) % steps[j].size());
            }
        }

        return table;
    }

    /**
     * Construct a list of parameters from the provided keys and values.
     *
     * @param keys
     * @param values
     * @return
     */
    public static Parameters[] getParameters(String[] keys, Object[] values) {
        List<Parameters> params = new ArrayList<Parameters>();
        params.add(new Parameters());

        for (int i = 0; i < keys.length; i++) {
            String[] key = keys[i].split("\\.");
            int j = 0;

            if (key.length > 1) {
                j = Integer.valueOf(key[0]) + 1;

                while (params.size() <= j) {
                    params.add(new Parameters());
                }
            }

            params.get(j).put(key[key.length - 1],
                    i < values.length ? values[i] : null);
        }

        return params.toArray(new Parameters[0]);
    }

    /**
     * Pause the current thread.
     *
     * @param wait milliseconds
     * @param logger logger
     * @param message message to log when called
     */
    public static void pause(final long wait, final ThreadLogger logger, final String message) {
        new Waiting(wait, 0, logger) {

            @Override
            public boolean waitWhile() {
                return true;
            }

            @Override
            public void retry() {
            }

            @Override
            public String message() {
                return message;
            }

            @Override
            public void timeout() {
            }
        };
    }

    /**
     * Returns the actual contract value.
     *
     * @param cd contractDetails
     * @param price the quoted price
     * @return
     */
    public static double getContractValue(ContractDetails cd, double price) {
        if (cd == null) {
            return price;
        }

        return price
                * (Integer.valueOf(cd.m_summary.m_multiplier)
                / cd.m_priceMagnifier);
    }

    /**
     * Construct an iterator from an array.
     *
     * @param <T>
     * @param array array of generic T
     * @return a new iterator
     */
    public static <T> Iterator<T> getIterator(T[] array) {
        return Arrays.asList(array).iterator();
    }

    /**
     * Parse an object to the desired type.
     *
     * @param obj
     * @param type
     * @return
     */
    public static Object parse(Object obj, Class type) {
        if (obj != null && !type.isInstance(obj)) {
            if (type == String.class) {
                return obj.toString();

            } else if (type == Boolean.class || type == boolean.class) {
                if (obj instanceof Number) {
                    return (Double) obj >= 1;
                }

                return Boolean.parseBoolean(obj.toString());

            } else if (type == Double.class || type == double.class) {
                return Double.parseDouble(obj.toString());

            } else if (type == Integer.class || type == int.class) {
                return (int) Double.parseDouble(obj.toString());

            } else if (type == Long.class || type == long.class) {
                return Long.parseLong(obj.toString());

            } else if (type == Account.class) {
                return Account.valueOf(obj.toString());

            } else if (type == IbClient.Platform.class) {
                return IbClient.Platform.valueOf(obj.toString());

            } else if (type == ChartType.class) {
                return ChartType.valueOf(obj.toString());

            } else if (type == Side.class) {
                return Side.valueOf(obj.toString());

            } else if (type == Level.class) {
                return Level.parse(obj.toString());

            } else if (type == Trend.class) {
                return Trend.valueOf(obj.toString());

            } else if (type == Dimension.class) {
                String[] s = obj.toString().split("[,;]");

                try {
                    return new Dimension(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
                } catch (Exception e) {
                    return new Dimension();
                }
            }
        }

        return obj;
    }

    /**
     * Returns an array of fields of an object by reflection.
     *
     * @param obj
     * @return
     */
    public static Field[] getFields(Object obj) {
        List<Field> fields = new ArrayList<Field>();
        Class theClass = obj.getClass();

        while (theClass != null) {
            fields.addAll(Arrays.asList(theClass.getDeclaredFields()));
            theClass = theClass.getSuperclass();
        }

        return fields.toArray(new Field[0]);
    }

    /**
     * Returns the transpose of an array.
     *
     * @param a
     * @return
     */
    public static Object[][] transpose(Object[][] a) {
        int r = a.length;
        int c = a[0].length;

        Object[][] t = new Object[c][r];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                t[j][i] = a[i][j];
            }
        }

        return t;
    }

    /**
     * Creates a new instance of an object given it's class name.
     *
     * @param classType the class that the instantiated object should be
     * assignable to -- an exception is thrown if this is not the case
     * @param className the fully qualified class name of the object May be
     * null. Any options accepted by the object will be removed from the array.
     * @return the newly created object, ready for use.
     * @exception Exception if the class name is invalid, or if the class is not
     * assignable to the desired class type, or the options supplied are not
     * acceptable to the object
     */
    public static Object forName(Class classType, String className) throws Exception {

        Class c = null;
        try {
            c = Class.forName(className);
        } catch (Exception ex) {
            throw new Exception("Can't find class called: " + className);
        }

        if (!classType.isAssignableFrom(c)) {
            throw new Exception(classType.getName() + " is not assignable from "
                    + className);
        }

        return c.newInstance();
    }

    public static <E> List<E> subListMaxSize(List<E> list, int maxSize) {
        return subListMaxSize(list, 0, maxSize);
    }

    public static <E> List<E> subListMaxSize(List<E> list, int fromIndex, int maxSize) {
        return list.subList(
                Math.min(list.size(), fromIndex),
                Math.min(list.size(), fromIndex + maxSize));
    }
    
    public static String getCpuUsage(Sigar sigar) {
        try {
            return "CPU usage combined = " + TextUtil.SIMPLE_FORMATTER.format(sigar.getCpuPerc().getCombined() * 100) + "%"
                    //                    + ", user="+sigar.getCpuPerc().getUser()
                    //                    + ", system="+sigar.getCpuPerc().getSys()
                    //                    + ", idle="+sigar.getCpuPerc().getIdle()
                    + "\n\tUsed memory = " + (sigar.getMem().getActualUsed() / 1024 / 1024) + " MB"
                    + "\n\tFree memory = " + (sigar.getMem().getActualFree() / 1024 / 1024) + " MB";

        } catch (Exception ex) {
            return "CPU information not found: " + ex.getMessage();
        }
    }
}
