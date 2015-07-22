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

import neembuu.uploader.translation.Translation;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.logging.Level;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.settings.Application;
import neembuu.uploader.settings.Settings;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.utils.UploadStatusUtils;

/**
 * This class manages the queuing mechanism..
 *
 * @author vigneshwaran
 */
public class QueueManager {

    //singleton instance
    private static QueueManager queueManager = new QueueManager();
    //max no. of simultaneous uploads. 2 by default.
    private int maxNoOfUploads = 2;
    //number of current uploads
    private int currentlyUploading = 0;
    //boolean variable to check whether queue is locked or not.
    private volatile boolean queueLock = false;
    //boolean variable to check whether stopfurther is set or not.
    private boolean stopfurther = true;

    //return the singleton instance
    public static QueueManager getInstance() {
        return queueManager;
    }

    //noninstantiable
    private QueueManager() {
    }

    /**
     * set the maximum averageProgress of simultaneous uploads
     *
     * @param maxNoOfUploads
     */
    public void setMaxNoOfUploads(int maxNoOfUploads) {
        this.maxNoOfUploads = maxNoOfUploads;
    }

    /**
     * Get the max no of simultaneous uploads
     *
     * @return
     */
    public int getMaxNoOfUploads() {
        return maxNoOfUploads;
    }

    /**
     * Updates the queuing mechanism.. Doesn't affect queuing by averageProgress of times called.
     */
    public void updateQueue() {
        //if queue is locked or uploads are at max or stopfurther is set, then return
        if ((stopfurther) || (queueLock) || (currentlyUploading >= maxNoOfUploads)) {
            return;
        }
        
        setFrameTitle();
        ArrayList<Uploader> uls = NUTableModel.getSortedListCopy();        
        //Iterate from the top of the upload list everytime
        for (int i = 0; i < /*NUTableModel.uploadList*/uls.size(); i++) {
            Uploader uploader = /*NUTableModel.uploadList*/uls.get(i);
            
            //Find queued uploads
            if (uploader.getStatus() == UploadStatus.QUEUED) {
                //Start uploading
                NULogger.getLogger().log(Level.INFO, "{0}: Starting next upload..", getClass().getName());
                uploader.startUpload();

                //Increment the averageProgress of current uploads
                currentlyUploading++;

                //If current upload less than max allowed, then continue the loop.. Else break..
                if (currentlyUploading >= maxNoOfUploads) {
                    break;
                }
            } else if ((Application.get(Settings.class).autoretryfaileduploads()
                    && uploader.getStatus() == UploadStatus.UPLOADFAILED) || 
                    uploader.getStatus() == UploadStatus.TORETRY) {
                try {
                    NULogger.getLogger().log(Level.INFO, "{0}: Restarting upload ({1})", new Object[]{getClass().getName(), i});

                    File file = uploader.getFile();
                    Constructor<? extends Uploader> uploaderConstructor = uploader.getClass().getConstructor();
                    AbstractUploader retryingUploader = (AbstractUploader) uploaderConstructor.newInstance();
                    retryingUploader.setFile(file);
                    NUTableModel.uploadList.set(i, retryingUploader);
                    retryingUploader.setRetry(true);
                    retryingUploader.startUpload();
                    
                    //Increment the averageProgress of current uploads
                    currentlyUploading++;

                    //If current upload less than max allowed, then continue the loop.. Else break..
                    if (currentlyUploading >= maxNoOfUploads) {
                        break;
                    }
                } catch (Exception ex) {
                    NULogger.getLogger().log(Level.INFO, "Retry failed for {0}. Cause: {1}", new Object[]{i, ex});
                }
            }
        }
    }

