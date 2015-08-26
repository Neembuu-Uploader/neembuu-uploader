/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.nio.charset.Charset;
import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.UploadizAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.exceptions.uploaders.NUUploadFailedException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={Uploadiz.class,UploadizAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Uploadiz.com",
    ignore = true
)
public class Uploadiz extends AbstractUploader {
    
    UploadizAccount uploadizAccount = (UploadizAccount) getAccountsProvider().getAccount("Uploadiz.com");
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpcontext;
    private HttpResponse response;
    
    private long notLoggedInMaxfileSizeLimit = 1073741824L; //1024 MB for anonymous
    private long loggedInMaxfileSizeLimit = 2147483648L; //2048 MB for logged users
    private String uid;
    private String utype;
    private String stringResponse;
    private String sess_id;
    //private String urlPostUpload = "http://www.uploadiz.com/cgi-bin/upload_status.cgi?uid=%s&files=:%s&ok=1&upload_type=file";
    private String urlPostUpload = "http://www.uploadiz.com/cgi-bin/upload.cgi?upload_id=%s&js_on=1&utype=%s&upload_type=file";
    // POST /cgi-bin/upload.cgi?upload_id=248755251933&js_on=1&utype=anon&upload_type=file HTTP/1.1

    public Uploadiz() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Uploadiz.com";

        if (uploadizAccount.loginsuccessful) {
            host = uploadizAccount.username + " | Uploadiz.com";
        }
    }

    @Override
    public void run() {
        try{
            if (uploadizAccount.loginsuccessful) {
                if (file.length() > loggedInMaxfileSizeLimit) {
                    throw new NUMaxFileSizeException(this.loggedInMaxfileSizeLimit, file.getName(), uploadizAccount.getHOSTNAME());
                }
                httpcontext = uploadizAccount.getHttpContext();
                utype = "reg";
                sess_id = CookieUtils.getCookieValue(httpcontext, "xfss");
            }
            else{
                if (file.length() > notLoggedInMaxfileSizeLimit) {
                    throw new NUMaxFileSizeException(this.notLoggedInMaxfileSizeLimit, file.getName(), uploadizAccount.getHOSTNAME());
                }
                httpcontext = new BasicHttpContext();
                utype = "anon";
                sess_id = "";
            }
            
            uploadInitialising();
            
            uid = StringUtils.uuid(12, 10);
            String newPostUrl = String.format(urlPostUpload, uid, utype);
            NULogger.getLogger().log(Level.INFO, "New Post URL : {0}", newPostUrl);
            NUHttpPost httpPost = new NUHttpPost(newPostUrl);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("sess_id", new StringBody(sess_id));
            mpEntity.addPart("file_0", createMonitoredFileBody());
            mpEntity.addPart("tos", new StringBody("1"));
            mpEntity.addPart("file_0_descr", new StringBody(""));
            httpPost.setEntity(mpEntity);
            
            uploading();
            response = httpclient.execute(httpPost, httpcontext);
            HttpEntity resEntity = response.getEntity();
            stringResponse = EntityUtils.toString(resEntity);
            Document doc = Jsoup.parse(stringResponse);
            Element body = doc.body();
            NULogger.getLogger().log(Level.INFO, "body : {0}", body.text());
            String st = doc.select("textarea[name=st]").first().text();
            
            if(!"OK".equals(st)){
                status = UploadStatus.GETTINGERRORS;
                NULogger.getLogger().log(Level.INFO, "st : {0}", st);
                throw new NUUploadFailedException(file.getName(), uploadizAccount.getHOSTNAME());
            }
            gettingLink();
            
            String fn = doc.select("textarea[name=fn]").first().text();
            String op = doc.select("textarea[name=op]").first().text();
            httpPost = new NUHttpPost("http://www.uploadiz.com/");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("st", st));
            formparams.add(new BasicNameValuePair("fn", fn));
            formparams.add(new BasicNameValuePair("op", op));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost, httpcontext);
            resEntity = response.getEntity();
            stringResponse = EntityUtils.toString(resEntity);
            doc = Jsoup.parse(stringResponse);
            Elements select = doc.select("div[class=result_slot] table tbody tr td input");
            downURL = select.eq(1).val();
            delURL = select.eq(2).val();
            
            NULogger.getLogger().log(Level.INFO, "Download URL : {0}", downURL);
            NULogger.getLogger().log(Level.INFO, "Delete URL : {0}", delURL);
            uploadFinished();
            
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        }  catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        }

    }
    
}
