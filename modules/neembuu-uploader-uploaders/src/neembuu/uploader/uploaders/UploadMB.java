/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
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

/**
 *
 * @author vigneshwaran
 */
@SmallModule(
    exports={UploadMB.class},
    interfaces={Uploader.class},
    name="UploadMB.com"
)
public class UploadMB extends AbstractUploader {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    
    String start = "<input type=\"text\" name='dwlink' size=\"80\"  value=\"";
    private long fileSizeLimit = 104857600l; //100 MB
    
    public UploadMB() {
        host = "UploadMB.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
    }

    @Override
    public void run() {
        try {
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), getHost());
            }
            uploadInitialising();
            
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            httpGet = new NUHttpGet("http://www.filecargo.com");
            //httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2 GTBDFff GTB7.0");
            httpResponse = httpclient.execute(httpGet, httpContext);
            EntityUtils.consume(httpResponse.getEntity());
            //------------------------------------------------------------
            httpPost = new NUHttpPost("http://www.filecargo.com/index.php");
            //httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2 GTBDFff GTB7.0");
            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            requestEntity.addPart("MAX_FILE_SIZE", new StringBody(Long.toString(fileSizeLimit)));

            requestEntity.addPart("gfile", createMonitoredFileBody());


            httpPost.setEntity(requestEntity);
            //-------------------------------------------------------------
            uploading();

            //-------------------------------------------------------------
            httpResponse = httpclient.execute(httpPost, httpContext);
            String strResponse = EntityUtils.toString(httpResponse.getEntity());
            //-------------------------------------------------------------
            gettingLink();
            downURL = strResponse.substring(strResponse.indexOf(start) + start.length());
            downURL = downURL.substring(0, downURL.indexOf("&"));
            //-------------------------------------------------------------

            


            NULogger.getLogger().info(downURL);
            
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "UploadMB exception: {0}", ex);
            
            uploadFailed();
        }

    }

}
