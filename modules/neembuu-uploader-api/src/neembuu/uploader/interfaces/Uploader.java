package neembuu.uploader.interfaces;

import java.io.File;

/**
 * This is the uploader interface you must implement if you are adding a host
 * This interface extends Runnable, so you'll have to override run and have your
 * login mechanism there.
 * @author vigneshwaran
 */
public interface Uploader extends Runnable {
    
    /*
    public static final String QUEUED = "Queued";
    public static final String INITIALISING = "Initialising";
    public static final String GETTINGCOOKIE = "Getting Cookie";
    public static final String UPLOADING = "Uploading";
    public static final String GETTINGLINK = "Getting link";
    public static final String UPLOADFINISHED = "Upload Finished";
    public static final String UPLOADFAILED = "Upload Failed";
    public static final String UPLOADSTOPPED = "Upload Stopped";
    
    public static final String PLEASEWAIT = "Please wait..";
    public static final String NA = "NA";
    //Following are for future use and not for status
    public static final String LOGGINGIN = "Logging in";
    public static final String LOGGEDIN = "Logged in";
    public static final String LOGGINGFAILED = "Logging Failed";
    */
    /**
     * @return the name of the website or a friendly user readable
     * name of this plugin
     */
    public String getDisplayName();
    /**
     * 
     * @return the file name
     */
    public String getFileName();
    
    public String getDisplayFileName();

    /**
     * 
     * @return Size of the file in String format (eg. 1GB, 240MB, 340KB, 50 bytes etc..)
     */
    public String getSize();
    
    
    /**
     * 
     * @return the max file size limit of the uploader.
     */
    public long getMaxFileSizeLimit();

    /**
     * 
     * @return the host name (differs for with and without account)
     */
    public String getHost();

    /**
     * 
     * @return the progress value - 0 to 100
     */
    public int getProgress();
    
    /**
     * 
     * @return the speed value.
     */
    public String getSpeed();

    /**
     * 
     * @return the status enum instance. It should be displayed with 
     * help of UploadStatusRenderer
     */
    public UploadStatus getStatus();

    /**
     * 
     * @return Download URL after completion of upload.. Returns "Please wait" until completion
     */
    public String getDownloadURL();

    /**
     * 
     * @return Delete URL after completion of upload if that site provides it.. Returns "NA" or "Please wait.." until completion
     */
    public String getDeleteURL();
    
    /**
     * starts the upload. calls the run method of thread.
     */
    public void startUpload();
    
    /**
     * stops the upload.. calls the stop method of thread. Deprecated.. but works well.
     */
    public void stopUpload();
    
    
    /**
     * Is this plugin dead?
     * @return True if the uploader is dead, false otherwise.
     */
    public boolean isDead();
    
    /**
     * 
     * @return the file.. This is necessary if we have to save the queued files list on exit.
     */
    public File getFile();
    
    
    /**
     * Set the file.
     * @param file The file to set.
     */
    public void setFile(File file);
}
