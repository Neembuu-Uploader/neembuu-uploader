/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.activation.MimetypesFileTypeMap;

/**
 * File utils class provide methods to save the content of a String in a File and
 * to work with extensions and content-type.
 * @author davidepastore
 */
public class FileUtils {
    
    /**
     * Non-instantiable
     */
    private FileUtils() {
    }

    /**
     * Save in a file the content of a string. It's very useful for debug.
     *
     * @param filename The file name.
     * @param content The content of the file.
     * @param charsetName The charset of the string.
     * @return New file instance
     * @throws Exception
     */
    public static File saveInFile(String filename, String content, String charsetName) throws Exception {
        InputStream is = new ByteArrayInputStream(content.getBytes(charsetName));
        byte[] b = new byte[2048];
        int length;
        OutputStream os = new FileOutputStream(filename);
        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }
        os.close();
        return new File(filename);
    }

    /**
     * Save in a file the content of a string. It's very useful for debug.
     *
     * @param filename The file name.
     * @param content The content of the file.
     * @return New file instance.
     * @throws Exception
     */
    public static File saveInFile(String filename, String content) throws Exception {
        return saveInFile(filename, content, "ISO-8859-1");
    }
    
    /**
     * Return the file extension.
     * @param file the file
     * @return The file extension.
     */
    public static String getFileExtension(File file){
        return file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }
    
    /**
     * Check the file extension.
     * @param extensions allowed extensions.
     * @param file the file.
     * @return True if the file has an extension included in extensions, false otherwise.
     */
    public static boolean checkFileExtension(ArrayList<String> extensions, File file){
        String fileExtension = getFileExtension(file).toLowerCase();
        String extension;
        for(int i = 0; i < extensions.size(); i++){
            extension = extensions.get(i);
            if(extension.equalsIgnoreCase(fileExtension)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Return the context type of the file. <a href="http://www.rgagnon.com/javadetails/java-0487.html">Link</a>
     * @param file the file
     * @return The content type of the file.
     */
    public static String getContentType(File file){
        return new MimetypesFileTypeMap().getContentType(file);
    }
    
}
