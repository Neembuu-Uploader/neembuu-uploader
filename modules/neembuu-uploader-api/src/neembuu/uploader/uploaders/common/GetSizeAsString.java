/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.common;

/**
 *
 * @author Shashank
 */
public class GetSizeAsString {

    /**
     * This public method can be used anywhere to get the String representation
     * of file size
     *
     * @param bytes - size of file in number of bytes (long type)
     * @return the string representation of file size.. Like 1GB, 200MB, 300KB,
     * 400bytes etc..
     */
    public static String getSize(long bytes) {
        if (bytes >= 1048576) {
            double div = bytes / 1048576;
            return div + "MB";
        } else if (bytes >= 1024) {
            double div = bytes / 1024;
            return div + "KB";
        } else {
            return bytes + "bytes";
        }
    }
}
