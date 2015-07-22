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
        return new File(file.getParentFile().getAbsolutePath() + File.separatorChar+ "build");
    }
    
}
