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
import java.util.logging.Level;
import OAT.trading.StrategyPlugin;
import OAT.trading.Main;
import OAT.util.GeneralUtil;
import OAT.util.MathUtil;

/**
 * Abstract class for all prediction engines.
 *
 * @author Antonio Yip
 */
public abstract class AbstractPredictor extends StrategyPlugin implements Predictor {

    protected int p_Training_Bars = 5;
    protected String p_Model_File;
    //
    private boolean crossValidating;
    /**
     * The default class label heading for classification.
     */
    protected static final String DEFAULT_CLASS_COLUMN = "class";
    /**
     * The default attributes for classification.
     */
    protected static final String DEFAULT_ATTRIBUTE_PREFIX = "x";
    //
    private CrossValidation crossValidation;

//    /**
//     * Default constructor of the class.
//     *
//     */
//    public AbstractPredictor() {
//        p_Ignore_Break = true;
//        p_Continue_Within_Gap = 1
//    }

    public boolean isCrossValidating() {
        return crossValidating;
    }

    public void setCrossValidating(boolean crossValidating) {
        this.crossValidating = crossValidating;
    }

    @Override
    public String[] getKeys() {
        return null;
    }

    @Override
    public Double[] getRange() {
        return null;
    }

    @Override
    public int getRequiredBars() {
        return p_Training_Bars;
    }

    @Override
    public void loadModel() {
        if (p_Model_File == null || p_Model_File.isEmpty()) {
//            if (!getTradingThread().isBacktesting()) {
//                getLogger().warning("Model file was not loaded.");
//            }
            return;
        }

        loadModel(p_Model_File);
    }

    @Override
    public void saveModel() {
        p_Model_File = Main.modelFolder
                + getTradingThread().getSymbol()
                + "_" + getClass().getSimpleName() + ".model";

        saveModel(p_Model_File);
    }

    /**
     *
     * @param file
     */
    public abstract void loadModel(String file);

    /**
     *
     * @param file
     */
    public abstract void saveModel(String file);

    /**
     * Returns attribute column names for classification.
     *
     * @return
     */
    public String[] getAttributeNames() {
        int barsPatternSize = p_Training_Bars * MathUtil.getBarStructure(null, 0, 0).size();

        String[] names = new String[barsPatternSize];

        for (int i = 0; i < barsPatternSize; i++) {
            names[i] = DEFAULT_ATTRIBUTE_PREFIX + i;
        }

        return names;
    }

    /**
     * Returns attribute column names with class label for classification.
     *
     * @return
     */
    public String[] getAttributeNamesWithClass() {
        return GeneralUtil.join(getAttributeNames(), DEFAULT_CLASS_COLUMN);
    }

    /**
     * Returns the cross validation object.
     *
     * @return
     */
    public CrossValidation getCrossValidation() {
        return crossValidation;
    }

    @Override
    public void crossValidate(List<TrainingSample> trainingSet) {
        crossValidation = new CrossValidation(this);

        try {
            crossValidation.perform(trainingSet);
        } catch (Exception ex) {
            log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Predict the accuracy of the provided prediction set using the existing
     * model.
     *
     * @param inputs
     * @return a list of output
     */
    protected Object[] predict(List<InputSample> inputs) {
        Object[] outputs = new Object[inputs.size()];
        int i = 0;

        for (InputSample input : inputs) {
            outputs[i++] = predict(input);
        }

        return outputs;
    }
}
