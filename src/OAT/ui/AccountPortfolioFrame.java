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

package OAT.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.table.TableColumnModel;
import OAT.trading.AccountPortfolio;
import OAT.trading.AccountPosition;
import OAT.ui.util.UiUtil;

/**
 *
 * @author Antonio Yip
 */
public class AccountPortfolioFrame extends AbstractTableFrame {

    public AccountPortfolioFrame(Component component, AccountPortfolio acct) {
        super(component, acct);

        setTitle(acct.toString());
    }

    public AccountPortfolio getAccountPortfolio() {
        return (AccountPortfolio) theObject;
    }

    @Override
    protected void initTable() {
        theTable.setAutoCreateRowSorter(true);
        theTable.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnModel = theTable.getColumnModel();
        columnModel.getColumn(1).setMinWidth(150);
        columnModel.getColumn(1).setCellRenderer(UiUtil.GENERAL_CELL_RENDERER);
    }

    @Override
    protected boolean isUpdatingAll() {
        return true;
    }

    @Override
    protected Dimension getMaxSize() {
        return new Dimension(400, 600);
    }

    @Override
    protected Object[][] getRows() {
        List<Object[]> rows = new ArrayList<Object[]>();

        rows.add(new Object[]{"Account"});
        for (String key : getAccountPortfolio().accountValues.keySet()) {
            rows.add(new Object[]{key});

            Properties prop = getAccountPortfolio().accountValues.get(key);

            for (String name : prop.stringPropertyNames()) {
                rows.add(new Object[]{"", name, prop.get(name)});
            }
        }

        rows.add(new Object[]{});
        rows.add(new Object[]{"Portfolio"});
        for (String key : getAccountPortfolio().accountPositions.keySet()) {
            AccountPosition position = getAccountPortfolio().accountPositions.get(key);

            rows.add(new Object[]{key, position.getPosition(), position.getDayPNL()});
        }

        return rows.toArray(new Object[0][0]);
    }

    @Override
    protected Object[] getColumnHeaders() {
        return new Object[]{"", "", ""};
    }

    @Override
    protected void preExit() {
    }

    @Override
    protected void windowLostFocusHandler(WindowEvent evt) {
        setVisible(false);
    }

    @Override
    protected void tableMouseClickHandler(MouseEvent evt) {
    }

    @Override
    protected void tableMouseEnteredHandler(MouseEvent evt) {
    }

    @Override
    protected void tableMouseExitedHandler(MouseEvent evt) {
    }
}
