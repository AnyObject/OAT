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

import java.util.List;
import java.util.prefs.Preferences;

/**
 *
 * @param <T>
 * @author Antonio Yip
 */
public interface PrefNode<T extends PrefNode> {

    /**
     * Return the child at the index.
     * @param index
     * @return
     */
    public abstract T getChild(int index);

    /**
     * Return a list of Children.
     * @return
     */
    public abstract List<T> getChildren();

    /**
     * Return the preference node.
     * @return
     */
    public abstract Preferences getPreferences();

    /**
     * Return the children preference node.
     * @return
     */
    public abstract Preferences getChildrenPreferences();

    /**
     * Return the index of the child.
     * @param childNode
     * @return
     */
    public abstract int indexOf(T childNode);

    /**
     * Add Child.
     * @param childNode
     * @return
     */
    public abstract boolean addChild(T childNode);

    /**
     * Save preferences to disk.
     * @throws Exception
     */
    public abstract void savePreferences() throws Exception;

    /**
     * Load preferences from disk.
     * @throws Exception
     */
    public abstract void loadPreferences() throws Exception;
}
