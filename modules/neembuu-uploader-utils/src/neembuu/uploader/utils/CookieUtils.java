/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.utils;

import java.util.List;
import java.util.logging.Level;
import neembuu.uploader.uploaders.common.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

/**
 * Cookie utils.
 * @author davidepastore
 */
public class CookieUtils {
    
    /**
     * Return all the cookie from this response.
     */
    public static String saveCookie(HttpResponse response){
        String cookie = "";
        String value;
        Header[] headers = response.getAllHeaders();
        for(int i = 0; i < headers.length; i++){
            if("Set-Cookie".equals(headers[i].getName())){
                value = StringUtils.stringUntilString(headers[i].getValue(), ";");
                cookie += value;
            }
        }
        
        return cookie;
    }
    
    /**
     * Change the cookie in this context. All cookies will be available for the subdomain.
     * @param httpContext HttpContext in which the cookies store.
     * @return the new HttpContext.
     */
    public static HttpContext cookiesToSubdomain(HttpContext httpContext){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        CookieStore newCookieStore = new BasicCookieStore();
        List<Cookie> cookies = cookieStore.getCookies();
        BasicClientCookie cookie;
        for(int i = 0; i < cookies.size(); i++){
            cookie = (BasicClientCookie) cookies.get(i);
            NULogger.getLogger().log(Level.INFO, "Domain: {0},  Value:{1}", new Object[]{cookie.getDomain(), cookie.getValue()});
            String domain = cookie.getDomain();
            domain = domain.replace("www.", ".");
            cookie.setDomain(domain);
            newCookieStore.addCookie(cookie);
        }
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        return httpContext;
    }
    
    /**
     * Get a cookie from the name.
     * @param httpContext HttpContext in which the cookies store.
     * @param name the name of the cookie.
     * @return the Cookie object.
     */
    public static Cookie getCookie(HttpContext httpContext, String name){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        String cookieName;
        Cookie cookie;
        for(int i = 0; i < cookies.size(); i++){
            cookie = cookies.get(i);
            cookieName = cookie.getName();
            if(cookieName.contains(name)){
                return cookie;
            }
        }
        return null;
    }
    
    /**
     * Get a cookie value from the name.
     * @param httpContext HttpContext in which the cookies store.
     * @param name the name of the cookie.
     * @return the value of the cookie.
     */
    public static String getCookieValue(HttpContext httpContext, String name){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        String cookieName;
        for(int i = 0; i < cookies.size(); i++){
            cookieName = cookies.get(i).getName();
            if(cookieName.contains(name)){
                return cookies.get(i).getValue();
            }
        }
        return null;
    }
    
    /**
     * Get a cookie value from the name. If the cookie starts with the name, you'll get this value.
     * @param httpContext HttpContext in which the cookies store
     * @param name the first part of the name of the cookie.
     * @return the String cookie.
     */
    public static String getCookieStartsWithValue(HttpContext httpContext, String name){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        String cookieName;
        for(int i = 0; i < cookies.size(); i++){
            cookieName = cookies.get(i).getName();
            if(cookieName.startsWith(name)){
                return cookies.get(i).getValue();
            }
        }
        return null;
    }
    
    /**
     * Return the name=value of the cookie.
     * @param httpContext HttpContext in which the cookies store
     * @param name the name of the cookie.
     * @return the name and the value of the cookie.
     */
    public static String getCookieNameValue(HttpContext httpContext, String name){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        Cookie cookie;
        String cookieName;
        for(int i = 0; i < cookies.size(); i++){
            cookie = cookies.get(i);
            cookieName = cookie.getName();
            if(cookieName.contains(name)){
                return cookieName + "=" + cookie.getValue();
            }
        }
        return null;
    }
    
    /**
     * Print all the cookie in the context (for debug).
     * @param httpContext HttpContext in which the cookies store
     * 
     */
    public static void printCookie(HttpContext httpContext){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        for(int i = 0; i < cookies.size(); i++){
            NULogger.getLogger().log(Level.INFO, "{0}: {1}",  new Object[]{cookies.get(i).getName(), cookies.get(i).getValue()});
        }
    }
    
    
    /**
     * There is a cookie with this name?
     * @param httpContext the context.
     * @param name the name of the cookie.
     * @return boolean value.
     */
    public static boolean existCookie(HttpContext httpContext, String name){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        String cookieName;
        for(int i = 0; i < cookies.size(); i++){
            cookieName = cookies.get(i).getName();
            if(cookieName.contains(name)){
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Get a cookie value from the exact name.
     * @param httpContext HttpContext in which the cookies store.
     * @param name the name of the cookie.
     * @return the value of the cookie.
     */
    public static String getCookieValueWithExactName(HttpContext httpContext, String name){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        String cookieName;
        for(int i = 0; i < cookies.size(); i++){
            cookieName = cookies.get(i).getName();
            if(cookieName.equals(name)){
                return cookies.get(i).getValue();
            }
        }
        return null;
    }
    
    /**
     * There is a cookie that starts with this name?
     * @param httpContext the context.
     * @param name the first part of the name of the cookie.
     * @return boolean value.
     */
    public static boolean existCookieStartWithValue(HttpContext httpContext, String name){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        String cookieName;
        for(int i = 0; i < cookies.size(); i++){
            cookieName = cookies.get(i).getName();
            if(cookieName.startsWith(name)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get a string with all the cookies (it calls getAllCookies(HttpContext httpContext, String separator = ";")).
     * @param httpContext the context
     * @return a new string with all the cookies.
     */
    public static String getAllCookies(HttpContext httpContext){
        return getAllCookies(httpContext, ";");
    }
    
    /**
     * Get a string with all the cookies.
     * @param httpContext the context.
     * @param separator the separator string between cookies.
     * @return a new string with all the cookies.
     */
    public static String getAllCookies(HttpContext httpContext, String separator){
        CookieStore cookieStore = (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        List<Cookie> cookies = cookieStore.getCookies();
        String cookieName;
        String cookieValue;
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < cookies.size(); i++){
            cookieName = cookies.get(i).getName();
            cookieValue = cookies.get(i).getValue();
            result
                .append(cookieName)
                .append("=")
                .append(cookieValue)
                .append(separator);
        }
        return result.toString();
    }
    
}
