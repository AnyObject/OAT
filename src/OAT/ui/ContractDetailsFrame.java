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

import com.ib.client.ContractDetails;
import com.ib.client.EWrapperMsgGenerator;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.table.TableColumnModel;
import OAT.ui.util.UiUtil;

/**
 *
 * @author Antonio Yip
 */
public class ContractDetailsFrame extends AbstractTableFrame {

    public ContractDetailsFrame(Component component, ContractDetails cd) {
        super(component, cd);

        if (cd.m_summary != null) {
            setTitle(cd.m_summary.m_localSymbol
                    + (cd.m_contractMonth == null
                    ? ""
                    : " - " + cd.m_summary.m_symbol + " " + cd.m_contractMonth));
        }
    }

    public ContractDetails getContractDetails() {
        return (ContractDetails) theObject;
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
        List<String[]> rows = new ArrayList<String[]>();

        StringTokenizer st = new StringTokenizer(
                EWrapperMsgGenerator.contractDetails(0, getContractDetails()), "\n");

        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();

            if (!nextToken.contains("reqId") && !nextToken.contains("----")) {
                StringTokenizer rowSt = new StringTokenizer(nextToken, "=");

                String[] rowData = new String[2];
                rowData[0] = rowSt.nextToken().trim();
                rowData[1] = rowSt.nextToken().trim();

                if (rowData[1].equals("null")) {
                    rowData[1] = "";
                }

                rows.add(rowData);
            }
        }

        return rows.toArray(new Object[0][0]);
    }

    @Override
    protected Object[] getColumnHeaders() {
        return new Object[]{"Field", ""};
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
