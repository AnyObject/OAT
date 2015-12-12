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

import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class DateTimeCellRenderer extends GeneralCellRenderer {

    @Override
    protected String getDateFormat() {
        return DateUtil.SIMPLE_DATETIME_FORMAT;
    }

    @Override
    protected int getAlignment() {
        return CENTER;
    }
}
