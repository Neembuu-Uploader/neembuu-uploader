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
import neembuu.uploader.accounts.UpBoothAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
@SmallModule(
    exports={UpBooth.class,UpBoothAccount.class},
    interfaces={Uploader.class,Account.class},
    name="UpBooth.com",
    ignore = true
)
public class UpBooth extends AbstractUploader {

    UpBoothAccount upBoothAccount = (UpBoothAccount) getAccountsProvider().getAccount("UpBooth.com");
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private long fileSizeLimit = 1073741824L; //1 GB for free and unregistered users
    //5 GB for premium users

    public UpBooth() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "UpBooth.com";

        if (upBoothAccount.loginsuccessful) {
            host = upBoothAccount.username + " | UpBooth.com";
        }
    }

    private void fileUpload() throws Exception {

        HttpPost httppost = new HttpPost("http://upbooth.com/uploadHandler.php?r=upbooth.com&p=http?aff=1db2f3b654350bf4");
        if (upBoothAccount.loginsuccessful) {
            httpContext = upBoothAccount.getHttpContext();
        }
        
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        

        
//        String cookie = fileHostingCookie.substring(fdisableLogin();ileHostingCookie.indexOf("=") + 1);
//        cookie = cookie.substring(0, cookie.indexOf(";"));
//        NULogger.getLogger().info(cookie);vb

        
        mpEntity.addPart("files[]", createMonitoredFileBody()); 
        httppost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httppost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into upbooth.com");
        uploading();
        HttpResponse response = httpclient.execute(httppost, httpContext);
        //        HttpEntity resEntity = response.getEntity();
        String uploadresponse = EntityUtils.toString(response.getEntity());

        NULogger.getLogger().info(String.valueOf(response.getStatusLine())); 
//        if (resEntity != null) {
//            uploadresponse = EntityUtils.toString(resEntity);
//        }
//  
        //NULogger.getLogger().log(Level.INFO, "Upload response : {0}", uploadresponse);
        String downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "\"url\":\"", "\"");
        downloadlink = downloadlink.replaceAll("\\\\", "");
        String deletelink = StringUtils.stringBetweenTwoStrings(uploadresponse, "\"delete_url\":\"", "\"");
        deletelink = deletelink.replaceAll("\\\\", "");
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
        NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
        downURL = downloadlink;
        delURL = deletelink;

        uploadFinished();
    }

    @Override
    public void run() {
        try {
            if (upBoothAccount.loginsuccessful) {

                host = upBoothAccount.username + " | UpBooth.com";
            } else {

                host = "UpBooth.com";
            }
            
            
            //Set fileSizeLimit
            if (upBoothAccount.loginsuccessful) {
                if(upBoothAccount.isPremium()) {
                    fileSizeLimit = 5368709120L; // 5 GB
                }
                else {
                    fileSizeLimit = 1073741824L; // 1 GB
                }
            }

            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(this.fileSizeLimit, file.getName(), upBoothAccount.getHOSTNAME());
            }
            fileUpload();

        } catch (NUException ex) {
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            NULogger.getLogger().severe(e.toString());

            uploadFailed();
        }
    }
}
