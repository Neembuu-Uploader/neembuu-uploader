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
import neembuu.uploader.accounts.FileSharkAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={FileShark.class,FileSharkAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FileShark.pl"
)
public class FileShark extends AbstractUploader{
    
    FileSharkAccount fileSharkAccount = (FileSharkAccount) getAccountsProvider().getAccount("FileShark.pl");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String directory = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public FileShark() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "FileShark.pl";
        if (fileSharkAccount.loginsuccessful) {
            host = fileSharkAccount.username + " | FileShark.pl";
        }
        maxFileSizeLimit = 1073741824L; // 1 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://fileshark.pl/wgraj", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[id=fileupload]").attr("action");
        if (userType.equals("reg")){
            directory = doc.select("form[id=fileupload]").select("select[name=directory]").select("option").first().attr("value");
        }
    }

    @Override
    public void run() {
        try {
            if (fileSharkAccount.loginsuccessful) {
                userType = "reg";
                httpContext = fileSharkAccount.getHttpContext();
                maxFileSizeLimit = 5368709120L; // 5 GB
            }
            else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 1073741824L; // 1 GB
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            // http://62.210.139.182:8086/upload/premium/alphanumericstring/number?X-Progress-ID=4cede05ed80826fa0ac2db51a6b5250e
            uploadURL += "?X-Progress-ID=" + CommonUploaderTasks.createRandomString(32);
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (userType.equals("reg")){
                mpEntity.addPart("directory", new StringBody(directory));
            }
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into FileShark.pl");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();

            // {"link":"\/pobierz\/1435366\/a3423\/settings-yaml","name":"settings.yaml"}
            responseString = responseString.replaceAll("\\\\", "");
            
            downloadlink = "http://fileshark.pl" + StringUtils.stringBetweenTwoStrings(responseString, "\"link\":\"", "\"");
            deletelink = UploadStatus.NA.getLocaleSpecificString();

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
