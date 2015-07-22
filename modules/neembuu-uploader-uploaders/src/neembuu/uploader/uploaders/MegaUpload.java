/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**DEAD*/

package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.MegaUploadAccount;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
@SmallModule(
    exports={MegaUpload.class,MegaUploadAccount.class},
    interfaces={Uploader.class,Account.class},
    name="MegaUpload.com",
    ignore = true
)
public class MegaUpload extends AbstractUploader {
    MegaUploadAccount megaUploadAccount = (MegaUploadAccount) getAccountsProvider().getAccount("MegaUpload.com");

    private boolean login = false;
    private URL u;
    private HttpURLConnection uc;
    private BufferedReader br;
    private long filelength;
    private String postURL = "";
    private String uploadID;
    private String downloadlink = "";
    private PrintWriter pw;
    private String usercookie = "";
    private String megauploadlink = "";
    private long fileSizeLimit = Long.MAX_VALUE; //No limit?

    public MegaUpload() {
        host = "MegaUpload.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        if (megaUploadAccount.loginsuccessful) {
            login = true;
            host = megaUploadAccount.username + " | MegaUpload.com";
        }

    }

    public void run() {
        try {
            uploadInitialising();
            
            if (megaUploadAccount.loginsuccessful) {
                login = true;
                host = megaUploadAccount.username + " | MegaUpload.com";
            } else {
                login = false;
                host = "MegaUpload.com";
            }

            initialize();
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            filelength = file.length();
            generateMegaUploadID();
            if (login) {
                status = UploadStatus.GETTINGCOOKIE;
                usercookie  = MegaUploadAccount.getUserCookie();
                postURL = megauploadlink + "upload_done.php?UPLOAD_IDENTIFIER=" + uploadID + "&" + usercookie + "&s=" + filelength;
            } else {
                postURL = megauploadlink + "upload_done.php?UPLOAD_IDENTIFIER=" + uploadID + "&user=undefined&s=" + filelength;
            }
            HttpPost httppost = new HttpPost(postURL);
            httppost.setHeader("Cookie",usercookie );
            MultipartEntity mpEntity = new MultipartEntity();
            mpEntity.addPart("", createMonitoredFileBody());
            httppost.setEntity(mpEntity);
            NULogger.getLogger().info("Now uploading your file into megaupload...........................");
            status=UploadStatus.UPLOADING;
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

            NULogger.getLogger().info(response.getStatusLine().toString());
            if (resEntity != null) {
                gettingLink();
                downloadlink = EntityUtils.toString(resEntity);
                downloadlink = StringUtils.stringBetweenTwoStrings(downloadlink, "downloadurl = '", "'");
                
                downURL = downloadlink;
                NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downURL);
                
                uploadFinished();
            }
        } catch (Exception ex) {
            Logger.getLogger(MegaUpload.class.getName()).log(Level.SEVERE, null, ex);
            
            uploadFailed();
        }

    }

    public void initialize() throws Exception {
        NULogger.getLogger().info("Parsing http://www.megaupload.com to get megaupload host value......");
        String data = getData("http://www.megaupload.com");
        megauploadlink = StringUtils.stringBetweenTwoStrings(data, "flashvars.servers = \"", ";");
        NULogger.getLogger().info(megauploadlink);
        NULogger.getLogger().info("Generating random ID value for uploading.......");
        generateMegaUploadID();

    }


    public void generateMegaUploadID() {
        int[] randno = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        int earlyID = (Math.random() < 0.5) ? 0 : 1;
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(earlyID + new Date().getTime()));
        int loop = 32 - String.valueOf(uploadID).length();
        int tmp = 1;
        while (tmp <= loop) {
            long sum = randno[(int) Math.round(Math.floor(Math.random() * 10))];
            sb.append(sum);
            tmp++;
        }
        uploadID = sb.toString();
        NULogger.getLogger().log(Level.INFO, "Megaupload key {0}", uploadID);
    }

    public void setHttpHeader(String cookie) {

        try {
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.megaupload.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://www.megaupload.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            uc.setRequestMethod("POST");
            uc.setInstanceFollowRedirects(false);


        } catch (Exception e) {
            NULogger.getLogger().log(Level.INFO, "ex{0}", e.toString());
        }
    }
    /*
     * This method is used to write the POST data like username and password.
     * This takes the "content" value as a parameter which needs to be posted.
     * 
     */

    public void writeHttpContent(String content) {

        try {

            NULogger.getLogger().info(content);
            pw = new PrintWriter(new OutputStreamWriter(uc.getOutputStream()), true);
            pw.print(content);
            pw.flush();
            pw.close();
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//            String httpResp = br.readLine();

            Map<String, List<String>> headerFields = uc.getHeaderFields();
            if (headerFields.containsKey("Set-Cookie")) {
                List<String> header = headerFields.get("Set-Cookie");
                for (int i = 0; i < header.size(); i++) {
                    String tmp = header.get(i);
                    if (tmp.contains("user")) {
                        usercookie = tmp;
                        usercookie = usercookie.substring(0, usercookie.indexOf(";"));
                    }
                }
                NULogger.getLogger().log(Level.INFO, "user cookie : {0}", usercookie);
            } else {
                NULogger.getLogger().info("MegaUpload: Invalid username or password");
            }


        } catch (Exception e) {
            //NULogger.getLogger().info("ex " + e.toString());
        }
    }

    public void postData(String content, String posturl) throws Exception {
        u = new URL(posturl);
            setHttpHeader(usercookie);
            writeHttpContent(content);
            u = null;
            uc = null;
        
    }

    public String getData(String url) throws Exception {

        
            u = new URL(url);
            uc = (HttpURLConnection) u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Host", "www.megaupload.com");
            uc.setRequestProperty("Connection", "keep-alive");
            uc.setRequestProperty("Referer", "http://mediafire.com/");
            uc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
            uc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            uc.setRequestProperty("Accept-Encoding", "html");
            uc.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
            uc.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
            //uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //   uc.setRequestProperty("Cookie", ukeycookie + ";" + skeycookie + ";" + usercookie);
            uc.setRequestMethod("GET");
            uc.setInstanceFollowRedirects(false);
            br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String temp = "", k = "";
            while ((temp = br.readLine()) != null) {
                //NULogger.getLogger().info(temp);
                k += temp;
            }
            br.close();
            u = null;
            uc = null;
            return k;

    }
    
}
