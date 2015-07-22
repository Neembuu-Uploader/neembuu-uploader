/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import java.util.Random;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 */
@SmallModule(
    exports={FileDude.class},
    interfaces={Uploader.class},
    name="FileDude.com"
)
public class FileDude extends AbstractUploader {

    
    HttpPost httppost;
    String strResponse, link;
    String filekey, hash;
    String start = "Your download link is: <br /><a href=\"";
    private long fileSizeLimit = Long.MAX_VALUE; //No limit?
   
    public FileDude() {
        host = "FileDude.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        status = UploadStatus.QUEUED;
    }

    @Override
    public void run() {
        try {
            uploadInitialising();
            String fname=file.getName();
            if(fname.substring(fname.lastIndexOf(".")+1).equals("txt")){
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().filetypenotsupported() + ": <b>txt</b></html>", getClass().getSimpleName());
                
                uploadInvalid();
                return;
            }
            HttpParams params = new BasicHttpParams();
            params.setParameter(
                    "http.useragent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            DefaultHttpClient httpclient = new DefaultHttpClient(params);
            HttpGet httpget = new HttpGet("http://www.filedude.com");
            HttpResponse httpresponse = httpclient.execute(httpget);
            strResponse = EntityUtils.toString(httpresponse.getEntity());
            link = strResponse.substring(strResponse.indexOf("<form action=\"") + 14);
            link = link.substring(0, link.indexOf("\""));
            NULogger.getLogger().info(link);
            //------------------------------------------------------------
            httppost = new HttpPost(link);
            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            requestEntity.addPart("Filedata", createMonitoredFileBody());
            int randint = new Random().nextInt(10);
            requestEntity.addPart("x", new StringBody((randint*100+randint)+""));
            requestEntity.addPart("y", new StringBody(randint+""));
            //NULogger.getLogger().info((randint*100+randint)+"");
            //NULogger.getLogger().info(randint+"");
            httppost.setEntity(requestEntity);
            //-------------------------------------------------------------
            uploading();

            //-------------------------------------------------------------
            NULogger.getLogger().log(Level.INFO, "Going to upload {0}", file);
            httpresponse = httpclient.execute(httppost);
            strResponse = EntityUtils.toString(httpresponse.getEntity());
            //-------------------------------------------------------------
            gettingLink();
            downURL = strResponse.substring(strResponse.indexOf(start) + start.length());
            downURL = downURL.substring(0, downURL.indexOf("\""));
            //--------------------------------------------------------------


            


            NULogger.getLogger().log(Level.INFO, "Download Link: {0}", downURL);
            uploadFinished();
        } catch (Exception ex) {
            ex.printStackTrace();
            NULogger.getLogger().severe(ex.toString());
            
            uploadFailed();
        } 


    }

    
}
