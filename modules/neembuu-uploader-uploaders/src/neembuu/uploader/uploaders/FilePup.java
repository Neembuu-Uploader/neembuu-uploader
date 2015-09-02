/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.FilePupAccount;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Valentin Lafranca
 */
@SmallModule(
        exports = {FilePup.class, FilePupAccount.class},
        interfaces = {Uploader.class, Account.class},
        name = "FilePup.net"
)
public class FilePup extends AbstractUploader {

    FilePupAccount filePupAccount = (FilePupAccount) getAccountsProvider().getAccount("FilePup.net");

    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String redirectUrl;
    private String uploadID = "";
    private String downloadlink = "";
    private String deletelink = "";

    private ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public FilePup() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "FilePup.net";
        if (filePupAccount.loginsuccessful) {
            host = filePupAccount.username + " | FilePup.net";
        }
        maxFileSizeLimit = 3221225472L; // 3 GB
    }

    private void initialize() throws Exception {
        httpPost = new NUHttpPost("http://www.filepup.net/link_upload.php");

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("upload_file[]", "uploading.db"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        responseString = EntityUtils.toString(httpResponse.getEntity());
        uploadID = StringUtils.stringBetweenTwoStrings(responseString, "\"", "\",");
        uploadURL = "http://www.filepup.net/cgi-bin/upload.cgi?upload_id=" + uploadID;
    }

    @Override
    public void run() {
        try {
            if (filePupAccount.loginsuccessful) {
                httpContext = filePupAccount.getHttpContext();
                
                if(filePupAccount.isPremium())
                    maxFileSizeLimit = 5368709120L; // 5 GB
                
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity uploadMpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            uploadMpEntity.addPart("upfile_" + System.currentTimeMillis(), createMonitoredFileBody());
            httpPost.setEntity(uploadMpEntity);

            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into FilePup.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            gettingLink();
            responseString = EntityUtils.toString(httpResponse.getEntity());
            redirectUrl = StringUtils.stringBetweenTwoStrings(responseString, "'", "', 1");

            responseString = NUHttpClientUtils.getData(redirectUrl, httpContext);
            doc = Jsoup.parse(responseString);

            downloadlink = doc.select("table.basic").select("input").first().attr("value");
            deletelink = doc.select("table.basic").select("input").last().attr("value");

            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
            downURL = downloadlink;
            delURL = deletelink;

            uploadFinished();
        } catch (NUException ex) {
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        }
    }

    public static void main(String[] args) {
        neembuu.uploader.paralytics_tests.GenericPluginTester.test(
                FilePup.class,
                neembuu.uploader.accounts.FilePupAccount.class
        );
    }
}
