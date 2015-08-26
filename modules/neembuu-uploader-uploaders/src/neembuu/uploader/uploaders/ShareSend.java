/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * <a href="http://support.sharesend.com/customer/portal/articles/840650">API</a>
 * @author Dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={ShareSend.class},
    interfaces={Uploader.class},
    name="ShareSend.com"
)
public class ShareSend extends AbstractUploader {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String stringResponse;
    private JSONObject jsonObject;
    private String uploadUrl;
    
    private long fileSizeLimit = 104857600l; //100 MB

    public ShareSend() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "ShareSend.com";
    }
    
    private boolean getUploadUrl() throws UnsupportedEncodingException, IOException, Exception{
        String contentType = FileUtils.getContentType(file);
        FileInputStream fis = new FileInputStream(file);
        //NULogger.getLogger().log(Level.INFO, "Content type: {0}", contentType);
        
        httpPost = new NUHttpPost("http://sharesend.com/api/get_upload_url");
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("name", new StringBody(file.getName()));
        mpEntity.addPart("content_type", new StringBody(contentType));
        mpEntity.addPart("size", new StringBody(Long.toString(file.length())));
        mpEntity.addPart("hash", new StringBody(DigestUtils.sha256Hex(fis)));
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        httpResponse = httpclient.execute(httpPost, httpContext);
        
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        //FileUtils.saveInFile("ShareSend.html", stringResponse);
        
        jsonObject = new JSONObject(stringResponse);
        
        if(jsonObject.has("error")){
            //Handle errors
            status = UploadStatus.GETTINGERRORS;
            throw new Exception(jsonObject.getString("error"));
        }
        else{
            if(jsonObject.has("token")){
                gettingLink();
                uploadProgress.set(100);
                downURL = "http://sharesend.com/" + jsonObject.getString("token");
                return true;
            }
            else{
                uploadUrl = jsonObject.getString("upload_url");
            }
        }
        return false;
    }
    
    private void upload() throws IOException, Exception {
        httpPost = new NUHttpPost(uploadUrl);
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("file", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        
        uploading();
        httpResponse = httpclient.execute(httpPost, httpContext);
        
        gettingLink();
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        jsonObject = new JSONObject(stringResponse);
        
        if(jsonObject.has("error")){
            //Handle errors
            status = UploadStatus.GETTINGERRORS;
            throw new Exception(jsonObject.getString("error"));
        }
        else{
            downURL = "http://sharesend.com/" + jsonObject.getString("token");
        }
        
        //FileUtils.saveInFile("ShareSend.html", stringResponse);
    }

    @Override
    public void run() {

        try {
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), getHost());
            }
            uploadInitialising();
            if(!getUploadUrl()){
                upload();
            }
            uploadFinished();
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "ShareSend upload failed: {0}", ex);

            uploadFailed();
        }

    }
}
