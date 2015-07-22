/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.FileServeAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
@SmallModule(
    exports={FileServe.class,FileServeAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FileServe.com"
)
public class FileServe extends AbstractUploader implements UploaderAccountNecessary {

    FileServeAccount fileServeAccount = (FileServeAccount) getAccountsProvider().getAccount("FileServe.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    private String postURL = "";
    private String downloadlink = "", deletelink = "";
    private String sessioncookie = "";
    private String tmpURL = "";
    private long fileSizeLimit = 1073741824l; //1 GB
    
    private ArrayList<String> disallowedExtensions = new ArrayList<String>();

    public FileServe() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "FileServe.com";
        if (fileServeAccount.loginsuccessful) {
            host = fileServeAccount.username + " | FileServe.com";
        }

    }

    @Override
    public void run() {
        try {
            if (fileServeAccount.loginsuccessful) {

                host = fileServeAccount.username + " | FileServe.com";
            } else {

                host = "FileServe.com";

                uploadInvalid();
                return;
            }
            
            //Check size
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), fileServeAccount.getHOSTNAME());
            }
            
            addExtensions();
            
            //Check extension
            if(FileUtils.checkFileExtension(disallowedExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }
            
            //Get HTTPContext from account
            httpContext = fileServeAccount.getHttpContext();


            //    initialize();
            uploadInitialising();
            tmpURL = StringUtils.stringBetweenTwoStrings(getData("http://fileserve.com/upload-file.php"), "http://upload.fileserve.com/upload/", "\"");
            postURL = "http://upload.fileserve.com/upload/" + tmpURL;
            long uploadtime = new Date().getTime();
            tmpURL = "http://upload.fileserve.com/upload/" + tmpURL + "?callback=jQuery" + Math.round((Math.random() * 1000000000000000000L) + 1000000000000000000L) + "_" + uploadtime + "&_=" + (uploadtime + Math.round(Math.random() * 100000));
            NULogger.getLogger().log(Level.INFO, "tmp URL: {0}", tmpURL);

            String sessionid = getData(tmpURL);
            sessionid = StringUtils.stringBetweenTwoStrings(sessionid, "sessionId:'", "'");
            NULogger.getLogger().log(Level.INFO, "Session ID: {0}", sessionid);

            postURL += sessionid;

            NULogger.getLogger().log(Level.INFO, "post URL : {0}", postURL);
            fileUpload();

            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {
            Logger.getLogger(FileServe.class.getName()).log(Level.SEVERE, null, ex);

            uploadFailed();
        }
    }

    public void initialize() throws IOException {

        uploadInitialising();
        NULogger.getLogger().info("Getting start up cookie from FileServe.com");
        httpGet = new NUHttpGet("http://www.fileserve.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        /*
        u = new URL("http://www.fileserve.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
                if (tmp.contains("PHPSESSID")) {
                    sessioncookie = tmp;
                    sessioncookie = sessioncookie.substring(0, sessioncookie.indexOf(";"));
                }
            }
            NULogger.getLogger().log(Level.INFO, "session cookie : {0}", sessioncookie);
        }
        */
    }

    public String getData(String url) throws Exception {
        httpGet = new NUHttpGet(url);
        httpResponse = httpclient.execute(httpGet, httpContext);
        return EntityUtils.toString(httpResponse.getEntity());
    }

    public void fileUpload() throws Exception {

        uploading();
        httpPost = new NUHttpPost(postURL);

        MultipartEntity mpEntity = new MultipartEntity();

        mpEntity.addPart("files", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().info("Now uploading your file into fileserve...........................");
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();

        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
        if (resEntity != null) {

            stringResponse = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "upload response : {0}", stringResponse);
            gettingLink();
            String shortencode = StringUtils.stringBetweenTwoStrings(stringResponse, "\"shortenCode\":\"", "\"");
            String fileName = StringUtils.stringBetweenTwoStrings(stringResponse, "\"fileName\":\"", "\"");
            String deleteCode = StringUtils.stringBetweenTwoStrings(stringResponse, "\"deleteCode\":\"", "\"");
            downloadlink = "http://www.fileserve.com/file/" + shortencode + "/" + fileName;
            deletelink = "http://www.fileserve.com/file/" + shortencode + "/delete/" + deleteCode;
            NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downloadlink);
            NULogger.getLogger().log(Level.INFO, "Delete Link : {0}", deletelink);
            downURL = downloadlink;
            delURL = deletelink;

        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try again later :(");
        }
    }
    
    /**
    * Add all the not allowed extensions.
    */
    private void addExtensions(){
        disallowedExtensions.add("html");
        disallowedExtensions.add("js");
        disallowedExtensions.add("css");
        disallowedExtensions.add("jsp");
        disallowedExtensions.add("asp");
        disallowedExtensions.add("aspx");
        disallowedExtensions.add("php");
    }
}
