/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.utils.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.rules.TemporaryFolder;

/**
 * Create files.
 * @author davidepastore
 */
public class FileCreator {
    
    private static final long defaultSize = 1024; // 1 KB
    
    /**
     * Create a temporary file.
     * @param tempFolder the temporary folder in which to create new file.
     * @param extension the file extension.
     * @return the temporary file.
     */
    public static File createTemporaryFile(TemporaryFolder tempFolder, String extension){
        return createTemporaryFile(tempFolder, extension, defaultSize);
    }
    
    
    /**
     * Create a temporary file.
     * @param tempFolder the temporary folder in which to create new file.
     * @param extension the file extension.
     * @param size the file size.
     * @return the temporary file.
     */
    public static File createTemporaryFile(TemporaryFolder tempFolder, String extension, long size){
        File file = null;
        try {
            file = tempFolder.newFile();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.setLength(size);
            return file;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileCreator.class.getName()).log(Level.SEVERE, "Exception in createTemporaryFile: {0}", ex);
        } catch (IOException ex) {
            Logger.getLogger(FileCreator.class.getName()).log(Level.SEVERE, "Exception in createTemporaryFile: {0}", ex);
        }
        return file;
    }
    
}
