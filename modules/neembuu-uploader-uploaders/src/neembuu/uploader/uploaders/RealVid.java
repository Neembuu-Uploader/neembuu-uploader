/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.RealVidAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Random;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={RealVid.class,RealVidAccount.class},
    interfaces={Uploader.class,Account.class},
    name="RealVid.net"
)
public class RealVid extends AbstractUploader implements UploaderAccountNecessary{
    
    RealVidAccount realVidAccount = (RealVidAccount) getAccountsProvider().getAccount("RealVid.net");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String srv_tmp_url = "";
    private String srv_id = "";
    private String disk_id = "";
    private String uploadid_s = "";
    private String upload_fn = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public RealVid() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "RealVid.net";
        if (realVidAccount.loginsuccessful) {
            host = realVidAccount.username + " | RealVid.net";
        }
        maxFileSizeLimit = 5242880000L; // 5,000 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://realvid.net/?op=upload", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[name=file]").attr("action");
        srv_tmp_url = doc.select("form[name=file]").select("input[name=srv_tmp_url]").attr("value");
        sessionID = doc.select("form[name=file]").select("input[name=sess_id]").attr("value");
	srv_id = doc.select("form[name=file]").select("input[name=srv_id]").attr("value");
	disk_id = doc.select("form[name=file]").select("input[name=disk_id]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (realVidAccount.loginsuccessful) {
                userType = "reg";
                httpContext = realVidAccount.getHttpContext();
                maxFileSizeLimit = 5242880000L; // 5,000 MB
            }
            else {
                host = "RealVid.net";
                uploadInvalid();
                return;
            }
			
            addExtensions();
            //Check extension
            if(!FileUtils.checkFileExtension(allowedVideoExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
			
            long uploadID;
            Random random = new Random();
            uploadID = Math.round(random.nextFloat() * Math.pow(10,12));
            uploadid_s = String.valueOf(uploadID);

            uploadURL += uploadid_s + "&utype=" + userType + "&disk_id=" + disk_id;
            // http://93.115.7.197/cgi-bin/upload.cgi?upload_id=
            // http://93.115.7.197/cgi-bin/upload.cgi?upload_id=940778806003&utype=reg&disk_id=01
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("srv_tmp_url", new StringBody(srv_tmp_url));
            mpEntity.addPart("srv_id", new StringBody(srv_id));
            mpEntity.addPart("disk_id", new StringBody(disk_id));
            mpEntity.addPart("file", createMonitoredFileBody());
            mpEntity.addPart("fakefilepc", new StringBody(file.getName()));
            mpEntity.addPart("file_title", new StringBody(removeExtension(file.getName())));
            mpEntity.addPart("file_descr", new StringBody("Uploaded via Neembuu Uploader!"));
            mpEntity.addPart("file_public", new StringBody("1"));
            mpEntity.addPart("tos", new StringBody("1"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into RealVid.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();

            upload_fn = doc.select("textarea[name=fn]").val();
            if (upload_fn != null) {
                httpPost = new NUHttpPost("http://realvid.net/");
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("fn", upload_fn));
                formparams.add(new BasicNameValuePair("op", "upload_result"));
                formparams.add(new BasicNameValuePair("st", "OK"));
                
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                
                doc = Jsoup.parse(responseString);
                downloadlink = doc.select("textarea").first().val();
                deletelink = UploadStatus.NA.getLocaleSpecificString();
                
                NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                delURL = deletelink;
                
                uploadFinished();
            }
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
    public static String removeExtension(String filePath) {
        final int lastPeriodPos = filePath.lastIndexOf('.');
        if (lastPeriodPos <= 0) {
            return filePath;
        }
        else {
            filePath = filePath.substring(0, lastPeriodPos);
            return filePath;
        }
    }
    
    /**
     * Add all the allowed extensions.
     * var ext_allowed='avi|mkv|mpg|mpeg|vob|wmv|flv|mp4|mov|m2v|divx|xvid|3gp|webm|ogv|ogg|rmvb';
     */
    private void addExtensions(){
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("vob");
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("m2v");
        allowedVideoExtensions.add("divx");
        allowedVideoExtensions.add("xvid");
        allowedVideoExtensions.add("3gp");
        allowedVideoExtensions.add("webm");
        allowedVideoExtensions.add("ogv");
        allowedVideoExtensions.add("ogg");
        allowedVideoExtensions.add("rmvb");
    }
    
}
