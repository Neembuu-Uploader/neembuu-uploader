/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.BoxDotComAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={BoxDotCom.class,BoxDotComAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Box.com"
)
public class BoxDotCom extends AbstractUploader implements UploaderAccountNecessary {

    BoxDotComAccount boxDotComAccount = (BoxDotComAccount) getAccountsProvider().getAccount("Box.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext;
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    
    private final String UPLOAD_TYPE = "html5";
    private final String FOLDER_ID = "0";
    private final String CREATED_DATE = "0";
    private final String LAST_MODIFIED_DATE = "Tue, 15 Jul 2014 08:25:56 GMT";
    
    private String downloadlink;

    public BoxDotCom() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Box.com";

        //It has to be successful.. as it won't work without login
        if (boxDotComAccount.loginsuccessful) {
            host = boxDotComAccount.username + " | Box.com";
            maxFileSizeLimit = 2147483648L; //2 GB
        }

    }

    @Override
    public void run() {
        if (boxDotComAccount.loginsuccessful) {
            host = boxDotComAccount.username + " | Box.com";
            httpContext = boxDotComAccount.getHttpContext();
        }
        else{
            host = "Box.com";
            uploadInvalid();
            return;
        }

        try {
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), boxDotComAccount.getHOSTNAME());
            }

            uploadInitialising();
            
            //Step 1 - Get information from /files and send the request
            responseString = NUHttpClientUtils.getData("https://app.box.com/files", httpContext);
            
            String realtimeSubscriberId = StringUtils.stringBetweenTwoStrings(responseString, "realtime_subscriber_id ='", "'");
            String requestToken = StringUtils.stringBetweenTwoStrings(responseString, "request_token = '", "'");
            String lastModified = LAST_MODIFIED_DATE;
            httpPost = new NUHttpPost("https://app.box.com/index.php?rm=box_start_upload");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("created_date", CREATED_DATE));
            formparams.add(new BasicNameValuePair("folder_id", FOLDER_ID));
            formparams.add(new BasicNameValuePair("last_modified_date", lastModified));
            formparams.add(new BasicNameValuePair("name", file.getName()));
            formparams.add(new BasicNameValuePair("realtime_subscriber_id", realtimeSubscriberId));
            formparams.add(new BasicNameValuePair("request_token", requestToken));
            formparams.add(new BasicNameValuePair("size", Long.toString(file.length())));
            formparams.add(new BasicNameValuePair("upload_type", UPLOAD_TYPE));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Step 2 - Get upload session id
            JSONObject jSonObject = new JSONObject(responseString);
//            NULogger.getLogger().log(Level.INFO, "JSON: {0}", jSonObject.toString());
            String uploadSessionId = jSonObject.getString("upload_session_id");
//            NULogger.getLogger().log(Level.INFO, "Upload session id: {0}", uploadSessionId);
            
            //Step 3 - Upload file
            String uploadUrl = "https://upload.app.box.com/html5?folder_id=" + FOLDER_ID + "&upload_type=" + UPLOAD_TYPE + "&upload_session_id=" + uploadSessionId + "&last_modified_date=" + URLEncoder.encode(lastModified, "UTF-8").replace("+", "%20") + "&realtime_subscriber_id=" + realtimeSubscriberId + "&request_token=" + requestToken;
            httpPost = new NUHttpPost(uploadUrl);
            httpPost.setEntity(createMonitoredFileEntity());
            
            //Set custom headers
            httpPost.setHeader("Content-Type", "application/octet-stream");
            httpPost.setHeader("X-File-Name", file.getName());
            httpPost.setHeader("X-File-Size", Long.toString(file.length()));

            NULogger.getLogger().log(Level.INFO, "{0} Executing ......{1}", new Object[]{getClass(), httpPost.getRequestLine()});

            // Here we go!
            NULogger.getLogger().log(Level.INFO, "{0} Now uploading your files into box.com", getClass());
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().log(Level.FINE, "Response status line: {0}", httpResponse.getStatusLine());
            responseString = EntityUtils.toString(httpResponse.getEntity());
            //FileUtils.saveInFile("BoxDotCom.html", stringResponse);
            
            //Step 4 - Get the download url
            gettingLink();
            responseString = NUHttpClientUtils.getData("https://app.box.com/index.php?rm=box_get_items&q[upload_session_ids][0]=" + uploadSessionId, httpContext);
            jSonObject = new JSONObject(responseString);
            if(jSonObject.has("error")){
                throw new Exception("Error to find uploaded files.");
            }
            else{
                NULogger.getLogger().log(Level.INFO, jSonObject.toString());
                String fileId = ( (JSONObject) jSonObject.getJSONObject("params").getJSONArray("items").get(0)).getString("unidb");
                
                NULogger.getLogger().log(Level.INFO, "fileid: {0}", fileId);
                httpPost = new NUHttpPost("https://app.box.com/index.php?rm=box_download_file_via_post");

                //Not follow redirect
                httpPost.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
                
                formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("file_id", fileId));
                formparams.add(new BasicNameValuePair("request_token", requestToken));
                entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                
                downloadlink = httpResponse.getLastHeader("Location").getValue();
                downURL = downloadlink;
                
                EntityUtils.consume(httpResponse.getEntity());
                uploadFinished();
            }

        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(BoxDotCom.class.getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        } finally {
            downloadlink = null;
        }

    }
}
