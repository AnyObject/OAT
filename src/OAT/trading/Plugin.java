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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import OAT.event.Listenable;
import OAT.trading.classification.Predictor;
import OAT.util.GeneralUtil;
import OAT.util.Loggable;
import OAT.util.PrefNode;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public abstract class Plugin extends Listenable implements Loggable, PrefNode<Plugin> {

    private String lastLogOnce;
    private Plugin parent;
    private Preferences preferences;
    private final List<Plugin> plugins = new ArrayList<Plugin>();

    public static Plugin newPlugin(Plugin parent, String className) throws Exception {
//        if (className == null || className.isEmpty()) {
//            throw new UnsupportedOperationException("className cannot be null or empty");
//        }
//
////        String packageName = StrategyPlugin.class.getPackage().getName() + ".";
//        Class c = Class.forName(className);
//        Constructor constructor = c.getConstructor();
        Plugin newPlugin = (Plugin) GeneralUtil.forName(Plugin.class, className);
        newPlugin.setParent(parent);

        return newPlugin;
    }

    //
    //Loggable
    //
    @Override
    public boolean isLoggable(Level level) {
        return getLogger() != null && getLogger().isLoggable(level);
    }

    @Override
    public void log(Level level, String msg, Object[] params) {
        if (isLoggable(level)) {
            getLogger().log(level, getLogPrefix() + msg, params);
        }
    }

    @Override
    public void log(Level level, String msg, Object param1) {
        if (isLoggable(level)) {
            getLogger().log(level, getLogPrefix() + msg, param1);
        }
    }

    @Override
    public void log(Level level, String msg) {
        if (isLoggable(level)) {
            getLogger().log(level, "{0}{1}", new Object[]{getLogPrefix(), msg});
        }
    }

    @Override
    public void log(Level level, String msg, Throwable throwable) {
        if (isLoggable(level)) {
            getLogger().log(level, getLogPrefix() + msg, throwable);
        }
    }

    public void logOnce(Level level, String string) {
        if (!string.equals(lastLogOnce)) {
            log(level, lastLogOnce = string);
        }
    }

    public void clearLogOnce() {
        lastLogOnce = null;
    }

    //
    //Getters
    //
    /**
     * Get parent.
     *
     * @return
     */
    public final Plugin getParent() {
        return parent;
    }

    @Override
    public final Plugin getChild(int index) {
        return plugins.get(index);
    }

    @Override
    public final List<Plugin> getChildren() {
        return plugins;
    }

    @Override
    public final int indexOf(Plugin childNode) {
        return plugins.indexOf(childNode);
    }

    @Override
    public Preferences getPreferences() {
        return preferences;
    }

    /**
     * Get default node name.
     *
     * @return
     */
    public String getDefaultNodeName() {
        return getClass().getSimpleName();
    }

    @Override
    public Preferences getChildrenPreferences() {
        String name = getChildrenNodeName();

        if (name == null || name.isEmpty()) {
            return null;
        }

        return getPreferences().node(getChildrenNodeName());
    }

    /**
     * Get Children node String.
     *
     * @return
     */
    public abstract String getChildrenNodeName();

    //
    //Setters
    //
    /**
     * Set parent.
     *
     * @param parent
     */
    public final void setParent(Plugin parent) {
        this.parent = parent;
    }

    /**
     * Set preferences.
     *
     * @param preferences
     */
    public void setPreferences(Preferences preferences) {
        if (this.preferences != null
                && !preferences.absolutePath().equals(this.preferences.absolutePath())) {
            try {
                this.preferences.removeNode();
            } catch (Exception e) {
            }
        }

        this.preferences = preferences;
    }

    /**
     * Add Child (duplication not allowed).
     *
     * @param child
     */
    @Override
    public boolean addChild(Plugin child) {
        if (child == null || plugins.contains(child)) {
            return false;
        }

        return plugins.add(child);
    }

    @Override
    public final void savePreferences() {
        if (getParent() != null) {
            setPreferences(getParent().getChildrenPreferences().node(
                    TextUtil.TWO_DIGIT_FORMATTER.format(getParent().indexOf(this))
                    + "." + getDefaultNodeName()));
        }

        getPreferences().put(Parameters.CLASS_KEY, this.getClass().getName());

        try {
            getDeclaredParameters().save(getPreferences());
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

//        System.out.println(getPreferences().absolutePath());

        for (Plugin child : getChildren()) {
            child.savePreferences();
        }
    }

    @Override
    public final void loadPreferences() {
        try {
            setDeclaredParameters(new Parameters(getPreferences()));
            if (getChildrenPreferences() == null) {
                return;
            }

            for (String childName : getChildrenPreferences().childrenNames()) {
                Preferences pref = getChildrenPreferences().node(childName);
                Plugin plugin = Plugin.newPlugin(this, new Parameters(pref).getClassName());
                plugin.setPreferences(pref);

                if (plugin != null) {
                    if (addChild(plugin)) {
                        plugin.loadPreferences();
                    } else {
                        getChild(indexOf(plugin)).loadPreferences();
                    }
                }
            }
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get root and children's parameters.
     *
     * @return
     */
    public final Parameters[] getParameters() {
        List<Plugin> childrens = getChildren();

        Parameters[] params = new Parameters[childrens.size() + 1];
        params[0] = getDeclaredParameters();

        for (int i = 0; i < childrens.size(); i++) {
            params[i + 1] = childrens.get(i).getDeclaredParameters();
        }

        return params;
    }

    /**
     * Get root and children's parameters in array.
     *
     * @return
     */
    public final Object[][] getParametersArray() {
        List<Object[]> al = new ArrayList<Object[]>();

        al.addAll(Arrays.asList(getDeclaredParameters().toArray()));

        for (int i = 0; i < getChildren().size(); i++) {
            Plugin child = getChild(i);

            al.addAll(Arrays.asList(child.getDeclaredParameters().toArray(
                    i + "." + child.getDefaultNodeName() + ".")));
        }

        return al.toArray(new Object[0][0]);
    }

    /**
     * Set root and children's parameters.
     *
     * @param params
     */
    public final void setParameters(Parameters[] params) {
        setDeclaredParameters(params[0]);

        if (params.length > 1) {
            for (int i = 1; i < params.length; i++) {
                Plugin plugin = null;

                if (i - 1 < getChildren().size()) {
                    plugin = getChild(i - 1);
                } else {
                    try {
                        plugin = Plugin.newPlugin(this, params[i].getClassName());
                    } catch (Exception ex) {
                        log(Level.SEVERE, null, ex);
                    }
                    addChild(plugin);
                }

                plugin.setDeclaredParameters(params[i]);
            }
        }

        fireChanged();
    }

    public Parameters getDeclaredParameters() {
        Parameters param = new Parameters();
        param.setClassName(this.getClass().getName());

        for (Field field : GeneralUtil.getFields(this)) {
            if (!isParameter(field)) {
                continue;
            }

            field.setAccessible(true);

            try {
                param.put(getParameterName(field), field.get(this));
            } catch (Exception ex) {
                log(Level.SEVERE, null, ex);
            }
        }

        return param;
    }

    /**
     * Set declared parameters.
     *
     * @param param
     */
    public void setDeclaredParameters(Parameters param) {
        for (String key : param.keySet()) {
            for (Field field : GeneralUtil.getFields(this)) {
                if (!isParameter(field)) {
                    continue;
                }

                if (key.equalsIgnoreCase(getParameterName(field))) {
                    field.setAccessible(true);

                    try {
                        field.set(this, GeneralUtil.parse(param.get(key), field.getType()));
                    } catch (Exception ex) {
                        log(Level.SEVERE, null, ex);
                    }

                    break;
                }
            }
        }

        if (this instanceof Predictor) {
            ((Predictor) this).loadModel();
        }
    }

    public void logParameters() {
        Class[] interfaces = getClass().getInterfaces();

        String name = (interfaces.length > 0
                ? TextUtil.toString(interfaces)
                : getClass().getGenericSuperclass()).toString();

        if (name.contains("tou.")) {
            name = name.replaceAll("class ", "").replaceAll("interface ", "");
        } else {
            name = "";
        }

        log(Level.INFO, "Parameters: {0} {1}",
                new Object[]{name, getDeclaredParameters()});
    }

    private String getParameterName(Field field) {
        return field.getName().replaceFirst("p_", "").replace("_", " ");
    }

    private boolean isParameter(Field field) {
        return field.getName().startsWith("p_");
    }
}
