/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import java.util.logging.Level;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 */
@SmallModule(
    exports={FileDropper.class},
    interfaces={Uploader.class},
    name="FileDropper.com"
)
public class FileDropper extends AbstractUploader {
    
    //FileDropperAccount fileDenAccount = (FileDropperAccount) getAccountsProvider().getAccount("FileDropper.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;

    private long fileSizeLimit = 5368709120l; //5 GB

    public FileDropper() {
        downURL=UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "FileDropper.com";
    }

    @Override
    public void run(){
        try {
            if (file.length() > fileSizeLimit) {
                //Change last parameter
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), getHost());
            }
            
            status=UploadStatus.INITIALISING;
            /*
            httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://www.filedropper.com");
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2 GTBDFff GTB7.0");
            HttpResponse httpresponse = httpclient.execute(httpget);
            httpresponse.getEntity().consumeContent();
            */
            //------------------------------------------------------------
            httpPost = new NUHttpPost("http://www.filedropper.com/index.php?xml=true");
            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            //requestEntity.addPart("Filename", new StringBody(file.getName()));
            //        requestEntity.addPart("", new StringBody(
            //                "Content-Disposition: form-data; name=\"file\"; filename=\""+f.getName()+"\"\r\n" +
            //                "Content-Type: application/octet-stream"
            //        ));
//            requestEntity.addPart("file", new FileBody(file));

            requestEntity.addPart("file", createMonitoredFileBody());
            
            
            requestEntity.addPart("Upload", new StringBody("Submit Query"));
            httpPost.setEntity(requestEntity);
            //-------------------------------------------------------------
            uploading();

            //-------------------------------------------------------------
            httpResponse = httpclient.execute(httpPost);
            String strResponse = EntityUtils.toString(httpResponse.getEntity());
            //-------------------------------------------------------------
            gettingLink();
            downURL = "http://www.filedropper.com/" + strResponse.substring(strResponse.lastIndexOf("=") + 1);
            

            NULogger.getLogger().info(downURL);
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "FileDropper error: {0}", ex);
            
            uploadFailed();
        }
    }
}
