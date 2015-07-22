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
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import neembuu.uploader.interfaces.UploadStatus;

/**
 *
 * @author Shashank Tulsyan
 */
public class UploadStatusRenderer extends DefaultTableCellRenderer {

    public UploadStatusRenderer() {
        //setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        String val = "";
        if(value!=null){
            //This if condition is must. Otherwise problems during removing rows.
            if((value instanceof UploadStatus)){
                val = ((UploadStatus)value).getLocaleSpecificString();
            }else {
                val = value.toString();
            }
        }
        setText(val);
        return this;
    }
    
}
