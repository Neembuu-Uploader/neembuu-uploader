/* 
 * Copyright (C) 2015 Shashank Tulsyan <shashaank at neembuu.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package neembuu.uploader.versioning;

import java.util.logging.Level;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.settings.Application;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Shashank Tulsyan
 */
public class CheckUser {

    public static void getCanCustomizeNormalizing(UserSetPriv usp) {
        boolean canCustomizeNormalizing = true;
        String normalization = ".neembuu";
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        HttpClient httpclient = NUHttpClient.getHttpClient();
        HttpGet httpget = new HttpGet("http://neembuu.com/uploader/api/user.xml?userid="+UserImpl.I().uid());
        NULogger.getLogger().info("Checking for user priviledges ...");
        try {
            HttpResponse response = httpclient.execute(httpget);
            String respxml = EntityUtils.toString(response.getEntity());
            canCustomizeNormalizing = getCanCustomizeNormalizingFromXml(respxml);
            normalization = getNormalization(respxml);
            NULogger.getLogger().log(Level.INFO, "CanCustomizeNormalizing: {0}", canCustomizeNormalizing);
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "Exception while checking update\n{0}", ex);
        }
        usp.setCanCustomizeNormalizing(canCustomizeNormalizing);
        usp.setNormalization(normalization);
    }
    
    private  static boolean getCanCustomizeNormalizingFromXml(String str) {
        boolean canCustomizeNormalizing = true;
        try {
            String start = "<canCustomizeNormalizing>";
            String end = "</canCustomizeNormalizing>";

            str = str.substring(str.indexOf(start) + start.length());

            str = str.substring(0, str.indexOf(end));
            canCustomizeNormalizing = findBooleanValue(str);
        } catch (Exception any) {
            NULogger.getLogger().severe(any.toString());
        }
        return canCustomizeNormalizing;
    }
    
    private static String getNormalization(String str) {
        String normalization = ".neembuu";
        try {
            String start = "<normalization>";
            String end = "</normalization>";

            str = str.substring(str.indexOf(start) + start.length());

            str = str.substring(0, str.indexOf(end));
            normalization = str;
        } catch (Exception any) {
            NULogger.getLogger().severe(any.toString());
        }
        return normalization;
    }
    
    private static boolean findBooleanValue(String name){
        return ((name == null) || !name.equalsIgnoreCase("false"));
    }
    
    public static void main(String[] args) throws Exception {
        UserImpl.init(Application.getUserId());
        for (int i = 0; i < 10000; i++) {
            getCanCustomizeNormalizing( new UserSetPriv() {

                @Override public void setCanCustomizeNormalizing(boolean canCustomizeNormalizing) {
                    System.out.println("canCustomize = "+canCustomizeNormalizing);
                }

                @Override public void setNormalization(String normalization) {
                    System.out.println("nomalization = "+normalization);
                }
            });
            Thread.sleep(1000);
        }
    }
}
