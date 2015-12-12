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

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import OAT.data.Chart;
import OAT.data.ChartDataset;
import OAT.data.ContractChart;
import OAT.data.TickChart;
import OAT.trading.Main;

/**
 *
 * @author Antonio Yip
 */
public class UiUtil {

    public static final CurrencyCellRenderer CURRENCY_CELL_RENDERER = new CurrencyCellRenderer();
    public static final DollarCellRenderer DOLLAR_CELL_RENDERER = new DollarCellRenderer();
    public static final GeneralCellRenderer GENERAL_CELL_RENDERER = new GeneralCellRenderer();
    public static final RightAlignedGeneralCellRenderer RIGHT_ALIGNED_GENERAL_CELL_RENDERER = new RightAlignedGeneralCellRenderer();
    public static final CenterAlignedGeneralCellRenderer CENTER_ALIGNED_GENERAL_CELL_RENDERER = new CenterAlignedGeneralCellRenderer();
    public static final DateTimeCellRenderer DATE_TIME_CELL_RENDERER = new DateTimeCellRenderer();
    public static final PositionCellRenderer POSITION_CELL_RENDERER = new PositionCellRenderer();
    public static final PriceCellRenderer PRICE_CELL_RENDERER = new PriceCellRenderer();
    public static final QuotePriceCellRenderer QUOTE_PRICE_CELL_RENDERER = new QuotePriceCellRenderer();
    public static final PriceChangeCellRenderer PRICE_CHANGE_CELL_RENDERER = new PriceChangeCellRenderer();
    public static final SizeCellRenderer SIZE_CELL_RENDERER = new SizeCellRenderer();
    public static final int MENU_BAR_HEIGHT = 22;

    public static void useCrossPlatformLookAndFeel(Component object) {
        String laf;
        boolean useNative = false;

        if (useNative) {
            laf = UIManager.getSystemLookAndFeelClassName();
        } else {
            laf = UIManager.getCrossPlatformLookAndFeelClassName();
        }

        try {
            UIManager.setLookAndFeel(laf);
            SwingUtilities.updateComponentTreeUI(object);
        } catch (Exception e) {
            System.err.println("Couldn't use the " + laf
                    + "look and feel: " + e);
        }
    }

    public static DefaultMutableTreeNode addChildNode(DefaultMutableTreeNode parent, Object child) {
        HashSet hashSet = new HashSet();
        DefaultMutableTreeNode childNode;

        for (int i = 0; i
                < parent.getChildCount(); i++) {
            hashSet.add(parent.getChildAt(i).toString());
        }

        if (hashSet.add(child.toString())) {
            childNode = new DefaultMutableTreeNode(child);
            parent.insert(childNode, parent.getChildCount());
        } else {
            int i = 0;
            do {
                childNode = (DefaultMutableTreeNode) parent.getChildAt(i++);
                //System.out.println("childNode: " + childNode);
            } while (!childNode.toString().equals(child.toString()));
        }

        return childNode;
    }

