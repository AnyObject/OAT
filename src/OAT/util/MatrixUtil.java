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

import Jama.Matrix;

/**
 *
 * @author Antonio Yip
 */
public class MatrixUtil {

    public static String printMatrix(Matrix matrix) {
        return printMatrix(matrix, "; ", "[", "]");
    }

    public static String printMatrix(Matrix matrix, String separator, String colBegin, String colEnd) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < matrix.getRowDimension(); i++) {
            sb.append(colBegin);
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                sb.append(TextUtil.PRECISE_FORMATTER.format(matrix.get(i, j))).append(separator);
            }
            sb.append(colEnd).append("\n");
        }

        return sb.toString().trim().replaceAll(separator + colEnd, colEnd);
    }

    public static Matrix getMatrix(Object[] array) {
        Matrix m = new Matrix(array.length, 1);

        for (int i = 0; i < array.length; i++) {
            m.set(i, 0, (Double) array[i]);
        }

        return m;
    }
}
