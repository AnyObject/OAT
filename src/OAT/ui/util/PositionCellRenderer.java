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

import javax.swing.table.DefaultTableCellRenderer;
import OAT.util.TextUtil;

/**
 *
 * @author Antonio Yip
 */
public class PositionCellRenderer extends DefaultTableCellRenderer {

    public PositionCellRenderer() {
        super();
        setHorizontalAlignment(RIGHT);
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            setText("");
            return;
        }

        if (value instanceof Number) {
            double size = ((Number) value).doubleValue();

            if (Math.abs(size) >= 1000000) {
                setText("" + TextUtil.SIZE_FORMATTER.format(size / 1000000) + "M");
            } else if (Math.abs(size) >= 100000) {
                setText("" + TextUtil.SIZE_FORMATTER.format(size / 1000) + "K");
            } else {
                setText("" + TextUtil.SIZE_FORMATTER.format(size));
            }
        } else {
            setText(value.toString());
        }
    }
}
