package neembuu.uploader.test.plugins;

import neembuu.uploader.uploaders.api._4shared.ApiException;
import neembuu.uploader.uploaders.api._4shared.DesktopAppJax2Service;
import neembuu.uploader.uploaders.api._4shared.DesktopAppJax2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author Dinesh
 */
public class FourSharedUploaderPlugin {

    static String login = "007007dinesh";
    static String password = "pwd";
    static String file = "/home/vigneshwaran/VIGNESH/naruto.txt";
    private static String downloadlink;

    private static void upload(String login, String password, String file) throws ApiException {

        System.out.println("Trying to login to 4shared........");
        File f = new File(file);
        if (!f.exists() || !f.canRead() || f.isDirectory()) {
            System.out.println("File does not exist, unreadable or not a file");
            return;
        }

        DesktopAppJax2 da = new DesktopAppJax2Service().getDesktopAppJax2Port();
        String loginRes = da.login(login, password);
        if (!loginRes.isEmpty()) {
            System.out.println("Login failed: " + loginRes);
            return;
        }

        if (!da.hasRightUpload()) {
            System.out.println("Uploading is temporarily disabled");
            return;
        }

        System.out.println("4shared Login successful :)");
        long newFileId = da.uploadStartFile(login, password, -1, f.getName(), f.length());
        System.out.println("File id : " + newFileId);
        String sessionKey = da.createUploadSessionKey(login, password, -1);
        long dcId = da.getNewFileDataCenter(login, password);
        String url = da.getUploadFormUrl((int) dcId, sessionKey);

        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            MultipartEntity me = new MultipartEntity();
            StringBody rfid = new StringBody("" + newFileId);
            StringBody rfb = new StringBody("" + 0);
            InputStreamBody isb = new InputStreamBody(new BufferedInputStream(new FileInputStream(f)), "FilePart");
            me.addPart("resumableFileId", rfid);
            me.addPart("resumableFirstByte", rfb);
            me.addPart("FilePart", isb);

            post.setEntity(me);
            HttpResponse resp = client.execute(post);
            HttpEntity resEnt = resp.getEntity();

            String res = da.uploadFinishFile(login, password, newFileId, DigestUtils.md5Hex(new FileInputStream(f)));
            if (res.isEmpty()) {
                System.out.println("File uploaded.");
                downloadlink = da.getFileDownloadLink(login, password, newFileId);
                System.out.println("Download link : " + downloadlink);
            } else {
                System.out.println("Upload failed: " + res);
            }
        } catch (Exception ex) {
            System.out.println("Upload failed: " + ex.getMessage());
        }

    }

    public static void main(String[] args) {


        try {
            upload(login, password, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showUsage() {
        System.out.println("Usage: upload login password fileToUpload");
    }
}
