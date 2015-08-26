/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
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
    exports={MultiUploadDotNl.class},
    interfaces={Uploader.class},
    name="MultiUpload.nl",
    ignore = true
)
public class MultiUploadDotNl extends AbstractUploader{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private Document doc;
    private String uploadURL;
    private String uploadIdentifier;
    private ArrayList<String> services = new ArrayList<String>();
    
    private String downloadlink = "";

    public MultiUploadDotNl() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "MultiUpload.nl";
        
        maxFileSizeLimit = Long.MAX_VALUE; //MAX
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://multiupload.nl", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("#uploadfrm").attr("action");
        uploadIdentifier = doc.select("input[name=UPLOAD_IDENTIFIER]").val();
        
        //Get all the services
        Elements elements = doc.select("input[type=checkbox][name^=service]");
        ListIterator<Element> listIterator = elements.listIterator();
        while(listIterator.hasNext()){
            //NULogger.getLogger().log(Level.INFO, "Value of name: {0}", listIterator.next().attr("name"));
            services.add(listIterator.next().attr("name"));
        }
    }

    @Override
    public void run() {
        try {
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(uploadIdentifier));
            mpEntity.addPart("u", new StringBody(""));
            
            //Adding all services
            for(int i = 0; i < services.size(); i++){
                mpEntity.addPart(services.get(i), new StringBody("1"));
            }
            
            mpEntity.addPart("file_0", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into MultiUpload.nl");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            
            //FileUtils.saveInFile("MultiUploadDotNl.html", responseString);
            
            downloadlink = StringUtils.stringBetweenTwoStrings(responseString, ":\"", "\"");
            downloadlink = "http://www.multiupload.nl/" + downloadlink;
            
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
 
