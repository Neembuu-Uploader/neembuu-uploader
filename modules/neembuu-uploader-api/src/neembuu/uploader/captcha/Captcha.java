package neembuu.uploader.captcha;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author Shashank
 */
public interface Captcha {

    /**
     * Find the C Challenge URL.
     * @return The C Challenge URL as a String.
     * @throws Exception
     */
    String findCCaptchaUrl() throws Exception;

    /**
     * Find the C Challenge URL from the given K Challenge URL.
     * @param kCaptchaUrl The K Challenge URL as a String.
     * @return The C Challenge URL as a String..
     * @throws IOException
     */
    String findCCaptchaUrlFromK(String kCaptchaUrl) throws IOException, Exception;

    /**
     * Find the captcha image URL.
     * @return The captcha image URL as an URL.
     * @throws IOException
     * @throws Exception
     */
    URL findCaptchaImageURL() throws IOException, Exception;

    /**
     * Find the K Challenge URL.
     * @return The K Challenge URL as a String.
     * @throws Exception
     */
    String findKCaptchaUrl() throws Exception;

    /**
     * Get C Captcha Url.
     * @return The C Captcha Url.
     */
    String getCCaptchaUrl();

    /**
     * Return the captcha string (entered by user).
     * @return The captcha string (entered by user).
     * @throws InterruptedException
     */
    String getCaptchaString() throws InterruptedException;

    /**
     * Set the title of the form.
     * @param title The title of the form.
     */
    void setFormTitle(String title);

    /**
     * Set the HttpContext
     * @param httpContext
     */
    void setHttpContext(HttpContext httpContext);

    /**
     * Set the image URL.
     * @param imageUrl The image URL (URL type).
     */
    void setImageURL(URL imageUrl);

    /**
     * Set the image URL.
     * @param imageUrl The image URL (String type).
     */
    void setImageURL(String imageUrl) throws MalformedURLException;
    
    void destroy();
    
}
