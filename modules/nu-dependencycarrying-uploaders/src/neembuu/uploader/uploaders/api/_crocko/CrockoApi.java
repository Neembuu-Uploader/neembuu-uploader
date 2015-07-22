/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.api._crocko;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.exceptions.uploaders.NUFileInBlackListException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.exceptions.uploaders.NUMinFileSizeException;
import neembuu.uploader.exceptions.uploaders.NUUploadFailedException;
import neembuu.uploader.exceptions.uploaders.NUUploadLimitExceededException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Converted class from the official PHP crockoapi.
 * I can add some other methods. Take a look at NS link.
 * @author davidepastore
 */
public class CrockoApi {
    
    public static long MAX_FILE_SIZE = 1073741824l; //1 GB
    public static long MIN_FILE_SIZE = 1l; //1 B
    public static String CROCKO_API_URL = "http://api.crocko.com";
    public static String NS = "http://www.crocko.com/developers.html";
    
    private static Map<String, String> errorMap = new HashMap<String, String>();
    private static boolean errorMapAllocates = false;
    private static final String HOSTNAME = "Crocko.com";
    
    //Error constants
    private static final String ERROR_FILE_SIZE = "errorFileSize";
    private static final String ERROR_EMPTY_FILE = "errorEmptyFile";
    private static final String ERROR_UPLOAD_LIMIT = "errorUploadLimit";
    private static final String ERROR_FILE_IN_BLACKLIST = "errorFileInBlackList";
    private static final String ERROR_UPLOAD_FAILD = "errorUploadFaild";
    private static final String ERROR_WRONG_CREDENTIALS = "errorWrongCredentials";
    
    private static enum ERROR{
        ERROR_FILE_SIZE,
        ERROR_EMPTY_FILE,
        ERROR_UPLOAD_LIMIT,
        ERROR_FILE_IN_BLACKLIST,
        ERROR_UPLOAD_FAILD,
        ERROR_WRONG_CREDENTIALS
    }
      
    private static float version = 0.1f;
    // HTTP code response
    private static int httpCode;
    // Error title
    private static String error;
    // Error message 
    private static String errorMessage;
    // API key
    private static String apikey;
    // download link
    private static String downloadLink;
    // delete link
    private static String deleteLink;
    // uploaded file id
    private static String id;
    private static String body;
    private static Map<String, List<String>> headers;
    private static String title;
    private static String length;
    private static String directLink;
    private static String accountID;
    private static String accountName;
    private static String accountEmail;
    private static String premiumStatus;
    private static String premiumTrafficLeft;
    private static String premiumStart;
    private static String premiumEnd;
    private static String folderID;
    private static String folderUrl;
    private static String balance;
    private static String clone_downloadLink;
    private static String clone_id;
    private static String clone_deleteLink;
    /**
     * Construct
     * If you have already an api key, you can call this function and
     * go with upload or other functions for user.
     * @param apiKey The api key of an account.
     */
    public CrockoApi(String apiKey){
        apikey = apiKey;
    }
    
