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
package neembuu.uploader.utils;

import javax.swing.JTable;
import neembuu.uploader.NUTableModel;
import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.interfaces.UploadStatus;

/**
 * Commons utils for UploadStatus.
 * @author davidepastore
 */
public class UploadStatusUtils {

    /**
     * Check if a row is with a status.
     * @param row The row index of the neembuu table.
     * @param uploadStatus A list of UploadStatus objects.
     * @return Returns true if the row status with i is one of the uploadStatus, false otherwise.
     */
    public static boolean isRowStatusOneOf(int row, UploadStatus... uploadStatus) {
        JTable table = NeembuuUploader.getInstance().getTable();
        for (UploadStatus status : uploadStatus) {
            if (table.getValueAt(row, NUTableModel.STATUS) == status) {
                return true;
            }
        }
        return false;
    }
    
}
