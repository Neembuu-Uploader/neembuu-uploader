/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.accounts;

import neembuu.uploader.exceptions.NUException;

/**
 * Generic exception of an account.
 * @author davidepastore
 */
public abstract class NUAccountException extends NUException {
    
    protected String userName;

    /**
     * Creates a new instance of
     * <code>NUAccountException</code> without detail message.
     */
    public NUAccountException() {
    }

    /**
     * Constructs an instance of
     * <code>NUAccountException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NUAccountException(String msg) {
        super(msg);
    }
}
