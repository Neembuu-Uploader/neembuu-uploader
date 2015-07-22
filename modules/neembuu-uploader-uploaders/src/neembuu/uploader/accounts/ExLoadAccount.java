/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.accounts.NUInvalidLoginException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import neembuu.uploader.utils.CookieUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *
 * @author Paralytic
 */
public class ExLoadAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    
    Map<Integer, Integer> assoc = new HashMap<Integer, Integer>();
    private String captcha = "";
    private String rand = "";

    public ExLoadAccount() {
        KEY_USERNAME = "exloadusername";
        KEY_PASSWORD = "exloadpassword";
        HOSTNAME = "Ex-Load.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to Ex-Load.com");
            httpPost = new NUHttpPost("http://ex-load.com/");
            httpPost.setHeader("Referer", "http://ex-load.com/login.html");
            httpPost.setHeader("Host", "ex-load.com");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0");
            httpPost.setHeader("DNT", "1");
            
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("code", captcha));
            formparams.add(new BasicNameValuePair("op", "login"));
            formparams.add(new BasicNameValuePair("login", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("rand", rand));
            formparams.add(new BasicNameValuePair("redirect", "http://ex-load.com/"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            responseString = EntityUtils.toString(httpResponse.getEntity());
            if (responseString.contains("Wrong captcha code")) {
                NULogger.getLogger().info("** Ex-Load.com ** => Server reports the captcha as incorrect");
                throw new Exception("Server reports incorrect captcha");
            }

            if (!CookieUtils.getCookieValue(httpContext, "xfss").isEmpty() && !CookieUtils.getCookieValue(httpContext, "login").isEmpty()) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                NULogger.getLogger().info("Ex-Load.com login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("ExLoadAccount.html", responseString);
                Document doc = Jsoup.parse(responseString);
                String error = doc.select(".err").first().text();
                
                if("Incorrect Login or Password".equals(error)){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }

                //Generic exception
                throw new Exception("Login error: " + error);
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            accountUIShow().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e});
            showWarningMessage( Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }

    private void initialize() throws Exception {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        NULogger.getLogger().info("** Ex-Load.com ** => Attempting to log-out before logging in.");
        responseString = NUHttpClientUtils.getData("http://ex-load.com/?op=logout", httpContext);
        Thread.sleep(2000);
        NULogger.getLogger().info("** Ex-Load.com ** => Retrieving login page");
        responseString = NUHttpClientUtils.getData("http://ex-load.com/login.html", httpContext);
        doc = Jsoup.parse(responseString);
        captcha = captchaSolver();
        NULogger.getLogger().info("** Ex-Load.com ** => FINAL captcha value: ["+captcha+"]");
        rand = doc.select("input[name=rand]").attr("value");
        Thread.sleep(2000);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

    private String captchaSolver(){
        NULogger.getLogger().info("** Ex-Load.com ** => Solving the captcha ...");
        int i = 0;
        String pd_lft = "", capt_digit = "", captcha_code = "";
        for (i=0; i<4 ; i++) {
            pd_lft = doc.select("input[name=code]").first()
                    .parent().previousElementSibling()
                    .select("div").select("span").eq(i).toString();
            pd_lft = StringUtils.stringBetweenTwoStrings(pd_lft, "padding-left:", "px;");
            
            capt_digit = doc.select("input[name=code]").first()
                    .parent().previousElementSibling()
                    .select("div").select("span").eq(i).text();
            assoc.put(Integer.parseInt(pd_lft), Integer.parseInt(capt_digit));
        }
        NULogger.getLogger().info("** Ex-Load.com ** => Appeared sequence "+assoc.toString());
        Map<Integer, Integer> treeMap = new TreeMap<Integer, Integer>(assoc);
        NULogger.getLogger().info("** Ex-Load.com ** => REAL sequence "+treeMap.toString());
        
        //iterating over values only
        for (Integer value : treeMap.values()) {
            captcha_code += value.toString();
        }
        return captcha_code;
    }
}
