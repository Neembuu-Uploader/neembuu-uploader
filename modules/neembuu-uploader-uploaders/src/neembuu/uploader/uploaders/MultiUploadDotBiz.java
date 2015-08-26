/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.MultiUploadDotBizAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={MultiUploadDotBiz.class,MultiUploadDotBizAccount.class},
    interfaces={Uploader.class,Account.class},
    name="MultiUpload.biz"
)
public class MultiUploadDotBiz extends AbstractUploader{
    
    MultiUploadDotBizAccount multiUploadDotBizAccount = (MultiUploadDotBizAccount) getAccountsProvider().getAccount("MultiUpload.biz");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private Document doc;
    private String uploadURL;
    private String userType;
    private String uploadId = "";
    private ArrayList<String> services = new ArrayList<String>();
    
    private String downloadlink = "";

    public MultiUploadDotBiz() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "MultiUpload.biz";
        if (multiUploadDotBizAccount.loginsuccessful) {
            host = multiUploadDotBizAccount.username + " | MultiUpload.biz";
        }
        maxFileSizeLimit = Long.MAX_VALUE; // Where can I find it?
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://multiupload.biz/", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form#F1").attr("action");
        uploadId = doc.select("input[name=upload_id]").val();
        
        //Get all the services
        Elements elements = doc.select(".srvtbl input[checked]");
        ListIterator<Element> listIterator = elements.listIterator();
        while(listIterator.hasNext()){
            //NULogger.getLogger().log(Level.INFO, "Value of name: {0}", listIterator.next().attr("name"));
            services.add(listIterator.next().val());
        }
    }

    @Override
    public void run() {
        try {
            if (multiUploadDotBizAccount.loginsuccessful) {
                userType = "reg";
                httpContext = multiUploadDotBizAccount.getHttpContext();
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
//            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("remote", new StringBody("0"));
            mpEntity.addPart("upload_id", new StringBody(uploadId));
            mpEntity.addPart("files", new StringBody("1"));
            mpEntity.addPart("file1", createMonitoredFileBody());
            mpEntity.addPart("description1", new StringBody(""));
            mpEntity.addPart("links", new StringBody(""));
            
            //Adding all services
            for(int i = 0; i < services.size(); i++){
                mpEntity.addPart("site", new StringBody(services.get(i)));
            }
            
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into MultiUpload.biz");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            
            //Read the links
            gettingLink();
            Header lastHeader = httpResponse.getLastHeader("Location");
            
            if (lastHeader != null) {
                downloadlink = lastHeader.getValue();
            }
            
            EntityUtils.consume(httpResponse.getEntity());
            //FileUtils.saveInFile("MultiUploadDotBiz.html", responseString);
            
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
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
    
}
 