    /**
     * Getting API key from Crocko.com. Set this.apikey new value. 
     * @param String username
     * @param String password
     * @return String apikey value
     */
    public static String getAPIkey(String username, String password) throws Exception{
        httpCode = 0;
        error = null;
        errorMessage = null;
        body = null;
        headers = null;
        apikey = "";
        HttpClient httpclient = NUHttpClient.getHttpClient();
       
        
        if (username == null || password == null) { 
            // Your login or password are empty!
            return null;
        }
        
        //Setting connection parameters
        HttpPost httpPost= new HttpPost(CROCKO_API_URL+"/apikeys");
        httpPost.setHeader("Accept", "application/atom+xml");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("login", username));
        formparams.add(new BasicNameValuePair("password", password));
        formparams.add(new BasicNameValuePair("submit_login", "Login+to+MediaFire"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        HttpResponse httpResponse = httpclient.execute(httpPost);
        httpResponse.getStatusLine().getStatusCode();
        
        httpCode = httpResponse.getStatusLine().getStatusCode();
        body = EntityUtils.toString(httpResponse.getEntity());
        
        NULogger.getLogger().log(Level.INFO, "Body: {0}", body);
        Document doc = Jsoup.parse(body);
        
        if(httpCode == 201){
            apikey = doc.select("feed entry content").first().text();
            return apikey;
        }
        else{
            error = doc.select("feed entry title").first().text();
            errorMessage = doc.select("feed entry content").first().text();
            throw new NUInvalidLoginException(username, HOSTNAME);
        }
    }
    
    
    /**
    * Check the final status of the upload.
    * @param body the body response from the server after upload.
    * @param responseHttpCode http code of response.
    * @param filename the file name you want to upload.
    * 
    * @return boolean true and  set CrockoAPI.id, CrockoAPI.downloadLink,  CrockoAPI.deleteLink,
    *      CrockoAPI.length and CrockoAPI.title if file has been successfully uploaded or false.
    *  If you want to obtain the links or other information, use the get methods.
    */    
    public static boolean uploadController (String body, int responseHttpCode, String fileName) throws Exception{    
        httpCode = responseHttpCode;   
        error = null;
        errorMessage = null;
        id = null;
        title = null;
        downloadLink = null;
        deleteLink = null;
        
        if (apikey == null) {
            //API key is empty
            return false;
        }
        
        //Create document
        Document doc = Jsoup.parse(body);
        //FileUtils.saveInFile("CrockoApi.xml", body);
        NULogger.getLogger().log(Level.INFO, "Body: {0}", body);
        NULogger.getLogger().log(Level.INFO, "HttpCode: {0}", httpCode);
        
        if(httpCode == 201){
            id = doc.select("feed entry id").text();
            downloadLink = doc.select("feed entry link").first().attr("href");
            deleteLink = doc.select("feed entry link").eq(1).attr("href");
            length = doc.select("feed entry size").text();
            title = doc.select("feed entry title").text();
            
            NULogger.getLogger().log(Level.INFO, "id: {0}", id);
            NULogger.getLogger().log(Level.INFO, "downloadLink: {0}", downloadLink);
            NULogger.getLogger().log(Level.INFO, "deleteLink: {0}", deleteLink);
            NULogger.getLogger().log(Level.INFO, "length: {0}", length);
            NULogger.getLogger().log(Level.INFO, "title: {0}", title);
            
            return true;
        }
        else if(doc != null){
            error = doc.select("feed entry title").text();
            errorMessage = doc.select("feed entry content").text();
            NULogger.getLogger().log(Level.INFO, "error: {0}", error);
            NULogger.getLogger().log(Level.INFO, "errorMessage: {0}", errorMessage);
            
            ERROR err = ERROR.valueOf(error);
            switch(err){
                case ERROR_EMPTY_FILE:
                    throw new NUMinFileSizeException(MIN_FILE_SIZE, fileName, HOSTNAME);
                case ERROR_FILE_IN_BLACKLIST:
                    throw new NUFileInBlackListException(fileName, HOSTNAME);
                case ERROR_FILE_SIZE:
                    throw new NUMaxFileSizeException(MAX_FILE_SIZE, fileName, HOSTNAME);
                case ERROR_UPLOAD_FAILD:
                    throw new NUUploadFailedException(fileName, HOSTNAME);
                case ERROR_UPLOAD_LIMIT:
                    throw new NUUploadLimitExceededException(fileName, HOSTNAME);
                default:
                    throw new Exception(convertErrorCode(error));
            }
        }
        return false;
    }
    
    
    /**
     * Return the error code. <b>Call this function after <i>getAPIkey()</i> to
     * obtain the error.</b>
     * @return Error code
     */
    public static String getError(){
        return error;
    }
    
    /**
     * Return the error message. <b>Call this function after <i>getAPIkey()</i> to
     * obtain the error message.</b>
     * @return Error message.
     */
    public static String getErrorMessage(){
        return Translation.T(convertErrorCode(error));
    }
    
    /**
     * Return the download URL. <b>Call this function after <i>upload()</i> to
     * obtain the download URL.</b>
     * @return Download URL.
     */
    public static String getDownloadURL(){
        return downloadLink;
    }
    
    /**
     * Return the delete URL. <b>Call this function after <i>upload()</i> to
     * obtain the delete URL.</b>
     * @return Download URL.
     */
    public static String getDeleteURL(){
        return deleteLink;
    }
    
    /**
     * Convert an error code of Crocko in an error code for NU.
     * @return Error code for crocko.
     */
    public static String convertErrorCode(String errorCode){
        allocateMap();
        return errorMap.get(errorCode);
}
    
    /**
     * Allocate the error map.
     */
    private static void allocateMap(){
        if(!errorMapAllocates){
            errorMap.put(ERROR_FILE_SIZE, "maxfilesize");
            errorMap.put(ERROR_EMPTY_FILE, "emptyfile");
            errorMap.put(ERROR_UPLOAD_LIMIT, "uploadlimit");
            errorMap.put(ERROR_FILE_IN_BLACKLIST, "fileinblacklist");
            errorMap.put(ERROR_UPLOAD_FAILD, "uploadfailed");
            errorMap.put(ERROR_WRONG_CREDENTIALS, "invalidlogin");
            errorMapAllocates = true;
        }
    }
}
