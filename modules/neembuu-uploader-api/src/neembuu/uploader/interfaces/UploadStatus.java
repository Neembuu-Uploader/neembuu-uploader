/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.interfaces;

import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.utils.NULogger;

/**Enum for Uploader's status
 *
 * @author Shashaank Tulsyan
 */
public enum UploadStatus {
    QUEUED,
    INITIALISING,
    GETTINGCOOKIE,
    UPLOADING,
    GETTINGLINK,
    UPLOADFINISHED,
    UPLOADFAILED,
    UPLOADSTOPPED,
    UPLOADINVALID,
    GETTINGERRORS,
    PLEASEWAIT,
    NA,
    
    TORETRY,
    RETRYING,
    REUPLOADING,
    RETRYFAILED,

    LOGGINGIN,
    LOGGEDIN,
    LOGGINGFAILED;
    
    /**
     * 
     * @return the locale specific text for a particular Enum value
     */
    public String getLocaleSpecificString(){
        
        try{
            return Translation.T(/*UploadStatus.class.getName()+"."+*/this.toString());
        }catch(Exception a){
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), a});
            return "Error";
        }
 
    }
    
    /**
     * 
     * @return the default English text for a particular Enum value
     */
    public String getDefaultLocaleSpecificString(){
        
        try{
            return Translation.getDefaultTranslation(/*UploadStatus.class.getName()+"."+*/this.toString());
        }catch(Exception a){
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), a});
            return "Error";
        }
 
    }
}
