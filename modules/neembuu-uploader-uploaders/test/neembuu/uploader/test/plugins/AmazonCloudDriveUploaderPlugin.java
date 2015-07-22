/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.plugins;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
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
 * @author Vigneshwaran
 */
public class AmazonCloudDriveUploaderPlugin {

    private static URL u;
    private static HttpURLConnection uc;
    private static BufferedReader br;
    private static String appactiontoken, appaction;
    private static String authage, openid_ns, openid_ns_pape;
    private static String pageid, identity;
    private static String claimedid, mode, handle;
    private static String returnto;
    private static String location;
    private static String amzid;
    private static StringBuilder cookies;

    public static void main(String[] args) throws Exception {
       //{"percent":100,"process":"done","redirect":"\/uploadcompleteie\/MzIwMTMwMjkwNHxmM2NjMTJhODJmYTcyNmUyNjhhNjFmZDdjY2E1ZTcxNg=="};
        String s = "\\/uploadcompleteie\\/MzIwMTMwMjkwNHxmM2NjMTJhODJmYTcyNmUyNjhhNjFmZDdjY2E1ZTcxNg==\"";
        //\/uploadcompleteie\/
        System.out.println("ReplaceFirst : "+s.replaceFirst("\\\\/uploadcompleteie\\\\/", ""));
        System.out.println("Replace : "+s.replace("\\/uploadcompleteie\\/", ""));
        
        
    }

    private static void initialize() throws Exception {
        System.out.println("Getting upload url from amazon");
        u = new URL("https://www.amazon.com/clouddrive/");
        uc = (HttpURLConnection) u.openConnection();
        uc.setInstanceFollowRedirects(false);
        //        if (login) {
//            uc.setRequestProperty("Cookie", sessioncookie);
//        }
//       




        for (int i = 0;; i++) {
            String headerName = uc.getHeaderFieldKey(i);
            String headerValue = uc.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                // No more headers
                break;
            }
            if (headerName != null) {
                System.out.println(headerName + " : " + headerValue);
                if (headerName.equals("Location")) {
                    location = headerValue;
                }
                if (headerName.equals("x-amz-id-2")) {
                    amzid = headerValue;
                }
                // The header value contains the server's HTTP version
            } else {
                System.out.println(headerValue);
            }

        }


        System.out.println("location : " + location);
        System.out.println("ama id : " + amzid);