    /**
     * Start next upload if any.. Decrements the current uploads averageProgress and
     * updates the queuing mechanism.
     */
    public void startNextUploadIfAny() {
        currentlyUploading--;
        //If no more uploads in queue and no more uploads currently running, reset the title
        if (getQueuedUploadCount() == 0 && currentlyUploading == 0){
            setFrameTitle();
            
            try{
                //If the tray icon is activated, then display message
                if(SystemTray.getSystemTray().getTrayIcons().length > 0) {
                    SystemTray.getSystemTray().getTrayIcons()[0].displayMessage(Translation.T().neembuuuploader(), Translation.T().allUploadsCompleted(), TrayIcon.MessageType.INFO);
                }
            }catch(UnsupportedOperationException a){
                //ignore
            }
        } else {
            updateQueue();
        }
    }

    /**
     * To lock or unlock the queue. If you lock, don't forget to unlock it at
     * the end
     */
    public void setQueueLock(boolean queueLock) {
        this.queueLock = queueLock;
        NULogger.getLogger().log(Level.INFO, "Queue Locked: {0}", queueLock);
        //if unlocking, update the queuing. 
        //If locking, this call will return.
        updateQueue();
    }

    /**
     * Set stopfurther true or false.
     *
     * @param value
     */
    public void setStopFurther(boolean value) {
        this.stopfurther = value;
        NULogger.getLogger().log(Level.INFO, "Stop Further: {0}", stopfurther);
        //if false, update the queuing.
        //if true, this call will return back.
        updateQueue();
    }

    /**
     *
     * @return whether stopfurther is set or not
     */
    public boolean isStopFurther() {
        return stopfurther;
    }

    /**
     * @return whether queue is locked or not
     */
    public boolean isQueueLocked() {
        return queueLock;
    }

    /**
     * Moves the selected rows to top of the list
     */
    public void moveRowsTop() {
        //If no rows selected, return
        if (NeembuuUploader.getInstance().getTable().getSelectedRowCount() <= 0) {
            return;
        }

        //Lock Queue
        NULogger.getLogger().log(Level.INFO, "{0}: Locking Queue..", getClass().getName());
        setQueueLock(true);

        //Get the rows from starting to end
        int starting = NeembuuUploader.getInstance().getTable().getSelectedRow();
        int noofrows = NeembuuUploader.getInstance().getTable().getSelectedRowCount();
        int ending = starting + noofrows;

        //Check if the rows are already at the top
        if (starting < 1) {
            NULogger.getLogger().info("Rows are already at top..");
            NULogger.getLogger().log(Level.INFO, "{0}: Unlocking Queue..", getClass().getName());
            setQueueLock(false);
            return;
        }

        NULogger.getLogger().info("Moving rows to top");

        //Copy the references to the selected rows and insert into the top (index:0) of the array list.
        NUTableModel.uploadList.addAll(0, NUTableModel.uploadList.subList(starting, ending));
        //Now the rows previously starting from 'starting' index will now be 
        //starting from 'ending' index due to the insertion above.
        //Remove those duplicate rows noofrows times
        for (int i = 1; i <= noofrows; i++) {
            NUTableModel.uploadList.remove(ending);
        }

        //Now the rows will be at the top. Set selection to them from 0 to noofrows-1
        NeembuuUploader.getInstance().getTable().setRowSelectionInterval(0, noofrows - 1);

        //Unlock Queue
        NULogger.getLogger().log(Level.INFO, "{0}: Unlocking Queue..", getClass().getName());
        setQueueLock(false);
    }

