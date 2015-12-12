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

package OAT.trading.classification;

import java.util.List;
import OAT.trading.DataRequirer;
import OAT.trading.Trend;

/**
 * Interface for predictor.
 *
 * @author Antonio Yip
 */
public interface Predictor extends DataRequirer {

    /**
     * Get trend.
     *
     * @return
     */
    public abstract Trend getTrend();

    /**
     * Is active.
     *
     * @return
     */
    public boolean isActive();

    /**
     * Load classification model;
     *
     */
    public abstract void loadModel();

    /**
     * Save classification model;
     *
     */
    public abstract void saveModel();

    /**
     * Predict the accuracy of the provided input using the existing model.
     *
     * @param input an array of input
     * @return the predicted output
     */
    public abstract Prediction predict(InputSample input);

    /**
     * Train the model from the provided training set.
     *
     * @param trainingSet
     */
    public abstract void train(List<TrainingSample> trainingSet);

    /**
     * Cross validate the model to calculate the prediction accuracy.
     *
     * @param trainingSet
     */
    public void crossValidate(List<TrainingSample> trainingSet);
}
