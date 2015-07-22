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
import neembuu.uploader.accounts.PogoPlugAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpPut;
import neembuu.uploader.uploaders.common.MonitoredFileEntity;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={PogoPlug.class,PogoPlugAccount.class},
    interfaces={Uploader.class,Account.class},
    name="PogoPlug.com"
)
public class PogoPlug extends AbstractUploader implements UploaderAccountNecessary{
    
    PogoPlugAccount pogoPlugAccount = (PogoPlugAccount) getAccountsProvider().getAccount("PogoPlug.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private String responseString;
    private String uploadURL;
    
    private String validation_token = "";
    private String temp_var = "";
    private String device_id = "";
    private String service_id = "";
    private String api_url = "";
    private String file_id = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public PogoPlug() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "PogoPlug.com";
        if (pogoPlugAccount.loginsuccessful) {
            host = pogoPlugAccount.username + " | PogoPlug.com";
        }
        maxFileSizeLimit = Long.MAX_VALUE; // Unlimited
    }

    private void initialize() throws Exception {
    }

    @Override
    public void run() {
        try {
            if (pogoPlugAccount.loginsuccessful) {
                validation_token = pogoPlugAccount.validation_token; // get the validation token received during login
                httpContext = pogoPlugAccount.getHttpContext();
                maxFileSizeLimit = Long.MAX_VALUE; // Unlimited
            }
            else {
                host = "PogoPlug.com";
                uploadInvalid();
                return;
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
			
            NULogger.getLogger().info("** PogoPlug.com ** => Retrieving available devices ...");
            uploadURL = "http://service.pogoplug.com/svc/api/listDevices?valtoken=" +validation_token;
            responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
            
            temp_var = responseString;
            temp_var = StringUtils.stringUntilString(temp_var, "\"services\"");
            device_id = StringUtils.stringBetweenTwoStrings(temp_var, "\"deviceid\":\"", "\"");
            
            temp_var = responseString;
            temp_var = StringUtils.stringStartingFromString(temp_var, "\"services\"");
            service_id = StringUtils.stringBetweenTwoStrings(temp_var, "\"serviceid\":\"", "\"");
            api_url = StringUtils.stringBetweenTwoStrings(temp_var, "\"apiurl\":\"", "\"");
            
            NULogger.getLogger().info("** PogoPlug.com ** => Creating an empty file ...");
            
            if (api_url.isEmpty()) {
                NULogger.getLogger().info("** PogoPlug.com ** => no API url received ... falling back to default");
                uploadURL = "http://service.pogoplug.com/svc/api/createFile?valtoken=" +validation_token;
                uploadURL += "&deviceid=" + device_id;
                uploadURL += "&serviceid=" + service_id;
                uploadURL += "&filename=" + file.getName();
                responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
                
                temp_var = responseString;
                temp_var = StringUtils.stringBetweenTwoStrings(temp_var, "\"fileid\":\"", "\"");
                file_id = temp_var;
                
                // /svc/files/<valtoken>/<deviceid>/<serviceid>/<fileid>/
                uploadURL = "http://service.pogoplug.com/svc/files/" +validation_token;
                uploadURL += "/" +device_id;
                uploadURL += "/" +service_id;
                uploadURL += "/" +file_id+ "/";
                
                HttpPut httpPut = new HttpPut(uploadURL);
                MonitoredFileEntity fileEntity = createMonitoredFileEntity();
                httpPut.setEntity(fileEntity);
                
                NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPut.getRequestLine());
                NULogger.getLogger().info("** PogoPlug.com ** => Uploading your file now");
                uploading();
                httpResponse = httpclient.execute(httpPut, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
            } else {
                NULogger.getLogger().info("** PogoPlug.com ** => API url received, using it instead of the default url");
                uploadURL = api_url;
                uploadURL += "createFile?valtoken=" +validation_token;
                uploadURL += "&deviceid=" + device_id;
                uploadURL += "&serviceid=" + service_id;
                uploadURL += "&filename=" + file.getName();
                responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
                
                temp_var = responseString;
                temp_var = StringUtils.stringBetweenTwoStrings(temp_var, "\"fileid\":\"", "\"");
                file_id = temp_var;
                
                // /svc/files/<valtoken>/<deviceid>/<serviceid>/<fileid>/
                uploadURL = StringUtils.stringUntilString(api_url, "/svc/");
                uploadURL += "/svc/files/" +validation_token;
                uploadURL += "/" +device_id;
                uploadURL += "/" +service_id;
                uploadURL += "/" +file_id+ "/";
                
                HttpPut httpPut = new HttpPut(uploadURL);
                MonitoredFileEntity fileEntity = createMonitoredFileEntity();
                httpPut.setEntity(fileEntity);
                
                NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPut.getRequestLine());
                NULogger.getLogger().info("** PogoPlug.com ** => Uploading your file now");
                uploading();
                httpResponse = httpclient.execute(httpPut, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
            }
            
            //Read the links
            gettingLink();

            // https://service.pogoplug.com/svc/api/enableShare?valtoken=&deviceid=&serviceid=&fileid=
            uploadURL = "https://service.pogoplug.com/svc/api/enableShare?valtoken=" +validation_token;
            uploadURL += "&deviceid=" + device_id;
            uploadURL += "&serviceid=" + service_id;
            uploadURL += "&fileid=" +file_id;
            responseString = NUHttpClientUtils.getData(uploadURL, httpContext);
            
            downloadlink = StringUtils.stringBetweenTwoStrings(responseString, "\"url\":\"", "\"");
            deletelink = UploadStatus.NA.getLocaleSpecificString();

            NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            delURL = deletelink;

            uploadFinished();

        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
}
