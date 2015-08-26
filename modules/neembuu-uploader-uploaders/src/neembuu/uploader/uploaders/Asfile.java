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
import neembuu.uploader.accounts.AsfileAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
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
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={Asfile.class,AsfileAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Asfile.com",
    ignore = true
)
public class Asfile extends AbstractUploader implements UploaderAccountNecessary {
    
    AsfileAccount asfileAccount = (AsfileAccount) getAccountsProvider().getAccount("Asfile.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String sessionID = "";
    private String token = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public Asfile() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Asfile.com";
        if (asfileAccount.loginsuccessful) {
            host = asfileAccount.username + " | Asfile.com";
        }
        maxFileSizeLimit = 5368709120l; //5 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://asfile.com/en/", httpContext);
        
        doc = Jsoup.parse(responseString);
        final Element form = doc.getElementById("upload");
        uploadURL = form.attr("action");
        token = form.select("input[name=token]").first().val();
        
        uploadURL += "?X-Progress-ID=" + CommonUploaderTasks.createRandomString(32);
    }

    @Override
    public void run() {
        try {
            if (asfileAccount.loginsuccessful) {
                httpContext = asfileAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                maxFileSizeLimit = 5368709120l; //5 GB
            } else {
                host = "Asfile.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("MAX_FILE_SIZE", new StringBody("5497558138880"));
            mpEntity.addPart("token", new StringBody(token));
            mpEntity.addPart("agree", new StringBody("on"));
            mpEntity.addPart("tos", new StringBody("1"));
            mpEntity.addPart("file[]", createMonitoredFileBody());
            mpEntity.addPart("description[]", new StringBody(""));
            mpEntity.addPart("password[]", new StringBody(""));
            
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into Asfile.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            responseString = StringUtils.stringBetweenTwoStrings(responseString, "parent.upload_data = ", "parent.upload_done()");
            
            responseString = responseString.trim();
            responseString = StringUtils.removeLastChars(responseString, 2);
            responseString = StringUtils.removeFirstChar(responseString);
            
            final JSONObject jSonObject = new JSONObject(responseString);
            
            final String path = jSonObject.getString("path");
            
            downloadlink = "http://asfile.com/file/" + path;
            deletelink = "http://asfile.com/file/delete/" + path + "/" + jSonObject.getString("secret");
            
            //NULogger.getLogger().log(Level.INFO, "response : {0}", responseString);
            
            //FileUtils.saveInFile("Asfile.html", responseString);
            
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