    /**
     * Moves the selected rows up one row in the list
     */
    public void moveRowsUp() {
        //If no rows selected, return
        if (NeembuuUploader.getInstance().getTable().getSelectedRowCount() <= 0) {
            return;
        }

        //Lock Queue
        NULogger.getLogger().log(Level.INFO, "{0}: Locking Queue..", getClass().getName());
        setQueueLock(true);

        //Get the rows from starting to end
        int starting = NeembuuUploader.getInstance().getTable().getSelectedRow();
        int noofrows = NeembuuUploader.getInstance().getTable().getSelectedRowCount();
        int ending = starting + noofrows;

        //Check if the rows are already at the top
        if (starting < 1) {
            NULogger.getLogger().info("Rows are already at top..");
            NULogger.getLogger().log(Level.INFO, "{0}: Unlocking Queue..", getClass().getName());
            setQueueLock(false);
            return;
        }

        NULogger.getLogger().info("Moving rows up one level");

        //Get the temporary reference to the row just above the starting row (index: starting -1)
        Uploader temp = NUTableModel.uploadList.get(starting - 1);
        //Now insert the duplicate just after the ending row. Now there are two copies of that row.
        NUTableModel.uploadList.add(ending, temp);
        //Now delete the above one..
        NUTableModel.uploadList.remove(starting - 1);

        //Shift the row selection just 1 level above
        //starting index will be starting -1
        //ending index is not ending -1. As we have just removed one row, it'll be ending-2
        NeembuuUploader.getInstance().getTable().setRowSelectionInterval(starting - 1, ending - 2);

        //Unlock Queue
        NULogger.getLogger().log(Level.INFO, "{0}: Unlocking Queue..", getClass().getName());
        setQueueLock(false);
    }

    /**
     * Moves the selected rows down one row in the list
     */
    public void moveRowsDown() {
        //If no rows selected, return
        if (NeembuuUploader.getInstance().getTable().getSelectedRowCount() <= 0) {
            return;
        }

        //Lock Queue
        NULogger.getLogger().log(Level.INFO, "{0}: Locking Queue..", getClass().getName());
        setQueueLock(true);

        //Get the rows from starting to end
        int starting = NeembuuUploader.getInstance().getTable().getSelectedRow();
        int noofrows = NeembuuUploader.getInstance().getTable().getSelectedRowCount();
        int ending = starting + noofrows;

        //Check if the rows are already at the bottom
        if (ending == NUTableModel.uploadList.size()) {
            NULogger.getLogger().info("Rows are already at the bottom..");
            NULogger.getLogger().log(Level.INFO, "{0}: Unlocking Queue..", getClass().getName());
            setQueueLock(false);
            return;
        }

        NULogger.getLogger().info("Moving rows down one level");

        //Get the temporary reference to the row just below the starting row (index: ending)
        Uploader temp = NUTableModel.uploadList.get(ending);
        //Now insert the duplicate just before the starting row. Now there are two copies of that row.
        NUTableModel.uploadList.add(starting, temp);
        //Now delete the below one.. As we have inserted the row above, the index will now be ending + 1
        NUTableModel.uploadList.remove(ending + 1);

        //Shift the row selection one level below
        //as we have inserted a row above, the rowselection (starting, ending-1) should be (starting+1,ending)
        NeembuuUploader.getInstance().getTable().setRowSelectionInterval(starting + 1, ending);

        //Unlock Queue
        NULogger.getLogger().log(Level.INFO, "{0}: Unlocking Queue..", getClass().getName());
        setQueueLock(false);
    }

