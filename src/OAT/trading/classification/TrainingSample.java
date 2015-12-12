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

/**
 *
 * @author Antonio Yip
 */
public class TrainingSample extends InputSample {

    private Object desiredOutput;
    private Object predictedOutput;

    /**
     * Default constructor of the class.
     *
     * @param input values of input
     * @param desiredOutput actual or desired output
     */
    public TrainingSample(Object[] input, Object desiredOutput) {
        super(input);
        this.desiredOutput = desiredOutput;
    }

    /**
     * Returns the actual or desired output.
     *
     * @return null if N/A
     */
    public Object getDesiredOutput() {
        return desiredOutput;
    }

    /**
     * Returns the predicted output.
     *
     * @return null if N/A
     */
    public Object getPredictedOutput() {
        return predictedOutput;
    }

    /**
     * Set the predicted output calculated by an engine.
     *
     * @param predictedOutput
     */
    public void setPredictedOutput(Object predictedOutput) {
        this.predictedOutput = predictedOutput;
    }

    /**
     * Returns a vector of input values and the actual output.
     *
     * @return an array of double values, size = {@link getInputVector()} + 1
     */
    public double[] getActualVector() {
        double[] inputVector = super.getInputVector();
        double[] vector = Arrays.copyOf(inputVector, inputVector.length + 1);
        vector[inputVector.length] = (Double) getDesiredOutput();

        return vector;
    }

    /**
     * Returns a vector of input values and the predicted output.
     *
     * @return an array of double values, size = {@link getInputVector()} + 1
     */
    public double[] getPredictedVector() {
        double[] inputVector = super.getInputVector();
        double[] vector = Arrays.copyOf(inputVector, inputVector.length + 1);
        vector[inputVector.length] = (Double) getPredictedOutput();

        return vector;
    }
}
