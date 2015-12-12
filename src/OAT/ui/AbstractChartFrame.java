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

package OAT.ui;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * Basic frame for any chart.
 *
 * @author Antonio Yip
 */
public abstract class AbstractChartFrame extends javax.swing.JFrame {

    private ChartPanel chartPanel;

    /**
     * Create a packed chart frame with default size and theme.
     *
     * @param title displayed in the title bar
     * @param jFreeChart  
     */
    public AbstractChartFrame(String title, JFreeChart jFreeChart) {
        super(title);
        chartPanel = new ChartPanel(jFreeChart);
        initComponent();
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    protected abstract boolean disposeOnClose();

    protected abstract boolean hideWhenFocusLost();

    protected abstract Dimension getChartPanelSize();

    protected abstract void init();

    private void initComponent() {
        setJMenuBar(new MainMenuBar(this));

        chartPanel.setPreferredSize(getChartPanelSize());
        setContentPane(chartPanel);

        setDefaultCloseOperation(disposeOnClose()
                ? WindowConstants.DISPOSE_ON_CLOSE
                : WindowConstants.HIDE_ON_CLOSE);

        if (hideWhenFocusLost()) {
            addFocusListener(new FocusAdapter() {

                @Override
                public void focusLost(FocusEvent e) {
                    setVisible(false);
                }
            });
        }

        init();

        //pack and display
        pack();
    }
}
