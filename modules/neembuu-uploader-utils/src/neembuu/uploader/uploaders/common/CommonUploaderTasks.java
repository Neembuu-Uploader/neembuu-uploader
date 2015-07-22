package neembuu.uploader.uploaders.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import neembuu.uploader.api.SuccessfulUploadsListener;
import neembuu.uploader.versioning.ProgramVersionProvider;
import neembuu.uploader.api.UserLanguageCodeProvider;
import neembuu.uploader.api.queuemanager.StartNextUploadIfAnyCallback;
//import neembuu.uploader.NeembuuUploader;
//import neembuu.uploader.QueueManager;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.utils.NULogger;
//import neembuu.uploader.utils.NeembuuUploaderLanguages;
import neembuu.uploader.versioning.UserProvider;
//import neembuu.uploader.utils.NeembuuUploaderLanguages;
//import neembuu.uploader.versioning.UserImpl;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/** Noninstantiable class with static methods for common tasks for uploaders
 *
 * @author vigneshwaran
 * @author davidepastore
 */
public class CommonUploaderTasks {

    private static StartNextUploadIfAnyCallback snuiac;
    private static ProgramVersionProvider pvp;
    private static UserProvider userP;
    private static UserLanguageCodeProvider ulcp;
    private static final ArrayList<SuccessfulUploadsListener> listeners = new ArrayList<SuccessfulUploadsListener>();
    public static void init(StartNextUploadIfAnyCallback startNextUploadIfAnyCallback,
            ProgramVersionProvider programVersionProvider,UserProvider userProvider,
            UserLanguageCodeProvider ulcp,SuccessfulUploadsListener ... sul){
        if(CommonUploaderTasks.snuiac!=null){
            throw new IllegalStateException("already initiazlied");
        }
        CommonUploaderTasks.snuiac = startNextUploadIfAnyCallback;
        CommonUploaderTasks.pvp = programVersionProvider;
        CommonUploaderTasks.userP = userProvider;
        CommonUploaderTasks.ulcp = ulcp;
        for (SuccessfulUploadsListener successfulUploadsListener : sul) {
            listeners.add(successfulUploadsListener);
        }
    }
    
    /**
     * Non-instantiable
     */
    private CommonUploaderTasks() {
    }

    /**
     * Call this method at the end of successful uploading. 
     * Without this, NeembuuUploader will not start the next upload.
     * 
     * It will print the upload record to file, send statistics 
     * and starts next upload
     * @param up 
     */
    public synchronized static void uploadFinished(Uploader up) {
        writeRecentlyUploaded(up);
        sendStatsInAnotherThread(up);
        callListenersInAnotherThread(up);
        snuiac.startNextUploadIfAny();
        //QueueManager.getInstance().startNextUploadIfAny();
    }

    /**
     * Call this method if the uploading failed.
     * Without this, NeembuuUploader will not start the next upload.
     * 
     * It will send failure statistics 
     * and starts next upload
     * @param up 
     */
    public synchronized static void uploadFailed(Uploader up) {
        sendStatsInAnotherThread(up);
        snuiac.startNextUploadIfAny();
        //QueueManager.getInstance().startNextUploadIfAny();
    }

    /**
     * Same as uploadFailed but may add more in future
     * @param up 
     */
    public synchronized static void uploadStopped(Uploader up) {
        sendStatsInAnotherThread(up);
        snuiac.startNextUploadIfAny();
        //QueueManager.getInstance().startNextUploadIfAny();
    }

    /**
     * This private method writes recently uploaded files to a file on user's home folder.
     * @param up 
     */
    private static void writeRecentlyUploaded(Uploader up) {
        try {
            //Validate URL
            if (!up.getDownloadURL().equals(UploadStatus.NA.getLocaleSpecificString())) {
                new URL(up.getDownloadURL());
            }

            //Append to the file instead of overwriting.
            PrintWriter writer = new PrintWriter(new FileWriter(System.getProperty("user.home") + File.separator + "recent.log", true));
            writer.write(up.getDisplayFileName() + "<>" + up.getHost() + "<>" + up.getDownloadURL() + "<>" + up.getDeleteURL() + "\n");
            writer.close();
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "Error while writing recent.log\n{0}", ex);
        }
    }

    /**
     * This private method will send statistics to the server.
     * Download Links or Delete Links will not be sent for privacy reasons.
     * 
     * These data are used for analysis and cleared periodically to avoid exceeding quota.
     * 
     * @param up 
     */
    private static void sendStats(Uploader up) {
        try {
            String status = up.getStatus().getDefaultLocaleSpecificString();
            if (!status.startsWith("Upload")) {
                return;
            }

            String hostName = up.getHost();
            if(hostName.contains("|")){
                hostName = hostName.substring(hostName.indexOf("|"));
                hostName = "account " + hostName;
            }

            NULogger.getLogger().info("Sending statistics..");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("version", pvp.getVersionForProgam() ) );
            formparams.add(new BasicNameValuePair("filename", up.getFileName()));
            formparams.add(new BasicNameValuePair("size", up.getSize()));
            formparams.add(new BasicNameValuePair("host", hostName));
            formparams.add(new BasicNameValuePair("status", status));
            formparams.add(new BasicNameValuePair("os", System.getProperty("os.name")));
            formparams.add(new BasicNameValuePair("locale", ulcp.getUserLanguageCode()
                    /*NeembuuUploaderLanguages.getUserLanguageCode()*/));
            formparams.add(new BasicNameValuePair("uid", userP.getUserInstance().uidString()
                    /*UserImpl.I().uidString()*/));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            HttpPost httppost = new HttpPost("http://neembuuuploader.sourceforge.net/insert.php");
            httppost.setEntity(entity);
            HttpParams params = new BasicHttpParams();
            params.setParameter(
                    "http.useragent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            httppost.setParams(params);
            HttpClient httpclient = NUHttpClient.getHttpClient();
            EntityUtils.consume(httpclient.execute(httppost).getEntity());
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "Error while sending statistics\n{0}", ex);
        }
    }
    
    /**
     * Send the stats in another Thread, so the queue doesn't freeze.
     * @param uploader The uploader instance.
     */
    private static void sendStatsInAnotherThread(final Uploader uploader){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendStats(uploader);
            }
        });
        thread.start();
    }
    
    private static void callListenersInAnotherThread(final Uploader uploader){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (SuccessfulUploadsListener successfulUploadsListener : listeners) {
                    try{
                        successfulUploadsListener.success(uploader);
                    }catch(Exception a){
                        a.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }


    
    /**
     * Get the String representation of the bytes per second. 
     * @param bytes the number of bytes.
     * @return the String representation of the bytes per second. Like 50KB/s
     */
    public static String getSpeed(long bytes){
        return /*getSize(bytes)*/ GetSizeAsString.getSize(bytes) + "/s";
    }

    /**
     * Create a random string of <i>length</i> size.
     * @param length Size of the string.
     * @return A random string of <i>length</i> size.
     */
    public static String createRandomString(int length) {
	Random random = new Random();
	StringBuilder sb = new StringBuilder();
	while (sb.length() < length) {
            sb.append(Integer.toHexString(random.nextInt()));
	}
	return sb.toString();
    }
    
    /**
     * Read all the content from an InputStream instance.
     * @param inputStream the instance from which you want to read.
     * @return Read all the content from an inputStream instance.
     */
    public static String readAllFromInputStream(InputStream inputStream) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String result = "", line = "";
        while((line = reader.readLine()) != null){
            result += line;
        }
        return result;
    }

}
