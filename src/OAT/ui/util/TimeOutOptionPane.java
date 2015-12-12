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

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import OAT.util.DateUtil;

/**
 *
 * @author Antonio Yip
 */
public class TimeOutOptionPane {

    public static int showConfirmDialog(Component parentComponent, final Object message, final String title, int optionType,
            int messageType, Object[] options, final Object initialValue, final int countDownSeconds, final boolean showSeconds) {

        final int CLOSED_OPTION = -1;

        final JOptionPane pane = new JOptionPane(message
                + (showSeconds
                ? " " + DateUtil.getDurationStr(countDownSeconds * 1000)
                : ""),
                messageType, optionType, null, options, initialValue);

        pane.setInitialValue(initialValue);
        pane.selectInitialValue();
        final JDialog dialog = pane.createDialog(parentComponent, title);

        new Thread() {

            @Override
            public void run() {

                try {
                    for (int i = (countDownSeconds) - 1; i >= 0; i--) {
                        Thread.sleep(1000);

                        if (dialog.isVisible()) {
                            if (showSeconds) {
                                pane.setMessage(message
                                        + " " + DateUtil.getDurationStr(i * 1000));
                            }
                        } else {
                            break;
                        }
                    }

                    if (dialog.isVisible()) {
                        dialog.setVisible(false);
                    }
                } catch (Throwable t) {
                } finally {
                }
            }
        }.start();



        dialog.setVisible(true);

        Object selectedValue = pane.getValue();

        if (selectedValue == null) {
            return CLOSED_OPTION;
        }
        if (selectedValue.equals("uninitializedValue")) {
            selectedValue = initialValue;
        }
        if (options == null) {
            if (selectedValue instanceof Integer) {
                return ((Integer) selectedValue).intValue();
            }
            return CLOSED_OPTION;
        }
        for (int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) {
            if (options[counter].equals(selectedValue)) {
                return counter;
            }
        }
        return CLOSED_OPTION;
    }
}
