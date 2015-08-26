/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import neembuu.uploader.accounts.FileCloudAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.exceptions.uploaders.NUMinFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author vigneshwaran
 * @author davidepastore
 * @author Paralytic - Plugin re-written completely on 05/jan/2015
 */
@SmallModule(
    exports={FileCloud.class,FileCloudAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FileCloud.io"
)
public class FileCloud extends AbstractUploader implements UploaderAccountNecessary {

    FileCloudAccount fileCloudAccount = (FileCloudAccount) getAccountsProvider().getAccount("FileCloud.io");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String stringResponse;
    private Document doc;

    private String uploadURL;
    private String akey;
    private final long minFileSizeLimit = 1024l;

    public FileCloud() {

        super();
        host = "FileCloud.io";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        //It has to be successful.. as it won't work without login
        if (fileCloudAccount.loginsuccessful) {
            host = fileCloudAccount.username + " | FileCloud.io";
        }
        
        maxFileSizeLimit = 2097152000l; //2000 MB
        setupSsl();
    }

    @Override
    public void run() {

        try {
            if (fileCloudAccount.loginsuccessful) {
                httpContext = fileCloudAccount.getHttpContext();
                host = fileCloudAccount.username + " | FileCloud.io";
            } else {
                host = "FileCloud.io";
                uploadInvalid();
                return;
            }

            //Check file size (max)
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            
            //Check file size (min)
            if(file.length() < minFileSizeLimit){
                throw new NUMinFileSizeException(minFileSizeLimit, file.getName(), host);
            }

            uploadInitialising();
            NULogger.getLogger().info("Getting upload url from FileCloud.....");
            stringResponse = NUHttpClientUtils.getData("http://filecloud.io/upload-classic.html", httpContext);
            
            doc = Jsoup.parse(stringResponse);
            uploadURL = doc.select("form[id=uploadFrm]").attr("action");
            akey = doc.select("form[id=uploadFrm]").select("input[name=akey]").attr("value");

            fileUpload();
            uploadFinished();
        } catch (NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(FileCloud.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }

    }

    private void fileUpload() throws Exception {
        httpPost = new NUHttpPost(uploadURL);

        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("akey", new StringBody(akey));
        mpEntity.addPart("Filedata", createMonitoredFileBody());
        
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into filecloud.io .....");
        uploading();
        
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();
        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
        
        if (resEntity != null) {
            stringResponse = EntityUtils.toString(resEntity);
        }
        
        stringResponse = httpResponse.getLastHeader("Location").getValue();
        stringResponse = StringUtils.stringStartingFromString(stringResponse, "new=");
        
        String downloadURL = "http://filecloud.io/" + stringResponse;
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadURL);
        downURL = downloadURL;

        status = UploadStatus.UPLOADFINISHED;
    }
    
    private void setupSsl() {
        SSLSocketFactory sf = null;
        SSLContext sslContext = null;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            NULogger.getLogger().log(Level.SEVERE, "FileCloud.io -> SSL error", e);
        } catch (KeyManagementException e) {
            NULogger.getLogger().log(Level.SEVERE, "FileCloud.io -> SSL error", e);
        }

        try {
            sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "FileCloud.io -> SSL error", e);
        }

        Scheme scheme = new Scheme("https", 443, sf);
        httpclient.getConnectionManager().getSchemeRegistry().register(scheme);
    }
}
