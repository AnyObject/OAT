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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import OAT.util.FileUtil;

/**
 *
 * @author Antonio Yip
 */
public  class FileChooserCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        Object cellValue;
        File selectedFile;
        JButton button;
        JFileChooser fileChooser;
        JDialog dialog;
        protected static final String EDIT = "edit";

        public FileChooserCellEditor() {
            //Set up the editor (from the table's point of view),
            //which is a button.
            //This button brings up the color chooser dialog,
            //which is the editor from the user's point of view.
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            //button.setBorderPainted(false);

            //Set up the dialog that the button brings up.
            fileChooser = new JFileChooser();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                //The user has clicked the cell, so
                //bring up the dialog.
                //button.setBackground(currentColor);
                //fileChooser.setColor(currentColor);
                //dialog.setVisible(true);
                int returnVal = fileChooser.showOpenDialog(button);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    //This is where a real application would open the file.
                } else {
                    //System.out.println("User cancelled");
                }

                //Make the renderer reappear.
                fireEditingStopped();
            }
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        @Override
        public Object getCellEditorValue() {
            if (selectedFile != null) {
                return FileUtil.readFileToArrayList(selectedFile);
            } else {
                return cellValue;
            }
        }

        //Implement the one method defined by TableCellEditor.
        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {

            cellValue = value;
            return button;
        }
    }
