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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import OAT.util.GeneralUtil;
import OAT.util.TextUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.*;

/**
 * Weka 3.6.4.
 *
 * <p> Mark Hall, Eibe Frank, Geoffrey Holmes, Bernhard Pfahringer, Pe- ter
 * Reutemann, Ian H. Witten (2009); The WEKA Data Mining Software: An Update;
 * SIGKDD Explorations, Volume 11, Issue 1.
 *
 * @author Antonio Yip
 */
public abstract class Weka extends AbstractPredictor {

    private Classifier classifier;
    private FastVector attributes;
    private FastVector classes;

    /**
     * Returns the class name of the classifier.
     *
     * @return
     */
    protected abstract String getClassifierType();

    /**
     * Returns a list of options.
     *
     * @return an array of String
     */
    protected abstract String[] getOptions();

    private void initClassifier() {
        try {
            initClassifier((Classifier) GeneralUtil.forName(Classifier.class, getClassifierType()));
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    private void initClassifier(Classifier classifier) {
        if (classifier == null) {
            return;
        }

        this.classifier = classifier;

        try {
            classifier.setOptions(getOptions());
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        if (!isCrossValidating()) {
            log(Level.INFO, "Classifier: {0} {1}",
                    new Object[]{
                        classifier.getClass().getSimpleName(),
                        TextUtil.toString(classifier.getOptions())
                    });
        }

        attributes = new FastVector();
        for (String string : getAttributeNames()) {
            attributes.addElement(new Attribute(string));
        }

        classes = new FastVector();
        classes.addElement("0");
        classes.addElement("1");

        attributes.addElement(new Attribute(DEFAULT_CLASS_COLUMN, classes));
    }

    @Override
    public void loadModel(String file) {
        try {
            classifier = (Classifier) SerializationHelper.read(file);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void saveModel(String file) {
        try {
            SerializationHelper.write(file, classifier);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Prediction predict(InputSample input) {
        if (classifier == null) {
            log(Level.WARNING, "null classifier");
            return null;
        }

        Instances data = getInstances(input);

        if (data == null) {
            log(Level.WARNING, "null data");
            return null;
        }

        if (!isCrossValidating()) {
            if (isLoggable(Level.FINER)) {
                log(Level.FINER, data.toString());
            }
        }

        try {
            double output = new Evaluation(data).evaluateModelOnce(classifier, data.firstInstance());

            return Prediction.valueOf(output < 0.5 ? -1 : 1);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public void train(List<TrainingSample> trainingSet) {
        initClassifier();

        if (classifier == null) {
            log(Level.WARNING, "null classifier");
            return;
        }

        Instances data = getInstances(trainingSet);

        if (data == null) {
            log(Level.WARNING, "null data");
            return;
        }

        if (!isCrossValidating()) {
            log(Level.FINE, "Training set size: {0}", data.numInstances());

            if (isLoggable(Level.FINER)) {
                log(Level.FINER, data.toString());
            }
        }

        try {
            classifier.buildClassifier(data);

        } catch (UnsupportedAttributeTypeException ex) {
            log(Level.WARNING, "{1}\nCapabilities: {0}",
                    new Object[]{
                        ex.getMessage(),
                        classifier.getCapabilities()
                    });

        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    private Instances getInstances(List<TrainingSample> trainingSet) {
        Instances data = new Instances("trainingSet", attributes, 0);

        for (TrainingSample trainingSample : trainingSet) {
            double[] vars = Arrays.copyOf(trainingSample.getInputVector(), attributes.size());

            int classIndex = attributes.size() - 1;
            vars[classIndex] = (Double) trainingSample.getDesiredOutput() < 0.5
                    ? classes.indexOf("0")
                    : classes.indexOf("1");

            data.add(new Instance(1.0, vars));
        }

        data.setClassIndex(attributes.size() - 1);

        return data;
    }

    private Instances getInstances(InputSample input) {
        Instances data = new Instances("inputSet", attributes, 0);

        double[] vars = Arrays.copyOf(input.getInputVector(), attributes.size());

        int classIndex = attributes.size() - 1;
        vars[classIndex] = classes.indexOf("0");

        data.add(new Instance(1.0, vars));

        data.setClassIndex(attributes.size() - 1);

        return data;
    }
}
