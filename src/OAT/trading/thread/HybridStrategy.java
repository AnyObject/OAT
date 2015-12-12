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

package OAT.trading.thread;

import OAT.trading.Stopper;
import OAT.trading.StrategyPlugin;
import OAT.trading.Presister;
import OAT.trading.Trend;
import OAT.trading.Main;
import OAT.trading.DataRequirer;
import OAT.trading.Calculator;
import OAT.trading.Side;
import OAT.trading.Plugin;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import OAT.data.Bar;
import OAT.data.BarDataset;
import OAT.trading.classification.InputSample;
import OAT.trading.classification.Prediction;
import OAT.trading.classification.Predictor;
import OAT.ui.BarChartFrame;
import OAT.util.GeneralUtil;
import OAT.util.MathUtil;

/**
 * Final class hybrid strategy.
 *
 * @author Antonio Yip
 */
public class HybridStrategy extends TradingThread {

    private List<Calculator> calculators = new LinkedList<Calculator>();
    private List<Predictor> predictors = new LinkedList<Predictor>();
    private List<Stopper> stoppers = new LinkedList<Stopper>();
    private List<Presister> presisters = new LinkedList<Presister>();
    private Map<Calculator, Side> lastCalculations;
//    private Map<Stopper, Double> lastStoppers;

    public HybridStrategy() {
    }

    public HybridStrategy(String symbol, String securityType, String exchange, String currency) {
        this.p_Symbol = symbol;
        this.p_Security_Type = securityType;
        this.p_Exchange = exchange;
        this.p_Currency = currency;
    }

    @Override
    public void init() {
        super.init();

        logParameters();
        for (Plugin plugin : getChildren()) {
            plugin.logParameters();
        }
    }

    @Override
    public boolean addChild(Plugin child) {
        boolean b = false;

        if (super.addChild(child)) {
            if (child instanceof StrategyPlugin) {
                getIndicators().add(((StrategyPlugin) child).getIndicatorDataset());
                b = true;

                if (child instanceof Calculator) {
                    calculators.add((Calculator) child);
                }

                if (child instanceof Stopper) {
                    stoppers.add((Stopper) child);
                }

                if (child instanceof Predictor) {
                    predictors.add((Predictor) child);
                }

                if (child instanceof Presister) {
                    presisters.add((Presister) child);
                }
            }
        }

        return b;
    }

    @Override
    public String toString() {
        return getSymbol() + " " + HybridStrategy.class.getSimpleName();
    }

    //
    //Getters
    //
    public List<Stopper> getStoppers() {
        return stoppers;
    }

    /**
     * Get calculators.
     *
     * @return
     */
    public List<Calculator> getCalculators() {
        return calculators;
    }

    /**
     * Get classifiers.
     *
     * @return
     */
    public List<Predictor> getPredictors() {
        return predictors;
    }

    /**
     * Get classifiers.
     *
     * @param trend
     * @return
     */
    public List<Predictor> getPredictors(Trend trend) {
        List<Predictor> trendPredictors = new LinkedList<Predictor>();

        for (Predictor predictor : predictors) {
            if (predictor.isActive() && predictor.getTrend() == trend) {
                trendPredictors.add(predictor);
            }
        }

        return trendPredictors;
    }

    @Override
    public StrategyPlugin[] getStrategyPlugins() {
        return getChildren().toArray(new StrategyPlugin[0]);
    }

    @Override
    public String getDefaultNodeName() {
        return getSymbol();
    }

    @Override
    public String getChildrenNodeName() {
        return "Plugins";
    }

    @Override
    public Map<Calculator, Side> getLastCalculations() {
        return lastCalculations;
    }

    @Override
    public BarChartFrame getChartFrame(BarDataset chartData) {
        BarChartFrame chartFrame = super.getChartFrame(chartData);
        chartFrame.addDataset(getTrades());

        if (chartData == primaryChart) {
            for (StrategyPlugin plugin : getStrategyPlugins()) {
                chartFrame.addDataset(plugin.getIndicatorDataset());
            }
        }

        return chartFrame;
    }

