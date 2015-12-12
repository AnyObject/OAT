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

import java.awt.*;
import java.awt.event.ComponentEvent;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import OAT.data.Chart;
import OAT.data.ChartDataset;
import OAT.trading.Calculator;
import OAT.trading.Main;
import OAT.trading.Stopper;
import OAT.trading.TradeDataset;
import OAT.ui.util.IndicatorRenderer;
import OAT.ui.util.TradeRenderer;
import OAT.ui.util.UiUtil;
import OAT.util.TextUtil;

/**
 * Basic bar chart frame.
 *
 * @author Antonio Yip
 */
public class BarChartFrame extends AbstractChartFrame { //implements Tabulable {

    private static final int MENU_BAR_HEIGHT = 22;
    ChartDataFrame chartDataFrame;

    public BarChartFrame(ChartDataset dataSet) {
        this(dataSet.getTitle(), dataSet);
    }

    public BarChartFrame(String title, ChartDataset dataset) {
        super(title,
                UiUtil.createTimeBasedChart(
                null, null, null,
                dataset,
                dataset.getTimeline(),
                Main.theme,
                false));
    }

    public ChartDataFrame getChartDataFrame() {
        return chartDataFrame;
    }

    @Override
    protected boolean disposeOnClose() {
        return false;
    }

    @Override
    protected boolean hideWhenFocusLost() {
        return false;
    }

    @Override
    protected Dimension getChartPanelSize() {
        Dimension size = new Dimension(Main.chartFramesGrid[0][0].getSize());
        size.height -= UiUtil.MENU_BAR_HEIGHT;
        return size;
    }

    @Override
    protected void init() {
//        setJMenuBar(new MainMenuBar(this));
//        setContentPane(chartPanel);
//        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        //set getItemCount and location
        //Rectangle mainBound = Main.frame.getBounds();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.height = screenSize.height - MENU_BAR_HEIGHT;
        //Dimension chartFrameSize = getChartPanelSize();
        //chartFrameSize.height += MENU_BAR_HEIGHT;

        int gridRows = Main.chartFramesGrid.length;
        int gridColumns = Main.chartFramesGrid[0].length;

        boolean isOccupied = false;
        for (int c = 0; c < gridColumns; c++) {
            for (int r = 0; r < gridRows; r++) {
                Rectangle bound = Main.chartFramesGrid[r][c];

                for (BarChartFrame existingFrame : Main.chartFrames) {
                    if (bound.contains(existingFrame.getLocation())
                            || existingFrame.getBounds().contains(
                            bound.intersection(new Rectangle(screenSize)))) {
                        isOccupied = true;
                        break;
                    }
                }

                if (!isOccupied) {
                    setBounds(bound);
                    break;
                }
            }

            if (!isOccupied) {
                break;
            }
        }

        if (isOccupied) {
            setBounds(Main.chartFramesGrid[Main.lastChartFrameGridId % gridRows][(Main.lastChartFrameGridId / gridRows)%gridColumns]);
        }
        
        Main.lastChartFrameGridId++;

        Main.chartFrames.add(this);

        addComponentListener(new java.awt.event.ComponentAdapter() {

            @Override
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });

        //pack and display
        pack();
    }

