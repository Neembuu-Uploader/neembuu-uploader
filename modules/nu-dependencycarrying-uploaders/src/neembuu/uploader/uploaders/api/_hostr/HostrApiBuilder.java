/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.api._hostr;

import org.json.JSONObject;

/**
 * This is a builder class that sets attributes from calls to methods.
 * @author davidepastore
 */
public class HostrApiBuilder {
    
    private JSONObject jsonObj;
    private String username;
    private String hostname;
    private long fileSizeLimit;
    private String fileName;
    private int dailyUploadAllowance;
    
    /**
     * Set the JSONObject
     * @param jsonObj the JSONObject
     * @return the HostrApiBuilder
     */
    public HostrApiBuilder setJSONObject(JSONObject jsonObj){
        this.jsonObj = jsonObj;
        return this;
    }
    
    /**
     * Se the username
     * @param username the username
     * @return the HostrApiBuilder
     */
    public HostrApiBuilder setUsername(String username){
        this.username = username;
        return this;
    }
    
    /**
     * Set the hostname
     * @param hostname the hostname
     * @return the HostrApiBuilder
     */
    public HostrApiBuilder setHostname(String hostname){
        this.hostname = hostname;
        return this;
    }
    
    /**
     * Set the file size limit
     * @param fileSizeLimit the file size limit
     * @return the HostrApiBuilder
     */
    public HostrApiBuilder setFileSizeLimit(long fileSizeLimit){
        this.fileSizeLimit = fileSizeLimit;
        return this;
    }
    
    /**
     * Set the file name
     * @param fileName the file name
     * @return the HostrApiBuilder
     */
    public HostrApiBuilder setFileName(String fileName){
        this.fileName = fileName;
        return this;
    }
    
    /**
     * Set the daily upload allowance
     * @param dailyUploadAllowance daily upload allowance
     * @return the HostrApiBuilder
     */
    public HostrApiBuilder setDailyUploadAllowance(int dailyUploadAllowance){
        this.dailyUploadAllowance = dailyUploadAllowance;
        return this;
    }
    
    
    /**
     * Create an instance of HostrApi
     * @return a new instance of HostrApi
     */
    public HostrApi build(){
        HostrApi hostrApi = new HostrApi();
        hostrApi.setJSONObject(jsonObj);
        hostrApi.setUsername(username);
        hostrApi.setHostname(hostname);
        hostrApi.setFileSizeLimit(fileSizeLimit);
        hostrApi.setFileName(fileName);
        hostrApi.setDailyUploadAllowance(dailyUploadAllowance);
        return hostrApi;
    }
}