    @Override
    public Side trigger() {
        boolean backtesting = isBacktesting();
        Side pool = Side.NEUTRAL;
        lastCalculations = new HashMap<Calculator, Side>();

//        Side presistentSide = Side.NEUTRAL;
//        for (Presister presister : presisters) {
//            if (!presister.isActive()) {
//                continue;
//            }
//
//            presistentSide = presistentSide.add(presister.getPresistentSide());
//        }

        for (Calculator calculator : calculators) {
            if (!calculator.isActive()) {
                continue;
            }

            Side side = calculator.trigger(
                    getDescendingBarsFor((StrategyPlugin) calculator));

            if (side == Side.NEUTRAL) {
                continue;
            }

//            if (presistentSide != Side.NEUTRAL) {
//                if (presistentSide != side) {
////                if ((calculator.getTrend() == Trend.FOLLOWER && presistentSide != side)
////                        || calculator.getTrend() == Trend.OSCILLATOR && presistentSide == side) {
//                    log(Level.FINE, "Presistent side doesn't match the pool");
//                    continue;
//                }
//            }

            if (backtesting) {
                if (Main.backtestThread.training) {
                    lastCalculations.put(calculator, side);
                    return side;

                } else if (Main.backtestThread.suppressClassifiers) {
                    pool = pool.add(side);
                    continue;
                }
            }

            List<Predictor> trendPredictors = getPredictors(calculator.getTrend());

            if (trendPredictors.isEmpty()) {
                pool = pool.add(side);
                lastCalculations.put(calculator, side);

            } else {
                Prediction prediction = Prediction.INSIGNIFICANT;
                Object[] barsPattern = MathUtil.getBarsPattern(
                        primaryChart.getDescendingItems(),
                        side,
                        getRequiredBars(trendPredictors));
                InputSample inputSample = new InputSample(barsPattern);

                for (Predictor predictor : trendPredictors) {
                    if (!predictor.isActive()) {
                        continue;
                    }

                    prediction = prediction.add(predictor.predict(inputSample));
                }

                if (prediction == Prediction.WIN) {
                    pool = pool.add(side);
                    lastCalculations.put(calculator, side);
                }
            }
        }

        if (pool != Side.NEUTRAL) {
            Side presistentSide = Side.NEUTRAL;
            int n = 0;
            for (Presister presister : presisters) {
                if (!presister.isActive()) {
                    continue;
                }

                presistentSide = presistentSide.add(presister.getPresistentSide());
                n++;
            }

            if (n > 0 && presistentSide != Side.NEUTRAL && pool != presistentSide) {
                if (getCurrentSide() == Side.NEUTRAL) {
                    log(Level.FINE, "Presistent side doesn't match the pool");
                }
                return Side.NEUTRAL;
            }
        }

        return pool;
    }

    @Override
    public void update() {
        boolean backtesting = isBacktesting();

        for (Calculator calculator : calculators) {
            if (backtesting && !calculator.isActive()) {
                continue;
            }

            calculator.update(getDescendingBarsFor((StrategyPlugin) calculator));
        }
    }

    @Override
    public double calculateStopPrice() {
        if (isBacktesting()
                && Main.backtestThread.suppressStoppers
                && !Main.backtestThread.training) {
            return Double.NaN;
        }

        double stopPrice = Double.NaN;
        Side currentSide = getCurrentSide();

        for (Stopper stopper : stoppers) {
            if (!stopper.isActive()) {
                continue;
            }

            double s = stopper.calculateStopPrice(
                    getDescendingBarsFor((StrategyPlugin) stopper));

//            if (!Double.isNaN(s) && s >0) {
//                lastStoppers.put(stopper, s);
//            }

            if (Double.isNaN(stopPrice)
                    || (currentSide == Side.LONG && s > 0 && s > stopPrice)
                    || (currentSide == Side.SHORT && s > 0 && s < stopPrice)) {

//                if (!Double.isNaN(stopPrice)) {
//                    System.out.println(s + (currentSide == Side.LONG ? " > " : " < ") + stopPrice);
//                }

//                if (stopper instanceof AdverseStop) {
//                    System.out.println(s + (currentSide == Side.LONG ? " > " : " < ") + stopPrice);
//                }

                stopPrice = s;
            }
        }

        return stopPrice;
    }

    //model
//    public void dropModelTrades() {
//        try {
//            getModelSchema().dropTrades(p_Symbol);
//        } catch (Exception ex) {
//            log(Level.SEVERE, null, ex);
//        }
//    }
    private int getRequiredBars(List<? extends DataRequirer> dataRequirers) {
        int reqBars = 0;

        for (DataRequirer dataRequirer : dataRequirers) {
            int n = dataRequirer.getRequiredBars();

            if (n > reqBars) {
                reqBars = n;
            }
        }

        return reqBars;
    }

    private List<Bar> getDescendingBarsFor(StrategyPlugin strategyPlugin) {
        List<Bar> descendingBars;

        if (strategyPlugin.continuePrevSession(getPrevSessionGap())) {
            descendingBars = primaryChart.getDescendingItems();
        } else {

            descendingBars = primaryChart.getDescendingItems().
                    subList(
                    0,
                    Math.max(0,
                    primaryChart.size() - 1 - getPrevSessionIndex()));
        }

        return GeneralUtil.subListMaxSize(
                descendingBars,
                1,
                strategyPlugin.getRequiredBars());
    }
}