    /**
     * Moves selected rows to the bottom of the list
     */
    public void moveRowsBottom() {
        //If no rows selected, return
        if (NeembuuUploader.getInstance().getTable().getSelectedRowCount() <= 0) {
            return;
        }

        //Lock Queue
        NULogger.getLogger().log(Level.INFO, "{0}: Locking Queue..", getClass().getName());
        setQueueLock(true);

        //Get the rows from starting to end
        int starting = NeembuuUploader.getInstance().getTable().getSelectedRow();
        int noofrows = NeembuuUploader.getInstance().getTable().getSelectedRowCount();
        int ending = starting + noofrows;

        //Check if the rows are already at the bottom
        if (ending == NUTableModel.uploadList.size()) {
            NULogger.getLogger().info("Rows are already at the bottom..");
            NULogger.getLogger().log(Level.INFO, "{0}: Unlocking Queue..", getClass().getName());
            setQueueLock(false);
            return;
        }

        NULogger.getLogger().info("Moving rows to bottom");

        //Copy the references to the selected rows and insert them at the end of the list.
        NUTableModel.uploadList.addAll(NUTableModel.uploadList.subList(starting, ending));

        //Unlike movetotop, the indices of the previous rows will not change.
        //Because addition at the end will not affect the indices of the above rows.
        //Remove the dupes at noofrows times.
        for (int i = 1; i <= noofrows; i++) {
            NUTableModel.uploadList.remove(starting);
        }

        //Set the noofrows selected at the end..
        //Starting index will be totalrowcount - noofselected rows
        //ending index will be totalrowcount - 1
        NeembuuUploader.getInstance().getTable().setRowSelectionInterval(NeembuuUploader.getInstance().getTable().getRowCount() - noofrows, NeembuuUploader.getInstance().getTable().getRowCount() - 1);

        //Unlock Queue
        NULogger.getLogger().log(Level.INFO, "{0}: Unlocking Queue..", getClass().getName());
        setQueueLock(false);
    }

    /**
     * Returns the count of queued upload.
     * It checks for two types of situation:
     * - upload status is QUEUED;
     * - upload status is UPLOADFAILED if the setting of auto retry uploads is
     *   actived.
     * @return Returns the count of queued upload.
     */
    public int getQueuedUploadCount() {
        //Initialize count as 0
        int count = 0;

        //Iterate through every row
        for (int i = 0; i < NUTableModel.uploadList.size(); i++) {
            //if a row is queued, increment the count.
            if (UploadStatusUtils.isRowStatusOneOf(i, UploadStatus.QUEUED) || 
                    (Application.get(Settings.class).autoretryfaileduploads() && 
                    UploadStatusUtils.isRowStatusOneOf(i, UploadStatus.UPLOADFAILED))
               ) {
                count++;
            }
        }

        //and return it.
        return count;
    }
    
    /**
     * 
     * @return The averageProgress of finished or failed uploads.
     */
    public int getFinishedOrFailedUploadCount(){
        //Initialize count as 0
        int count = 0;

        //Iterate through every row
        for (int i = 0; i < NUTableModel.uploadList.size(); i++) {
            //if a row is queued, increment the count.
            if ( UploadStatusUtils.isRowStatusOneOf(i, UploadStatus.RETRYFAILED, UploadStatus.UPLOADFAILED,
                    UploadStatus.UPLOADFINISHED, UploadStatus.UPLOADINVALID)) {
                count++;
            }
        }

        //and return it.
        return count;
    }
    
    
    /**
     * Set the Neembuu Uploader frame title.
     */
    private void setFrameTitle(){
        if(Application.get(Settings.class).showoverallprogress()){
            if(getQueuedUploadCount() == 0 && currentlyUploading == 0){
                NeembuuUploader.getInstance().resetTitle();
            }
            else{
                int averageProgress = getUploadProgressPercentage();
                NeembuuUploader.getInstance().setTitle( averageProgress + "% " + Translation.T().neembuuuploader());
            }
        }
    }
    
    /**
     * Get upload progress percentage (could be more intelligent, checking the file size).
     * @return Returns average upload progress.
     */
    private int getUploadProgressPercentage(){
        int sum = 0;
        int progress = 0;
        
        //Iterate through every row
        for (int i = 0; i < NUTableModel.uploadList.size(); i++) {
            progress = (Integer) NeembuuUploader.getInstance().getTable().getValueAt(i, NUTableModel.PROGRESS);
            
            if (UploadStatusUtils.isRowStatusOneOf(i, UploadStatus.RETRYFAILED, UploadStatus.UPLOADFAILED,
                    UploadStatus.UPLOADFINISHED, UploadStatus.UPLOADINVALID)) {
                progress = 100;
            }
            sum += progress;
        }
        
        return sum/NUTableModel.uploadList.size();
    }
}
