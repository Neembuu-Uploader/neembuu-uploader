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
import neembuu.uploader.accounts.GoFourUpAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.Random;
import neembuu.uploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
@SmallModule(
    exports={GoFourUp.class,GoFourUpAccount.class},
    interfaces={Uploader.class,Account.class},
    name="Go4Up.com"
)
public class GoFourUp extends AbstractUploader{
    
    GoFourUpAccount goFourUpAccount = (GoFourUpAccount) getAccountsProvider().getAccount("Go4Up.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String id_user = "";
    private String upload_id = "";
    private String server = "";
    private String uploadid_s = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public GoFourUp() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Go4Up.com";
        if (goFourUpAccount.loginsuccessful) {
            host = goFourUpAccount.username + " | Go4Up.com";
        }
        maxFileSizeLimit = 20971520000L; // 20,000 MB (default)
        
    }

    private void initialize() throws Exception {
        
        if (userType.equals("anon")){
            responseString = NUHttpClientUtils.getData("http://go4up.com/", httpContext);

            doc = Jsoup.parse(responseString);
            uploadURL = doc.select("form[id=myformupload]").attr("action");
            upload_id = doc.select("form[id=myformupload]").select("input[name=uploadID]").attr("value");
            id_user = doc.select("form[id=myformupload]").select("input[name=id_user]").attr("value");
            server = doc.select("form[id=myformupload]").select("input[name=server]").attr("value");
        }
    }

