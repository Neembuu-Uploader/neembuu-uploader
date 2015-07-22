/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import neembuu.uploader.accounts.DepositFilesAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.exceptions.uploaders.NUMinFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

/**
 * If there is a problem, take a look to <a href="http://dfiles.eu/filemanager.html">Depositfiles Filemanager</a>.
 * @author dinesh
 * @author davidepastore
 * @author Paralytic
 */
@SmallModule(
    exports={DepositFiles.class,DepositFilesAccount.class},
    interfaces={Uploader.class,Account.class},
    name="DepositFiles.com"
)
public class DepositFiles extends AbstractUploader {

    DepositFilesAccount depositFilesAccount = (DepositFilesAccount) getAccountsProvider().getAccount("DepositFiles.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private NUHttpPost httpPost;
    
    private Document doc;
    private String postURL = "";
    private String uploadresponse = "";
    private String downloadlink = "";
    private String deletelink = "";
    private String MAX_FILE_SIZE = "";
    private String UPLOAD_IDENTIFIER = "";
    private String go = "";
    private final long logMaxFileSizeLimit = 10737418240L; // 10 GB
    private final long notLogMaxFileSizeLimit = 2147483648l; //2 GB
    private final long minFileSizeLimit = 1l; //1B

    public DepositFiles() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "DepositFiles.com";
        if (depositFilesAccount.loginsuccessful) {
            host = depositFilesAccount.username + " | DepositFiles.com";
        }
    }

    @Override
    public void run() {
        try {
            if (depositFilesAccount.loginsuccessful) {
                httpContext = depositFilesAccount.getHttpContext();
                host = depositFilesAccount.username + " | DepositFiles.com";
            } else {
                host = "DepositFiles.com";
                CookieStore cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            }
            
            if (!depositFilesAccount.loginsuccessful) {
                if (file.length() > notLogMaxFileSizeLimit) {
                    throw new NUMaxFileSizeException(notLogMaxFileSizeLimit, file.getName(), depositFilesAccount.getHOSTNAME());
                }
            } else {
                if (file.length() > logMaxFileSizeLimit) {
                    throw new NUMaxFileSizeException(logMaxFileSizeLimit, file.getName(), depositFilesAccount.getHOSTNAME());
                }
            }

            if (file.length() == 0) {
                throw new NUMinFileSizeException(minFileSizeLimit, file.getName(), depositFilesAccount.getHOSTNAME());
            }

            uploadInitialising();
            NULogger.getLogger().info("Now getting deposifiles page post action value........");
            if(depositFilesAccount.loginsuccessful){
                uploadresponse = NUHttpClientUtils.getData("http://depositfiles.com/en/", httpContext);
                doc = Jsoup.parse(uploadresponse);
                postURL = doc.select("form[id=upload_form]").first().attr("action");
                MAX_FILE_SIZE = doc.select("form[id=upload_form]").first().select("input[name=MAX_FILE_SIZE]").attr("value");
                UPLOAD_IDENTIFIER = doc.select("form[id=upload_form]").first().select("input[name=UPLOAD_IDENTIFIER]").attr("value");
                go = doc.select("form[id=upload_form]").first().select("input[name=go]").attr("value");
            }
            else{
                postURL = NUHttpClientUtils.getData("http://depositfiles.com/api/get_upload_info.php", httpContext);
                postURL = Jsoup.parse(postURL, "", Parser.xmlParser()).select("http_upload_path").text();
            }
            
            NULogger.getLogger().log(Level.INFO, "Post URL  : {0}", postURL);
            fileUpload();

            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            
            uploadInvalid();
        } 
        catch (Exception ex) {
            NULogger.getLogger().severe(ex.toString());

            uploadFailed();
        } finally {
            postURL = null;
            uploadresponse = null;
            downloadlink = null;
            deletelink = null;
        }
    }

    public void fileUpload() throws Exception {
        uploading();
        
        httpPost = new NUHttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = createMonitoredFileBody();
        
        mpEntity.addPart("MAX_FILE_SIZE", new StringBody(MAX_FILE_SIZE));
        mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(UPLOAD_IDENTIFIER));
        mpEntity.addPart("go", new StringBody(go));
        mpEntity.addPart("files", cbFile);
        mpEntity.addPart("agree", new StringBody("1"));
        mpEntity.addPart("submit", new StringBody("Upload Now"));
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().info("Now uploading your file into depositfiles...........................");
        HttpResponse response = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = response.getEntity();

        NULogger.getLogger().info(response.getStatusLine().toString());
        if (resEntity != null) {
            gettingLink();
            uploadresponse = EntityUtils.toString(resEntity);
            downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "ud_download_url = '", "'");
            deletelink = StringUtils.stringBetweenTwoStrings(uploadresponse, "ud_delete_url = '", "'");
            NULogger.getLogger().log(Level.INFO, "download link : {0}", downloadlink);
            NULogger.getLogger().log(Level.INFO, "delete link : {0}", deletelink);
            downURL = downloadlink;
            delURL = deletelink;
        } else {
            throw new Exception("Error in depositfiles!");
        }
    }
}
