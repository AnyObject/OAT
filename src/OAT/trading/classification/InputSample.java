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

/**
 *
 * @author Antonio Yip
 */
public class InputSample {

    private Object[] input;

    /**
     * Default constructor of the class.
     *
     * @param input a row of data sample
     */
    public InputSample(Object[] input) {
        this.input = input;
    }

    /**
     * Returns the data sample.
     *
     * @return an array of the values
     */
    public Object[] getInput() {
        return input;
    }

    /**
     * Returns the data sample vector
     *
     * @return an array of double values
     */
    public double[] getInputVector() {
        double[] vector = new double[input.length];

        for (int i = 0; i < vector.length; i++) {
            vector[i] = (Double) input[i];
        }

        return vector;
    }
}