    @Override
    public void run() {
        try {
            if (goFourUpAccount.loginsuccessful) {
                userType = "reg";
                httpContext = goFourUpAccount.getHttpContext();
                maxFileSizeLimit = 20971520000L; // 20,000 MB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 20971520000L; // 20,000 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            if (userType.equals("reg")){
                responseString = NUHttpClientUtils.getData("http://go4up.com/api/getserver.php", httpContext);
                
                httpPost = new NUHttpPost(responseString);
                MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                mpEntity.addPart("user", new StringBody(goFourUpAccount.username));
                mpEntity.addPart("pass", new StringBody(goFourUpAccount.password));
                mpEntity.addPart("filedata", createMonitoredFileBody());
                httpPost.setEntity(mpEntity);
                
                NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
                NULogger.getLogger().info("Now uploading your file into Go4Up.com");
                uploading();
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                
                gettingLink();
                // <link>http://go4up.com/dl/7f310db7f857</link>
                responseString = StringUtils.stringStartingFromString(responseString, "<link>");
                responseString = StringUtils.stringUntilString(responseString, "</link>");
                
                downloadlink = responseString;
                deletelink = UploadStatus.NA.getLocaleSpecificString();
            }
            
            if (userType.equals("anon")){
                long uploadID;
                Random random = new Random();
                uploadID = Math.round(random.nextFloat() * Math.pow(10,12));
                uploadid_s = String.valueOf(uploadID);

                uploadURL += "?upload_id=" + uploadid_s + "&js_on=1&xpass=&xmode=1";
                // http://u5.go4up.com/cgi-bin/upload.cgi
                // http://u5.go4up.com/cgi-bin/upload.cgi?upload_id=509765818554&js_on=1&xpass=&xmode=1
                httpPost = new NUHttpPost(uploadURL);
                MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                mpEntity.addPart("uploadID", new StringBody(upload_id));
                mpEntity.addPart("id_user", new StringBody(id_user));
                mpEntity.addPart("server", new StringBody(server));
                mpEntity.addPart("file_0", createMonitoredFileBody());
                mpEntity.addPart("box[]", new StringBody("43")); // 1Fichier.com
                mpEntity.addPart("box[]", new StringBody("21")); // BitShare.com
                mpEntity.addPart("box[]", new StringBody("20")); // FileCloud.io
                mpEntity.addPart("box[]", new StringBody("12")); // FileFactory.com
                mpEntity.addPart("box[]", new StringBody("69")); // FileParadox.com
                mpEntity.addPart("box[]", new StringBody("56")); // HugeFiles.net
                mpEntity.addPart("box[]", new StringBody("32")); // MegaShares.com
                mpEntity.addPart("box[]", new StringBody("14")); // NetLoad.in
                mpEntity.addPart("box[]", new StringBody("65")); // Oboom.com
                mpEntity.addPart("box[]", new StringBody("33")); // RapidGator.net
                mpEntity.addPart("box[]", new StringBody("34")); // Share-Online.biz
                mpEntity.addPart("box[]", new StringBody("57")); // TusFiles.net
                mpEntity.addPart("box[]", new StringBody("68")); // UploadAble.ch
                mpEntity.addPart("box[]", new StringBody("13")); // Uploaded.net
                mpEntity.addPart("box[]", new StringBody("26")); // UploadHero.co
                mpEntity.addPart("box[]", new StringBody("2"));  // UptoBox.com
                httpPost.setEntity(mpEntity);

                NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
                NULogger.getLogger().info("Now uploading your file into Go4Up.com");
                uploading();
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                doc = Jsoup.parse(responseString);
                
                String duration = doc.select("form[name=F1]").select("textarea[name=duration]").val();
                String file_mime = doc.select("form[name=F1]").select("textarea[name=file_mime[]]").val();
                String file_name = doc.select("form[name=F1]").select("textarea[name=file_name[]]").val();
                String file_name_orig = doc.select("form[name=F1]").select("textarea[name=file_name_orig[]]").val();
                String file_size = doc.select("form[name=F1]").select("textarea[name=file_size[]]").val();
                String file_status = doc.select("form[name=F1]").select("textarea[name=file_status[]]").val();
                String host_val = doc.select("form[name=F1]").select("textarea[name=host]").val();
                id_user = doc.select("form[name=F1]").select("textarea[name=id_user]").val();
                String ip = doc.select("form[name=F1]").select("textarea[name=ip]").val();
                String number_of_files = doc.select("form[name=F1]").select("textarea[name=number_of_files]").val();
                server = doc.select("form[name=F1]").select("textarea[name=server]").val();
                uploadid_s = doc.select("form[name=F1]").select("textarea[name=uploadID]").val();
                
                httpPost = new NUHttpPost("http://go4up.com/home/upload_process");
                mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                mpEntity.addPart("box[]", new StringBody("43")); // 1Fichier.com
                mpEntity.addPart("box[]", new StringBody("21")); // BitShare.com
                mpEntity.addPart("box[]", new StringBody("20")); // FileCloud.io
                mpEntity.addPart("box[]", new StringBody("12")); // FileFactory.com
                mpEntity.addPart("box[]", new StringBody("69")); // FileParadox.com
                mpEntity.addPart("box[]", new StringBody("56")); // HugeFiles.net
                mpEntity.addPart("box[]", new StringBody("32")); // MegaShares.com
                mpEntity.addPart("box[]", new StringBody("14")); // NetLoad.in
                mpEntity.addPart("box[]", new StringBody("65")); // Oboom.com
                mpEntity.addPart("box[]", new StringBody("33")); // RapidGator.net
                mpEntity.addPart("box[]", new StringBody("34")); // Share-Online.biz
                mpEntity.addPart("box[]", new StringBody("57")); // TusFiles.net
                mpEntity.addPart("box[]", new StringBody("68")); // UploadAble.ch
                mpEntity.addPart("box[]", new StringBody("13")); // Uploaded.net
                mpEntity.addPart("box[]", new StringBody("26")); // UploadHero.co
                mpEntity.addPart("box[]", new StringBody("2"));  // UptoBox.com
                mpEntity.addPart("duration", new StringBody(duration));
                mpEntity.addPart("file_descr[]", new StringBody(""));
                mpEntity.addPart("file_mime[]", new StringBody(file_mime));
                mpEntity.addPart("file_name[]", new StringBody(file_name));
                mpEntity.addPart("file_name_orig[]", new StringBody(file_name_orig));
                mpEntity.addPart("file_size[]", new StringBody(file_size));
                mpEntity.addPart("file_status[]", new StringBody(file_status));
                mpEntity.addPart("host", new StringBody(host_val));
                mpEntity.addPart("id_user", new StringBody(id_user));
                mpEntity.addPart("ip", new StringBody(ip));
                mpEntity.addPart("number_of_files", new StringBody(number_of_files));
                mpEntity.addPart("server", new StringBody(server));
                mpEntity.addPart("uploadID", new StringBody(uploadid_s));
                httpPost.setEntity(mpEntity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                
                gettingLink();
                
                /*responseString = StringUtils.stringUntilString(responseString, "\"USERID\"");
                responseString = StringUtils.stringStartingFromString(responseString, "string");
                responseString = StringUtils.stringBetweenTwoStrings(responseString, "\"", "\"");*/
                responseString = NUHttpClientUtils.getData("http://go4up.com/home/result", httpContext);
                doc = Jsoup.parse(responseString);
                responseString = doc.select("a[title=download]").eq(1).text();
                
                downloadlink = responseString;
                deletelink = UploadStatus.NA.getLocaleSpecificString();
            }

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
    
    public static void main(String[] args) {
        neembuu.uploader.paralytics_tests.GenericPluginTester.test(
                GoFourUp.class,
                neembuu.uploader.accounts.GoFourUpAccount.class
        );
    }
}
