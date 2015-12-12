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

package OAT.trading;

import OAT.data.Bar;
import OAT.data.ChartDataset;

/**
 *
 * @author Antonio Yip
 */
public class IndicatorDataset extends ChartDataset<Indicator> {

    protected String title;
    protected String[] keys;
    protected Double[] axisRange;
    protected Object source;

    public IndicatorDataset(StrategyPlugin strategyPlugin) {
        this.title = strategyPlugin.getDefaultNodeName();
        this.keys = strategyPlugin.getKeys();
        this.axisRange = strategyPlugin.getRange();
        this.source = strategyPlugin;
    }

    public IndicatorDataset(String title, String[] keys, Double[] axisRange) {
        this.title = title;
        this.keys = keys;
        this.axisRange = axisRange;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public Double[] getAxisRange() {
        return axisRange;
    }

    @Override
    public Object[] getRow(Bar bar) {
        for (Indicator indicator : getDescendingItems()) {
            if (indicator.getBar() == bar) {
                return getRow(indexOf(indicator));
            }
        }

        return new Object[getSeriesCount()];
    }

    @Override
    public Object getSource() {
        return source;
    }

    /**
     * Return the indicator at the specified bar.
     *
     * @param bar
     * @return
     */
    public Indicator get(Bar bar) {

        for (Indicator indicator : getDescendingItems()) {
            if (indicator.getBar() == bar) {
                return indicator;
            }
        }

        return null;
    }
}
