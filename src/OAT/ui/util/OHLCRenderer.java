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
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import OAT.trading.Main;

/**
 *
 * @author Antonio Yip
 */
public class OHLCRenderer extends HighLowRenderer {

    /**
     * The paint used to fill the candle when the price moved up from open to
     * close.
     */
    private transient Paint upPaint;
    /**
     * The paint used to fill the candle when the price moved down from open
     * to close.
     */
    private transient Paint downPaint;
    /**
     * The paint used to fill the candle when the price is unchanged from open
     * to close.
     */
    private transient Paint flatPaint;

    public OHLCRenderer() {
        super();

        this.upPaint = Main.upColor;
        this.downPaint = Main.downColor;
        this.flatPaint = Main.flatColor;
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

        if (dataset instanceof OHLCDataset) {
            OHLCDataset highLowData = (OHLCDataset) dataset;

            double yOpen = highLowData.getOpenValue(series, item);
            double yClose = highLowData.getCloseValue(series, item);

            if (yClose > yOpen) {
                setSeriesPaint(series, upPaint, false);

            } else if (yClose < yOpen) {
                setSeriesPaint(series, downPaint, false);

            } else {
                setSeriesPaint(series, flatPaint, false);
            }
        }

        super.drawItem(g2, state, dataArea, info, plot,
                domainAxis, rangeAxis, dataset, series, item,
                crosshairState, pass);
    }

    /**
     * Returns the paint used to fill candles when the price moves up from open
     * to close.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setUpPaint(Paint)
     */
    public Paint getUpPaint() {
        return this.upPaint;
    }

    /**
     * Sets the paint used to fill candles when the price moves up from open
     * to close and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getUpPaint()
     */
    public void setUpPaint(Paint paint) {
        this.upPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to fill candles when the price moves down from
     * open to close.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setDownPaint(Paint)
     */
    public Paint getDownPaint() {
        return this.downPaint;
    }

    /**
     * Sets the paint used to fill candles when the price moves down from open
     * to close and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param paint  The paint (<code>null</code> permitted).
     */
    public void setDownPaint(Paint paint) {
        this.downPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to fill candles when the price is unchanged from
     * open to close.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setDownPaint(Paint)
     */
    public Paint getFlatPaint() {
        return this.flatPaint;
    }

    /**
     * Sets the paint used to fill candles when the price is unchanged from open
     * to close and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param paint  The paint (<code>null</code> permitted).
     */
    public void setflatPaint(Paint paint) {
        this.flatPaint = paint;
        fireChangeEvent();
    }
}
