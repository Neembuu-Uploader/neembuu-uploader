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
import neembuu.uploader.accounts.VShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import neembuu.uploader.uploaders.common.StringUtils;
import java.util.Random;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={VShare.class,VShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="VShare.eu"
)
public class VShare extends AbstractUploader{
    
    VShareAccount vShareAccount = (VShareAccount) getAccountsProvider().getAccount("VShare.eu");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String sess_id = "";
    private String uploadid_s = "";
    private String upload_fn = "";
    private String srv_tmp_url = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public VShare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "VShare.eu";
		
        if (vShareAccount.loginsuccessful) {
            host = vShareAccount.username + " | VShare.eu";
        }
        maxFileSizeLimit = 2122317824L; // 2,024 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://vshare.eu", httpContext);
        doc = Jsoup.parse(responseString);
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "name=\"srv_tmp_url\" value=\"", "\"");
    }

    @Override
    public void run() {
        try {
            if (vShareAccount.loginsuccessful) {
                userType = "reg";
                httpContext = vShareAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                maxFileSizeLimit = 3196059648L; // 3,048 MB          
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2122317824L; // 2,024 MB
            }

            addExtensions();
            //Check extension
            if(!FileUtils.checkFileExtension(allowedVideoExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            } 
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            long uploadID;
            Random random = new Random();
            uploadID = Math.round(random.nextFloat() * Math.pow(10,12));
            uploadid_s = String.valueOf(uploadID);
            
            sess_id = StringUtils.stringBetweenTwoStrings(responseString, "name=\"sess_id\" value=\"", "\"");
            srv_tmp_url = uploadURL;
            uploadURL = StringUtils.removeLastChars(uploadURL, 3) + "cgi-bin/upload.cgi?upload_id=" + uploadid_s + "&js_on=1&utype=" + userType + "&upload_type=file";
			
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("js_on", new StringBody("1"));
            mpEntity.addPart("upload_id", new StringBody(uploadid_s));
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("utype", new StringBody(userType));
            mpEntity.addPart("sess_id", new StringBody(sess_id));
            mpEntity.addPart("srv_tmp_url", new StringBody(srv_tmp_url));
            mpEntity.addPart("file_0_descr", new StringBody(""));
            mpEntity.addPart("file_0_public", new StringBody("1"));
            mpEntity.addPart("file_0", createMonitoredFileBody());
            mpEntity.addPart("submit_btn", new StringBody("Done"));
            mpEntity.addPart("tos", new StringBody("1"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into VShare.eu");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            upload_fn = doc.select("textarea[name=fn]").val();
            
            if (upload_fn != null) {
                httpPost = new NUHttpPost("http://vshare.eu/");
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("fn", upload_fn));
                formparams.add(new BasicNameValuePair("op", "upload_result"));
                formparams.add(new BasicNameValuePair("st", "OK"));

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());

                doc = Jsoup.parse(responseString);
                downloadlink = doc.select("textarea").first().val();
                deletelink = doc.select("textarea").eq(2).val();

                NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                delURL = deletelink;

                uploadFinished();
            }
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
/**
* Add all the allowed extensions.
* 
* var ext_allowed='avi|divx|xvid|mpg|mpeg|vob|mov|flv|mp4|wmv|mkv';
* http://vshare.eu
*/
    private void addExtensions(){
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("divx");
        allowedVideoExtensions.add("xvid");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("vob");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("mkv");
    }
}
