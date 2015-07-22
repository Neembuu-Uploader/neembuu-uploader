package neembuu.uploader.accounts;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.SSLContext;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.utils.NULogger;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author elmoyak
 */
public class UpdownBzAccount extends AbstractAccount {

    private final HttpClient httpclient = NUHttpClient.getHttpClient();

    public static final long LOGIN_INTERVAL_SEC = 20 * 60;
    private long lastLogin = 0;
    private String sessionId = "";
    private long expiresAt = 0;
    private final String userAgent;

    public UpdownBzAccount() {
        KEY_USERNAME = "udwnbzUsername";
        KEY_PASSWORD = "udwnbzPassword";
        HOSTNAME = "Updown.bz";

        userAgent = "NeembuuUploader-" + ManagementFactory.getRuntimeMXBean().getName().split("@", 2)[0];

        setupSsl();
    }

    @Override
    public void disableLogin() {
        resetLogin();
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getSessionId() {
        login();
        return sessionId;
    }

    public long getExpiresAt() {
        login();
        return expiresAt;
    }

    private String computePassword(final String username, final String password) {
        if (username == null || password == null) {
            return null;
        }
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update((username + password).getBytes("UTF-8"));
            final byte[] salt = digest.digest();

            final int iterations = 120;
            final int bits = 512;

            byte[] bpass = password.getBytes("UTF-8");
            char[] cpass = new char[bpass.length];
            for (int i = 0; i < cpass.length; i++) {
                cpass[i] = (char) (bpass[i] & 0xFF);
            }

            final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            final KeySpec keySpec = new PBEKeySpec(cpass, salt, iterations, bits);
            final SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            final byte[] key = secretKey.getEncoded();

            result = Base64.encodeBase64String(key);
        } catch (UnsupportedEncodingException e) {
            NULogger.getLogger().log(Level.SEVERE, null, e);
        } catch (NoSuchAlgorithmException e) {
            NULogger.getLogger().log(Level.SEVERE, null, e);
        } catch (InvalidKeySpecException e) {
            NULogger.getLogger().log(Level.SEVERE, null, e);
        }
        return result;
    }

    @Override
    public void login() {
        NULogger.getLogger().info("Updown.bz -> Start login to service");
        boolean timeIsUp = System.currentTimeMillis() - lastLogin > LOGIN_INTERVAL_SEC * 1000;
        if (sessionId == null || sessionId.length() <= 0 || timeIsUp) {
            NULogger.getLogger().info("Updown.bz -> Renew login session");

            lastLogin = System.currentTimeMillis();
            resetLogin();

            try {
                String query = "{\"i\":\"" + CommonUploaderTasks.createRandomString(10) + "\",\"m\":\"auth\",\"a\":\"getsid\",\"d\":{\"u\":\"" + getUsername() + "\",\"p\":\"" + computePassword(getUsername(), getPassword()) + "\"}}";
                NULogger.getLogger().log(Level.INFO,"Updown.bz -> Send login call - Query: {0}" , query);
                NUHttpPost httpPost = new NUHttpPost("https://api.updown.bz");
                httpPost.setEntity(new StringEntity(query, ContentType.APPLICATION_JSON));
                httpPost.setHeader("User-Agent", userAgent);
                httpContext = new BasicHttpContext();
                HttpResponse httpResponse = httpclient.execute(httpPost, httpContext);
                String responseString = EntityUtils.toString(httpResponse.getEntity());

                NULogger.getLogger().log(Level.INFO,"Updown.bz -> Parse call''s response - Response: {0}" , responseString);
                JSONObject response = new JSONObject(responseString);
                long code = response.getLong("c");
                if (code != 1) {
                    throw new Exception("Updown.bz -> Login failed - Response code: " + code);
                }
                sessionId = response.getJSONObject("d").getString("s");
                if (sessionId.length() <= 0) {
                    throw new Exception("Updown.bz -> Login failed - Invalid session token: " + sessionId);
                }
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();

                expiresAt = response.getJSONObject("d").getLong("e");
                premium = expiresAt * 1000 > System.currentTimeMillis();

                NULogger.getLogger().log(Level.INFO, "Updown.bz -> User''s premium account expires at {0}", expiresAt);
                if (premium) {
                    NULogger.getLogger().info("Updown.bz -> User is a premium member");
                } else {
                    NULogger.getLogger().info("Updown.bz -> User is registered user");
                }
            } catch (Exception e) {
                resetLogin();
                NULogger.getLogger().log(Level.SEVERE, "Updown.bz -> Login exception: {0}", e);
                showWarningMessage( Translation.T().loginerror(), HOSTNAME);
                accountUIShow().setVisible(true);
            }
        } else {
            NULogger.getLogger().info("Updown.bz -> Renew session not needed");
        }
    }

    private void resetLogin() {
        username = "";
        password = "";
        loginsuccessful = false;
        sessionId = "";
        expiresAt = 0;
        premium = false;
    }

    private void setupSsl() {
        /*
        SSLSocketFactory sf = null;
        SSLContext sslContext = null;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException e) {
            NULogger.getLogger().log(Level.SEVERE, "Updown.bz -> SSL error", e);
        } catch (KeyManagementException e) {
            NULogger.getLogger().log(Level.SEVERE, "Updown.bz -> SSL error", e);
        }

        try {
            sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "Updown.bz -> SSL error", e);
        }

        Scheme scheme = new Scheme("https", 443, sf);
        httpclient.getConnectionManager().getSchemeRegistry().register(scheme);
        */
    }
}
