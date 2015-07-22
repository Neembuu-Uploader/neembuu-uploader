/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.ZohoDocsAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
@SmallModule(
    exports={ZohoDocs.class,ZohoDocsAccount.class},
    interfaces={Uploader.class,Account.class},
    name="ZohoDocs.com"
)
public class ZohoDocs extends AbstractUploader implements UploaderAccountNecessary {
    ZohoDocsAccount zohoDocsAccount = (ZohoDocsAccount) getAccountsProvider().getAccount("ZohoDocs.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();

    public ZohoDocs() {
        downURL = UploadStatus.NA.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "ZohoDocs.com";
        maxFileSizeLimit = 104857600l; //100 MB
        
        //It has to be successful.. as it won't work without login
        if (zohoDocsAccount.loginsuccessful) {
            host = zohoDocsAccount.username + " | ZohoDocs.com";
        }

    }

    @Override
    public void run() {
        try {
            if (zohoDocsAccount.loginsuccessful) {
                host = zohoDocsAccount.username + " | ZohoDocs.com";
            } else {
                host = "ZohoDocs.com";
                
                uploadInvalid();
                return;
            }
            
            
             if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), zohoDocsAccount.getHOSTNAME());
            }
            
            uploadInitialising();
            HttpPost httppost = new HttpPost("https://docs.zoho.com/uploadsingle.do?isUploadStatus=false&folderId=-1&refFileElementId=refFileElement0");
            httppost.setHeader("Cookie", ZohoDocsAccount.getZohodocscookies().toString());
            MultipartEntity mpEntity = new MultipartEntity();
            mpEntity.addPart("multiupload_file", createMonitoredFileBody());
            mpEntity.addPart("filename", new StringBody(file.getName()));
            httppost.setEntity(mpEntity);
            NULogger.getLogger().info("Now uploading your file into zoho docs...........................");
            uploading();
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            NULogger.getLogger().info(response.getStatusLine().toString());
            String stringResponse = EntityUtils.toString(resEntity);
            if (resEntity != null) {
                if (stringResponse.contains("Uploaded Sucessfully")) {
                    NULogger.getLogger().info("File Uploaded Successfully");
                    
                    uploadFinished();

                } else {
                    throw new Exception("There might be a problem with your internet connection or server error. Please try after some time. :( "+stringResponse);
                }

            }


        } catch (Exception e) {
            Logger.getLogger(ZohoDocs.class.getName()).log(Level.SEVERE, null, e);
            
            uploadFailed();

        }
    }
}
