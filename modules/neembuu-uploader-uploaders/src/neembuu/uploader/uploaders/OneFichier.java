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
import neembuu.uploader.accounts.OneFichierAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
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
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author dinesh
 */
@SmallModule(
    exports={OneFichier.class,OneFichierAccount.class},
    interfaces={Uploader.class,Account.class},
    name="1fichier.com"
)
public class OneFichier extends AbstractUploader {

    OneFichierAccount oneFichierAccount = (OneFichierAccount) getAccountsProvider().getAccount("1fichier.com");
    final String UPLOAD_ID_CHARS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private final HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    Document doc;
    private String uploadURL = "";
    
    private String uploadresponse;
    private String downloadlink;
    private String deletelink;
    private final long fileSizeLimit = 10995116277760L; //10 GB

    public OneFichier() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "1fichier.com";
        if (oneFichierAccount.loginsuccessful) {
            //  login = true;
            host = oneFichierAccount.username + " | 1fichier.com";
        }
    }

    /*public void generateOneFichierID() {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 5; i++) {
            int idx = 1 + (int) (Math.random() * 51);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
        NULogger.getLogger().log(Level.INFO, "uid : {0}", uid);
    }*/

    public void fileUpload() throws Exception {
        String getsource = NUHttpClientUtils.getData("https://1fichier.com/", httpContext);
        doc = Jsoup.parse(getsource);
        uploadURL = doc.select("form").first().attr("action");
        
        httpPost = new NUHttpPost(uploadURL);
        if (oneFichierAccount.loginsuccessful) {
            httpPost.setHeader("Cookie", OneFichierAccount.getSidcookie());
        }
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("file[]", createMonitoredFileBody());
        mpEntity.addPart("domain", new StringBody("0"));
        mpEntity.addPart("submit", new StringBody("Send"));
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().info("Now uploading your file into 1fichier...........................");
        NULogger.getLogger().log(Level.INFO, "Now executing.......{0}", httpPost.getRequestLine());
        uploading();
        httpResponse = httpclient.execute(httpPost, httpContext);
        gettingLink();
//        HttpEntity resEntity = response.getEntity();
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
        if (httpResponse.containsHeader("Location")) {
            uploadresponse = httpResponse.getFirstHeader("Location").getValue();
            NULogger.getLogger().log(Level.INFO, "Upload location link : {0}", uploadresponse);
        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try again");
        }
    }

    @Override
    public void run() {

        try {

            if (oneFichierAccount.loginsuccessful) {
                host = oneFichierAccount.username + " | 1fichier.com";
            } else {
                host = "1fichier.com";
            }

            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), oneFichierAccount.getHOSTNAME());
            }
            
            uploadInitialising();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            //generateOneFichierID();
            fileUpload();
            
            uploadURL = StringUtils.stringUntilString(uploadURL, "upload.cgi");
            NULogger.getLogger().info(uploadURL);
            
            NULogger.getLogger().info("Getting file links.............");
            httpGet = new NUHttpGet(uploadURL + uploadresponse);
            if (oneFichierAccount.loginsuccessful) {
                httpGet.setHeader("Cookie", OneFichierAccount.getSidcookie());
            }
            
            httpResponse = httpclient.execute(httpGet, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            //FileUtils.saveInFile("OneFichier.html", stringResponse);
            
            doc = Jsoup.parse(stringResponse);
            downloadlink = "https://1fichier.com/" + StringUtils.stringBetweenTwoStrings(stringResponse, "https://1fichier.com/?", "\"");
            deletelink = "https://1fichier.com/remove/" + StringUtils.stringBetweenTwoStrings(stringResponse, "https://1fichier.com/remove/", "<");
            
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
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
    
    public static void main(String[] args) {
        neembuu.uploader.paralytics_tests.GenericPluginTester.test(
                OneFichier.class,
                OneFichierAccount.class
        );
    }
}
