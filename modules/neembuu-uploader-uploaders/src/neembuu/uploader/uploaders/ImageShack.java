/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.ImageShackAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.StringUtils;
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

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={ImageShack.class,ImageShackAccount.class},
    interfaces={Uploader.class,Account.class},
    name="ImageShack.us"
)
public class ImageShack extends AbstractUploader implements UploaderAccountNecessary {

    ImageShackAccount imageShackAccount = (ImageShackAccount) getAccountsProvider().getAccount("ImageShack.us");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    
    private String downloadlink = "";
    private boolean support = false;
    private String upload_key;
    private long fileSizeLimit = 5242880l; //5 MB
    
    private ArrayList<String> allowedExtensions = new ArrayList<String>();
    private ArrayList<String> allowedImageExtensions = new ArrayList<String>();
    private ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public ImageShack() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "ImageShack.us";

        if (imageShackAccount.loginsuccessful) {
            host = imageShackAccount.username + " | ImageShack.us";
        }

    }

    private void fileUpload() throws Exception {
        //DefaultHttpClient httpclient = new DefaultHttpClient();
        // int Min = 1, Max = 10;
        //Min + (int)(Math.random() * ((Max - Min) + 1))
        //1+(int)(Math.random() * ((10 - 1) + 1))
        //1+(int)(Math.random() * 10)
//        int k = 1 + (int) (Math.random() * 10);

        //This is to check again whether file being uploaded is a video or image
        //file so we can send the file to appropriate url
        if (FileUtils.checkFileExtension(allowedImageExtensions, file)) {
            httpPost = new NUHttpPost("http://www.imageshack.us/upload_api.php");
        }
        if (FileUtils.checkFileExtension(allowedVideoExtensions, file)) {
            httpPost = new NUHttpPost("http://render.imageshack.us/upload_api.php");
        }

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("Filename", new StringBody(file.getName()));
        reqEntity.addPart("optimage", new StringBody("1"));
        reqEntity.addPart("new_flash_uploader", new StringBody("y"));
        reqEntity.addPart("rembar", new StringBody("0"));
        reqEntity.addPart("myimages", new StringBody("null"));
        reqEntity.addPart("optsize", new StringBody("optimize"));
        reqEntity.addPart("rem_bar", new StringBody("0"));
        if (imageShackAccount.loginsuccessful) {
            reqEntity.addPart("isUSER", new StringBody(imageShackAccount.getUsercookie()));
            reqEntity.addPart("myimages", new StringBody(imageShackAccount.getMyimagescookie()));
        } else {
            reqEntity.addPart("isUSER", new StringBody("null"));
        }
        reqEntity.addPart("swfupload", new StringBody("1"));
        reqEntity.addPart("ulevel", new StringBody("null"));
        reqEntity.addPart("always_opt", new StringBody("null"));
        reqEntity.addPart("key", new StringBody(upload_key));
        reqEntity.addPart("Filedata", createMonitoredFileBody());
        reqEntity.addPart("upload", new StringBody("Submit Query"));
        httpPost.setEntity(reqEntity);
        NULogger.getLogger().info("Now uploading your file into imageshack.us Please wait......................");
        uploading();
        httpResponse = httpclient.execute(httpPost, httpContext);
        gettingLink();
        HttpEntity resEntity = httpResponse.getEntity();

        if (resEntity != null) {
            downloadlink = EntityUtils.toString(resEntity);
            downloadlink = StringUtils.stringBetweenTwoStrings(downloadlink, "<image_link>", "<");
            NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downloadlink);
            downURL = downloadlink;

            uploadFinished();
        } else {
            throw new Exception("Temporary ImageShack server problem or network problem");
        }
    }

    @Override
    public void run() {

        if (imageShackAccount.loginsuccessful) {
            host = imageShackAccount.username + " | ImageShack.us";
        } else {
            host = "ImageShack.us";
            uploadInvalid();
            return;
        }
        
        try {
            
            addExtensions();
            
            //Check extension
            if(!FileUtils.checkFileExtension(allowedExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }
            
            //Check size
            if (FileUtils.checkFileExtension(allowedImageExtensions, file)) {
                if (file.length() > fileSizeLimit) {
                    throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), imageShackAccount.getHOSTNAME());
                }
            }
        
            uploadInitialising();
            httpContext = imageShackAccount.getHttpContext();
            upload_key = imageShackAccount.getUpload_key();
            
            fileUpload();
            
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {
            Logger.getLogger(ImageShack.class.getName()).log(Level.SEVERE, null, ex);

            uploadFailed();
        }

    }
    
    /**
    * Add all the allowed extensions.
    */
    private void addExtensions(){
        //Image
        allowedImageExtensions.add("jpeg");
        allowedImageExtensions.add("jpg");
        allowedImageExtensions.add("bmp");
        allowedImageExtensions.add("gif");
        allowedImageExtensions.add("png");
        allowedImageExtensions.add("tiff");
        
        //Video
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("3gp");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("3gpp");
        
        //Container for all
        allowedExtensions.addAll(allowedImageExtensions);
        allowedExtensions.addAll(allowedVideoExtensions);
    }
}
