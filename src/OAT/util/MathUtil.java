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

package OAT.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import OAT.data.Bar;
import OAT.data.OHLC;
import OAT.data.Price;
import OAT.trading.Side;

/**
 *
 * @author Antonio Yip
 */
public class MathUtil {

    public static double roundStep(double number, double step) {
        return roundStep(number, Side.NEUTRAL, step);
    }

    public static double roundStep(double number, Side side, double step) {
        if (step == 0) {
            return number;
        }

        BigDecimal numberBd = BigDecimal.valueOf(Double.isNaN(number) ? 0 : number);
        BigDecimal stepBd = BigDecimal.valueOf(Double.isNaN(step) ? 0 : step);

        RoundingMode roundingMode;

        if (Side.LONG == side) {
            roundingMode = RoundingMode.CEILING;
        } else if (Side.SHORT == side) {
            roundingMode = RoundingMode.FLOOR;
        } else {
            roundingMode = RoundingMode.HALF_UP;
        }

        return numberBd.divide(stepBd, 0, roundingMode).multiply(stepBd).doubleValue();
    }

    public static double parseDouble(Object object) {
        try {
            return Double.parseDouble(object.toString());
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public static boolean isSteppable(Object object) {
        return !Double.isNaN(parseDouble(object));
    }

    public static double getStep(Object object) {
        double value = parseDouble(object);

        if (Double.isNaN(value)) {
            return value;
        }

        if (value == 0
                || (object instanceof Integer && value == 1)
                || (value < 50 && value > 1)) {
            return 1;
        }

        return Math.pow(10, (int) Math.log10(value) - 1);
    }

    public static List<Double> getSteps(double start, double end, double step) {
        List<Double> steps = new ArrayList<Double>();

        int sign = (int) Math.signum(end - start);
        int n = (BigDecimal.valueOf(end).subtract(
                BigDecimal.valueOf(start))).divideToIntegralValue(
                BigDecimal.valueOf(step)).intValue();

        for (int i = 0; i <= n; i++) {
            steps.add((BigDecimal.valueOf(start).
                    add(BigDecimal.valueOf(i).
                    multiply(BigDecimal.valueOf(step)).
                    multiply(BigDecimal.valueOf(sign)))).doubleValue());
        }

        return steps;
    }

    public static double getMidPoint(OHLC bar, double ratio) {
        return bar.getLow() + (bar.getHigh() - bar.getLow()) * Math.max(Math.min(ratio, 1), 0);
    }

    /**
     * Return the RSI value of the list of {@link Price}
     *
     * @param <T>
     * @param prices
     * @return RSI
     */
    public static <T extends Price> double RSI(List<T> prices) {
        double up = 0;
        double down = 0;

        if (prices.size() < 2) {
            return Double.NaN;
        }

        for (int i = 0; i < prices.size() - 1; i++) {
            double diff = prices.get(i).getPrice() - prices.get(i + 1).getPrice();

            if (diff > 0) {
                up += diff;
            } else if (diff < 0) {
                down -= diff;
            }
        }

        if (down == 0) {
            return 1;
        }

        return 1 - 1 / (1 + up / down);
    }

    /**
     * Return the open price of a collection of OHLC bars.
     *
     * @param bars any collection of OHLC
     * @return the open price
     */
    public static double getOpen(Iterable<? extends OHLC> bars) {
        double open = Double.NaN;
        long openTime = 0;

        for (OHLC bar : bars) {
            if (openTime == 0 || bar.getOpenTime() <= openTime) {
                openTime = bar.getOpenTime();
                open = bar.getOpen();
            }
        }

        return open;
    }

    /**
     * Return the open time of a collection of OHLC bars.
     *
     * @param bars any collection of {@link OHLC}
     * @return UNIX time in milliseconds
     */
    public static long getOpenTime(Iterable<? extends OHLC> bars) {
        long openTime = 0;

        for (OHLC bar : bars) {
            if (openTime == 0 || bar.getOpenTime() <= openTime) {
                openTime = bar.getOpenTime();
            }
        }

        return openTime;
    }

    /**
     * Return the close price of a collection of OHLC bars.
     *
     * @param bars any collection of OHLC
     * @return the close price
     */
    public static double getClose(Iterable<? extends OHLC> bars) {
        double close = Double.NaN;
        long closeTime = 0;

        for (OHLC bar : bars) {
            if (closeTime == 0 || bar.getCloseTime() >= closeTime) {
                closeTime = bar.getCloseTime();
                close = bar.getClose();
            }
        }

        return close;
    }

    /**
     * Return the close time of a collection of OHLC bars.
     *
     * @param bars any collection of {@link OHLC}
     * @return UNIX time in milliseconds
     */
    public static long getCloseTime(Iterable<? extends OHLC> bars) {
        long closeTime = 0;

        for (OHLC bar : bars) {
            if (closeTime == 0 || bar.getCloseTime() >= closeTime) {
                closeTime = bar.getCloseTime();
            }
        }

        return closeTime;
    }

    /**
     * Return the highest price of a collection of OHLC bars.
     *
     * @param bars any collection of OHLC
     * @return the highest price
     */
    public static double getHigh(Iterable<? extends OHLC> bars) {
        if (bars == null) {
            return Double.NaN;
        }

        double highest = Double.NaN;

        for (OHLC bar : bars) {
            double high = bar.getHigh();

            if (!(high <= highest)) {
                highest = high;
            }
        }

        return highest;
    }

    /**
     * Return the high time of a collection of OHLC bars.
     *
     * @param bars any collection of {@link OHLC}
     * @return UNIX time in milliseconds
     */
    public static long getHighTime(Iterable<? extends OHLC> bars) {
        if (bars == null) {
            return 0;
        }

        double highest = Double.NaN;
        long highTime = 0;

        for (OHLC bar : bars) {
            double high = bar.getHigh();

            if (!(high <= highest)) {
                highest = high;
                highTime = bar.getTime();
            }
        }

        return highTime;
    }

    /**
     * Return the lowest price of a collection of OHLC bars.
     *
     * @param bars any collection of OHLC
     * @return the lowest price
     */
    public static double getLow(Iterable<? extends OHLC> bars) {
        if (bars == null) {
            return Double.NaN;
        }

        double lowest = Double.NaN;

        for (OHLC bar : bars) {
            double low = bar.getLow();

            if (!(low >= lowest)) {
                lowest = low;
            }
        }

        return lowest;
    }

    /**
     * Return the low time of a collection of OHLC bars.
     *
     * @param bars any collection of {@link OHLC}
     * @return UNIX time in milliseconds
     */
    public static long getLowTime(Iterable<? extends OHLC> bars) {
        if (bars == null) {
            return 0;
        }

        double lowest = Double.NaN;
        long lowTime = 0;

        for (OHLC bar : bars) {
            double low = bar.getLow();

            if (!(low >= lowest)) {
                lowest = low;
                lowTime = bar.getTime();
            }
        }

        return lowTime;
    }

    /**
     * Return a bar from a collection of OHLC bars.
     *
     * @param bars any collection of {@link OHLC}
     * @return a new instance of {@link Bar}
     */
    public static Bar getBar(Iterable<? extends OHLC> bars) {
        Bar newBar = new Bar();

        if (bars != null) {
            double open = Double.NaN;
            long openTime = 0;
            double highest = Double.NaN;
            long highTime = 0;
            double lowest = Double.NaN;
            long lowTime = 0;
            double close = Double.NaN;
            long closeTime = 0;
            int tickCount = 0;
            long volume = 0;
            double wp = 0;

            for (OHLC bar : bars) {
                if (openTime == 0 || bar.getOpenTime() <= openTime) {
                    openTime = bar.getOpenTime();
                    open = bar.getOpen();
                }

                double high = bar.getHigh();
                if (!(high <= highest)) {
                    highest = high;
                    highTime = bar.getTime();
                }

                double low = bar.getLow();
                if (!(low >= lowest)) {
                    lowest = low;
                    lowTime = bar.getTime();
                }

                if (closeTime == 0 || bar.getCloseTime() >= closeTime) {
                    closeTime = bar.getCloseTime();
                    close = bar.getClose();
                }

                tickCount += bar.getTickCount();
                volume += bar.getVolume();
                wp += bar.getWap() * bar.getVolume();
            }

            newBar.setOpen(open);
            newBar.setOpenTime(openTime);
            newBar.setHigh(highest);
            newBar.setHighTime(highTime);
            newBar.setLow(lowest);
            newBar.setLowTime(lowTime);
            newBar.setClose(close);
            newBar.setCloseTime(closeTime);
            newBar.setTickCount(tickCount);
            newBar.setVolume(volume);
            newBar.setWap(wp / volume);
        }

        return newBar;
    }

    /**
     * Test if the bar is black colour (Close lower than Open).
     *
     * @param bar
     * @return
     */
    public static boolean isBlack(OHLC bar) {
        return bar.getClose() < bar.getOpen();
    }

    /**
     * Test if the bar is white colour (Close higher than Open).
     *
     * @param bar
     * @return
     */
    public static boolean isWhite(OHLC bar) {
        return bar.getClose() > bar.getOpen();
    }

    /**
     * Test if the bar is a Doji Star (Open = Close).
     *
     * @param bar
     * @param body the maximum body to length ratio
     * @return
     */
    public static boolean isDojiStar(OHLC bar, double body) {
        double length = bar.getHigh() - bar.getLow();

        if (length == 0) {
            return true;
        }

        double bodyLength = Math.abs(bar.getClose() - bar.getOpen());

        return bodyLength / length <= body;
    }

    /**
     * Test if the bar is a Evening Star.
     *
     * @param bar
     * @param tail the minimum tail to head ratio
     * @param body the maximum body to tail ratio
     * @return
     */
    public static boolean isEveningStar(OHLC bar, double tail, double body) {
        double tailLength = bar.getHigh() - bar.getOpen();
        double headLength = bar.getOpen() - bar.getLow();
        double bodyLength = bar.getClose() - bar.getOpen();

        return bodyLength / tailLength <= body
                && headLength * tail <= tailLength;
    }

    /**
     * Test if the bar is a Morning Star.
     *
     * @param bar
     * @param tail the minimum tail to head ratio
     * @param body the maximum body to tail ratio
     * @return
     */
    public static boolean isMorningStar(OHLC bar, double tail, double body) {
        double tailLength = bar.getOpen() - bar.getLow();
        double headLength = bar.getHigh() - bar.getOpen();
        double bodyLength = bar.getOpen() - bar.getClose();

        return bodyLength / tailLength <= body
                && headLength * tail <= tailLength;
    }

    public static int intValue(long value) {
        int valueInt = (int) value;

        if (valueInt != value) {
            throw new IllegalArgumentException(
                    "The long value " + value + " is not within range of the int type");
        }

        return valueInt;
    }

//    public static Matrix[] getFormulaMatrixes(String formula) {
//        if (formula == null || formula.isEmpty()) {
//            return null;
//        }
//
//        int i = 0;
//        int j = 0;
//        int row = GeneralUtil.count("=", formula);
//
//        if (row == 0) {
//            return null;
//        }
//
//        int column = GeneralUtil.count("X", formula) / row;
//        Matrix M = new Matrix(row, column);
//        Matrix K = new Matrix(row, 1);
//
//        StringTokenizer st = new StringTokenizer(
//                formula.replaceAll("Output [\\d]* =", "!=").substring(1), "!");
//
//        while (st.hasMoreTokens()) {
//            String nextToken = st.nextToken();
//            StringTokenizer st1 = new StringTokenizer(nextToken, "+(*)");
//
//            while (st1.hasMoreTokens()) {
//                String nextToken1 = st1.nextToken();
//
//                if (nextToken1.codePointAt(0) == 65279) {
//                    continue;
//                }
//
//                if (nextToken1.matches("=" + "[\\d\\D]*")) {
//                    K.set(i, 0, Double.valueOf(nextToken1.replaceAll("=", "")));
//                    j = 0;
//
//                } else {
//                    if (!nextToken1.matches("X\\d+|\\s+")) {
//                        M.set(i, j, Double.valueOf(nextToken1));
//                        j++;
//                    }
//                }
//            }
//
//            i++;
//        }
//
//        return new Matrix[]{M, K};
//    }
//    public static Matrix getPriceMatrix(List<Bar> bars) {
//        Matrix M = new Matrix(bars.size() * 4, 1);
//        int i = 0;
//
//
//        for (int n = 0; n < bars.size(); n++) {
//            OHLC bar = bars.get(n);
//
//            M.set(i++, 0, bar.getOpen());
//            M.set(i++, 0, bar.getHigh());
//            M.set(i++, 0, bar.getLow());
//            M.set(i++, 0, bar.getClose());
//        }
//
//        return M;
//    }
//
//    public static Matrix getPriceMatrix(List<Bar> bars, double price) {
//        return normaliseMatrix(getPriceMatrix(bars), price);
//    }
//
//    public static Matrix getPriceMatrix(List<Bar> bars, double price, long duration, long tickCount) {
//        Matrix M = new Matrix(bars.size() * 4 + (bars.size() - 1) * 2, 1);
//        int i = 0;
//
//        for (int n = 0; n < bars.size(); n++) {
//            OHLC bar = bars.get(n);
//
//            if (n > 0) {
//                M.set(i++, 0, (double) (bars.get(n - 1).getOpenTime() - bar.getOpenTime()) / duration);
//                M.set(i++, 0, (double) bar.getTickCount() / tickCount);
//            }
//
//            M.set(i++, 0, bar.getOpen() / price);
//            M.set(i++, 0, bar.getHigh() / price);
//            M.set(i++, 0, bar.getLow() / price);
//            M.set(i++, 0, bar.getClose() / price);
//        }
//
//        return M;
//    }
//
//    public static Matrix normaliseMatrix(Matrix M, double value) {
//        return M.times(1 / value);
//    }
    public static Double[] newArray(int size, double fill) {
        Double[] array = new Double[size];
        Arrays.fill(array, fill);

        return array;
    }

    public static Double[] newArray(int size) {
        return newArray(size, Double.NaN);
    }

    public static double zerolize(double value) {
        if (value == Double.MAX_VALUE
                || value == Double.MIN_VALUE
                || value == Double.POSITIVE_INFINITY
                || value == Double.NEGATIVE_INFINITY
                || Double.isNaN(value)) {
            return 0;
        } else {
            return value;
        }
    }

//    public static Number[] getPredictionVector(List<Bar> bars) {
//        List<Number> al = new ArrayList<Number>();
//
//        double lastClose = bars.get(bars.size() - 2).getClose();
//
//        for (int i = 0; i < bars.size() - 1; i++) {
//            al.addAll(getBarStructure(bars.get(i), lastClose, 1));
//        }
//
//        //output
//        Bar outputBar = bars.get(bars.size() - 1);
////        al.add((lastBar.getClose()));
//        al.add(normalize(outputBar.getClose(), lastClose));
//
//        return al.toArray(new Number[0]);
//    }
    /**
     * Get a vector of bars pattern for classification training in descending
     * order.
     *
     * @param descendingBars
     * @param side
     * @param size
     * @return
     */
    public static Object[] getBarsPattern(List<Bar> descendingBars, Side side, int size) {
        return getBarsPattern(descendingBars, side, size, null);
    }

    /**
     * Get a vector of bars pattern for classification training in descending
     * order.
     *
     * @param descendingBars
     * @param side
     * @param size
     * @param output
     * @return
     */
    public static Object[] getBarsPattern(List<Bar> descendingBars, Side side, int size, Object output) {
        if (descendingBars == null || descendingBars.isEmpty()) {
            return null;
        }

        int sign = side.sign;
        double lastClose = descendingBars.get(0).getClose();
        List<Object> al = new ArrayList();

        int i = 0;

        while (i < size) {
            if (i < descendingBars.size()) {
                al.addAll(getBarStructure(descendingBars.get(i), lastClose, sign));
            } else {
                al.addAll(getBarStructure(null, lastClose, sign));
            }

            i++;
        }

        if (output != null) {
            al.add(output);
        }

        return al.toArray();
    }

    /**
     * Get the normalised vector of a bar in descending order.
     *
     *
     * @param bar
     * @param normal
     * @param sign
     * @return
     */
    public static List<Double> getBarStructure(Bar bar, double normal, int sign) {
        List<Double> al = new ArrayList<Double>();

        double ticks = 0;
        double wap = 0;
        // p0 = close
        double t0_1 = 0;
        double p1 = 0; // high or low whichever later
        double t1_2 = 0;
        double p2 = 0; // high or low whichever earlier
        double t2_3 = 0;
        double p3 = 0; // open

        if (bar != null) {
//            ticks = bar.getTickCount();
            wap = normalize(bar.getWap(), normal);// * sign;

            double t0 = bar.getCloseTime();
            double t1;
            double t2;
            double t3 = bar.getOpenTime();

            if (bar.getHighTime() > bar.getLowTime()) {
                t1 = bar.getHighTime();
                p1 = normalize(bar.getHigh(), normal);// * sign;
                t2 = bar.getLowTime();
                p2 = normalize(bar.getLow(), normal);// * sign;
            } else {
                t1 = bar.getLowTime();
                p1 = normalize(bar.getLow(), normal);// * sign;
                t2 = bar.getHighTime();
                p2 = normalize(bar.getHigh(), normal);// * sign;
            }

            p3 = normalize(bar.getOpen(), normal);// * sign;

            double tBase = DateUtil.MINUTE_TIME;
            t0_1 = (t0 - t1) / tBase;
            t1_2 = (t1 - t2) / tBase;
            t2_3 = (t2 - t3) / tBase;
        }

        double pMultiplier = 1000;

//        al.add(ticks);
//        al.add(wap * pMultiplier);
        al.add(t0_1);
        al.add(p1 * pMultiplier);
        al.add(t1_2);
        al.add(p2 * pMultiplier);
        al.add(t2_3);
        al.add(p3 * pMultiplier);

        return al;
    }

    public static double normalize(double y, double y0) {
        return y / y0 - 1.0;
    }

//    public static double normalize(double y, double y0) {
//        return Math.log(y / y0);
//    }
//    public static double denormalize(double y, double y0) {
//        return y0 * Math.exp(y);
//    }
    /**
     * Test if the number is not NaN, infinite, Double.MIN_VALUE or
     * Double.MAX_VALUE
     *
     * @param number
     * @return
     */
    public static boolean isValid(double number) {
        return !Double.isNaN(number)
                && !Double.isInfinite(number)
                && number > Double.MIN_VALUE
                && number < Double.MAX_VALUE;
    }

    /**
     * Length ratio of body over shadow of a candlestick.
     *
     * @param bar
     * @return Range 0 to 1. 0 means a doji, 1 means a full body bar.
     */
    public static double getBodyToShadowRatio(Bar bar) {
        return Math.abs((bar.getClose() - bar.getOpen()) / (bar.getHigh() - bar.getLow()));
    }

    public static double getFluctuation(double high, double low) {
        return Math.abs(high - low) / (high + low) * 2;
    }

    public static double getFluctuation(Collection<? extends OHLC> bars) {
        Bar bar = new Bar(bars);
        return getFluctuation(bar.getHigh(), bar.getLow());
    }

    public static int getSignChange(double n1, double n2) {
        if (n1 > n2) {
            return 1;
        } else if (n1 < n2) {
            return -1;
        } else {
            return 0;
        }
    }
}
