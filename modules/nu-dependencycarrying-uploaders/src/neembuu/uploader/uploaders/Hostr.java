/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.CrockoAccount;

import neembuu.uploader.accounts.HostrAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.api._crocko.CrockoApi;
import neembuu.uploader.uploaders.api._hostr.HostrApi;
import neembuu.uploader.uploaders.api._hostr.HostrApiBuilder;
import neembuu.uploader.uploaders.common.MonitoredFileBody;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import shashaank.smallmodule.SmallModule;

/**
 * If you have some problems with this plugin, take a look to <a href="https://hostr.co/developer">developers page</a>.
 * @author dinesh
 * @author davidepastore
 */
@SmallModule(
        exports = {Hostr.class,HostrAccount.class},
        interfaces = {Uploader.class, Account.class},
        name = "Hostr.co",
        dependsOn = {HostrApi.class,HostrApiBuilder.class}
)
public class Hostr extends AbstractUploader implements UploaderAccountNecessary {

    HostrAccount hostrAccount = (HostrAccount) getAccountsProvider().getAccount("Hostr.co");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;

    public Hostr() {
        super();
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Hostr.co";
        if (hostrAccount.loginsuccessful) {
            host = hostrAccount.username + " | Hostr.co";
        }

    }

    @Override
    public void run() {


        try {

            if (hostrAccount.loginsuccessful) {
                host = hostrAccount.username + " | Hostr.co";
            } else {
                host = "Hostr.co";
                uploadInvalid();
                return;

            }

            if (file.length() > hostrAccount.getMaxFileSize()) {
                throw new NUMaxFileSizeException(hostrAccount.getMaxFileSize(), file.getName(), hostrAccount.getHOSTNAME());
            }
            uploadInitialising();


            UsernamePasswordCredentials upc = new UsernamePasswordCredentials(hostrAccount.getUsername(), hostrAccount.getPassword());


            httpPost = new NUHttpPost("http://api.hostr.co/file");

            httpPost.addHeader(new BasicScheme().authenticate(upc, httpPost));

            MultipartEntity mpEntity = new MultipartEntity();
            
            mpEntity.addPart("name", new StringBody(file.getName()));
            mpEntity.addPart("file", createMonitoredFileBody());


            httpPost.setEntity(mpEntity);
            NULogger.getLogger().info("Now uploading your file into hostr...........................");
            uploading();
            httpResponse = httpclient.execute(httpPost);
            gettingLink();
            HttpEntity resEntity = httpResponse.getEntity();

            if (resEntity != null) {

                String tmp = EntityUtils.toString(resEntity);
                JSONObject json = new JSONObject(tmp);
                
                //Handle the errors
                if(json.has("error")){
                    //@todo: we must stop all upload in localhostr.com if you have exceeded the upload limit
                    HostrApiBuilder hostrApiBuilder = new HostrApiBuilder();
                    HostrApi hostrApi = hostrApiBuilder
                            .setDailyUploadAllowance(hostrAccount.getDailyUploadAllowance())
                            .setFileName(file.getName())
                            .setFileSizeLimit(hostrAccount.getMaxFileSize())
                            .setHostname(hostrAccount.getHOSTNAME())
                            .setJSONObject(json)
                            .setUsername(hostrAccount.getUsername())
                            .build();
                    hostrApi.handleErrors();
                }
                
                downURL = json.getString("href");
                NULogger.getLogger().log(Level.INFO, "Download link: {0}", downURL);
                status=UploadStatus.UPLOADFINISHED;
                uploadFinished();
            }
            else{
                throw new Exception();
            }


        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        }  catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
}
