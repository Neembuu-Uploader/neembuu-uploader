/* 
 * Copyright 2015 Shashank Tulsyan <shashaank at neembuu.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package neembuu.uploader.external;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import neembuu.rus.Rusila;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.versioning.ShowUpdateNotification;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/** This thread checks for new updates at every launch.
 *
 * @author vigneshwaran
 */
public class CheckMajorUpdate /*extends Thread*/ {
    private final String local_version_xml;
    private final ShowUpdateNotification showUpdateNotification;

    public CheckMajorUpdate(Path external,ShowUpdateNotification showUpdateNotification) {
        currentver = showUpdateNotification.pvp().getVersion();
        this.local_version_xml = Rusila.get(Rusila.create(external), "version.xml").s(
                "<version>"+currentver+ "</version>"
        ); this.showUpdateNotification = showUpdateNotification;
    }
        
    //variables to store current and available version
    private float availablever;
    private final float currentver;
    private long notificationdate;

    /**
     * 
     * @return whether the NeembuuUploader is uptodate or not. If not, launch Update Notification window..
     */
    public boolean isCurrentVersion() {
        //Get the version.xml and read the version value.
        
        try {
            String respxml = local_version_xml;
            availablever = getVersionFromXML(respxml);
            notificationdate = notificationDate(respxml);
            NULogger.getLogger().log(Level.INFO, "Available version: {0}", availablever);
            NULogger.getLogger().log(Level.INFO, "Notification date : {0}", new Date(notificationdate));

            NULogger.getLogger().log(Level.INFO, "Current version: {0}", currentver);
            NULogger.getLogger().log(Level.INFO, "Current date : {0}", new Date(System.currentTimeMillis()));

            //Compare both
            if (availablever > currentver) {
                return false;
            }
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "Exception while checking update\n{0}", ex);
        }
        return true;
    }
    
    /**
     * @deprecated version.xml is located in update.zip which is used to get list 
     * of latest plugins
     */
    @Deprecated 
    private static String getVersionXmlOnline()throws IOException{
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);
        HttpGet httpget = new HttpGet("http://neembuuuploader.sourceforge.net/version.xml");
        NULogger.getLogger().info("Checking for new version...");
        HttpResponse response = httpclient.execute(httpget);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * 
     * @param str
     * @return the value between <version> and </version> tags from the specified string.
     */
    public static float getVersionFromXML(String str) {
        float ver = 0;
        try {
            String start = "<version>";
            String end = "</version>";

            str = str.substring(str.indexOf(start) + start.length());

            str = str.substring(0, str.indexOf(end));
            ver = Float.parseFloat(str);
        } catch (Exception any) {
            NULogger.getLogger().severe(any.toString());
        }
        return ver;
    }
    
    /**
     * 
     * @param str
     * @return the value between <version> and </version> tags from the specified string.
     */
    public static long notificationDate(String str) {
        long time = 0;
        try {
            String start = "<notificationdate>";
            String end = "</notificationdate>";

            str = str.substring(str.indexOf(start) + start.length());

            str = str.substring(0, str.indexOf(end));
            time = Long.parseLong(str);
        } catch (Exception any) {
            NULogger.getLogger().severe(any.toString());
        }
        return time;
    }

    //@Override
    public void check() {
        if (!isCurrentVersion()) {
            NULogger.getLogger().info("New version found..");
            showUpdateNotification.showUpdate(availablever);
        }
        
        if(showNotification()){
            showUpdateNotification.showNotification(notificationdate);
        }
    }
    
    private boolean showNotification(){
        Date notificationDate = new Date(notificationdate);
        Date today  = new Date(System.currentTimeMillis());
        
        long diff = System.currentTimeMillis() - notificationdate;
        if(diff<0)diff = Long.MAX_VALUE;
        
        double diff_in_hrs = (diff*1d/(1000*60*60d));
        NULogger.getLogger().log(Level.INFO, "Time difference (in hours): {0}", 
                    diff_in_hrs );
        if(diff_in_hrs < 24){
            
            return true;
        }
        
        return false;
    }
}
