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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import OAT.data.Bar;
import OAT.data.BarDataset;
import OAT.trading.thread.TradingThread;
import OAT.util.GeneralUtil;
import OAT.util.TextUtil;
import OAT.util.ThreadLogger;

/**
 * Abstract class for strategy plug-in.
 *
 * @author Antonio Yip
 */
public abstract class StrategyPlugin extends Plugin implements DataRequirer {

    /**
     * Maximum open gap to continue on the previous session. 0 means never
     * continue, >= 1 means always continue.
     */
    protected double p_Within_Gap = 0;
    /**
     * Is the plug-in active.
     */
    protected boolean p_Active = true;
    //
    private IndicatorDataset indicatorDataset;

    /**
     * Create a strategy plug-in.
     */
    public StrategyPlugin() {
        indicatorDataset = new IndicatorDataset(this);
    }

    /**
     * Define the names of the indicator.
     *
     * @return can be null
     */
    public abstract String[] getKeys();

    /**
     * Define the value ranges of the indicator.
     *
     * @return return null if use the same range of the price
     */
    public abstract Double[] getRange();

    /**
     *
     * @return
     */
    public boolean isActive() {
        return p_Active;
    }

    public double getWithin_Gap() {
        return p_Within_Gap;
    }

    /**
     * Add indicator to the indicator dataset.
     *
     * @param indicator
     */
    public void addIndicator(Indicator indicator) {
        if (indicator != null && indicator.getBar() != null) {
            indicatorDataset.add(indicator);

            if (!getTradingThread().isBacktesting()) {
                logIndicator(indicator);
            }
        }
    }

    /**
     * Returns the last item of the indicator dataset.
     *
     * @return
     */
    public Indicator getLastIndicator() {
        List<Indicator> indicators = getDescendingIndicators(1);

        if (indicators == null || indicators.isEmpty()) {
            return null;
        } else {
            return indicators.get(0);
        }
    }

    /**
     * Returns the indicator that is attached to the provided bar.
     *
     * @param bar
     * @return
     */
    public Indicator getIndicator(Bar bar) {
        return indicatorDataset.get(bar);
    }

    /**
     * Returns the indicator dataset.
     *
     * @return
     */
    public IndicatorDataset getIndicatorDataset() {
        return indicatorDataset;
    }

    /**
     * Returns the underlying TradingThread.
     *
     * @return
     */
    public final TradingThread getTradingThread() {
        return (TradingThread) getParent();
    }

    /**
     * Returns the primary chart of the underlying TradingThread.
     *
     * @return
     */
    public final BarDataset getPrimaryChart() {
        return getTradingThread().getPrimaryChart();
    }

    /**
     * Returns the current session of the underlying TradingThread.
     *
     * @return
     */
    protected final Session getCurrentSession() {
        return getTradingThread().getCurrentSession();
    }

    /**
     * Returns the trading hours of the underlying TradingThread.
     *
     * @return
     */
    protected final TradingHours getTradingHours() {
        return getTradingThread().getTradingHours();
    }

    /**
     * Returns a list of the indicators in descending order.
     *
     * @return
     */
    protected final List<Indicator> getDescendingIndicators() {
        return getDescendingIndicators(Integer.MAX_VALUE);
    }

    /**
     * Returns a list of the indicators in descending order.
     *
     * @param maxSize
     * @return
     */
    protected final List<Indicator> getDescendingIndicators(int maxSize) {
//        List<Indicator> descendingItems = indicatorDataset.getDescendingItems(maxSize, false);
        List<Indicator> descendingItems = indicatorDataset.getDescendingItems();

        if (!continuePrevSession(getTradingThread().getPrevSessionGap())) {
            int i = 0;
            for (Indicator indicator : descendingItems) {
                if (!getCurrentSession().isIn(indicator)) {
                    return descendingItems.subList(0, i);
                }

                i++;
            }

//            for (int i = 0; i < descendingItems.getItemCount(); i++) {
//                Indicator indicator = descendingItems.get(i);
//
//                if (!getCurrentSession().isIn(indicator)) {
//                    return descendingItems.subList(0, i);
//                }
//            }
        }

        return GeneralUtil.subListMaxSize(descendingItems, maxSize);
    }

