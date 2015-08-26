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
import neembuu.uploader.accounts.UploadHeroAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Paralytic & mnidhal
 */
@SmallModule(
    exports={UploadHero.class,UploadHeroAccount.class},
    interfaces={Uploader.class,Account.class},
    name="UploadHero.co",
    ignore = true
)
public class UploadHero extends AbstractUploader{
    
    UploadHeroAccount uploadHeroAccount = (UploadHeroAccount) getAccountsProvider().getAccount("UploadHero.co");
    
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
    
    private Pattern p;
    private Matcher m;
    private String delete_uh = "";
    private String reg_upload_id = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public UploadHero() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "UploadHero.co";
        if (uploadHeroAccount.loginsuccessful) {
            host = uploadHeroAccount.username + " | UploadHero.co";
        }
        maxFileSizeLimit = 2147483648l; // 2 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://uploadhero.co/", httpContext);
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "upload_url: \"", "\"");
        sessionID = StringUtils.stringBetweenTwoStrings(responseString, "PHPSESSID\" : \"", "\"");
        reg_upload_id = StringUtils.stringBetweenTwoStrings(responseString, "\"ID\" : \"", "\"");
    }

    @Override
    public void run() {
        try {
            if (uploadHeroAccount.loginsuccessful) {
                userType = "reg";
                httpContext = uploadHeroAccount.getHttpContext();
                //sessionID = CookieUtils.getCookieValue(httpContext, "PHPSESSID");
                maxFileSizeLimit = 2147483648l; // 2 GB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2147483648l; // 2 GB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            httpPost = new NUHttpPost(uploadURL);
            
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("PHPSESSID", new StringBody(sessionID));
            if (userType.equals("reg") && !(reg_upload_id.isEmpty())) {
                mpEntity.addPart("ID", new StringBody(reg_upload_id));
            }
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            mpEntity.addPart("Upload", new StringBody("Submit Query"));
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into UploadHero.co");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            httpPost = new NUHttpPost("http://uploadhero.co/fileinfo.php?folder=&name="+file.getName()+"&size="+file.length());
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            p = Pattern.compile("http:\\/\\/uploadhero\\.co\\/delete\\/(.*)(?=\")");
            m = p.matcher(responseString);

            while (m.find()){
                delete_uh = m.group();
            }
            
            // if delete url is incomplete, fail the upload
            boolean tester;
            tester = delete_uh.matches("http:\\/\\/uploadhero\\.co\\/delete\\/$"); // matches "http://uploadhero.co/delete/" EXACTLY
            
            if (tester){
                uploadFailed();
            }
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("a.olaylinkdl").first().text();
            if (delete_uh.isEmpty()) {
                deletelink = "NA";
            }
            else {
                deletelink = delete_uh;
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
