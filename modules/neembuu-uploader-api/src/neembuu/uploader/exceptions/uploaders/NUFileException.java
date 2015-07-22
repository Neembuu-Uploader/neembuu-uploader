/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.exceptions.uploaders;

import neembuu.uploader.exceptions.NUException;

/**
 * Generic NU file exception.
 * @author davidepastore
 */
public abstract class NUFileException extends NUException {
    
    protected String fileName;

    /**
     * Creates a new instance of
     * <code>NUFileException</code> without detail message.
     */
    public NUFileException() {
    }

    /**
     * Constructs an instance of
     * <code>NUFileException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NUFileException(String msg) {
        super(msg);
    }
    
    
    /**
     * Return the filename with which it is born the exception.
     * @return The filename.
     */
    public String getFileName(){
        return this.fileName;
    }
}
