/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.zip.generator.utils;

import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author davidepastore
 */
public class NUFileUtils {
    
    /**
     * Get the nth parent directory of the given file.
     *
     * @param file The file.
     * @param n The nth directory.
     * @return Returns the nth parent directory.
     */
    public static File getNthParentDirectory(File file, int n) {
        if (n > 1) {
            return getNthParentDirectory(file.getParentFile(), n - 1);
        }
        return file.getParentFile();
    }
    
    
    /**
     * Get all the files with the given extension.
     * @param file The directory where there are all the files.
     * @param extension The extension of the files to search for.
     * @return The list of the files.
     */
    public static Collection<File> listAllFilesWithExt(File file, final String extension){
        return FileUtils.listFiles(file, new IOFileFilter() {

            @Override
            public boolean accept(File file) {
                return FilenameUtils.isExtension(file.getName(), extension);
            }

            @Override
            public boolean accept(File dir, String name) {
                return FilenameUtils.isExtension(name, extension);
            }
        }, TrueFileFilter.INSTANCE);
    }
    
    /**
     * List all the Java files.
     *
     * @param file The directory where there are all files.
     * @return The list of the files.
     */
    public static Collection<File> listAllJavaFiles(File file) {
        return listAllFilesWithExt(file, "java");
    }
    
}
