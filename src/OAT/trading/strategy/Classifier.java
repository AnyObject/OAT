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

package OAT.trading.strategy;

import OAT.trading.Trend;
import OAT.trading.classification.Weka;

/**
 * Generic classifier that uses the Weka API.
 *
 * @author Antonio Yip
 */
public class Classifier extends Weka {

    protected String p_Engine = "weka.classifiers.functions.SMO";
    protected Trend p_Trend = Trend.FOLLOWER;
    protected int p_Type = 0;
    protected int p_Kernel = 3;
    protected int p_Degree = 10;
    protected double p_Tolerance = 0.001;
    protected double p_C = 1;

    @Override
    protected String getClassifierType() {
        return p_Engine;
    }

    @Override
    protected String[] getOptions() {
        return new String[]{ //                    "-S", "" + p_Type,
                //                    "-K", "" + p_Kernel,
                //                    "-D", "" + p_Degree,
                //                    "-E", "" + p_Tolerance,
                //                    "-C", "" + p_C,
                //                    "-M", "500",
                //                    "-Z",
                };
    }

    @Override
    public Trend getTrend() {
        return p_Trend;
    }
}
