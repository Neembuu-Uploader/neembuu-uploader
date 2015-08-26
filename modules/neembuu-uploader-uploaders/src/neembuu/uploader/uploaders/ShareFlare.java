/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.ShareFlareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={ShareFlare.class,ShareFlareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="ShareFlare.net"
)
public class ShareFlare extends AbstractUploader implements UploaderAccountNecessary{
    
    ShareFlareAccount shareFlareAccount = (ShareFlareAccount) getAccountsProvider().getAccount("ShareFlare.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String afterUploadURL;
    private String uploadURL;
    private String sessionID = "";
    private String pin;
    
    private String downloadlink = "";
    private String deletelink = "";

    public ShareFlare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "ShareFlare.net";
        if (shareFlareAccount.loginsuccessful) {
            host = shareFlareAccount.username + " | ShareFlare.net";
        }
        maxFileSizeLimit = 2147483647l; //2 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://shareflare.net", httpContext);
        
        doc = Jsoup.parse(responseString);
        
        final String acuplUploadServer = StringUtils.stringBetweenTwoStrings(responseString, "ACUPL_UPLOAD_SERVER = '", "'");
        
        final String acupl_UID = Long.toString((new Date()).getTime(), 16).toUpperCase() + "_" + randomString(40); 
        
        uploadURL = "http://" + acuplUploadServer + "/marker=" + acupl_UID;
        afterUploadURL = "http://shareflare.net/acupl_proxy.php?srv="+ acuplUploadServer + "&uid=" + acupl_UID;
        
        pin = doc.select("input[name=pin]").first().val();
    }

    @Override
    public void run() {
        try {
            if (shareFlareAccount.loginsuccessful) {
                httpContext = shareFlareAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                maxFileSizeLimit = 2147483647l; //2 GB
            } else {
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            
            mpEntity.addPart("MAX_FILE_SIZE", new StringBody("2147483647"));
            mpEntity.addPart("owner", new StringBody(shareFlareAccount.getUsername()));
            mpEntity.addPart("pin", new StringBody(pin));
            mpEntity.addPart("base", new StringBody("shareflare"));
            mpEntity.addPart("host", new StringBody("shareflare.net"));
            mpEntity.addPart("source", new StringBody("shareflare.net"));
            mpEntity.addPart("file_0", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into ShareFlare.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            responseString = NUHttpClientUtils.getData(afterUploadURL, httpContext);
            
            //FileUtils.saveInFile("ShareFlare.html", responseString);
            
            JSONObject jSonObject = new JSONObject(responseString);
            final String finalUrl = jSonObject.getString("post_result");
            
            
            NULogger.getLogger().log(Level.INFO, "finalUrl : {0}", finalUrl);
            responseString = NUHttpClientUtils.getData(finalUrl);
            
            NULogger.getLogger().log(Level.INFO, "response : {0}", responseString);
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("textarea").first().val();
            deletelink = doc.select("textarea").eq(1).val();
            
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
    
    /**
     * Create a random string.
     * @param length the length of the string
     * @return The random string.
     */
    private String randomString(int length ) {
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        int chars_length = chars.length();
        String str = "";
        for ( int i = 0; i < length; i++ ) {
//            str += chars.substring( (int)( Math.random() * chars_length ), 1 );
            str += chars.charAt( (int)( Math.random() * chars_length ) );
        }
        return str;
    } 
    
}
