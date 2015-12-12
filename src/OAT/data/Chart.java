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

package OAT.data;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import OAT.util.Tabulable;

/**
 *
 * @author Antonio Yip
 */
public class Chart extends JFreeChart implements Tabulable {

    public Chart(String title, Font titleFont, Plot plot, boolean createLegend) {
        super(title, titleFont, plot, createLegend);
    }

    public Chart(String title, Plot plot) {
        super(title, plot);
    }

    public Chart(Plot plot) {
        super(plot);
    }

    public int getDatasetCount() {
        return getXYPlot().getDatasetCount();
    }

    public ChartDataset getDataset(int index) {
        return (ChartDataset) getXYPlot().getDataset(index);
    }

    @Override
    public String[] getColumnHeaders() {
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < getDatasetCount(); i++) {
            list.addAll(Arrays.asList(getDataset(i).getColumnHeaders()));
        }

        return list.toArray(new String[0]);
    }

    @Override
    public Object[] getRow(int index) {
        List list = new ArrayList();

        for (int i = 0; i < getDatasetCount(); i++) {
            list.addAll(Arrays.asList(getDataset(i).getRow((Bar) getDataset(0).get(index))));
        }

        return list.toArray(new Object[0]);
    }

    @Override
    public int getItemCount() {
        return getDataset(0).getItemCount();
    }
}
