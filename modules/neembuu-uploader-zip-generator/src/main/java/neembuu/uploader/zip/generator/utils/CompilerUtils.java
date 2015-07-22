/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.zip.generator.utils;

import java.io.File;

/**
 * This contains methods to help to work on some
 * operations relating to compilation.
 * @author davidepastore
 */
public class CompilerUtils {
    
     /**
     * Get the build directory
     * @param file The java file.
     * @return Returns the build directory.
     */
    public static File getBuildDirectory(File file){
        while(!"src".equals(file.getName())){
            file = file.getParentFile();
        }
        return new File(file.getParentFile().getAbsolutePath() + "\\build");
    }
    
}