    public static void clearTreeIcon(JTree tree) {
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);
        //renderer.setLeafIcon(null);
        tree.setCellRenderer(renderer);
    }

    public static void setupColumns(JTable table) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            TableColumn column = table.getColumnModel().getColumn(i);
            Class columnClass = table.getColumnClass(i);
            String columnName = table.getColumnName(i);
            renderer.setToolTipText(columnName);

            if (columnClass.getSuperclass().equals(Number.class)) {
                renderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
            }

            if (table.isEnabled()) {
                if (columnName.equals("Currency")) {
                    //addComboCell(column, Product.CURRENCIES);
                } else if (columnName.equals("Holidays")) {
                    //column.setCellEditor(new FileChooserCellEditor());
                }
            }

            column.setCellRenderer(renderer);
        }
    }

    public static void addComboCell(TableColumn column, Object[] items) {
        column.setCellEditor(new DefaultCellEditor(new JComboBox(items)));
    }

    public static void triggerTextField(JTextField textField, JCheckBox checkBox) {
        if (checkBox.isSelected()) {
            textField.setDisabledTextColor(Color.LIGHT_GRAY);
        }

        textField.setEnabled(!checkBox.isSelected());
    }

    public static void appendRow(JTable table, Object[] row) {
        ((DefaultTableModel) table.getModel()).addRow(row);
    }

    public static void appendRow(JTable table) {
        appendRow(table, new Object[table.getColumnCount()]);
    }

    public static void fillCombo(JComboBox comboBox, Collection items) {
        comboBox.setModel(new DefaultComboBoxModel(items.toArray()));
    }

    public static void updateTable(Object value, int rowId, int columnId, JTable table) {
        if (rowId > -1 && rowId < table.getRowCount()
                && columnId > -1 && columnId < table.getColumnCount()) {
            table.setValueAt(value, rowId, columnId);
        }
    }

    public static JMenuItem newMenuItem(String name, KeyStroke keyStroke, final Runnable runnable) {
        JMenuItem menuItem = new JMenuItem(name);

        menuItem.setAccelerator(keyStroke);

        menuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                java.awt.EventQueue.invokeLater(runnable);
            }
        });

        return menuItem;
    }

    public static void toggleFrame(JFrame frame) {
        if (frame == null) {
            return;
        }

        frame.setVisible(!frame.isVisible());

//        if (frame.isVisible()) {
//            if (frame.isFocusOwner()) {
//                frame.setVisible(false);
//            } else {
//                frame.toFront();
//            }
//        } else {
//            frame.setVisible(true);
//        }
    }

    /**
     * Return the object color given by the RGB values (rrr, ggg, bbb).
     *
     * @param RGB
     * @return
     */
    public static Color getColor(String RGB) {
        String[] rgb = RGB.split(",");

        return new Color(Integer.parseInt(rgb[0]),
                Integer.parseInt(rgb[1]),
                Integer.parseInt(rgb[2]));
    }

    /**
     * Returns the RGB components (rrr, ggg, bbb) in the range of 0-255.
     *
     * @param color
     * @return
     */
    public static String getColorRGB(Color color) {
        return color.getRed() + ","
                + color.getGreen() + ","
                + color.getBlue();
    }

    /**
     * Returns a chart object.
     *
     * @param title
     * @param timeAxisLabel
     * @param valueAxisLabel
     * @param dataSet
     * @param timeline
     * @param theme
     * @param legend
     * @return
     */
    public static Chart createTimeBasedChart(String title,
            String timeAxisLabel,
            String valueAxisLabel,
            ChartDataset dataSet,
            Timeline timeline,
            DefaultTheme theme,
            boolean legend) {

        //x-axis
        DateAxis timeAxis = new DateAxis(timeAxisLabel);
        timeAxis.setTimeline(timeline);
        //timeAxis.setTimeZone(dataSet.getTimeline().timeZone);

        if (dataSet instanceof TickChart || dataSet instanceof ContractChart) {
            timeAxis.setStandardTickUnits(createSimpleTimeTickUnits());
            //timeAxis.setTickUnit(new DateTickUnit(DateTickUnitType.MINUTE, 30,
            //        new SimpleDateFormat("HH:mm")),
            //       false, true);
        }

        //y-axis
        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
        valueAxis.setAutoRangeStickyZero(false);
        valueAxis.setAutoRangeIncludesZero(false);

        //renderer
        OHLCRenderer renderer = new OHLCRenderer();
        renderer.setBaseToolTipGenerator(new HighLowItemLabelGenerator());

        //Primary plot
        XYPlot plot = new XYPlot(dataSet, timeAxis, valueAxis, renderer);
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT, false);

        Chart chart = new Chart(
                title,
                Chart.DEFAULT_TITLE_FONT,
                plot,
                legend);

        theme.apply(chart);

        return chart;
    }

    public static TickUnits createSimpleTimeTickUnits() {
        TickUnits tickUnits = new TickUnits();
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
        SimpleDateFormat weekDayFormatter = new SimpleDateFormat("EEE");
        SimpleDateFormat dayFormatter = new SimpleDateFormat("dd-mm");
        SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM-yy");
        SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");

        //tickUnits.add(new DateTickUnit(DateTickUnitType.MINUTE, 30, DateTickUnitType.SECOND, 1, timeFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.HOUR, 1, DateTickUnitType.SECOND, 1, timeFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.HOUR, 2, timeFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.HOUR, 4, timeFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.HOUR, 6, timeFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.HOUR, 12, timeFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.DAY, 1, DateTickUnitType.HOUR, 1, weekDayFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.DAY, 2, weekDayFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.DAY, 7, dayFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.DAY, 15, dayFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 1, DateTickUnitType.DAY, 1, monthFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 3, monthFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 6, monthFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.YEAR, 1, DateTickUnitType.MONTH, 1, yearFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.YEAR, 2, yearFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.YEAR, 5, yearFormatter));
        tickUnits.add(new DateTickUnit(DateTickUnitType.YEAR, 10, yearFormatter));

        return tickUnits;
    }

    public static Dimension getChartPanelSize() {
        Rectangle mainBound = Main.frame.getBounds();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = screenSize.height - MENU_BAR_HEIGHT;
        Dimension chartSize;

        int clearWidth = screenSize.width - mainBound.x - mainBound.width;
        if (clearWidth > Main.p_Chart_Max_Size.width) {
            chartSize = Main.p_Chart_Max_Size;

        } else if (clearWidth > Main.p_Chart_Min_Size.width) {
            double newWidth = clearWidth / (int) (clearWidth / Main.p_Chart_Min_Size.width);
            double newHeight = newWidth / Main.p_Chart_Min_Size.width * Main.p_Chart_Min_Size.height;
            chartSize = new Dimension((int) newWidth, (int) newHeight);

        } else {
            chartSize = Main.p_Chart_Min_Size;
        }

        return new Dimension(chartSize.width, chartSize.height - MENU_BAR_HEIGHT);
    }

    public static Rectangle[][] getChartFrameGrid() {
        Rectangle mainBound = Main.frame.getBounds();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = screenSize.height - MENU_BAR_HEIGHT;
        Dimension chartFrameSize = getChartPanelSize();

        chartFrameSize.height += MENU_BAR_HEIGHT;

        int clearWidth = screenSize.width - mainBound.x - mainBound.width;
        int clearHeight = screenSize.height - mainBound.y;

        int x0 = mainBound.x + mainBound.width;
        int y0 = mainBound.y;
        int xN = clearWidth / chartFrameSize.width;
        int yN = clearHeight / chartFrameSize.height;

        Rectangle[][] grid = new Rectangle[yN][xN];

        for (int xI = 0; xI < xN; xI++) {
            for (int yI = 0; yI < yN; yI++) {
                grid[yI][xI] = new Rectangle(
                        x0 + xI * chartFrameSize.width, 
                        y0 + yI * chartFrameSize.height,
                        chartFrameSize.width, 
                        chartFrameSize.height);
            }
        }

        return grid;
    }
}
