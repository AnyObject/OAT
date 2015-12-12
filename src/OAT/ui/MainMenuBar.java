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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import OAT.trading.Main;
import OAT.ui.util.UiUtil;

/**
 *
 * @author Antonio Yip
 */
public class MainMenuBar extends JMenuBar {

    private JFrame parentFrame;

    public MainMenuBar(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        initComponents();
    }

    private void initComponents() {
        addFileMenu();
        addControlMenu();
        addWindowMenu();
    }

    private void addFileMenu() {
        JMenu fileMenu = new JMenu("File");
        add(fileMenu);

        //Save as
        final JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
        saveAsItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Save As...");
            }
        });
        fileMenu.add(saveAsItem);

        //Close Window
        final JMenuItem closeWindowItem = new JMenuItem("Close Window");
        closeWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                java.awt.event.InputEvent.META_MASK));
        closeWindowItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                parentFrame.setVisible(false);
            }
        });
        closeWindowItem.setEnabled(!(parentFrame instanceof MainFrame));
        fileMenu.add(closeWindowItem);
    }

    private void addControlMenu() {
        JMenu controlMenu = new JMenu("Control");
        add(controlMenu);


        //Debug frame        
        controlMenu.add(UiUtil.newMenuItem(
                "Debug",
                KeyStroke.getKeyStroke(KeyEvent.VK_D,
                java.awt.event.InputEvent.META_MASK),
                new Runnable() {

                    @Override
                    public void run() {
                        UiUtil.toggleFrame(Main.debugFrame);
                    }
                }));


        //Backtest
        controlMenu.add(UiUtil.newMenuItem(
                "Backtest",
                KeyStroke.getKeyStroke(KeyEvent.VK_B,
                java.awt.event.InputEvent.META_MASK),
                new Runnable() {

                    @Override
                    public void run() {
                        UiUtil.toggleFrame(Main.backtestFrame);
                    }
                }));

        //Data
        controlMenu.add(UiUtil.newMenuItem(
                "Get Data",
                KeyStroke.getKeyStroke(KeyEvent.VK_G,
                java.awt.event.InputEvent.META_MASK),
                new Runnable() {

                    @Override
                    public void run() {
                        UiUtil.toggleFrame(Main.dataFrame);
                    }
                }));


        //
//        controlMenu.addMenuListener(new MenuListener() {
//
//            @Override
//            public void menuSelected(MenuEvent e) {
//                debugFrameItem.setText((Main.p_Debug_Mode ? "Hide" : "Show") + " Debug Frame");
//            }
//
//            @Override
//            public void menuDeselected(MenuEvent e) {
//            }
//
//            @Override
//            public void menuCanceled(MenuEvent e) {
//            }
//        });
    }

    private void addWindowMenu() {
        JMenu WindowMenu = new JMenu("Window");
        add(WindowMenu);

        //Chart Data
        if (parentFrame instanceof BarChartFrame) {
            final JMenuItem chartDataItem = new JMenuItem("Chart Data");
            chartDataItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                    (InputEvent.META_MASK | InputEvent.SHIFT_MASK)));

            chartDataItem.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    BarChartFrame chartFrame = (BarChartFrame) parentFrame;
                    if (chartFrame.chartDataFrame == null || !chartFrame.chartDataFrame.isDisplayable()) {
                        chartFrame.chartDataFrame =
                                new ChartDataFrame(parentFrame, chartFrame);
                        chartFrame.chartDataFrame.setVisible(true);
                    } else {
                        chartFrame.chartDataFrame.toFront();
                    }

                }
            });
            WindowMenu.add(chartDataItem);
        }
    }
}