    protected final double getLastTradeAdverseStop(int bars) {
        if (bars < 1) {
            return Double.NaN;
        }

        Side currentSide = getTradingThread().getCurrentSide();
        List<Bar> lastTradePreviousBars = getLastTradePreviousBarsOn(this.getClass(), currentSide);

        if (lastTradePreviousBars == null) {
            return Double.NaN;
        }

        List<Bar> prevBars = lastTradePreviousBars.subList(0, bars);
        Bar prevBar = new Bar(prevBars);

        if (currentSide == Side.LONG) {
            log(Level.FINE, "Stop at enter bar low = " + prevBar.getLow());
            return prevBar.getLow();

        } else if (currentSide == Side.SHORT) {
            log(Level.FINE, "Stop at enter bar high = " + prevBar.getHigh());
            return prevBar.getHigh();
        }

        return Double.NaN;
    }

    protected final List<Bar> getLastTradePreviousBarsOn(Class CalculatorClass, Side side) {
        if (side == Side.NEUTRAL || side == null || CalculatorClass == null) {
            return null;
        }

        Trade lastTrade = getTradingThread().getLastTrade();

        if (lastTrade != null && lastTrade.isOpen()) {
            if (lastTrade.getCalculations() == null || lastTrade.getCalculations().isEmpty()) {
                return null;
            }

            for (Map.Entry<Calculator, Side> entry : lastTrade.getCalculations().entrySet()) {
                Calculator calculator = entry.getKey();
                Side enteredSide = entry.getValue();

                if (enteredSide == side && CalculatorClass.isInstance(calculator)) {
                    return lastTrade.getEnterPreviousBars();
                }
            }
        }

        return null;
    }

    protected final boolean isLastTradeRunningOn(Class CalculatorClass, Side side) {
        if (side == Side.NEUTRAL || side == null || CalculatorClass == null) {
            return false;
        }

        if (!StrategyPlugin.class.isAssignableFrom(CalculatorClass)) {
            return false;
        }

        Trade lastTrade = getTradingThread().getLastTrade();

        if (lastTrade != null && lastTrade.isOpen()) {
            if (lastTrade.getCalculations().isEmpty()) {
                return false;
            }

            for (Map.Entry<Calculator, Side> entry : lastTrade.getCalculations().entrySet()) {
                Calculator calculator = entry.getKey();
                Side enteredSide = entry.getValue();

                if (enteredSide == side && CalculatorClass.isInstance(calculator)) {
                    log(Level.FINE, "Current trade is " + CalculatorClass.getSimpleName());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof StrategyPlugin)) {
            return false;
        }

        StrategyPlugin theOther = (StrategyPlugin) obj;

        if (this.getClass() != theOther.getClass()) {
            return false;
        }

        if (this.getPreferences() == null
                || !this.getPreferences().equals(theOther.getPreferences())) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return class simple name
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public String getChildrenNodeName() {
        return null;
    }

    @Override
    public String getLogPrefix() {
        return getDefaultNodeName() + ": ";
    }

    @Override
    public ThreadLogger getLogger() {
        return getTradingThread().getLogger();
    }

    public boolean continuePrevSession(double gap) {
        return !(p_Within_Gap == 0 || (p_Within_Gap < 1 && gap >= p_Within_Gap));
    }

    protected void logIndicator(Indicator indicator) {
        log(Level.FINE, "[{0}] = [{1}]",
                new Object[]{
                    TextUtil.toString(getKeys()),
                    TextUtil.toString(indicator.getValues())
                });
        
        clearLogOnce();
    }

    protected double triggerStopPrice(Side stopSide) {
        Side currentSide = getTradingThread().getCurrentSide();

        double lastPrice = getTradingThread().getLastTickPrice();
        double minTick = getTradingThread().getMinTick();

        switch (currentSide) {
            case LONG:
                if (stopSide == Side.SHORT) {
                    log(Level.FINE, "Stop triggered.");
                    return lastPrice + minTick;
                }
                break;
            case SHORT:
                if (stopSide == Side.LONG) {
                    log(Level.FINE, "Stop triggered.");
                    return lastPrice - minTick;
                }
                break;
            default:
                return Double.NaN;
        }

        return Double.NaN;
    }
}
