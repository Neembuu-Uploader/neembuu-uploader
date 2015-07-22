/* 
 * Copyright 2015 Shashank Tulsyan <shashaank at neembuu.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                // to prevent old libs compiled by netbeans from getting into.
                // i know since we are using git this should not happen, but when the code
                // was been tweaked for testing, this statement was reqired.
                if(file.getAbsolutePath().replace('/', '\\').contains("\\dist\\"))return false;
                return FilenameUtils.isExtension(file.getName(), extension);
            }

            @Override
            public boolean accept(File dir, String name) {
                if(dir.getAbsolutePath().replace('/', '\\').contains("\\dist\\"))return false;
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
