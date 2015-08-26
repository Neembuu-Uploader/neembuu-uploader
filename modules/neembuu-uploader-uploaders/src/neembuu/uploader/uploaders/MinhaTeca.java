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
import neembuu.uploader.accounts.MinhaTecaAccount;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={MinhaTeca.class,MinhaTecaAccount.class},
    interfaces={Uploader.class,Account.class},
    name="MinhaTeca.com.br"
)
public class MinhaTeca extends AbstractUploader implements UploaderAccountNecessary{
    
    MinhaTecaAccount minhaTecaAccount = (MinhaTecaAccount) getAccountsProvider().getAccount("MinhaTeca.com.br");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private String uploadURL;
    private String userType;
    private Document doc;
    
    private String vToken = "";
    private String accountid = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public MinhaTeca() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "MinhaTeca.com.br";
        if (minhaTecaAccount.loginsuccessful) {
            host = minhaTecaAccount.username + " | MinhaTeca.com.br";
        }
        maxFileSizeLimit = Long.MAX_VALUE; // Unlimited
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://minhateca.com.br/", httpContext);
        doc = Jsoup.parse(responseString);
        vToken = doc.select("input[name=__RequestVerificationToken]").attr("value");
        
        if (!vToken.isEmpty()){
            NULogger.getLogger().info("** MinhaTeca.com.br ** => Verification token found! Proceeding further.");
        } else {
            NULogger.getLogger().info("** MinhaTeca.com.br ** => Error! Unable to find the verification token!");
        }
        
        accountid = doc.select("a").attr("data-accountid");
        
        if (!accountid.isEmpty()){
            NULogger.getLogger().info("** MinhaTeca.com.br ** => Account ID found! Proceeding further.");
        } else {
            NULogger.getLogger().info("** MinhaTeca.com.br ** => Error! Unable to find the Account ID!");
        }
        
        uploadURL = "http://minhateca.com.br/action/Upload/GetUrl/";
        httpPost = new NUHttpPost(uploadURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        mpEntity.addPart("__RequestVerificationToken", new StringBody(vToken));
        mpEntity.addPart("accountid", new StringBody(accountid));
        mpEntity.addPart("folderid", new StringBody("0"));
        httpPost.setEntity(mpEntity);

        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        NULogger.getLogger().info("** MinhaTeca.com.br ** => Trying to get the Upload URL ...");
        httpResponse = httpclient.execute(httpPost, httpContext);
        responseString = EntityUtils.toString(httpResponse.getEntity());
        
        // {"Url":"http://s8231.minhateca.com.br/UploadHandler.aspx?e=string\u0026pv=2","ChomikId":accountid,"FolderId":0,"AnonymousUpload":false}
        // \u0026 is equal to "&"
        
        try {
            // {"Url":"http://s8231.minhateca.com.br/UploadHandler.aspx?e=string\u0026pv=2
            uploadURL = StringUtils.stringUntilString(responseString, "u0026");
            // {"Url":"http://s8231.minhateca.com.br/UploadHandler.aspx?e=string\
            
            uploadURL = StringUtils.removeLastChar(uploadURL);
            // {"Url":"http://s8231.minhateca.com.br/UploadHandler.aspx?e=string
            
            uploadURL = StringUtils.removeFirstChars(uploadURL, 8);
            // http://s8231.minhateca.com.br/UploadHandler.aspx?e=string
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Upload URL not found! Upload Failed!");
        }
        
        if (!uploadURL.isEmpty()){
            NULogger.getLogger().info("** MinhaTeca.com.br ** => Upload URL found! Proceeding further.");
        }
        
        uploadURL += "&pv=2&ms=" +System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            if (minhaTecaAccount.loginsuccessful) {
                userType = "reg";
                httpContext = minhaTecaAccount.getHttpContext();
                maxFileSizeLimit = Long.MAX_VALUE; // Unlimited
            } else {
                host = "MinhaTeca.com.br";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            // http://s8231.minhateca.com.br/UploadHandler.aspx?e=string&pv=2&ms=1436872028634
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("files[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("** MinhaTeca.com.br ** => Uploading your file to the server ...");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            // {"files":[{"name":"file.avi","size":651526,
            // "id":1626184959,"fileId":662162195,
            // "url":"/username/file.avi(video)","folderName":""}]}
            
            //Read the links
            gettingLink();

            try {
                responseString = "http://minhateca.com.br" +StringUtils.stringBetweenTwoStrings(responseString, "\"url\":\"", "\"");
            } catch (NullPointerException e) {
                NULogger.getLogger().info("** MinhaTeca.com.br ** => Download link not found! Upload Failed!");
                e.printStackTrace();
            }
            
            NULogger.getLogger().log(Level.INFO, "** MinhaTeca.com.br ** => Download link received as: {0}", responseString);
            downloadlink = responseString;
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