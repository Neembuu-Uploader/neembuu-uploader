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


import java.util.logging.Level;
import neembuu.uploader.interfaces.Uploader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.utils.NULogger;

/**
 * Custom Table Model for Neembuu Uploader table
 * @author vigneshwaran
 */
public class NUTableModel extends AbstractTableModel {
    //Singleton
    private static NUTableModel INSTANCE = new NUTableModel();
    
    //These are the names for the table's columns.
    private static final String[] columnNames = new String[8];
    
    static {
        languageChanged_UpdateColumnNames();
    }

    //These are the classes for each column's values.
    private static final Class[] columnClasses = {String.class, String.class,
        Uploader.class, UploadStatus.class, String.class, JProgressBar.class, String.class, String.class};

    //These int are used to access Column names without using explicit index
    public static final int FILE = 0;
    public static final int SIZE = 1;
    public static final int HOST = 2;
    public static final int STATUS = 3;
    public static final int SPEED = 4;
    public static final int PROGRESS = 5;
    public static final int DOWNLOADURL = 6;
    public static final int DELETEURL = 7;
    
    
    //The table's list of uploads.
    public static final ArrayList<Uploader> uploadList = new ArrayList<Uploader>();
    public static List<Uploader> getUnsyncCopy_uploadList(){
        synchronized (uploadList){
            ArrayList<Uploader> uploadList2 = new ArrayList<Uploader>();
            Collections.copy(uploadList, uploadList2);
            return uploadList2;
        }
    }
    public static ArrayList<Uploader> getSortedListCopy(){
        ArrayList<Uploader> uls = new ArrayList<>();
        synchronized (NUTableModel.uploadList){
            for (int i = 0; i < NUTableModel.uploadList.size(); i++) {
                Uploader upld = (Uploader)NeembuuUploader.getInstance()
                        .neembuuUploaderTable.getValueAt(i,NUTableModel.HOST);
                uls.add(upld);
            }
        }return uls;
    }
    
    /**
     * Non instantiable. Use getInstance().
     */
    private NUTableModel(){
    }
    
    /**
     * 
     * @return singleton instance of table model
     */
    public static NUTableModel getInstance() {
        return INSTANCE;
    }
    

    /**
     * Adds a new upload to the table.
     */
    public void addUpload(Uploader upload){
        synchronized (uploadList){
            uploadList.add(upload);
        }
        //Fire table row insertion notification to table.
        fireTableRowsInserted(getRowCount()-1,getRowCount()-1);
        NULogger.getLogger().log(Level.INFO, "{0}New upload added", getClass().getName());
    }

    /**
     * Remove the selected row from table. 
     * Careful when removing as index of all rows change 
     * after removing a particular row
     * (if that row is not the last)
     */
    public void removeUpload(int selectedrow) {
        int actualSelectedRow;
        synchronized (uploadList){
            actualSelectedRow = 
                NeembuuUploader.getInstance().neembuuUploaderTable.convertRowIndexToModel(selectedrow);
            uploadList.remove(actualSelectedRow);
        }
        //Fire table row insertion notification to table.
        fireTableRowsDeleted(selectedrow, selectedrow);
        NULogger.getLogger().log(Level.INFO, "{0}: Row at {1} deleted actual {2}", new Object[]{
            getClass().getName(), selectedrow, actualSelectedRow});
    }
    
    /**
     * 
     * @return no of rows
     */
    @Override
    public int getRowCount() {
        synchronized (uploadList){
            return uploadList.size();
        }
    }

    /**
     * 
     * @return no of columns
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Gets a column's name.
     */
    @Override
    public String getColumnName(int col){
        return columnNames[col];
    }

    /**
     * Gets a column's class.
     */
    @Override
    public Class getColumnClass(int col){
        return columnClasses[col];
    }

    /**
     * 
     * @param rowIndex
     * @param columnIndex
     * @return the value at the cell under particular row index and column index
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
        Uploader upload ;
        synchronized (uploadList){
            upload = uploadList.get(rowIndex);
        }
        switch(columnIndex){
            case 0: //Filename
                return upload.getDisplayFileName();
            case 1: //Size
                return upload.getSize();
            case 2: //Host
                return upload;//.getHost();
            case 3: //Status
                return upload.getStatus();
            case 4: //Speed
                return upload.getSpeed();
            case 5: //Progress
                return new Integer(upload.getProgress());
            case 6: //DownloadURL
                return upload.getDownloadURL();
            case 7: //DeleteURL
                return upload.getDeleteURL();
        }
        
        } catch(Exception e) {
            ///Exception occurs when user removes some rows and progress bar requesting old index.. Must catch this otherwise runtime error
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e});
        }
        return "";
    }
    
    
    /**
     * Update the columns when the language is changed..
     */
    final static void languageChanged_UpdateColumnNames(){ 
        NULogger.getLogger().log(Level.INFO, "{0}Updating column names", NUTableModel.class.getName());
        
        columnNames[0]=Translation.T().File();
        columnNames[1]=Translation.T().Size();
        columnNames[2]=Translation.T().Host();
        columnNames[3]=Translation.T().Status();
        columnNames[4]=Translation.T().Speed();
        columnNames[5]=Translation.T().Progress();
        columnNames[6]=Translation.T().Download_URL();
        columnNames[7]=Translation.T().Delete_URL();
        
        //Must call this to reflect change on runtime..
        INSTANCE.fireTableStructureChanged();
        //
    }
}
