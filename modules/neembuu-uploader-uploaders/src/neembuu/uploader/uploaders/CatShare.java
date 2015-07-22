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
import neembuu.uploader.accounts.CatShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
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
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={CatShare.class,CatShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="CatShare.net"
)
public class CatShare extends AbstractUploader implements UploaderAccountNecessary{
    
    CatShareAccount catShareAccount = (CatShareAccount) getAccountsProvider().getAccount("CatShare.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String sessionID = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public CatShare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "CatShare.net";
        if (catShareAccount.loginsuccessful) {
            host = catShareAccount.username + " | CatShare.net";
        }
        maxFileSizeLimit = 5368709120L; // 5 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://catshare.net/upload/http", httpContext);
        
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "uploader: '", "',");
        sessionID = StringUtils.stringBetweenTwoStrings(responseString, "'session': '", "'");
    }

    @Override
    public void run() {
        try {
            if (catShareAccount.loginsuccessful) {
                httpContext = catShareAccount.getHttpContext();
                maxFileSizeLimit = 5368709120L; // 5 GB
            }
            else {
                host = "CatShare.net";
                uploadInvalid();
                return;
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
			
            // http://a38.catshare.net/upload
            httpPost = new NUHttpPost(uploadURL);
            
            uploadURL = StringUtils.stringStartingFromString(uploadURL, "http://");
            uploadURL = StringUtils.stringUntilString(uploadURL, "/upload");

            httpPost.setHeader("Host", uploadURL);
            httpPost.setHeader("User-Agent", "Shockwave Flash");
            
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("session", new StringBody(sessionID));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            mpEntity.addPart("Upload", new StringBody("Submit Query"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into CatShare.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();

            uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "\"file_hash\":\"", "\"");
            responseString = StringUtils.stringBetweenTwoStrings(responseString, "\"filename_url\":\"", "\"");
            // {"result":1,"file_hash":"filehash","filename_url":"test.txt"}
            
            downloadlink = "http://catshare.net/" +uploadURL+ "/" +responseString;
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