        cookies=new StringBuilder();
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-cookie")) {
            List<String> header = headerFields.get("Set-cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
//                System.out.println(tmp);
//                if (tmp.contains("z=")) {
//                    zcookie = tmp;
//                }
                cookies.append(tmp).append(";");

            }
        }


        uc.disconnect();

        u = new URL(location);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", cookies.toString()); 
        uc.setRequestProperty("x-amz-id-2", amzid);
        
        
        
         br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "", tmp;
        while ((tmp = br.readLine()) != null) {
//            System.out.println(tmp);
            k += tmp;
        }
        
        
        appactiontoken = parseResponse(k, "name=\"appActionToken\" value=\"", "\"");
        appaction = parseResponse(k, "name=\"appAction\" value=\"", "\"");
        authage = parseResponse(k, "name=\"openid.pape.max_auth_age\" value=\"", "\"");
        openid_ns = parseResponse(k, "name=\"openid.ns\" value=\"", "\"");
        openid_ns_pape = parseResponse(k, "name=\"openid.ns.pape\" value=\"", "\"");
        pageid = parseResponse(k, "name=\"pageId\" value=\"", "\"");
        identity = parseResponse(k, "name=\"openid.identity\" value=\"", "\"");
        claimedid = parseResponse(k, "name=\"openid.claimed_id\" value=\"", "\"");
        mode = parseResponse(k, "name=\"openid.mode\" value=\"", "\"");
        handle = parseResponse(k, "name=\"openid.assoc_handle\" value=\"", "\"");
        returnto = parseResponse(k, "name=\"openid.return_to\" value=\"", "\"");

        System.out.println(appactiontoken);
        System.out.println(appaction);
        System.out.println(authage);
        System.out.println(openid_ns);
        System.out.println(openid_ns_pape);
        System.out.println(pageid);
        System.out.println(identity);
        System.out.println(claimedid);
        System.out.println(mode);
        System.out.println(handle);
        System.out.println(returnto);


        for (int i = 0;; i++) {
            String headerName = uc.getHeaderFieldKey(i);
            String headerValue = uc.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                // No more headers
                break;
            }
            if (headerName != null) {
                System.out.println(headerName + " : " + headerValue);
                // The header value contains the server's HTTP version
            } else {
                System.out.println(headerValue);
            }

        }



        //        postURL = parseResponse(k, "\"upload_url\":\"", "\"");
        //        postURL = postURL.replaceAll("\\\\", "");
        //        System.out.println("Post URL : " + postURL);
    }

    public static String parseResponse(String response, String stringStart, String stringEnd) {

        response = response.substring(response.indexOf(stringStart));
        response = response.replace(stringStart, "");
        response = response.substring(0, response.indexOf(stringEnd));
        return response;
    }

    public static void loginAmazoncloudDrive() throws Exception {
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to amazon.com");
        HttpPost httppost = new HttpPost("https://www.amazon.com/ap/signin");
        httppost.setHeader("Cookie", cookies.toString());
        httppost.setHeader("x-amz-id-2", amzid);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();

        formparams.add(new BasicNameValuePair("appActionToken", appactiontoken));
        formparams.add(new BasicNameValuePair("appAction", appaction));
        formparams.add(new BasicNameValuePair("openid.pape.max_auth_age", authage));
        formparams.add(new BasicNameValuePair("openid.ns", openid_ns));
        formparams.add(new BasicNameValuePair("openid.ns.pape", openid_ns_pape));
        formparams.add(new BasicNameValuePair("pageId", pageid));
        formparams.add(new BasicNameValuePair("openid.identity", identity));
        formparams.add(new BasicNameValuePair("openid.claimed_id", claimedid));
        formparams.add(new BasicNameValuePair("openid.mode", mode));
        formparams.add(new BasicNameValuePair("openid.assoc_handle", handle));
        formparams.add(new BasicNameValuePair("openid.return_to", returnto));
        formparams.add(new BasicNameValuePair("email", "007007dinesh@gmail.com"));
        formparams.add(new BasicNameValuePair("create", "0"));
        formparams.add(new BasicNameValuePair("password", "*******************"));
        formparams.add(new BasicNameValuePair("x", "0"));
        formparams.add(new BasicNameValuePair("y", "0"));
        formparams.add(new BasicNameValuePair("metadata1", "z2852AgFvFLdGzUwuI%2Bx%2BDt2CtT%2BIx1RZDxHQl%2Bj%2FmijzBdMoQDpfeG9V4RskgUuq%2FrdkiqgoirJTTUZn9UjqAHIvZ0ifw3PD040242h9v0SCcivb1prXNDdqf5uZRdQMDcooVNvdr2%2FxIY9DrykC44oUU0jKzkpyV4Ebph1BIp47z1nwUQEIuGbWnFziWOhrOkrdH%2BAFKIGz%2B%2FgeMTiyjhFUtGPJYTbg%2BLQY7MSwAlzKowZFTXCU9j6gjDw%2FczgcLQ1Ng4hcYaMGnMi57nsl3lF2cluhkUp7bJXj0piUa9QUAFCC63ISgoRoa4wv33bHG6HIWu68q%2BOGcoOE94TSZuu4kwd8KRals4%2FRNw7IBTwoclqXKq9GKxfVeIkCegpZeKZlwUmlQzOYaVhq72fwqMg6k3tFpMw%2FKoQuRNXda1u8bov46d%2FkfGHeCoW0S%2BsKog2CcIsEP%2Bkx33ailjqcMEyC8yUgoeVEpcXBcsDTXkEOldv7veVc6tmBRKHufN7z%2FioHUWPvgXti7MRhIeicFEJFdB9ArlI8cbyn5TsRADIpLq6rk1M59clcObDqeq1%2F5u%2BFNe4JsptAShiO%2Bg%2Fw%2BSFWASuBILwFg0i%2BjoAeXFyQ3QbcvQuOA5G%2BBy9b4AWWhoHxfjYp5WsNUTyeA1UIVQmWn5hzUUfko9TmaoY%2BGJ5BRS0kHR4AWgH%2B3xTwVBDG6cK559O8Zdy6FymADx5XPwmPXG7iHW9JmrS3yriHIUV%2Bz4%2FcfOuW0Yovn6FVTpkn8WRrz5AOt3AnH4L9SdqW9gok9mdl3Nm7Bl67jml%2F%2F0mccIwfbeoug%3D%3D"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        System.out.println("response");
        Header[] allHeaders = httpresponse.getAllHeaders();
        for (int i = 0; i < allHeaders.length; i++) {
            System.out.println(allHeaders[i].getName() + " : " + allHeaders[i].getValue());
        }
        System.out.println(EntityUtils.toString(httpresponse.getEntity()));
//        System.exit(0);
        System.out.println("Getting cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            System.out.println(escookie.getName() + " : " + escookie.getValue());


        }

    }
}
