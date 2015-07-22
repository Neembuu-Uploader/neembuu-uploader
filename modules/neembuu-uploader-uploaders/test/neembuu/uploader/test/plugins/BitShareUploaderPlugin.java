/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class BitShareUploaderPlugin {
    
    public static void main(String[] args) throws Exception {
        loginBitShare();
    }
    
    public static void loginBitShare() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);
        
        System.out.println("Trying to log in to bitshare.com");
        HttpPost httppost = new HttpPost("http://bitshare.com/login.html");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("user", "007007dinesh"));
        formparams.add(new BasicNameValuePair("password", ""));
        formparams.add(new BasicNameValuePair("submit", "Login"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            System.out.println(escookie.getName()+" = "+escookie.getValue());
        }
        
        System.out.println(EntityUtils.toString(httpresponse.getEntity()));
        
    }
}
