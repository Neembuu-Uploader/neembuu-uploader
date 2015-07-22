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
package neembuu.uploader.captcha;

import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import org.apache.http.protocol.HttpContext;

/**
 * This class allows you to search within a string, the k challenge, the c challenge, 
 * the captcha image and displays it to the user to obtain in this way the input.
 * @author davidepastore
 */
public class CaptchaImpl implements Captcha{
    
    private String string;
    private String formTitle;
    
    /* Google */
    private String googleRegK = "http://www\\.google\\.com/recaptcha/api/challenge\\?k=";
    private String googleRegC = "challenge.*?:.*?'(.*?)',";
    private String googleURL = "http://www.google.com/recaptcha/api/image?c=";
    
    /* Recaptcha */
    private String recaptchaRegK = "http://www\\.api\\.recaptcha\\.net/challenge\\?k=";
    
    private Pattern regex;
    private Matcher regexMatcher;
    private int start;
    private int end;
    private String kCaptchaUrl;
    private String cCaptchaUrl;
    private HttpContext httpContext;
    private URL imageURL;
    
    private final CaptchaForm captchaForm;
    
    /**
     * Every how many milliseconds to check if the form of captcha has been closed.
     */
    private final static long WAIT_TIME = 1000;
    
    /**
     * Basic constructor without setting string. If you already have the K challenge link,
     * you can use this and then set the formTitle with <b>setFormTitle()</b>.
     */
    public CaptchaImpl(){
        this(null);
    }
    
    /**
     * Constructor
     * @param string The string into which you want to find the captcha urls.
     */
    public CaptchaImpl(String string){
        this(string,null);
    }
    
    /**
     * Constructor
     * @param string The string into which you want to find the captcha urls.
     * @param formTitle The form title.
     */
    public CaptchaImpl(String string, String formTitle){
        this.string = string;
        this.formTitle = formTitle;
        captchaForm = new CaptchaForm();
    }
    
    /**
     * Find the K Challenge URL.
     * @return The K Challenge URL as a String.
     * @throws Exception 
     */
    @Override
    public String findKCaptchaUrl() throws Exception{
        kCaptchaUrl = null;
        
        //FileUtils.saveInFile("Recaptcha.html", string);
        
        //For google
        regex = Pattern.compile(googleRegK);
        regexMatcher = regex.matcher(string);
        
        if(regexMatcher.find()){
            //NULogger.getLogger().info("Google K recaptcha found!");
            start = regexMatcher.start();
            string = string.substring(start);
            end = string.indexOf("\"");
            kCaptchaUrl = string.substring(0, end);
            NULogger.getLogger().log(Level.INFO, "kCaptchaUrl: {0}", kCaptchaUrl);
            return kCaptchaUrl;
        }
        
        //For recaptcha
        regex = Pattern.compile(recaptchaRegK);
        regexMatcher = regex.matcher(string);
        
        if(regexMatcher.find()){
            //NULogger.getLogger().info("Recaptcha K recaptcha found!");
            start = regexMatcher.start();
            string = string.substring(start);
            end = string.indexOf("\"");
            kCaptchaUrl = string.substring(0, end);
            NULogger.getLogger().log(Level.INFO, "kCaptchaUrl: {0}", kCaptchaUrl);
            return kCaptchaUrl;
        }
        
        return null;
    }

    /**
     * Find the C Challenge URL.
     * @return The C Challenge URL as a String.
     * @throws Exception 
     */
    @Override
    public String findCCaptchaUrl() throws Exception{
        return findCCaptchaUrlFromK(kCaptchaUrl);
    }
    
    /**
     * Find the C Challenge URL from the given K Challenge URL.
     * @param kCaptchaUrl The K Challenge URL as a String.
     * @return The C Challenge URL as a String..
     * @throws IOException 
     */
    @Override
    public String findCCaptchaUrlFromK(String kCaptchaUrl) throws IOException, Exception{
        cCaptchaUrl = null;
        
        if(kCaptchaUrl == null){
            return null;
        }
        
        String body =  NUHttpClientUtils.getData(kCaptchaUrl, httpContext);
        
        //CommonUploaderTasks.saveInFile("Recaptcha.html", body);
        
        regex = Pattern.compile(googleRegC);
        regexMatcher = regex.matcher(body);
        
        if(regexMatcher.find()){
            NULogger.getLogger().info("Google C recaptcha found!");
            cCaptchaUrl = body.substring(regexMatcher.start(), regexMatcher.end());
            cCaptchaUrl = StringUtils.stringBetweenTwoStrings(cCaptchaUrl, "'", "'");
            NULogger.getLogger().log(Level.INFO, "cCaptchaUrl: {0}", cCaptchaUrl);
            return cCaptchaUrl;
        }
        
        NULogger.getLogger().log(Level.INFO, "kCaptchaUrl: {0}", kCaptchaUrl);
        
        return cCaptchaUrl;
    }
    
    /**
     * Find the captcha image URL.
     * @return The captcha image URL as an URL.
     * @throws IOException
     * @throws Exception 
     */
    @Override
    public URL findCaptchaImageURL() throws IOException, Exception{
        imageURL = new URL(googleURL+cCaptchaUrl);
        return imageURL;
    }

    /**
     * Return the captcha string (entered by user).
     * @return The captcha string (entered by user).
     * @throws InterruptedException 
     */
    @Override
    public String getCaptchaString() throws InterruptedException {
        //CaptchaForm captchaForm;
        //captchaForm = new CaptchaForm(imageURL, formTitle, httpContext);
        captchaForm.setImageURL(imageURL);
        captchaForm.setTitle(formTitle);
        captchaForm.setHttpContext(httpContext);
        
        captchaForm.setVisible(true);
        NULogger.getLogger().info("Captcha Form opened.");
        
        //Is this the better method?
        while(!captchaForm.isClosing){
            Thread.sleep(WAIT_TIME);
        }
        NULogger.getLogger().log(Level.INFO, "Captcha Form closed. I read: {0}", captchaForm.captchaString);
        return captchaForm.captchaString;
    }
    
    /**
     * Get C Captcha Url.
     * @return The C Captcha Url.
     */
    @Override
    public String getCCaptchaUrl(){
        return cCaptchaUrl;
    }
    
    /**
     * Set the title of the form.
     * @param title The title of the form.
     */
    @Override
    public void setFormTitle(String title){
        this.formTitle = title;
    }
    
    /**
     * Set the image URL.
     * @param imageUrl The image URL (URL type).
     */
    @Override
    public void setImageURL(URL imageUrl){
        this.imageURL = imageUrl;
}
    
    /**
     * Set the image URL.
     * @param imageUrl The image URL (String type).
     */
    @Override
    public void setImageURL(String imageUrl) throws MalformedURLException{
        this.setImageURL(new URL(imageUrl));
    }

    /**
     * Set the HttpContext
     * @param httpContext 
     */
    @Override
    public void setHttpContext(HttpContext httpContext) {
        this.httpContext = httpContext;
    }

    @Override
    public void destroy() {
        captchaForm.destory();
    }

}
