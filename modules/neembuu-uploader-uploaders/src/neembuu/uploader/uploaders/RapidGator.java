/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import neembuu.uploader.accounts.RapidGatorAccount;
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
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 * @author davidepastore
 */
@SmallModule(
    exports={RapidGator.class,RapidGatorAccount.class},
    interfaces={Uploader.class,Account.class},
    name="RapidGator.net"
)
public class RapidGator extends AbstractUploader implements UploaderAccountNecessary {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private NUHttpPost httpPost;
    private String stringResponse;

    private String uid;
    private URL u;
    private HttpURLConnection uc;
    RapidGatorAccount rapidGatorAccount = (RapidGatorAccount) getAccountsProvider().getAccount("RapidGator.net");
    String UPLOAD_ID_CHARS = "1234567890qwertyuiopasdfghjklzxcvbnm";
    private BufferedReader br;
    private String uploadURL;
    private String downloadlink;
    private String deletelink;
    private long fileSizeLimit = 5368709120L; //5 GB is the file size limit for all users

    public RapidGator() {
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "RapidGator.net";

        if (rapidGatorAccount.loginsuccessful) {
            host = rapidGatorAccount.username + " | RapidGator.net";
        }

    }

    public void generateRapidGatorID() {
        StringBuilder sb = new StringBuilder();
        //sb.append(new Date().getTime() / 1000);
        for (int i = 0; i < 32; i++) {
            int idx = 1 + (int) (Math.random() * 35);
            sb.append(UPLOAD_ID_CHARS.charAt(idx));
        }
        uid = sb.toString();
//        NULogger.getLogger().info("uid : " + uid + " - " + uid.length());
    }

    private void initialize() throws Exception {

        generateRapidGatorID();
        httpContext = rapidGatorAccount.getHttpContext();
        NULogger.getLogger().info("Getting upload url from rapidgator");
        String k = NUHttpClientUtils.getData("http://rapidgator.net/", httpContext);
        
        /*
        u = new URL("http://rapidgator.net/");

        uc = (HttpURLConnection) u.openConnection();
        if (rapidGatorAccount.loginsuccessful) {
            uc.setRequestProperty("Cookie", rapidGatorAccount.getLoginCookie());
        }
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp = "";
        while ((tmp = br.readLine()) != null) {
//            NULogger.getLogger().info(tmp);
            k += tmp;
        }
        */


        uploadURL = StringUtils.stringBetweenTwoStrings(k, "form_url = \"", "\"");
        uploadURL += uid + "&folder_id=0";
        NULogger.getLogger().log(Level.INFO, "Upload URL :{0}", uploadURL);


    }

    @Override
    public void run() {
        
        try {
            
            if (rapidGatorAccount.loginsuccessful) {

                host = rapidGatorAccount.username + " | RapidGator.net";
            } else {
                host = "RapidGator.net";
                uploadInvalid();
                return;
            }

            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), getHost() );
            }
            uploadInitialising();

            initialize();
            httpPost = new NUHttpPost(uploadURL);
            /*
            HttpPost httppost = new HttpPost(uploadURL);
            if (rapidGatorAccount.loginsuccessful) {
                httppost.setHeader("Cookie", rapidGatorAccount.getLoginCookie());
            }
            */
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
//            NULogger.getLogger().info("executing request " + httppost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into rapidgator.com");
            String uploadTime = String.valueOf(new Date().getTime());
            uploading();
            HttpResponse response = httpclient.execute(httpPost, httpContext);


            NULogger.getLogger().info(EntityUtils.toString(response.getEntity()));


            String uploadResponse = "";
            do {
                String url = uploadURL.substring(0, uploadURL.indexOf("/?")) + "/?r=upload/jsonprogress&data%5B0%5D%5Buuid%5D=" + uid + "&data%5B0%5D%5Bstart_time%5D=" + uploadTime;
                uploadResponse = NUHttpClientUtils.getData(url, httpContext);
                /*
                uploadResponse="";
                u = null;
                uc = null;
                br = null;
                u = new URL(uploadURL.substring(0, uploadURL.indexOf("/?")) + "/?r=upload/jsonprogress&data%5B0%5D%5Buuid%5D=" + uid + "&data%5B0%5D%5Bstart_time%5D=" + uploadTime);
                uc = (HttpURLConnection) u.openConnection();
//        NULogger.getLogger().info("Checking url.." + uc.getURL());
                if (rapidGatorAccount.loginsuccessful) {
                    uc.setRequestProperty("Cookie", rapidGatorAccount.getLoginCookie());
                }
                br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                String k = "", tmp = "";
                while ((tmp = br.readLine()) != null) {
                    NULogger.getLogger().info(tmp);
                    k += tmp;
                }
                uploadResponse=k;
                */

            } while (!uploadResponse.contains("done")); 


            downloadlink = StringUtils.stringBetweenTwoStrings(uploadResponse, "\"download_url\":\"", "\"");
            downloadlink = downloadlink.replaceAll("\\\\", "");
            deletelink = StringUtils.stringBetweenTwoStrings(uploadResponse, "\"remove_url\":\"", "\"");
            deletelink = deletelink.replaceAll("\\\\", "");
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
            downURL = downloadlink;
            delURL = deletelink;
            uploadFinished();

        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            NULogger.getLogger().severe(e.toString());

            uploadFailed();

        }
    }
}
