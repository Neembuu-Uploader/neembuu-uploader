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
package neembuu.uploader.zip.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import neembuu.rus.Rusila;

/**
 * The application starts here.
 * @author davidepastore
 * @author Shashank Tulsyan
 */
public class Main {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String args[]) throws IOException{
        if(args==null || args.length < 1){
            System.out.println("Usage java -Dfile.encoding=UTF-8 -jar nu-small-modules-generator.jar <settings_foldername>");
            System.out.println("Assuming F:\\NeembuuUploader\\generator_settings");
            args = new String[]{"F:\\NeembuuUploader\\generator_settings"};
            //return;
        }
        
        System.out.println("Settings Directory - "+args[0]);
        Path p = Paths.get(args[0]);
        /*Path ri = Paths.get(System.getProperty("user.home")).resolve("nu");
        if(!Files.exists(ri))Files.createDirectories(ri);*/
        if(!Files.exists(p)){
            System.out.println("Settings directory does not exists!");
            return;
        }
        
        java.io.File stdoutFile = p.resolve("nu_plugin_generator_out.txt").toFile();
        
        shashaank.HandleSystemOutErr.handle(stdoutFile);
        Environment e = Rusila.I(Rusila.create(Paths.get(args[0])), Environment.class);
        UpdaterGenerator ug = new UpdaterGenerator(e);
        ug.run();
    }
}
