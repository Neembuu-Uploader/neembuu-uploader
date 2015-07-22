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
import neembuu.uploader.accounts.ShareSixAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import neembuu.uploader.uploaders.common.StringUtils;
import javax.activation.MimetypesFileTypeMap;


/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={ShareSix.class,ShareSixAccount.class},
    interfaces={Uploader.class,Account.class},
    name="ShareSix.com"

)
public class ShareSix extends AbstractUploader{
    
    ShareSixAccount shareSixAccount = (ShareSixAccount) getAccountsProvider().getAccount("ShareSix.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
	
    private String downloadlink = "";
    private String deletelink = "";

    public ShareSix() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "ShareSix.com";

        if (shareSixAccount.loginsuccessful) {
            host = shareSixAccount.username + " | ShareSix.com";
        }
        maxFileSizeLimit = 1073741824L; // 1024 MB (default)        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://sharesix.com/", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("input[id=file-upload]").first().attr("data-url");
    }

    @Override
    public void run() {
        try {
            if (shareSixAccount.loginsuccessful) {
                // registered user
                httpContext = shareSixAccount.getHttpContext();
                maxFileSizeLimit = 1073741824L; // 1024 MB
            } else {
                // anon user
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 1073741824L; // 1024 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            // http://185.56.28.40/upload/?s=x&u=string&gt=alphanumeric
            httpPost = new NUHttpPost(uploadURL);
            httpPost.setHeader("Content-Disposition", "attachment; filename=\"" +file.getName()+ "\"");
            
            // Get the mime type of the file
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            String mimeType = mimeTypesMap.getContentType(file.getName());
            
            httpPost.setHeader("Content-Type", mimeType);
            httpPost.setHeader("DNT", "1");
            httpPost.setHeader("Origin", "http://sharesix.com");
            httpPost.setHeader("Referer", "http://sharesix.com/");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0");
            httpPost.setEntity(createMonitoredFileEntity());
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("** ShareSix.com ** => Uploading your file to the server ...");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            // {"status":1,"row":{"name":"file.ext","url":"http:\/\/sharesix.com\/f\/fileid"}}
            responseString = responseString.replaceAll("\\\\", "");
            
            //Read the links
            gettingLink();
            
            try {
                if (responseString.contains("\"status\":0")) {
                    NULogger.getLogger().info("** ShareSix.com ** => The server refused to accept the file you uploaded.");
                    NULogger.getLogger().info("** ShareSix.com ** => Upload failed.");
                    uploadFailed();
                } else if (StringUtils.stringBetweenTwoStrings(responseString, "\"status\":", ",\"").equals("1")) {
                    downloadlink = StringUtils.stringBetweenTwoStrings(responseString, "\"url\":\"", "\"");
                    deletelink = UploadStatus.NA.getLocaleSpecificString();
                }
            } catch(StringIndexOutOfBoundsException e) {
                NULogger.getLogger().info("** ShareSix.com ** => ERROR: Server response after file upload was:");
                NULogger.getLogger().info(responseString);
                NULogger.getLogger().info("** ShareSix.com ** => Report this plugin as broken @ http://www.neembuu.com/uploader/forum/");
                NULogger.getLogger().info("** ShareSix.com ** => Send this \"nu.log\" file along with your plugin report.");
                uploadFailed();
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