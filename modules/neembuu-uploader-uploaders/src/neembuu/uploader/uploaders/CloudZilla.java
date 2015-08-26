/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.CloudZillaAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={CloudZilla.class,CloudZillaAccount.class},
    interfaces={Uploader.class,Account.class},
    name="CloudZilla.to",
    ignore = true // redirects to http://neodrive.co/
)
public class CloudZilla extends AbstractUploader implements UploaderAccountNecessary{
    
    CloudZillaAccount cloudZillaAccount = (CloudZillaAccount) getAccountsProvider().getAccount("CloudZilla.to");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    
    private int i = 0;
    private String uploadURL;
    private String user_hash = "";
    private String folder = "";
    private String uid = "";
    private String temp_url = "";
    private String filename_host = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public CloudZilla() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "CloudZilla.to";
        if (cloudZillaAccount.loginsuccessful) {
            host = cloudZillaAccount.username + " | CloudZilla.to";
        }
        maxFileSizeLimit = 2147483648L; // 2 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.cloudzilla.to/ajax_calls.php?call=fileupload&id=0", httpContext);
        
        // During development it was discovered that this host is somewhat unstable
        // sometimes their server(s) fail to respond
        // Deploying counter-measures ...
        i=0;
        while (responseString.isEmpty() && i<5){
            NULogger.getLogger().log(Level.INFO, "{0} server(s) not responding ... retry attempt (1/5)", host);
            responseString = NUHttpClientUtils.getData("http://www.cloudzilla.to/ajax_calls.php?call=fileupload&id=0", httpContext);
            i++;
        }
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[id=fileupload]").attr("action");
        folder = doc.select("form[id=fileupload]").select("input[name=folder]").attr("value");
        uid = doc.select("form[id=fileupload]").select("input[name=uid]").attr("value");
        user_hash = doc.select("form[id=fileupload]").select("input[name=user_hash]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (cloudZillaAccount.loginsuccessful) {
                httpContext = cloudZillaAccount.getHttpContext();
                maxFileSizeLimit = 2147483648L; // 2 GB
            } else {
                host = "CloudZilla.to";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            // http://u1.cloudzilla.to/server_process_upload.php?l=en
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("folder", new StringBody(folder));
            mpEntity.addPart("uid", new StringBody(uid));
            mpEntity.addPart("user_hash", new StringBody(user_hash));
            mpEntity.addPart("files", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into CloudZilla.to");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            // {"files":[{"url":"","thumbnail_url":"","delete_url":"","delete_type":"DELETE","name":"settings.yaml","size":296}]}
            
            temp_url = responseString;
            temp_url = StringUtils.stringBetweenTwoStrings(temp_url, "\"url\":\"", "\"");
            filename_host = responseString;
            filename_host = StringUtils.stringBetweenTwoStrings(filename_host, "\"name\":\"", "\""); 
            
            //Read the links
            gettingLink();
            
            if (filename_host.isEmpty()) {
                NULogger.getLogger().info("** CloudZilla.to ** => Unexpected response from server.");
                NULogger.getLogger().info("** CloudZilla.to ** => Either the plugin is broken or the file-host's server(s) are offline.");
                uploadFailed();
            }
            
            if (temp_url.isEmpty()) {
                NULogger.getLogger().info("** CloudZilla.to ** => No download link sent by server.");
                NULogger.getLogger().info("** CloudZilla.to ** => Retrieving manually ...");

                uploadURL = "http://www.cloudzilla.to/storage/";
                httpPost = new NUHttpPost(uploadURL);
                mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                mpEntity.addPart("folder", new StringBody(folder));
                mpEntity.addPart("orderby", new StringBody("added"));
                httpPost.setEntity(mpEntity);
                
                NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                
                doc = Jsoup.parse(responseString);
                // build "select" target
                temp_url = "div[class=item_title]";
                
                downloadlink = "http://www.cloudzilla.to" +doc.select(temp_url).first().select("a").attr("href");
                deletelink = UploadStatus.NA.getLocaleSpecificString();
            } else {
                downloadlink = temp_url;
                deletelink = UploadStatus.NA.getLocaleSpecificString();
            }

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
