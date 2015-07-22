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
import neembuu.uploader.accounts.RapidShareAccount;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
@SmallModule(
    exports={RapidShare.class,RapidShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="RapidShare.com",
    ignore = true
)
public class RapidShare extends AbstractUploader implements UploaderAccountNecessary {

    RapidShareAccount rapidShareAccount = (RapidShareAccount) getAccountsProvider().getAccount("RapidShare.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    
    private String postURL;
    private String uploadresponse;
    private String downloadid;
    private String filename;
    private String downloadlink;
    private long fileSizeLimit = Long.MAX_VALUE; //It seems that there is no limit

    public RapidShare() {
        host = "RapidShare.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        //It has to be successful.. as it won't work without login
        if (rapidShareAccount.loginsuccessful) {
            host = rapidShareAccount.username + " | RapidShare.com";
        }
    }

    @Override
    public void run() {

        try {
            if (rapidShareAccount.loginsuccessful) {
                host = rapidShareAccount.username + " | RapidShare.com";
            } else {
                host = "RapidShare.com";

                uploadInvalid();
                return;
            }

            uploadInitialising();
            NULogger.getLogger().info("Now getting dynamic rs link");
            String link = getData("http://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=nextuploadserver&cbid=3&cbf=rs.jsonp.callback");
            link = link.substring(link.indexOf("\"") + 1);
            link = link.substring(0, link.indexOf("\""));
            NULogger.getLogger().log(Level.INFO, "rs link : {0}", link);
            long uploadID = (long) Math.floor(Math.random() * 90000000000L) + 10000000000L;
            postURL = "http://rs" + link + ".rapidshare.com/cgi-bin/rsapi.cgi?uploadid=" + uploadID;
            NULogger.getLogger().log(Level.INFO, "rapidshare : {0}", postURL);
            fileUpload();
        } catch (Exception e) {
            Logger.getLogger(RapidShare.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }

    }

    public String getData(String url) throws Exception {
        
        httpGet = new NUHttpGet(url);

        httpResponse = httpclient.execute(httpGet, httpContext);
        return EntityUtils.toString(httpResponse.getEntity());

    }

    public void fileUpload() throws Exception {
        httpPost = new NUHttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("sub", new StringBody("upload"));
        mpEntity.addPart("cookie", new StringBody(RapidShareAccount.getRscookie()));
        mpEntity.addPart("folder", new StringBody("0"));
        mpEntity.addPart("filecontent", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into rs...........................");
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();

        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
        if (resEntity != null) {
            gettingLink();
            uploadresponse = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "Actual response : {0}", uploadresponse);
            uploadresponse = uploadresponse.replace("COMPLETE\n", "");

//            downloadid=uploadresponse.substring(uploadresponse.indexOf("E")+1);
            downloadid = uploadresponse.substring(0, uploadresponse.indexOf(","));
            uploadresponse = uploadresponse.replace(downloadid + ",", "");
            filename = uploadresponse.substring(0, uploadresponse.indexOf(","));
            NULogger.getLogger().log(Level.INFO, "download id : {0}", downloadid);
//            filename=uploadresponse.substring(uploadresponse.indexOf(","));
//            filename=uploadresponse.substring(0, uploadresponse.indexOf(","));
            NULogger.getLogger().log(Level.INFO, "File name : {0}", filename);
            downloadlink = "http://rapidshare.com/files/" + downloadid + "/" + filename;
            NULogger.getLogger().log(Level.INFO, "Download Link :{0}", downloadlink);
            downURL = downloadlink;

            uploadFinished();
        }
    }
}
