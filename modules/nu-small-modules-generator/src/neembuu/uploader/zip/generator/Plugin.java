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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shashank
 */
public class Plugin {
    private final Class uploader,account;
    private final String metadata;

    public Plugin(Class uploader, Class account) {
        this.uploader = uploader;
        this.account = account;
        
        metadata = createMetadata();
    }
    
    private String createMetadata(){
        return "\"neembuuModule\": \"true\",\n" +
            "    \"name\": \""+getPluginName()+ "\",\n" +
            "    \"exports\" : [\n" +
            "        {\n" +
            "            \"interface\": \"neembuu.uploader.interfaces.Uploader\",\n" +
            "            \"implementation\": \""+ uploader.getName() + "\"\n" +
            "        }"+
                
                (account==null?"\n":(
                    ",\n" +
                    "        {\n" +
                    "            \"interface\": \"neembuu.uploader.interfaces.Account\",\n" +
                    "            \"implementation\": \""+ account.getName() + "\"\n" +
                    "        }\n" 
                ))+
                
            "    ]";   
    }

    public Class getUploader() {
        return uploader;
    }

    public Class getAccount() {
        return account;
    }

    public String getMetadata() {
        return metadata;
    }
    
    public final String getPluginName(){
        /*
        if(false){
            //disabled
            try{
                Uploader u = (Uploader)uploader.newInstance();
                return u.getDisplayName();
            }catch(Exception a){
                a.printStackTrace();
            }
        }
        */
        return uploader.getSimpleName();
    }
    
    public void generatePluginFile(String outputDirectory, String optionalFileName)throws IOException{
        if(optionalFileName==null){
            optionalFileName = uploader.getSimpleName()+".zip";
        }
        
        Path outfile = Paths.get(outputDirectory,optionalFileName);
        
        Files.deleteIfExists(outfile);
        
        Map<String, String> env = new HashMap<>(); 
        env.put("create", "true");
        URI uri = URI.create("jar:"+outfile.toUri());
        
        try (FileSystem zipfs = FileSystems.newFileSystem(uri,env,null)) {
            Files.write(zipfs.getPath("metadata.json"), metadata.getBytes());
            
            putClass(zipfs, account);
            putClass(zipfs, uploader);
        }
    }
    
    private static void putClass(FileSystem zipfs,Class c)throws IOException{
        String p = c.getName(); p=p.substring(0,p.lastIndexOf("."));
        p = p.replaceAll("\\.", "/"); p = "/"+p;
        Files.createDirectories(zipfs.getPath(p));
        
        byte[]classRaw = convertToByteArray(c);
        Files.write(zipfs.getPath(p+"/"+c.getSimpleName()+".class"), classRaw);
    }
    
    private static byte[]convertToByteArray(Class c)throws IOException{
        String className = c.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream stream = c.getClassLoader().getResourceAsStream(classAsPath);
        return convertToByteArray(stream);
    }
    
    private static byte[]convertToByteArray(InputStream is)throws IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }
    
    
}
