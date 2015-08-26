/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.BufferedReader;
import java.util.logging.Level;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.accounts.IFileAccount;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
@SmallModule(
    exports={IFile.class,IFileAccount.class},
    interfaces={Uploader.class,Account.class},
    name="IFile.it"
)
public class IFile extends AbstractUploader implements UploaderAccountNecessary {

    IFileAccount iFileAccount = (IFileAccount) getAccountsProvider().getAccount("IFile.it");
    private URL u;
    private HttpURLConnection uc;
    private BufferedReader br;
    private String postURL;
    private String uploadresponse;
    private String file_ukey;
    private String downloadlink;
    private long fileSizeLimit = 1090519040; //1040 MB

    public IFile() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "IFile.it";
//It has to be successful.. as it won't work without login
        if (iFileAccount.loginsuccessful) {
            host = iFileAccount.username + " | IFile.it";
        }
    }

    @Override
    public void run() {
        try {
            //-------------------------------------------------------------


            if (iFileAccount.loginsuccessful) {
                host = iFileAccount.username + " | IFile.it";
            } else {
                host = "IFile.it";

                uploadInvalid();
                return;
            }

            if (file.length() > fileSizeLimit) {
                showWarningMessage( "<html><b>" + getClass().getSimpleName() + "</b> " + Translation.T().maxfilesize() + ": <b>1040MB</b></html>", getClass().getSimpleName());
                uploadInvalid();
                return;
            }
            uploadInitialising();

            NULogger.getLogger().info("Getting upload url from ifile.......");
            u = new URL("http://ifile.it/api-fetch_upload_url.api");
            uc = (HttpURLConnection) u.openConnection();

            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
                NULogger.getLogger().info(temp);
                k += temp;
            }

            uc.disconnect();
            postURL = StringUtils.stringBetweenTwoStrings(k, "upload_url\":\"", "\"");
            postURL = postURL.replaceAll("\\\\", "");
            NULogger.getLogger().log(Level.INFO, "Post URL : {0}", postURL);



            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(postURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("akey", new StringBody(properties().getEncryptedProperty("ifile_api_key")));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            httppost.setEntity(mpEntity);
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httppost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into ifile.it");
            uploading();
            HttpResponse response = httpclient.execute(httppost);
            gettingLink();
            NULogger.getLogger().info("Now getting downloading link.........");
            HttpEntity resEntity = response.getEntity();

            httpclient.getConnectionManager().shutdown();
            NULogger.getLogger().info(response.getStatusLine().toString());
            if (resEntity != null) {
                uploadresponse = EntityUtils.toString(resEntity);
                NULogger.getLogger().log(Level.INFO, "Upload response : {0}", uploadresponse);
            } else {
                throw new Exception("There might be a problem with your internet connection or server error. Please try after some time :(");
            }

            if (uploadresponse.contains("\"status\":\"ok\"")) {
                NULogger.getLogger().info("File uploaded successfully :)");
                file_ukey = StringUtils.stringBetweenTwoStrings(uploadresponse, "\"ukey\":\"", "\"");
                NULogger.getLogger().log(Level.INFO, "File ukey : {0}", file_ukey);
                //http://ifile.it/6phxv2z

                downloadlink = "http://www.ifile.it/" + file_ukey + "/" + file.getName();
                downURL = downloadlink;
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            } else {
                throw new Exception("There might be a problem with your internet connection or server error. Please try after some time :(");
            }


            NULogger.getLogger().log(Level.INFO, "Download Link: {0}", downURL);
            //NULogger.getLogger().log(Level.INFO, "Delete link: {0}", delURL);
            uploadFinished();
        } catch (Exception ex) {
            ex.printStackTrace();
            NULogger.getLogger().severe(ex.toString());
            uploadFailed();
        }
    }
}
