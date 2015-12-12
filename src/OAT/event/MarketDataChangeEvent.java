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

package OAT.event;

import javax.swing.event.ChangeEvent;
import OAT.data.SnapShot;
import OAT.data.SnapShot.Field;

/**
 *
 * @author Antonio Yip
 */
public class MarketDataChangeEvent extends ChangeEvent {

    protected SnapShot snapShot;
    protected Field field;
    protected Object value;

    public MarketDataChangeEvent(SnapShot source, Field field, Object value) {
        super(source);
        this.snapShot = source;
        this.field = field;
        this.value = value;
    }

    public SnapShot getSnapShot() {
        return snapShot;
    }

    public Field getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
