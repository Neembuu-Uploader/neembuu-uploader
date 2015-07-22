/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 *
 * @author dinesh
 */
public class FileSonicAccount extends AbstractAccount {

    
    private static String sessioncookie = "", mailcookie = "", namecookie = "", affiliatecookie = "";
    
    private URL u;
    private HttpURLConnection uc;
//    private static BufferedReader br;
    private static String rolecookie = "", langcookie = "";
    private static String filesoniclink = "";
//    private BufferedReader br;
//    private String tmp;
//    private static String fsdomain;

    public FileSonicAccount() {
        KEY_USERNAME = "fsncusername";
        KEY_PASSWORD = "fsncpassword";
        HOSTNAME = "FileSonic.com";
    }
    
    

    public static String getFsdomain() {
        return filesoniclink;
    }


    public static String getAffiliatecookie() {
        return affiliatecookie;
    }

    public static String getLangcookie() {
        return langcookie;
    }

    public static String getMailcookie() {
        return mailcookie;
    }
//
//    public static String getMsgcheckcookie() {
//        return msgcheckcookie;
//    }
//
//    public static String getMsgreadcookie() {
//        return msgreadcookie;
//    }

    public static String getRolecookie() {
        return rolecookie;
    }

    public static String getNamecookie() {
        return namecookie;
    }

    public static String getSessioncookie() {
        return sessioncookie;
    }

    @Override
    public void login() {
        try {

            loginFileSonic();
        } catch (Exception ex) {
            Logger.getLogger(FileSonicAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disableLogin() {
        loginsuccessful = false;
        affiliatecookie = "";
        langcookie = "";
        mailcookie = "";

        rolecookie = "";
        namecookie = "";
        sessioncookie = "";
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    

    public void loginFileSonic() throws Exception {

        loginsuccessful = false;


//        u = new URL("http://api.filesonic.com/utility?method=getFilesonicDomainForCurrentIp");
//        br = new BufferedReader(new InputStreamReader(u.openStream()));
//        String k = "";
//        while ((tmp = br.readLine()) != null) {
//            k += tmp;
//        }
//        fsdomain = CommonUploaderTasks.parseResponse(k, "\"response\":\"", "\"");
        u = new URL("http://www.filesonic.com/");
        uc = (HttpURLConnection) u.openConnection();
        uc.setInstanceFollowRedirects(false);
        filesoniclink = uc.getHeaderField("Location");
        NULogger.getLogger().info(filesoniclink);
        NULogger.getLogger().log(Level.INFO, "FileSonic Domain: {0}", filesoniclink);
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        NULogger.getLogger().info("Trying to log in to FileSonic");
        HttpPost httppost = new HttpPost(filesoniclink + "/user/login");
        httppost.setHeader("Referer", "http://www.filesonic.com/");
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        formparams.add(new BasicNameValuePair("redirect", "/"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);

        NULogger.getLogger().info("Getting filesonic cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("PHPSESSID")) {
                sessioncookie = "PHPSESSID=" + escookie.getValue();
                NULogger.getLogger().info(sessioncookie);
            }
            if (escookie.getName().equalsIgnoreCase("email")) {
                mailcookie = "email=" + escookie.getValue();
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info(mailcookie);
            }
            if (escookie.getName().equalsIgnoreCase("nickname")) {
                namecookie = "nickname=" + escookie.getValue();
                NULogger.getLogger().info(namecookie);
            }
            if (escookie.getName().equalsIgnoreCase("isAffiliate")) {
                affiliatecookie = "isAffiliate=" + escookie.getValue();
                NULogger.getLogger().info(affiliatecookie);
            }
            if (escookie.getName().equalsIgnoreCase("role")) {
                rolecookie = "role=" + escookie.getValue();
                NULogger.getLogger().info(rolecookie);
            }

        }
        if (loginsuccessful) {
            NULogger.getLogger().info("FileSonic Login Success");
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            
        } else {
            NULogger.getLogger().info("FileSonic Login failed");
            loginsuccessful = false;
            username = "";
            password = "";
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }

    }

//    public void getMessagingCookies() throws Exception {
//        u = new URL("http://www" + fsdomain + "/");
//        uc = (HttpURLConnection) u.openConnection();
//        uc.setRequestProperty("Cookie", langcookie + ";" + sessioncookie + ";" + mailcookie + ";" + namecookie + ";" + rolecookie);
//        Map<String, List<String>> headerFields = uc.getHeaderFields();
//        if (headerFields.containsKey("Set-Cookie")) {
//            List<String> header = headerFields.get("Set-Cookie");
//            for (int i = 0; i < header.size(); i++) {
//                String tmp = header.get(i);
//                if (tmp.contains("messagingUnread")) {
//                    msgreadcookie = tmp;
//                    msgreadcookie = msgreadcookie.substring(0, msgreadcookie.indexOf(";"));
//                }
//                if (tmp.contains("messagingUnreadLastcheck")) {
//                    msgcheckcookie = tmp;
//                    msgcheckcookie = msgcheckcookie.substring(0, msgcheckcookie.indexOf(";"));
//                }
//            }
//            NULogger.getLogger().log(Level.INFO, "msgreadcookie : {0}", msgreadcookie);
//            NULogger.getLogger().log(Level.INFO, "msgcheckcookie : {0}", msgcheckcookie);
//        }
//
//    }
}
