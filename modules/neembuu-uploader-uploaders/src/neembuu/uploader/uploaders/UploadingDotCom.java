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
import neembuu.uploader.accounts.UploadingDotComAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * 
 * @author Dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={UploadingDotCom.class,UploadingDotComAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Uploading.com"
)
public class UploadingDotCom extends AbstractUploader implements UploaderAccountNecessary {

    UploadingDotComAccount uploadingDotComAccount = (UploadingDotComAccount) getAccountsProvider().getAccount("Uploading.com");
    private String uploadresponse = "", uploadinglink = "", postURL = "", sid = "";
    private String afterloginpage = "";
    private String downloadlink = "";
    private String fileID;
    private HttpClient httpClient = NUHttpClient.getHttpClient();
    private HttpContext httpContext;
    private NUHttpPost httpPost;

    public UploadingDotCom() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Uploading.com";
        if (uploadingDotComAccount.loginsuccessful) {
            host = uploadingDotComAccount.username + " | Uploading.com";
        }
        maxFileSizeLimit = 2147483648L; //2 GB
    }

    private String getData() throws Exception {
        NUHttpGet httpGet = new NUHttpGet("http://www.uploading.com");
        HttpResponse httpResponse = httpClient.execute(httpGet, httpContext);
        return EntityUtils.toString(httpResponse.getEntity());
    }

    private void fileUpload() throws Exception {
        //httpClient = new DefaultHttpClient();
        httpPost = new NUHttpPost(postURL);
        
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("Filename", new StringBody(getFileName()));
        reqEntity.addPart("SID", new StringBody(sid));
        reqEntity.addPart("folder_id", new StringBody("0"));
        reqEntity.addPart("file", new StringBody(fileID));
        reqEntity.addPart("file", createMonitoredFileBody());
        reqEntity.addPart("upload", new StringBody("Submit Query"));
        httpPost.setEntity(reqEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into uploading.com. Please wait......................");
        HttpResponse response = httpClient.execute(httpPost, httpContext);
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null) {
            gettingLink();
            uploadresponse = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "PAGE :{0}", uploadresponse);
            uploadresponse = StringUtils.stringBetweenTwoStrings(uploadresponse, "answer\":\"", "\"");
            downURL = downloadlink;

            uploadFinished();

        } else {
            throw new Exception("There might be a problem with your internet connection or server error. Please try after some time :(");
        }
    }

    private void getPreDownloadLink() throws Exception {
        httpPost = new NUHttpPost("http://uploading.com/files/generate/?ajax");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("name", file.getName()));
        formparams.add(new BasicNameValuePair("size", String.valueOf(file.length())));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");

        HttpResponse httpResponse = httpClient.execute(httpPost, httpContext);
        String response = EntityUtils.toString(httpResponse.getEntity());
        NULogger.getLogger().log(Level.INFO, "Response : {0}", response);
        JSONObject jsonObject = new JSONObject(response);
        JSONObject fileJson = jsonObject.getJSONObject("file");
        fileID = String.valueOf(fileJson.getLong("file_id"));
        downloadlink = fileJson.getString("link");
        
        NULogger.getLogger().log(Level.INFO, "File ID : {0}", fileID);
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);

    }

    @Override
    public void run() {
        try {

            if (uploadingDotComAccount.loginsuccessful) {
                host = uploadingDotComAccount.username + " | Uploading.com";
                httpContext = uploadingDotComAccount.getHttpContext();
                httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true); 
            } else {
                host = "Uploading.com";

                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), uploadingDotComAccount.getHOSTNAME());
            }

            uploadInitialising();
            
            getPreDownloadLink();
            

            afterloginpage = getData();
            //NULogger.getLogger().log(Level.INFO, "after : {0}", afterloginpage);
            uploadinglink = StringUtils.stringBetweenTwoStrings(afterloginpage, "upload_url: '", "'");
            uploadinglink = uploadinglink.replaceAll("\\\\", "");
            NULogger.getLogger().log(Level.INFO, "New Upload link : {0}", uploadinglink);
            postURL = uploadinglink;
            sid = StringUtils.stringBetweenTwoStrings(afterloginpage, "SID': '", "'");
            NULogger.getLogger().log(Level.INFO, "New sid from site : {0}", sid);
            fileUpload();

        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(UploadingDotCom.class.getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        }

    }
}
