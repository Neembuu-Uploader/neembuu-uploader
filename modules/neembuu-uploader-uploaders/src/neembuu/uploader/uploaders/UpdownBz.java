package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import neembuu.uploader.accounts.UpdownBzAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author elmoyak
 */
@SmallModule(
    exports={UpdownBz.class,UpdownBzAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Updown.bz"
)
public class UpdownBz extends AbstractUploader implements UploaderAccountNecessary {

    UpdownBzAccount updownBzToAccount = (UpdownBzAccount) getAccountsProvider().getAccount("Updown.bz");

    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();

    private String ulHost;
    private String ulEndpoint;
    private long ulMaxSize;
    private long ulTrafficLeft;

    public UpdownBz() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Updown.bz";
        if (updownBzToAccount.loginsuccessful) {
            host = updownBzToAccount.username + " | Updown.bz";
        }
        maxFileSizeLimit = 1 * 1024 * 1024;
    }

    private void premiumFileUpload() throws Exception {
        NULogger.getLogger().info("Updown.bz -> Start premium file upload");
        NUHttpPost httpPost = new NUHttpPost(ulEndpoint);
        httpPost.setEntity(createMonitoredFileEntity());
        httpPost.setHeader("Content-Type", URLConnection.guessContentTypeFromName(file.getName()));
        httpPost.setHeader("User-Agent", updownBzToAccount.getUserAgent());

        uploading();
        HttpResponse httpResponse = httpclient.execute(httpPost, httpContext);

        gettingLink();
        HttpEntity responseEntity = httpResponse.getEntity();
        NULogger.getLogger().info(httpResponse.getStatusLine().toString());

        String uploadResponse = "";
        if (responseEntity != null) {
            uploadResponse = EntityUtils.toString(responseEntity);
        }
        NULogger.getLogger().log(Level.INFO, "Updown.bz -> Upload response: {0}", uploadResponse);

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            uploadFailed();
            throw new Exception("Updown.bz -> Uploading the file " + file.getName() + " failed");
        }

        Matcher m0 = Pattern.compile("\"id\":\"([a-zA-Z0-9]*)\"").matcher(uploadResponse);
        String fileId = m0.find() ? m0.group(1) : "";
        Matcher m1 = Pattern.compile("\"share_id\":\"([a-zA-Z0-9]*)\"").matcher(uploadResponse);
        String shareId = m1.find() ? m1.group(1) : "";

        if (shareId.length() <= 0 && fileId.length() > 0) {
            shareId = apiShareFile(fileId);
        }

        downURL = "https://updown.bz/" + shareId;
        delURL = "https://updown.bz/#!/fm";

        uploadFinished();
    }

    @Override
    public void run() {
        try {
            NULogger.getLogger().info("Updown.bz -> Checking upload conditions");
            if (updownBzToAccount.loginsuccessful) {
                host = updownBzToAccount.username + " | Updown.bz";
                httpContext = updownBzToAccount.getHttpContext();
            } else {
                host = "Updown.bz";
                uploadInvalid();
                return;
            }

            uploadInitialising();

            String query = "{\"i\":\"" + CommonUploaderTasks.createRandomString(10) + "\",\"m\":\"prv\",\"a\":\"ul\",\"s\":\"" + updownBzToAccount.getSessionId() + "\"}";
            NULogger.getLogger().log(Level.INFO, "Updown.bz -> Requesting public upload - Query: {0}", query);
            
            NUHttpPost httpPost = new NUHttpPost("https://api.updown.bz");
            httpPost.setHeader("User-Agent", updownBzToAccount.getUserAgent());
            httpPost.setEntity(new StringEntity(query, ContentType.APPLICATION_JSON));
            String responseString = EntityUtils.toString(httpclient.execute(httpPost, httpContext).getEntity());

            NULogger.getLogger().log(Level.INFO, "Updown.bz -> Parsing requested parameters - Response: {0}", responseString);
            JSONObject response = new JSONObject(responseString);
            long code = response.getLong("c");
            if (code != 1) {
                throw new Exception("Updown.bz -> Request private upload failed - Reponse code: " + code);
            }
            ulHost = response.getJSONObject("d").getString("h");
            ulEndpoint = "https://" + ulHost + "/u/?name=" + URLEncoder.encode(file.getName(), "UTF-8") + "&size=" + file.length() + "&range=0-" + (file.length() - 1) + "&dir=root&id=" + CommonUploaderTasks.createRandomString(10) + "&sid=" + updownBzToAccount.getSessionId();
            ulMaxSize = response.getJSONObject("d").getLong("m");
            maxFileSizeLimit = ulMaxSize;
            ulTrafficLeft = response.getJSONObject("d").getLong("l");

            if (ulHost.length() <= 0) {
                throw new Exception("Updown.bz -> Invalid upload host");
            }
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(ulMaxSize, file.getName(), host);
            }
            if (ulTrafficLeft < file.length()) {
                throw new Exception("Updown.bz -> Not enough traffic left");
            }

            premiumFileUpload();
        } catch (NUException e) {
            e.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(UpdownBz.class.getName()).log(Level.SEVERE, "Updown.bz -> Unknown upload error", e);
            uploadFailed();
        }
    }

    private String apiShareFile(String fileId) {
        String shareId = "";
        try {
            String query = "{\"i\":\"" + CommonUploaderTasks.createRandomString(10) + "\",\"m\":\"fm\",\"a\":\"share\",\"s\":\"" + updownBzToAccount.getSessionId() + "\",\"d\":{\"fis\":[\"" + fileId + "\"]}}";
            NULogger.getLogger().log(Level.INFO, "Updown.bz -> Share uploaded file - Query: {0}", query);
            NUHttpPost httpPost = new NUHttpPost("https://api.updown.bz");
            httpPost.setHeader("User-Agent", updownBzToAccount.getUserAgent());
            httpPost.setEntity(new StringEntity(query, ContentType.APPLICATION_JSON));
            String responseString = EntityUtils.toString(httpclient.execute(httpPost, httpContext).getEntity());
            Matcher m = Pattern.compile("\"si\":\"([a-zA-z0-9]+)\"").matcher(responseString);
            shareId = m.find() ? m.group(1) : "";
        } catch (MalformedURLException ex) {
        } catch (IOException ex) {
        }
        return shareId;
    }
}
