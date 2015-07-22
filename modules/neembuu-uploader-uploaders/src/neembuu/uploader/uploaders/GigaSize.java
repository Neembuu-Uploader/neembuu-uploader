/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.GigaSizeAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
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
    exports={GigaSize.class,GigaSizeAccount.class},
    interfaces={Uploader.class,Account.class},
    name="GigaSize.com"
)
public class GigaSize extends AbstractUploader {
    GigaSizeAccount GigaSizeAccount = (GigaSizeAccount) getAccountsProvider().getAccount("GigaSize.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    private Document doc;

    private String downloadlink;
    private String uploadid;
    private String sid;
    static String referer = "";
    static final String UPLOAD_ID_CHARS = "1234567890abcdef";
    private long fileSizeLimit = 2147483648l; //2 GB: the last letter is alphabet 'L' in small case, not numeric '1'

    public GigaSize() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "GigaSize.com";
        if (GigaSizeAccount.loginsuccessful) {
            host = GigaSizeAccount.username + " | GigaSize.com";
        }
    }

    
    private void initialize() throws Exception {
        if(GigaSizeAccount.loginsuccessful){
            httpContext = GigaSizeAccount.getHttpContext();
        }
        else{
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        }
        
        NULogger.getLogger().info("Getting startup cookies from gigasize.com");
        httpGet = new NUHttpGet("http://www.gigasize.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());

        generateGigasizeID();
    }

    public void generateGigasizeID() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            int idx = 1 + (int) (Math.random() * 15);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        sid = sb.toString();
        NULogger.getLogger().log(Level.INFO, "sid : {0} - {1}", new Object[]{sid, sid.length()});
        uploadid = sid;

    }

    public String getData(String url) throws IOException {
        httpGet = new NUHttpGet(url);
        httpResponse = httpclient.execute(httpGet, httpContext);
        return EntityUtils.toString(httpResponse.getEntity());
    }

    @Override
    public void run() {
        try {

            if (GigaSizeAccount.loginsuccessful) {
                host = GigaSizeAccount.username + " | GigaSize.com";
            } else {
                host = "GigaSize.com";
            }

            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), GigaSizeAccount.getHOSTNAME());
            }

            uploadInitialising();


            initialize();



            fileUpload();
            gettingLink();
            String uploadCompleteID;
            long randID;
            do {
                randID = (long) Math.floor(Math.random() * 90000000000000000L) + 10000000000L;
                uploadCompleteID = getData("http://www.gigasize.com/status.php?sid=" + sid + "&rnd=" + randID);
            } while (!uploadCompleteID.contains("done"));
            
            NULogger.getLogger().log(Level.INFO, "Response: {0}", uploadCompleteID);
            uploadCompleteID = StringUtils.stringBetweenTwoStrings(uploadCompleteID, "\\/uploadcompleteie\\/", "\"");
            uploadCompleteID = uploadCompleteID.replaceAll("\\\\","");
            NULogger.getLogger().log(Level.INFO, "Upload Complete ID : {0}", uploadCompleteID);
            //http://www.gigasize.com/uploadcomplete/MzIwMDIxMzc1NHwwNTNiMDkwNDdkMWNlMTZjNGJhMTJiMTRhNmVjZjM0MQ==
            referer = "http://www.gigasize.com/uploadcompleteie/" + uploadCompleteID;
            NULogger.getLogger().log(Level.INFO, "referer : {0}", referer);
            downloadlink = getData("http://www.gigasize.com" + uploadCompleteID);
            NULogger.getLogger().log(Level.INFO, "address : {0}", "http://www.gigasize.com" + uploadCompleteID);
            
            //FileUtils.saveInFile("GigaSize.html", downloadlink);
            
            //Get download link
            doc = Jsoup.parse(downloadlink);
            downloadlink = doc.select("div#container section#content div#uploadedList div.fileUploaded p.downloadLink a").text();
            NULogger.getLogger().log(Level.INFO, "Download URL : {0}", downloadlink);
            downURL = downloadlink;
            
            uploadFinished();


        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            
            uploadFailed();

        }
    }

    public void fileUpload() throws Exception {
        httpPost = new NUHttpPost("http://www.gigasize.com/uploadie");
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(uploadid));
        mpEntity.addPart("sid", new StringBody(sid));
        mpEntity.addPart("fileUpload1", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into Gigasize...........................");
        HttpResponse response = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = response.getEntity();
        NULogger.getLogger().info(response.getStatusLine().toString());
        if (resEntity != null) {
            sid = "";
            sid = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "After upload sid value : {0}", sid);
        } else {
            throw new Exception("There might be a problem with your internet connection or GigaSize server problem. Please try after some time :(");
        }
    }
}
