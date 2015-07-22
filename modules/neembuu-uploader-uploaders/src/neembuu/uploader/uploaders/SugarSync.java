/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.SugarSyncAccount;
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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
@SmallModule(
    exports={SugarSync.class,SugarSyncAccount.class},
    interfaces={Uploader.class,Account.class},
    name="SugarSync.com"
)
public class SugarSync extends AbstractUploader implements UploaderAccountNecessary {

    SugarSyncAccount sugarSyncAccount = (SugarSyncAccount) getAccountsProvider().getAccount("SugarSync.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    private String USER_INFO_API_URL = "https://api.sugarsync.com/user";
    private String CREATE_FILE_REQUEST_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
            + "<file>"
            + "<displayName>%s</displayName>"
            + "<mediaType>%s</mediaType>"
            + "</file>";
    private String upload_folder_url;
    private String SugarSync_File_Upload_URL;
    private long fileSizeLimit = Long.MAX_VALUE; //No limit

    public SugarSync() {
        host = "SugarSync.com";
        downURL = UploadStatus.NA.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        //It has to be successful.. as it won't work without login
        if (sugarSyncAccount.loginsuccessful) {
            host = sugarSyncAccount.username + " | SugarSync.com";
        }
    }

    private void getUserInfo() throws Exception {
        httpGet = new NUHttpGet(USER_INFO_API_URL);
        httpGet.setHeader("Authorization", sugarSyncAccount.getAuth_token());
        httpResponse = httpclient.execute(httpGet);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        upload_folder_url = StringUtils.stringBetweenTwoStrings(stringResponse, "<magicBriefcase>", "</magicBriefcase>");
        NULogger.getLogger().log(Level.INFO, "Upload_Folder : {0}", upload_folder_url);

    }

    /*
     * This method is used to write the POST data like username and password.
     * This takes the "content" value as a parameter which needs to be posted.
     * 
     */
    public void writeHttpContent(String content) throws Exception {
        
        httpPost.setEntity(new StringEntity(content, ContentType.TEXT_XML)); //Send a XML file
        httpPost.addHeader("Authorization", sugarSyncAccount.getAuth_token());
        httpResponse = httpclient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        stringResponse = EntityUtils.toString(httpEntity);
        
        if (httpResponse.getStatusLine().getStatusCode() == 401) {
            NULogger.getLogger().info("hey SugarSync login failed :(");
            return;
        }
        
        if(httpResponse.containsHeader("Location")){
            SugarSync_File_Upload_URL = httpResponse.getLastHeader("Location").getValue();
            SugarSync_File_Upload_URL = SugarSync_File_Upload_URL + "/data";
            NULogger.getLogger().log(Level.INFO, "Post URL : {0}", SugarSync_File_Upload_URL);
        }
        else {
            NULogger.getLogger().info("There might be problem interface getting Upload URL from SugarSync. Please try after some time :(");

        }

    }

    public void postData(String content, String posturl) throws Exception {
        httpPost = new NUHttpPost(posturl);
        httpGet = new NUHttpGet(posturl);

        writeHttpContent(content);
    }

    @Override
    public void run() {

        try {
            if (sugarSyncAccount.loginsuccessful) {
                host = sugarSyncAccount.username + " | SugarSync.com";
            } else {
                host = "SugarSync.com";
                uploadInvalid();
                return;
            }


            uploadInitialising();
            getUserInfo();
            String ext = FileUtils.getFileExtension(file);
            String CREATE_FILE_REQUEST = String.format(CREATE_FILE_REQUEST_TEMPLATE, file.getName(), ext + " file");
            NULogger.getLogger().info("now creating file request............");
            postData(CREATE_FILE_REQUEST, upload_folder_url);

            HttpPut httpput = new HttpPut(SugarSync_File_Upload_URL);
            httpput.setHeader("Authorization", sugarSyncAccount.getAuth_token());
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("", createMonitoredFileBody());

            httpput.setEntity(reqEntity);
            uploading();
            NULogger.getLogger().info("Now uploading your file into sugarsync........ Please wait......................");
            NULogger.getLogger().log(Level.INFO, "Now executing.......{0}", httpput.getRequestLine());
            httpResponse = httpclient.execute(httpput);
            HttpEntity entity = httpResponse.getEntity();
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());

            if (httpResponse.getStatusLine().getStatusCode() == 204) {
                NULogger.getLogger().info("File uploaded successfully :)");
                uploadFinished();
            } else {
                throw new Exception("There might be problem with your internet connection or server error. Please try again some after time :(");
            }

        } catch (Exception e) {
            Logger.getLogger(SugarSync.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();

        }
    }
}
