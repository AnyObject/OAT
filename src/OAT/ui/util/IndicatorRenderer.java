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

package OAT.ui.util;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Antonio Yip
 */
public class IndicatorRenderer extends XYLineAndShapeRenderer {

    protected Paint[] seriesPaint;
    protected Shape shape;
    protected Stroke stroke;

    public IndicatorRenderer(Paint[] seriesPaint, Shape shape, Stroke stroke) {
        this.seriesPaint = seriesPaint;
        this.shape = shape;
        this.stroke = stroke;
    }

    @Override
    public void drawItem(Graphics2D g2,
            XYItemRendererState state,
            Rectangle2D dataArea,
            PlotRenderingInfo info,
            XYPlot plot,
            ValueAxis domainAxis,
            ValueAxis rangeAxis,
            XYDataset dataset,
            int series,
            int item,
            CrosshairState crosshairState,
            int pass) {

        setSeriesShape(series, shape, false);
        setSeriesStroke(series, stroke, false);

        if (seriesPaint != null && series < seriesPaint.length) {
            setSeriesPaint(series, seriesPaint[series], false);
        }

        super.drawItem(g2, state, dataArea, info, plot,
                domainAxis, rangeAxis, dataset, series, item,
                crosshairState, pass);
    }
}
