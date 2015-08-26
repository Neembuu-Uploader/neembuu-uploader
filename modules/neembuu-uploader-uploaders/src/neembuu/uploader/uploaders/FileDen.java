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
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.accounts.FileDenAccount;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.uploaders.NUFileExtensionException;
import neembuu.uploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.uploaders.common.FileUtils;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
@SmallModule(
    exports={FileDen.class,FileDenAccount.class},
    interfaces={Uploader.class,Account.class},
    name="FileDen.com"
)
public class FileDen extends AbstractUploader implements UploaderAccountNecessary {
    
    FileDenAccount fileDenAccount = (FileDenAccount) getAccountsProvider().getAccount("FileDen.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    
    private String uploadresponse;
    private String downloadlink;
    private long fileSizeLimit = 1073741824; //1 GB
    
    private ArrayList<String> disallowedExtensions = new ArrayList<String>();

    public FileDen() {
        host = "FileDen.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        //It has to be successful.. as it won't work without login
        if (fileDenAccount.loginsuccessful) {
            host = fileDenAccount.username + " | FileDen.com";
        }
    }

    @Override
    public void run() {
        try {
            
            if (fileDenAccount.loginsuccessful) {
                host = fileDenAccount.username + " | FileDen.com";
            } else {
                host = "FileDen.com";
                
                uploadInvalid();
                return;
            }

            //Check size
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), fileDenAccount.getHOSTNAME());
            }
            
            addExtensions();
            
            //Check extension
            if(FileUtils.checkFileExtension(disallowedExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }


//Disallowed File Types: html, htm, php, php3, phtml, htaccess, htpasswd, cgi, pl, asp, aspx, cfm, exe, ade, adp, bas, bat, chm, cmd, com, cpl, crt, hlp, hta, inf, ins, isp, jse, lnk, mdb, mde, msc, msi, msp, mst, pcd, pif, reg, scr, sct, shs, url, vbe, vbs, wsc, wsf, wsh, shb, js, vb, ws, mdt, mdw, mdz, shb, scf, pl, pm, dll            
            uploadInitialising();

            httpPost = new NUHttpPost("http://www.fileden.com/upload_old.php");
            httpPost.setHeader("Cookie", fileDenAccount.getCookies().toString());
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("action", new StringBody("upload"));
            mpEntity.addPart("upload_to", new StringBody(""));
            mpEntity.addPart("overwrite_option", new StringBody("overwrite"));
            mpEntity.addPart("thumbnail_size", new StringBody("small"));
            mpEntity.addPart("create_img_tags", new StringBody("1"));
            mpEntity.addPart("file0", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into fileden");
            uploading();
            httpResponse = httpclient.execute(httpPost);
            HttpEntity resEntity = httpResponse.getEntity();
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            gettingLink();
            if (resEntity != null) {
                uploadresponse = EntityUtils.toString(resEntity);
            }


            //NULogger.getLogger().info(uploadresponse);
            //FileUtils.saveInFile("FileDen.html", uploadresponse);
            downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "'link':'", "'");
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            
            uploadFinished();


        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(FileDen.class.getName()).log(Level.SEVERE, null, e);
            
            uploadFailed();

        }
    }

    /**
     * Add all the not allowed extensions.
     */
    private void addExtensions(){
        disallowedExtensions.add("html");
        disallowedExtensions.add("htm");
        disallowedExtensions.add("php");
        disallowedExtensions.add("php3");
        disallowedExtensions.add("phtml");
        disallowedExtensions.add("htaccess");
        disallowedExtensions.add("htpasswd");
        disallowedExtensions.add("cgi");
        disallowedExtensions.add("pl");
        disallowedExtensions.add("asp");
        disallowedExtensions.add("aspx");
        disallowedExtensions.add("cfm");
        disallowedExtensions.add("exe");
        disallowedExtensions.add("ade");
        disallowedExtensions.add("adp");
        disallowedExtensions.add("bas");
        disallowedExtensions.add("bat");
        disallowedExtensions.add("chm");
        disallowedExtensions.add("cmd");
        disallowedExtensions.add("com");
        disallowedExtensions.add("cpl");
        disallowedExtensions.add("cpl");
        disallowedExtensions.add("crt");
        disallowedExtensions.add("hlp");
        disallowedExtensions.add("hta");
        disallowedExtensions.add("inf");
        disallowedExtensions.add("ins");
        disallowedExtensions.add("isp");
        disallowedExtensions.add("jse");
        disallowedExtensions.add("lnk");
        disallowedExtensions.add("mdb");
        disallowedExtensions.add("mde");
        disallowedExtensions.add("msc");
        disallowedExtensions.add("msi");
        disallowedExtensions.add("msp");
        disallowedExtensions.add("mst");
        disallowedExtensions.add("pcd");
        disallowedExtensions.add("pif");
        disallowedExtensions.add("reg");
        disallowedExtensions.add("scr");
        disallowedExtensions.add("sct");
        disallowedExtensions.add("shs");
        disallowedExtensions.add("url");
        disallowedExtensions.add("vbe");
        disallowedExtensions.add("vbs");
        disallowedExtensions.add("wsc");
        disallowedExtensions.add("wsf");
        disallowedExtensions.add("wsh");
        disallowedExtensions.add("shb");
        disallowedExtensions.add("js");
        disallowedExtensions.add("vb");
        disallowedExtensions.add("ws");
        disallowedExtensions.add("mdt");
        disallowedExtensions.add("mdw");
        disallowedExtensions.add("mdz");
        disallowedExtensions.add("shb");
        disallowedExtensions.add("scf");
        disallowedExtensions.add("pl");
        disallowedExtensions.add("pm");
        disallowedExtensions.add("dll");
    }

    
}