    public void addDataset(ChartDataset dataset) {
        if (dataset == null || dataset.getSeriesCount() == 0) {
            return;
        }

        XYPlot plot = getChart().getXYPlot();
        int i = plot.getDatasetCount();

        for (int j = 0; j < i; j++) {
            if (plot.getDataset(j).equals(dataset)) {
//                System.out.println("eq " + i
//                        + " " + ((ChartDataset) plot.getDataset(j)).getTitle()
//                        + " " + dataset.getTitle());
                return;
            }
        }

        plot.setDataset(i, dataset);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        Double[] range = dataset.getAxisRange();

        //axis
        int axisId = 0;

        if (range != null) {
//        if (range == null || range.length < 2) {
//            plot.mapDatasetToRangeAxis(i, 0);
//        } else {

            //scan for equal axis range, reuse if found
            boolean hasSameRange = false;

            if (range.length > 1) {
                for (int j = 1; j < plot.getRangeAxisCount(); j++) {
                    Range otherRange = plot.getRangeAxis(j).getRange();

                    if (otherRange != null
                            && otherRange.getLowerBound() == range[0]
                            && otherRange.getUpperBound() == range[1]) {
                        axisId = j;
                        hasSameRange = true;
                        break;
                    }
                }
            }

            if (!hasSameRange) {
                NumberAxis newAxis = new NumberAxis();

                if (range.length > 1) {
                    newAxis.setAutoRange(false);
                    newAxis.setRange(range[0], range[1]);
                }

                if (range.length > 2) {
                    newAxis.setAutoTickUnitSelection(false, false);
                    newAxis.setTickUnit(new NumberTickUnit(range[2]));
                }

                newAxis.setNumberFormatOverride(TextUtil.SIMPLE_FORMATTER);
//                    newAxis.setAxisLinePaint(new Color(100, 0, 0));
//                    newAxis.setLabelPaint(paints[i][0]);
//                    newAxis.setTickLabelPaint(paints[i][0]);
//                    newAxis.setTickMarkPaint(paints[i][0]);
//                    newAxis.setTickLabelsVisible(true);

                axisId = plot.getRangeAxisCount();
                plot.setRangeAxis(axisId, newAxis, false);
                plot.setRangeAxisLocation(axisId, AxisLocation.BOTTOM_OR_LEFT, false);
            }
//            plot.mapDatasetToRangeAxis(i, newAxisId);
        }
        plot.mapDatasetToRangeAxis(i, axisId);
        //

        //renderer
        XYLineAndShapeRenderer renderer;

        if (dataset instanceof TradeDataset) {
            renderer = new TradeRenderer();

            for (int j = 0; j < dataset.getSeriesCount(); j++) {
                renderer.setSeriesLinesVisible(j, false);
            }
        } else {

            Shape shape = Main.defaultShape;
            Paint[][] seriesPaints;
            Stroke stroke;

            if (dataset.getSource() instanceof Stopper
                    && !(dataset.getSource() instanceof Calculator)) {
                seriesPaints = Main.greyPaints;
                stroke = Main.dottedStoke;
            } else {
                seriesPaints = Main.defaultPaints;
                stroke = Main.defaultStoke;
            }

            renderer = new IndicatorRenderer(
                    seriesPaints[(i - 1) % seriesPaints.length],
                    shape,
                    stroke);
        }

        plot.setRenderer(i, renderer, false);
    }

    public int getDatasetCount() {
        return getChart().getXYPlot().getDatasetCount();
    }

    public ChartDataset getDataset(int index) {
        return (ChartDataset) getChart().getXYPlot().getDataset(index);
    }

    public Chart getChart() {
        return (Chart) getChartPanel().getChart();
    }

//    @Override
//    public String[] getColumnHeaders() {
//        List<String> al = new ArrayList<String>();
//
//        for (int i = 0; i < getDatasetCount(); i++) {
//            al.addAll(Arrays.asList(getDataset(i).getColumnHeaders()));
//        }
//
//        return al.toArray(new String[0]);
//    }
//
//    @Override
//    public int getItemCount() {
//        return getDataset(0).getItemCount();
//    }
//
//    @Override
//    public Object[] getRow(int index) {
//        List al = new ArrayList();
//
//        for (int i = 0; i < getDatasetCount(); i++) {
//            al.addAll(Arrays.asList(getDataset(i).getRow((Bar) getDataset(0).get(index))));
//        }
//
//        return al.toArray(new Object[0]);
//    }
    @Override
    public void setVisible(boolean b) {
        if (chartDataFrame != null) {
            chartDataFrame.setVisible(false);
        }

        super.setVisible(b);

        if (b) {
            toFront();
        } else if (!b && !Main.getMainThread().isExiting()) {
            Main.chartFrames.remove(this);
            dispose();

            if (Main.chartFrames.size() > 0) {
                Main.chartFrames.get(Main.chartFrames.size() - 1).toFront();
            }
        }
    }

    private void formComponentHidden(ComponentEvent evt) {
        if (chartDataFrame != null) {
            chartDataFrame.setVisible(false);
        }
    }
}
