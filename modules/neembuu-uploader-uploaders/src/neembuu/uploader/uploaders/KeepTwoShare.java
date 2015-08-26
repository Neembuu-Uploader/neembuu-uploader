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
import java.util.logging.Logger;
import neembuu.uploader.accounts.KeepTwoShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={KeepTwoShare.class,KeepTwoShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Keep2Share.cc"
)
public class KeepTwoShare extends AbstractUploader implements UploaderAccountNecessary{
    
    KeepTwoShareAccount keepTwoShareAccount = (KeepTwoShareAccount) getAccountsProvider().getAccount("Keep2Share.cc");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    JSONObject jSonObject;
    
    private String uploadURL;
    private String catalog;
    private int userId;
    private long expires;
    private String hmac;
    private boolean ajax;
    private String qquuid;
    
    private String downloadlink = "";
    private String deletelink = "";

    public KeepTwoShare() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Keep2Share.cc";
        if (keepTwoShareAccount.loginsuccessful) {
            host = keepTwoShareAccount.username + " | Keep2Share.cc";
        }
        maxFileSizeLimit = 5242880000l; //5000 MB
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://keep2share.cc/", httpContext);

        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("div#content div form.form-vertical").attr("action");
        catalog = doc.select("form.form-vertical div.panel input:eq(0)").val();
        expires = Long.parseLong(doc.select("form.form-vertical div.panel input:eq(1)").val());
        userId = Integer.parseInt(doc.select("form.form-vertical div.panel input:eq(2)").val());
        hmac = doc.select("form.form-vertical div.panel input:eq(4)").val();
        ajax = true;
        qquuid = getUniqueId();
        
        
        NULogger.getLogger().log(Level.INFO, "hmac: {0}", hmac);
    }

    @Override
    public void run() {
        try {
            if (keepTwoShareAccount.loginsuccessful) {
                httpContext = keepTwoShareAccount.getHttpContext();
                maxFileSizeLimit = 5242880000l; //5000 MB
            } else {
                host = "Keep2Share.cc";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), keepTwoShareAccount.getHOSTNAME());
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("catalog", new StringBody(catalog));
            mpEntity.addPart("userId", new StringBody(Integer.toString(userId)));
            mpEntity.addPart("expires", new StringBody(Long.toString(expires)));
            mpEntity.addPart("hmac", new StringBody(hmac));
            mpEntity.addPart("projectName", new StringBody("k2s"));
            mpEntity.addPart("ajax", new StringBody(Boolean.toString(ajax)));
            mpEntity.addPart("qquuid", new StringBody(qquuid));
            mpEntity.addPart("qqtotalfilesize", new StringBody(Long.toString(file.length())));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into Keep2Share.cc");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //FileUtils.saveInFile("KeepTwoShare.html", responseString);
            
            jSonObject = new JSONObject(responseString);
            if(jSonObject.has("success") && jSonObject.getBoolean("success")){
                doc = Jsoup.parse(responseString);
            
                //Read the links
                gettingLink();
                responseString = NUHttpClientUtils.getData("http://keep2share.cc/files.html", httpContext);
                
                doc = Jsoup.parse(responseString);
                Elements aElements = doc.select("div#content div#file-manager.file-manager table.items tbody tr.file td.name a");
                boolean found = false;
                
                //Find the file name
                for(Element aElement : aElements){
                    if(!found && aElement.text().equals(file.getName())){
                        responseString = NUHttpClientUtils.getData("http://keep2share.cc" + aElement.attr("href"), httpContext);
                        doc = Jsoup.parse(responseString);
                        downloadlink = doc.select("textarea").first().text();
                        found = true;
                    }
                }
                
                if(found){
                    NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                    
                    downURL = downloadlink;
                }
                else{
                    throw new Exception("Download and delete link not found!");
                }
                
            }
            else{
                throw new Exception("Upload error: " + responseString);
            }
            
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
    /**
     * Get unique id. From <a href="http://static2.keep2share.cc/assets/2348cf4b/jquery.fineuploader-3.5.0.js">here</a>.
     * @return The unique id.
     */
    private String getUniqueId(){
        String uniqueId = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
        char charArray[] = uniqueId.toCharArray();
        
        char singleChar;
        for(int i = 0; i < charArray.length; i++){
            singleChar = charArray[i];
            if(singleChar == 'x' || singleChar == 'y'){
                int r = (int)(Math.random() * 16) | 0;
                int v;
                if(singleChar == 'x'){
                    v = r;
                }
                else{
                    v = (r & 0x3 | 0x8);
                }
                charArray[i] = Integer.toHexString(v).charAt(0);
            }
        }
        
        uniqueId = String.valueOf(charArray);
        return uniqueId;
        /*
        return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace("/[xy]/g", function (c) {
            var r = Math.random() * 16 | 0,
                v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
        */
    }
    
}
 
