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
package neembuu.uploader.external;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shashank
 */
public class ZipFSClassLoader extends ClassLoader {
    //private final Path zipPath;

    private final FileSystem fs; 
    private final Path zipPath;
    public ZipFSClassLoader(Path zipPath)throws IOException {
        this.zipPath = zipPath;
        
        Map<String, String> env = new HashMap<>();
        URI uri = URI.create("jar:" + zipPath.toUri());
        fs = FileSystems.newFileSystem(uri, env);
    }

    public Path getZipPath() { return zipPath; }
    public FileSystem getFs() {return fs;}

    @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
        String relp = name.replaceAll("\\.", "/")+".class";
        try{
            byte [] b = Files.readAllBytes(fs.getPath(relp));
            
            //we may define a protection domain and stop plugins
            //from doing arbitary stuff
            //http://www.informit.com/articles/article.aspx?p=1187967&seqNum=3      
            return defineClass(name, b, 0, b.length);
        }catch(Exception a){
            throw new ClassNotFoundException(name, a);
        }
    }
    
    public static void quickExtract(Path zipPath)throws Exception{
        Map<String, String> env = new HashMap<>();
        URI uri = URI.create("jar:" + zipPath.toUri());
        FileSystem fs = FileSystems.newFileSystem(uri, env);
        
        
        try(DirectoryStream<Path> ds = Files.newDirectoryStream( 
                    fs.getRootDirectories().iterator().next()
                )){
            for(Path p : ds){
                byte [] b = Files.readAllBytes(p);
                Path dest = zipPath.getParent().resolve(p.getFileName().toString());

                Files.write(dest,
                        b, StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.CREATE);
            }
        }
        
    }
    
}
