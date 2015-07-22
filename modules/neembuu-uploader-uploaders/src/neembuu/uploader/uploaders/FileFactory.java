/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.FileFactoryAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={FileFactory.class,FileFactoryAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FileFactory.com"
)
public class FileFactory extends AbstractUploader implements UploaderAccountNecessary {

    FileFactoryAccount fileFactoryAccount = (FileFactoryAccount) getAccountsProvider().getAccount("FileFactory.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    
    private boolean login = false;
    private String downloadLink = "";
    private long fileSizeLimit = 5368709120L; // 5 GB

    public FileFactory() {
        host = "FileFactory.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        if (fileFactoryAccount.loginsuccessful) {
            login = true;
            host = fileFactoryAccount.username + " | FileFactory.com";
        }

    }

    @Override
    public void run() {
        try {
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), fileFactoryAccount.getHOSTNAME());
            }

            if (fileFactoryAccount.loginsuccessful) {
                login = true;
                host = fileFactoryAccount.username + " | FileFactory.com";
            } else {
                host = "FileFactory.com";
                uploadInvalid();
                return;
            }
            uploadInitialising();
            fileupload();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(FileFactory.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }

    }

    private void fileupload() throws Exception {
        httpContext = fileFactoryAccount.getHttpContext();
        
        httpPost = new NUHttpPost("http://upload.filefactory.com/upload-beta.php");

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("Filedata", createMonitoredFileBody());
        reqEntity.addPart("cookie", new StringBody(URLDecoder.decode(fileFactoryAccount.getFileFactoryMembershipcookie(), "UTF-8")));
        reqEntity.addPart("Filename", new StringBody(file.getName()));
        httpPost.setEntity(reqEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into filefactory.com. Please wait......................");
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();
        String id = "";
        if (resEntity != null) {
            id = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "ID value: {0}", id);
        }
        gettingLink();
        downloadLink = NUHttpClientUtils.getData("http://www.filefactory.com/upload/results.php?files=" + id, httpContext);
        
        //FileUtils.saveInFile("FileFactory.com.html", downloadLink);
        
        Document doc = Jsoup.parse(downloadLink);
        downloadLink = doc.select("#row_"+ id +" td a").attr("href");

        NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downloadLink);
        downURL = downloadLink;

        uploadFinished();
    }

}
