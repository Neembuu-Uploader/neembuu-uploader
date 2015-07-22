/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.api._hostr;

import neembuu.uploader.exceptions.accounts.NUAccountNotActivedException;
import neembuu.uploader.exceptions.accounts.NUBannedUserException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.exceptions.uploaders.NUDailyUploadLimitException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import org.json.JSONObject;

/**
 * This class includes methods to make common operations between login and upload for hostr.co.
 * @author davidepastore
 */
public class HostrApi {
    
    private JSONObject jsonObj;
    private String username;
    private String hostname;
    private long fileSizeLimit;
    private String fileName;
    private int dailyUploadAllowance;
    
    /**
     * Handles the exceptions based on the code read from the jsonObj.
     * @throws Exception 
     */
    public void handleErrors() throws Exception{
        int code = jsonObj.getJSONObject("error").getInt("code");
        
        switch(code){
            case 601:
                throw new NUMaxFileSizeException(this.fileSizeLimit, fileName, hostname);
            case 602:
                throw new NUDailyUploadLimitException(dailyUploadAllowance, fileName, hostname);
            case 603:
                throw new NUAccountNotActivedException(username, hostname);
            case 605:
                throw new Exception("The user is unauthenticated: " + jsonObj.toString());
            case 606:
                throw new NUInvalidLoginException(username, hostname);
            case 607:
                throw new NUBannedUserException(username, hostname);
            default:
                throw new Exception("Generic exception for localhostr account: " + jsonObj.toString());
        }
        
    }
    
    
    public void setJSONObject(JSONObject jsonObj){
        this.jsonObj = jsonObj;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setHostname(String hostname){
        this.hostname = hostname;
    }
    
    public void setFileSizeLimit(long fileSizeLimit){
        this.fileSizeLimit = fileSizeLimit;
    }
    
    public void setFileName(String fileName){
        this.fileName = fileName;
    }
    
    public void setDailyUploadAllowance(int dailyUploadAllowance){
        this.dailyUploadAllowance = dailyUploadAllowance;
    }
    
}
