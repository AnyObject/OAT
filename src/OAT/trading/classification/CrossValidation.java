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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import OAT.trading.Plugin;
import OAT.util.DateUtil;
import OAT.util.TextUtil;

/**
 * Class for cross validation.
 *
 * @author Antonio Yip
 */
public class CrossValidation {

    private Class engineType;
    private int truePositive;
    private int falsePositive;
    private int falseNegative;
    private int trueNegative;
//    private ModifiedLogger logger;
    private Plugin parent;

    /**
     * The constructor.
     *
     * @param predictor
     */
    public CrossValidation(AbstractPredictor predictor) {
        this.parent = predictor;
        this.engineType = predictor.getClass();
//        this.logger = parent.getLogger();
    }

    /**
     * Returns the underlay predictor.
     *
     * @return
     */
    public AbstractPredictor getPredictor() {
        return (AbstractPredictor) parent;
    }

    /**
     * Cross validate the model to calculate the prediction accuracy.
     *
     * @param trainingSet
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InterruptedException
     */
    public synchronized void perform(final List<TrainingSample> trainingSet) throws InstantiationException, IllegalAccessException, InterruptedException {
//        getLogger().info(TextUtil.LOG_SECTION_BREAK);
        parent.log(Level.INFO, "Performing cross validation for {0}...", engineType.getSimpleName());
        long counterStart = DateUtil.getTimeNow();

        for (final TrainingSample testSample : trainingSet) {
            final AbstractPredictor predictor = (AbstractPredictor) engineType.newInstance();
            predictor.setParent(parent);
            predictor.setCrossValidating(true);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    // new training set excluding the test sample
                    List<TrainingSample> newTrainingSet = new LinkedList<TrainingSample>();
                    for (TrainingSample trainingSample : trainingSet) {
                        if (trainingSample != testSample) {
                            newTrainingSet.add(trainingSample);
                        }
                    }

                    predictor.train(newTrainingSet);

                    testSample.setPredictedOutput(
                            predictor.predict(testSample) == Prediction.LOSS ? 0.0 : 1.0);

                    double actual = (Double) testSample.getDesiredOutput();
                    double predicted = (Double) testSample.getPredictedOutput();

                    if (actual >= 0.5) {
                        if (predicted >= 0.5) {
                            addTruePositive();
                        } else {
                            addFalseNegative();
                        }
                    } else {
                        if (predicted >= 0.5) {
                            addFalsePositive();
                        } else {
                            addTrueNegative();
                        }
                    }
                }
            }).start();
        }

        while (getSampleCount() < trainingSet.size()) {
            wait(25);
        }

        List<Object[]> comparision = new LinkedList<Object[]>();

        for (TrainingSample trainingSample : trainingSet) {
            comparision.add(new Object[]{
                        trainingSample.getDesiredOutput(),
                        trainingSample.getPredictedOutput()
                    });
        }

        parent.log(Level.INFO, "Cross validation done in {0}.", DateUtil.getDurationStrSince(counterStart));
        parent.log(Level.FINE, "Cross validation results:\n{0}", TextUtil.toString(new String[]{"Actual", "Predicted"}, comparision, " | ", 9));

//      log(Level.INFO, TextUtil.LOG_SECTION_BREAK);
        parent.log(Level.INFO, "Confusion table:\n\tTP FP\n\tFN TN\n\t{0}", TextUtil.toString(getConfusionTable(), "\n\t", " "));

        parent.log(Level.INFO, "Accuracy = {0}", getAccuracy());
        parent.log(Level.INFO, "Precision P = TP / (TP + FP) = {0}", getPrecision());
        parent.log(Level.INFO, "Recall R = TP / (TP + FN) = {0}", getRecall());
        parent.log(Level.INFO, "F-mearsure F = 2PR / (P + R) = {0}", getFMeasure());
    }

    /**
     * Return the number of False Negative (incorrectly predicted inaccurate).
     *
     * @return
     */
    public int getFalseNegative() {
        return falseNegative;
    }

    /**
     * Return the number of False Positive (incorrectly predicted accurate).
     *
     * @return
     */
    public int getFalsePositive() {
        return falsePositive;
    }

    /**
     * Return the number of True Negative (correctly predicted inaccurate).
     *
     * @return
     */
    public int getTrueNegative() {
        return trueNegative;
    }

    /**
     * Return the number of True Positive (correctly predicted accurate).
     *
     * @return
     */
    public int getTruePositive() {
        return truePositive;
    }

    /**
     * Return the confusion table.
     *
     * @return
     */
    public Integer[][] getConfusionTable() {
        return new Integer[][]{
                    new Integer[]{truePositive, falsePositive},
                    new Integer[]{falseNegative, trueNegative}};
    }

    /**
     * Return the Accuracy of the trained model. (TP + TN) / Total no. of
     * samples
     *
     * @return
     */
    public double getAccuracy() {
        return (double) (truePositive + trueNegative) / getSampleCount();
    }

    /**
     * Return the Precision (P) of the trained model. P = TP / (TP + FP)
     *
     * @return
     */
    public double getPrecision() {
        return (double) truePositive / (truePositive + falsePositive);
    }

    /**
     * Return the Recall (R) of the trained model. R = TP / (TP + FN)
     *
     * @return
     */
    public double getRecall() {
        return (double) truePositive / (truePositive + falseNegative);
    }

    /**
     * Return the F-measure (F) of the trained model. F = 2PR / (P + R)
     *
     * @return
     */
    public double getFMeasure() {
        double P = getPrecision();
        double R = getRecall();

        return P * R * 2 / (P + R);
    }

    private synchronized void addFalseNegative() {
        falseNegative++;
    }

    private synchronized void addFalsePositive() {
        falsePositive++;
    }

    private synchronized void addTrueNegative() {
        trueNegative++;
    }

    private synchronized void addTruePositive() {
        truePositive++;
    }

    private int getSampleCount() {
        return truePositive + falseNegative + falsePositive + trueNegative;
    }
}
