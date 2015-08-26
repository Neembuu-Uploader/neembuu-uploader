package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.ZShareAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
@SmallModule(
    exports={ZShare.class,ZShareAccount.class},
    interfaces={Uploader.class,Account.class},
    name="ZShare.ma",
    ignore = true
)
public class ZShare extends AbstractUploader {

    ZShareAccount zShareAccount = (ZShareAccount) getAccountsProvider().getAccount("ZShare.ma");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    private String zshareuploadlink = "";
    private String uploadresponse;
    private String linkpage;
    private String linkActionPage;
    private String downloadlink = "";
    private String deletelink;
    private String fnvalue;

    public ZShare() {
        host = "ZShare.ma";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        if (zShareAccount.loginsuccessful) {
            //  login = true;
            host = zShareAccount.username + " | ZShare.ma";
        }
        maxFileSizeLimit = 1073741824l; //1 GB
    }

    private void fileUpload() throws IOException, Exception {

        if (zShareAccount.loginsuccessful) {
            zshareuploadlink = zShareAccount.getZsharelink();
            zshareuploadlink = zshareuploadlink.replaceAll("anon", "reg");
            httpPost = new NUHttpPost(zshareuploadlink);
            httpPost.setHeader("Cookie", zShareAccount.getXfsscookie());
        } else {
            httpGet = new NUHttpGet("http://www.zshare.ma/");
            httpResponse = httpclient.execute(httpGet);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            Document doc = Jsoup.parse(stringResponse);
            zshareuploadlink = doc.select("div#div_file form").attr("action");
            zshareuploadlink += StringUtils.uuid(12, 10) + "&js_on=1&utype=anon&upload_type=file";  
            httpPost = new NUHttpPost(zshareuploadlink);
        }
        NULogger.getLogger().log(Level.INFO, "Upload link: {0}", zshareuploadlink);

        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        mpEntity.addPart("upload_type", new StringBody("file"));
        mpEntity.addPart("sess_id", new StringBody(zShareAccount.getXfsscookie().replaceAll("xfss=", "")));
        mpEntity.addPart("file_0", createMonitoredFileBody());
        mpEntity.addPart("tos", new StringBody("1"));
        mpEntity.addPart("image.x", new StringBody("81"));
        mpEntity.addPart("image.y", new StringBody("23"));
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        uploading();
        NULogger.getLogger().info("Now uploading your file into zshare.ma");
        HttpResponse response = httpclient.execute(httpPost);
        HttpEntity resEntity = response.getEntity();
        NULogger.getLogger().info(response.getStatusLine().toString());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }

        //NULogger.getLogger().log(Level.INFO, "upload response : {0}", uploadresponse);
        //FileUtils.saveInFile("ZShare.html", uploadresponse);
        
        fnvalue = StringUtils.stringBetweenTwoStrings(uploadresponse, "name='fn'>", "<");
        linkActionPage = StringUtils.stringBetweenTwoStrings(uploadresponse, "action='", "'");
        //NULogger.getLogger().log(Level.INFO, "fn value : {0}", fnvalue);
        //NULogger.getLogger().log(Level.INFO, "linkActionPage : {0}", linkActionPage);

    }

    private void getDownloadLink() throws Exception {

        NULogger.getLogger().info("Now Getting Download link...");
        httpPost = new NUHttpPost(linkActionPage);

        if (zShareAccount.loginsuccessful) {
            httpPost.setHeader("Cookie", zShareAccount.getXfsscookie());
        }
        List formparams = new ArrayList();
        formparams.add(new BasicNameValuePair("fn", fnvalue));
        formparams.add(new BasicNameValuePair("st", "OK"));
        formparams.add(new BasicNameValuePair("op", "upload_result"));
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(formEntity);

//        h.setHeader("Referer", postURL);
//        h.setHeader("Cookie", sidcookie + ";" + mysessioncookie);
        HttpResponse res = httpclient.execute(httpPost);
        HttpEntity entity = res.getEntity();
        linkpage = EntityUtils.toString(entity);
//        NULogger.getLogger().info(linkpage);

        //FileUtils.saveInFile("afterdownload.html", linkpage);

        downloadlink = StringUtils.stringBetweenTwoStrings(linkpage, "Direct Link:</b></td>", "</td>");
        downloadlink = StringUtils.stringBetweenTwoStrings(downloadlink, "value=\"", "\"");
        deletelink = StringUtils.stringBetweenTwoStrings(linkpage, "Delete Link:</b></td>", "</td>");
        deletelink = StringUtils.stringBetweenTwoStrings(deletelink, "value=\"", "\"");
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
        NULogger.getLogger().log(Level.INFO, "Delete Link : {0}", deletelink);


        downURL = downloadlink;
        delURL = deletelink;
        uploadFinished();
    }

    @Override
    public void run() {
        try {
            if (zShareAccount.loginsuccessful) {
                host = zShareAccount.username + " | ZShare.ma";
            } else {
                host = "ZShare.ma";
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), zShareAccount.getHOSTNAME());
            }

            uploadInitialising();
            if (!zShareAccount.loginsuccessful) {
                zShareAccount.initialize();
            }
            fileUpload();
            getDownloadLink();

        } catch(NUException ex){
            ex.printError();
            
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(ZShare.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
}
