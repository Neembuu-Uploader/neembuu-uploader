/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions;

import javax.swing.JFrame;

/**
 * This handles all other exceptions within the classes of NU.
 * @author davidepastore
 */
public abstract class NUException extends Exception {
    
    // Use this final static vars to refer to the translation key.
    
    /**
     * If there are some proxy exceptions for NUHttpClient.
     */
    public final static String INVALID_PROXY = "invalidproxy";
    
    /**
     * If the proxy port isn't good.
     */
    public final static String INVALID_PROXY_PORT = "invalidproxyport";
    
    /**
     * If the proxy host isn't good.
     */
    public final static String INVALID_PROXY_HOST = "invalidproxyhost";
    
    /**
     * If the proxy reach timeout.
     */
    public final static String PROXY_TIMEOUT = "proxytimeout";
    
    /**
     * If a file has a size less than the minimum allowed.
     */
    public final static String MIN_FILE_SIZE = "minfilesize";
    
    /**
     * If a file has a size greater than the maximum allowed.
     */
    public final static String MAX_FILE_SIZE = "maxfilesize";
    
    /**
     * If the login or password is incorrect.
     */
    public final static String INVALID_LOGIN = "invalidlogin";
    
    /**
     * If the user is incorrect.
     */
    public final static String INVALID_USER = "invaliduser";
    
    /**
     * If the password is incorrect.
     */
    public final static String INVALID_PASSWORD = "invalidpassword";
    
    /**
     * If the user is banned.
     */
    public final static String BANNED_USER = "banneduser";
    
    /**
     * If the user needs to activate his account.
     */
    public final static String ACCOUNT_NOT_ACTIVED = "accountnotactived";
    
    /**
     * If the user has been temporarily blocked.
     */
    public final static String LOCKED_ACCOUNT = "lockedaccount";
    
    /**
     * If the server has banned your IP.
     */
    public final static String BANNED_IP = "bannedip";
    
    /**
     * If the server declined to allow the requested access.
     */
    public final static String HTTP_403 = "http403";
    
    /**
     * If the captcha is incorrect.
     */
    public final static String CAPTCHA_ERROR = "captchaerror";
    
    /**
     * If you exceed the number of upload daily..
     */
    public final static String DAILY_UPLOAD_LIMIT = "dailyuploadlimit";

    /**
     * If the file is in the black list.
     */
    public final static String FILE_IN_BLACK_LIST = "fileinblacklist";

    /**
     * If the upload failed.
     */
    public final static String UPLOAD_FAILED = "uploadfailed";
    
    /**
     * If the upload failed for upload limit exceeded.
     */
    public final static String UPLOAD_LIMIT_EXCEEDED = "uploadlimit";
    
    
    /**
     * If the file that you want to upload has a not allowed extension.
     */
    public final static String FILE_EXTENSION = "fileextension";
    
    /**
     * If the user try to login too much times with incorrect credentials.
     */
    public final static String TOO_MANY_TRIES = "toomanytries";
    
    protected String hostName;
    

    /**
     * Creates a new instance of
     * <code>NUException</code> without detail message.
     */
    public NUException() {
    }
    
    /**
     * Constructs an instance of
     * <code>NUException</code> with the specified detail message.
     * 
     * @param msg the detail message.
     */
    public NUException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of
     * <code>NUException</code> with the specified detail message and the host name.
     * 
     * @param msg the detail message.
     * @param hostName the host name.
     */
    public NUException(String msg, String hostName) {
        super(msg);
        this.hostName = hostName;
    }
    
    /**
     * Print the error. Extends this method to print new error types.
     */
    public abstract void printError();
    
    protected static JFrame parent = null;
    public static void init(JFrame parent){
        if(NUException.parent!=null){throw new IllegalStateException("already initialized");}
        NUException.parent = parent;
    }
    
    
    }
