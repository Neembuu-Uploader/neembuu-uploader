/* 
 * Copyright (C) 2015 Shashank Tulsyan <shashaank at neembuu.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package neembuu.uploader;

import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/*This class renders a JProgressBar in a table cell.
 * 
 */
class ProgressRenderer extends JProgressBar
        implements TableCellRenderer{

    public ProgressRenderer(int min, int max) {
        super(min,max);
    }
    /*Returns this JProgressBar as the renderer
     * for the given table cell. */

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        //Set JProgressBar's percent complete value.
        //This if condition is must.
        if(value!=null && value!="")
        setValue(((Integer)value).intValue());
        return this;
    }

}
