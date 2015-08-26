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
import neembuu.uploader.accounts.CloudZerAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
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
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={CloudZer.class,CloudZerAccount.class},
    interfaces={Uploader.class,Account.class},
    name="CloudZer.net",
    ignore = true
)
public class CloudZer extends AbstractUploader implements UploaderAccountNecessary {
    
    CloudZerAccount cloudZerAccount = (CloudZerAccount) getAccountsProvider().getAccount("CloudZer.net");
    //Necessary variables
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String stringResponse;
    private String uploadUrl;
    
    private String uploadServer;
    private String editKey;
    private String password;
    
    private final String UPLOAD_URL_FORMAT = "%s" + "upload?admincode=%s" + "&id=%s" + "&pw=%s";
    private final String DOWNLOAD_URL_FORMAT = "http://clz.to/%s";
    
    
    public CloudZer() {
        host = "CloudZer.net";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        maxFileSizeLimit = 1073741824l; //1 GB

        if (cloudZerAccount.loginsuccessful) {
            host = cloudZerAccount.username + " | CloudZer.net";
        }

    }

    @Override
    public void run() {
        try {
            if (cloudZerAccount.loginsuccessful) {
                host = cloudZerAccount.username + " | CloudZer.net";
            } else {
                host = "CloudZer.net";
                uploadInvalid();
                return;
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), getHost());
            }
            
            uploadInitialising();
            init();
            
            uploading();
            httpPost = new NUHttpPost(uploadUrl);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            mpEntity.addPart("Upload", new StringBody("Submit Query"));
            httpPost.setEntity(mpEntity);
            
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            HttpEntity resEntity = httpResponse.getEntity();
            stringResponse = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "stringResponse : {0}", stringResponse);
            
            gettingLink();
            downURL = String.format(DOWNLOAD_URL_FORMAT, StringUtils.stringUntilString(stringResponse, ","));
            delURL = editKey;
            NULogger.getLogger().log(Level.INFO, "Download URL : {0}", downURL);
            NULogger.getLogger().log(Level.INFO, "Delete URL : {0}", delURL);
            uploadFinished();
            
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {
            Logger.getLogger(CloudZer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Generates an edit key (reproduction of <a href="http://cloudzer.net/js/script.js">function generate(len)</a>).
     * @param len the length of the string to generate.
     * @return an edit key.
     */
    private String generate(int len){
        String pwd = "";
	char[] con = new char[]{'b','c','d','f','g','h','j','k','l','m','n','p','r','s','t','v','w','x','y','z'};
	char[] voc = new char[]{'a','e','i','o','u'};
	
	for(int i = 0; i < len/2; i++){
            int c = (int) (Math.round(Math.random() * 1000) % 20);
            int v = (int) (Math.round(Math.random() * 1000) % 5);
            pwd +=  String.valueOf(con[c]) + String.valueOf(voc[v]);
	}
	
	return pwd;
    }
    
    /**
     * Reads the server address.
     */
    private void readServer() throws Exception{
        String scriptContent = NUHttpClientUtils.getData("http://cloudzer.net/js/script.js", httpContext);
        uploadServer = StringUtils.stringBetweenTwoStrings(scriptContent, "uploadServer = '", "'");
    }
    
    /**
     * Reads the password.
     * @throws Exception 
     */
    private void readPassword() throws Exception{
        String pageContent = NUHttpClientUtils.getData("http://cloudzer.net/start", httpContext);
        Document doc = Jsoup.parse(pageContent);
        password = doc.select("input#user_pw").first().val();
    }

    /**
     * Initializes the object.
     */
    private void init() throws Exception {
        httpContext = cloudZerAccount.getHttpContext();
        editKey = generate(6);
        readServer();
        readPassword();
        uploadUrl = String.format(UPLOAD_URL_FORMAT, uploadServer, editKey, cloudZerAccount.getUsername(), password);
    }
    
}
