/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.interfaces.abstractimpl;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import neembuu.release1.api.ui.MainComponent;
import neembuu.uploader.api.accounts.AccountsProvider;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.GetSizeAsString;
import neembuu.uploader.uploaders.common.MonitoredFileBody;
import neembuu.uploader.uploaders.common.MonitoredFileEntity;
import neembuu.uploader.utils.NUProperties;
//import neembuu.uploader.versioning.UserImpl;
import neembuu.uploader.versioning.UserProvider;
import shashaank.smallmodule.SmallModule;

/**
 *
 * @author vigneshwaran
 */
public abstract class AbstractUploader implements Uploader {

    protected File file;
    protected String downURL = "";
    protected String delURL = "";
    protected String host = "";
    protected final AtomicInteger uploadProgress = new AtomicInteger(0);
    protected StringBuffer speed = new StringBuffer();
    protected UploadStatus status = UploadStatus.QUEUED;
    protected Thread thread = new Thread(this);
    protected long maxFileSizeLimit = Long.MAX_VALUE;
    
    /**
     * Is this uploader dead?
     */
    protected boolean isDead = false;
    private boolean retry = false;
    
    
    public AbstractUploader() {
    }
    
    private static UserProvider userProvider = null;
    private static MainComponent mainComponent = null;
    private static AccountsProvider accountsProvider = null;
    private static NUProperties properties = null;
    public static void init(UserProvider userProvider,MainComponent mainComponent,
            AccountsProvider accountsProvider,NUProperties properties){
        if(AbstractUploader.userProvider!=null){
            throw new IllegalStateException("Already initialized");
        }
        AbstractUploader.userProvider = userProvider;
        AbstractUploader.mainComponent = mainComponent;
        AbstractUploader.accountsProvider = accountsProvider;
        AbstractUploader.properties = properties;
    }
    
    protected static NUProperties properties(){
        return properties;
    }
    
    protected static void showErrorMessage(String message,String title){
        mainComponent.newMessage().error()
                .setTitle(title)
                .setMessage(message)
                .setTimeout(10000)
                .show();
    }
    
    protected static void showWarningMessage(String message,String title){
        mainComponent.newMessage().warning()
                .setTitle(title)
                .setMessage(message)
                .setTimeout(10000)
                .show();
    }

    public static AccountsProvider getAccountsProvider() {
        return accountsProvider;
    }
    
    protected MainComponent mainComponent(){
        return mainComponent;
    }

    public String getFileName() {
        String toRet = file.getName();
        if(!userProvider.getUserInstance().canCustomizeNormalizing()
                /*UserImpl.I().canCustomizeNormalizing()*/){
            //toRet = UserImpl.I().normalizeFileName(toRet);
            toRet = userProvider.getFileNameNormalizer().normalizeFileName(toRet);
        }
        return toRet;
    }

    @Override
    public String getDisplayFileName() {
        return getFileName();
    }

    public String getSize() {
        return GetSizeAsString.getSize(file.length());
        //return CommonUploaderTasks.getSize(file.length());
    }
    
    public long getMaxFileSizeLimit() {
        return maxFileSizeLimit;
    }

    public String getHost() {
        return host;
    }

    public int getProgress() {
        return uploadProgress.get();
    }
    
    public String getSpeed(){
        return speed.toString();
    }

    public UploadStatus getStatus() {
        return status;
    }

    public String getDownloadURL() {
        return downURL;
    }

    public String getDeleteURL() {
        return delURL;
    }

    public void startUpload() {
        thread.start();
    }

    public void stopUpload() {
        status = UploadStatus.UPLOADSTOPPED;
        CommonUploaderTasks.uploadStopped(this);
        thread.stop();
    }

    public File getFile() {
        return file;
    }
    
    
    public void setFile(File file){
        this.file = file;
    }

    public abstract void run();

    /**
     * The Uploader is initializing its variables.
     */
    protected void uploadInitialising() {
        if (retry) {
            status = UploadStatus.RETRYING;
        } else {
            status = UploadStatus.INITIALISING;
        }
    }
    
    /**
     * The upload operation is starting now.
     */
    protected void uploading() {
        if (retry) {
            status = UploadStatus.REUPLOADING;
        } else {
            status = UploadStatus.UPLOADING;
        }
    } 
    
    /**
     * The upload is invalid. One reason can be the file size.
     */
    protected void uploadInvalid() {
        status = UploadStatus.UPLOADINVALID;
        resetSpeed();
        CommonUploaderTasks.uploadFailed(this);
    }
    
    /**
     * The upload is failed.
     */
    protected void uploadFailed() {
        if (retry) {
            status = UploadStatus.RETRYFAILED;
        } else {
            status = UploadStatus.UPLOADFAILED;
        }
        resetSpeed();
        CommonUploaderTasks.uploadFailed(this);
    }

    /**
     * The upload is completed correctly.
     */
    protected void uploadFinished() {
        status = UploadStatus.UPLOADFINISHED;
        resetSpeed();
        CommonUploaderTasks.uploadFinished(this);
    }
    
    /**
     * Change the status of the Uploader.
     */
    protected void gettingLink(){
        status = UploadStatus.GETTINGLINK;
    }
    
    public void setRetry(boolean retry) {
        this.retry = retry;
        status = UploadStatus.TORETRY;
    }
    
    /**
     * Reset the speed.
     */
    private void resetSpeed(){
        speed.setLength(0);
    }
    
    /**
     * Create the MonitoredFileBody.
     * @return returns the MonitoredFileBody.
     */
    protected MonitoredFileBody createMonitoredFileBody(){
        return new MonitoredFileBody(file, uploadProgress, speed);
    }
    
    /**
     * Create the MonitoredFileEntity.
     * @return returns the MonitoredFileEntity.
     */
    protected MonitoredFileEntity createMonitoredFileEntity(){
        return new MonitoredFileEntity(file, uploadProgress, speed);
    }
    
    /**
     * Check if the uploader is dead.
     * @return Returns true if the uploader is dead, false otherwise.
     */
    public boolean isDead(){
        return isDead;
    }

    @Override
    public String toString() {
        return getHost()+"@"+getDisplayFileName();
    }
    
    @Override
    public String getDisplayName() {
        SmallModule sm = (SmallModule)this.getClass().getAnnotation(SmallModule.class);
        if(sm!=null){
            return sm.name();
        }
        
        if(host!=null)return host;
        
        String nm = getClass().getSimpleName();
        nm = nm.toLowerCase();
        if(nm.contains("dot")){
            nm = nm.replace("dot", ".");
        }if(nm.contains("one")){
            nm = nm.replace("one", "1");
        }if(!nm.endsWith(".com")){
            nm = nm+".com"; //assume .com
        }    
        return nm;
    }
}
