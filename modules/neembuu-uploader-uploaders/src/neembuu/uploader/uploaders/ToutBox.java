/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.ToutBoxAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.StringUtils;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
@SmallModule(
        exports = {ToutBox.class, ToutBoxAccount.class},
        interfaces = {Uploader.class, Account.class},
        name = "ToutBox.fr"
)
public class ToutBox extends AbstractUploader {

    ToutBoxAccount toutBoxAccount = (ToutBoxAccount) getAccountsProvider().getAccount("ToutBox.fr");

    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String accNo = "";
    private String baseUrl = "http://toutbox.fr";
    private String downloadlink = "";
    private String deletelink = "";

    private ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public ToutBox() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "ToutBox.fr";
        if (toutBoxAccount.loginsuccessful) {
            host = toutBoxAccount.username + " | ToutBox.fr";
        }
        maxFileSizeLimit = 1047527424L; // 999 MB
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://toutbox.fr/", httpContext);
        doc = Jsoup.parse(responseString);
        accNo = doc.select("input[name=__accno]").attr("value");

        httpPost = new NUHttpPost("http://toutbox.fr/action/Upload/GetUrl/");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("accountId", new StringBody(accNo));
        mpEntity.addPart("folderId", new StringBody("0"));
        mpEntity.addPart("__RequestVerificationToken", new StringBody("undefined"));
        httpPost.setEntity(mpEntity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        responseString = EntityUtils.toString(httpResponse.getEntity());
        JSONObject jSon = new JSONObject(responseString);

        uploadURL = jSon.getString("Url") + "&ms=" + System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            if (toutBoxAccount.loginsuccessful) {
                httpContext = toutBoxAccount.getHttpContext();

            } else {
                host = "ToutBox.fr";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity uploadMpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            uploadMpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(uploadMpEntity);

            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into ToutBox.fr");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            gettingLink();
            responseString = EntityUtils.toString(httpResponse.getEntity());
            JSONObject uploadJSon = new JSONObject(responseString);
            JSONObject results = (JSONObject) uploadJSon.getJSONArray("files").get(0);
            String fileId = (String) results.getString("url");
            downloadlink = baseUrl + fileId;

            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;

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
                ToutBox.class,
                neembuu.uploader.accounts.ToutBoxAccount.class
        );
    }
}
