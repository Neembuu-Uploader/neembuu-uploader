/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.CloudCornerAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;

/**
 *
 * @author Paralytic
 * @author davidepastore
 */
@SmallModule(
    exports={CloudCorner.class,CloudCornerAccount.class},
    interfaces={Uploader.class,Account.class},
    name="CloudCorner.com"
)
public class CloudCorner extends AbstractUploader implements UploaderAccountNecessary{
    
    CloudCornerAccount cloudCornerAccount = (CloudCornerAccount) getAccountsProvider().getAccount("CloudCorner.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private final String userType = "reg";
    
    private String filePrivacy = "";
    private String fileID = "";
    private String dlURL = "";
	
    private String downloadlink = "";
    private String deletelink = "";

    public CloudCorner() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "CloudCorner.com";

        if (cloudCornerAccount.loginsuccessful) {
            host = cloudCornerAccount.username + " | CloudCorner.com";
        }
        maxFileSizeLimit = Long.MAX_VALUE; // Unlimited
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("https://www.cloudcorner.com/cloud", httpContext);
        uploadURL = "http://uploadfr.cloudcorner.com/";
    }

    @Override
    public void run() {
        try {
            if (cloudCornerAccount.loginsuccessful) {
                httpContext = cloudCornerAccount.getHttpContext();
                maxFileSizeLimit = Long.MAX_VALUE; // Unlimited
            } else {
                host = "CloudCorner.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            httpPost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
            httpPost.setHeader("Host", "uploadfr.cloudcorner.com");
            httpPost.setHeader("Origin", "https://www.cloudcorner.com");
            httpPost.setHeader("Referer", "https://www.cloudcorner.com/cloud");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0");
            
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("id_folder", new StringBody("0"));
            mpEntity.addPart("session_id", new StringBody(cloudCornerAccount.session_id));
            mpEntity.addPart("master_key", new StringBody(cloudCornerAccount.master_key));
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            /*
            NULogger.getLogger().log(Level.INFO, "cloudCornerAccount.session_id: {0}", cloudCornerAccount.session_id);
            NULogger.getLogger().log(Level.INFO, "decoded cloudCornerAccount.master_key: {0}", cloudCornerAccount.master_key);
            NULogger.getLogger().log(Level.INFO, "cloudCornerAccount.master_key: {0}", URLDecoder.decode(cloudCornerAccount.master_key, "UTF-8"));
            */
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("** CloudCorner.com ** => Now uploading your file to the server ...");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());

            NULogger.getLogger().log(Level.INFO, "Response string: {0}", responseString);
            responseString = responseString.replaceAll("\\\\", "");
            fileID = StringUtils.stringBetweenTwoStrings(responseString, "\"id\":\"", "\"");
            dlURL = StringUtils.stringBetweenTwoStrings(responseString, "\"url\":\"", "\"");
            
            if (dlURL.isEmpty() || fileID.isEmpty()) {
                NULogger.getLogger().info("** CloudCorner.com ** => Download link (and/or) fileID not received!");
                NULogger.getLogger().info("** CloudCorner.com ** => Upload Failed.");
                uploadFailed();
            } else {
                NULogger.getLogger().info("** CloudCorner.com ** => Received the fileID & download link!.");
            }
            
            filePrivacy = StringUtils.stringBetweenTwoStrings(responseString, "\"privacy\":\"", "\"");
            
            //Read the links
            gettingLink();
            if (filePrivacy.equals("private")) {
                NULogger.getLogger().info("** CloudCorner.com ** => Uploaded file detected as \"private\" by default.");
                NULogger.getLogger().info("** CloudCorner.com ** => Manually setting it as \"public\".");
                httpPost = new NUHttpPost("https://www.cloudcorner.com/api/manager/share/");
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("id_list[]", fileID));
                formparams.add(new BasicNameValuePair("privacy", "public"));

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                responseString = StringUtils.stringBetweenTwoStrings(responseString, "\"success\":", ",\"");

                if (responseString.equals("true")) {
                    NULogger.getLogger().info("** CloudCorner.com ** => Uploaded file successfully set as \"public\".");
                }
            }

            downloadlink = dlURL;
            deletelink = UploadStatus.NA.getLocaleSpecificString();

            NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            delURL = deletelink;

            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        }
    }
}