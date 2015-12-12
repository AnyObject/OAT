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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.*;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import OAT.ui.util.DefaultTheme;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class Parameters extends HashMap<String, Object> {

    public static final String CLASS_KEY = "Class";
    private List<String> keys = new ArrayList<String>();

    public Parameters() {
    }

    public Parameters(Preferences pref) throws BackingStoreException {
        for (String key : pref.keys()) {
            put(TextUtil.getSimpleName(key), pref.get(key, null));
        }
    }

    public Parameters(String[] keys, Object[] values) {
        for (int i = 0; i < keys.length; i++) {
            put(keys[i], i < values.length ? values[i] : null);
        }
    }

    @Override
    public Object put(String key, Object value) {
        addKey(key);

        Object storedValue;

        if (value instanceof Enum) {
            storedValue = ((Enum) value).name();

        } else if (value instanceof Level) {
            storedValue = ((Level) value).getName();

        } else if (value instanceof DefaultTheme) {
            storedValue = ((DefaultTheme) value).getName();

        } else if (value instanceof Font) {
            storedValue = ((Font) value).getSize();

        } else if (value instanceof Color) {
            storedValue = ((Color) value).getRGB();

        } else if (value instanceof Dimension) {
            storedValue = "" + ((Dimension) value).width  
                     + "," + ((Dimension) value).height ;

        } else {
            storedValue = value;
        }

        return super.put(key, storedValue);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (String key : m.keySet()) {
            addKey(key);
        }

        super.putAll(m);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String key : getKeys()) {
            String value = String.valueOf(get(key));
            if (value.length() > 70) {
                value = value.substring(0, 68) + "...";
            }

            sb.append("\n\t").append(key).append(": ").append(value);
        }

        return sb.toString();
    }

    public Object[][] toArray(String prefix) {
        List<Object[]> al = new ArrayList<Object[]>();

        for (String key : getKeys()) {
            al.add(new Object[]{prefix + key, get(key)});
        }

        return al.toArray(new Object[0][0]);
    }

    public Object[][] toArray() {
        return toArray("");
    }

    public List<String> getKeys() {
        return keys;
    }

    public Iterator<String> getKeysIterator() {
        return keys.iterator();
    }

    public String getClassName() {
        return getString(CLASS_KEY, null);
    }

    public void setClassName(String className) {
        put(CLASS_KEY, className);
    }

    public String getString(String key, String defaultValue) {
        Object value = get(key);

        if (value instanceof String) {
            return (String) value;
        } else {
            try {
                return value.toString();
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            try {
                return Boolean.parseBoolean(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }

    public double getDouble(String key, double defaultValue) {
        Object value = get(key);

        if (value instanceof Double) {
            return (Double) value;
        } else {
            try {
                return Double.parseDouble(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }

    public int getInteger(String key, int defaultValue) {
        return (int) getDouble(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        Object value = get(key);

        if (value instanceof Long) {
            return (Long) value;
        } else {
            try {
                return Long.parseLong(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }
    
//    public Dimension getDimension(String key, Dimension defaultValue) {
//        Object value = get(key);
//
//        if (value instanceof Dimension) {
//            return (Dimension) value;
//        } else {
//            try {
//                return new Dimension();
//            } catch (Exception e) {
//                return defaultValue;
//            }
//        }
//    }

    public void save(Preferences pref) throws BackingStoreException {
        pref.clear();

        for (String key : keySet()) {
            Object value = get(key);

            if (value != null) {
                pref.put(key, value.toString());
            }
        }

        pref.sync();

//        System.out.println(pref.absolutePath());
    }

    private void addKey(String key) {
        if (!keys.contains(key)) {
            keys.add(key);
        }
    }
}
