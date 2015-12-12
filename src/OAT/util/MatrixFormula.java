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
import java.util.StringTokenizer;

/**
 *
 * @author Antonio Yip
 */
public class MatrixFormula {

    private Matrix K;
    private Matrix M;

    public MatrixFormula(Matrix K, Matrix M) {
        if (K == null || M == null
                || K.getRowDimension() != M.getRowDimension()) {
            throw new UnsupportedOperationException("Invalid matrix.");
        }

        this.K = K;
        this.M = M;
    }

    public MatrixFormula(String string) throws Exception {
        try {
            int i = 0;
            int j = 0;
            int row = TextUtil.count("=", string);
            if (row == 0) {
                return;
            }
            int column = TextUtil.count("X", string) / row;

            K = new Matrix(row, 1);
            M = new Matrix(row, column);

            StringTokenizer st = new StringTokenizer(
                    string.replaceAll("Output [\\d]* =", "!=").substring(1), "!");

            while (st.hasMoreTokens()) {
                String nextToken = st.nextToken();
                StringTokenizer st1 = new StringTokenizer(nextToken, "+(*)");

                while (st1.hasMoreTokens()) {
                    String nextToken1 = st1.nextToken();

                    if (nextToken1.codePointAt(0) == 65279) {
                        continue;
                    }

                    if (nextToken1.matches("=[\\d\\D]*")) {
                        K.set(i, 0, Double.valueOf(nextToken1.replaceAll("=", "")));
                        j = 0;

                    } else {
                        if (!nextToken1.matches("\\s*X\\d+|\\s+")) {
                            M.set(i, j, Double.valueOf(nextToken1));
                            j++;
                        }
                    }
                }

                i++;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String toString() {
        if (M == null || K == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < M.getRowDimension(); i++) {
            sb.append("Output ").append(i + 1).
                    append(" = ").append(K.get(i, 0));

            for (int j = 0; j < M.getColumnDimension(); j++) {
                sb.append(" + (").append(M.get(i, j)).
                        append(" * X").append(j + 1).append(")");
            }

            sb.append(" ");
        }

        return sb.toString();
    }

    public Matrix calculate(Matrix input) {
        if (M == null || input == null
                || M.getRowDimension() != input.getColumnDimension()) {
            return null;
        }

        return M.times(input).plus(K);
    }

    public Matrix getK() {
        return K;
    }

    public Matrix getM() {
        return M;
    }
}
