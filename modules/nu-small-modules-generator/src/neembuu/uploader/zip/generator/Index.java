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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import shashaank.smallmodule.SmallModule;

/**
 *
 * @author Shashank
 */
public class Index {
    private final Path location;
    private final JSONObject index = new JSONObject();
    private final JSONArray smallmodules = new JSONArray();
    private final String hashalgorithm = "MD5";
    private final Environment environment;
    
    public Index(Path location,Environment env) {
        this.location = location; this.environment = env;
    }
    
    public void intialize()throws JSONException{
        while(Files.exists(location)){
            try{
                Files.deleteIfExists(location);
            }catch(IOException a){
                a.printStackTrace();
            }
        }
        
        index.put("description", "Small module system index file.");
        index.put("version", 1);
        index.put("basepath", environment.baseUpdateURL());
        index.put("timezone",Calendar.getInstance().getTimeZone().getID());
        index.put("timestamp", System.currentTimeMillis());
        index.put("hashalgorithm",hashalgorithm);
    }

    public String getHashalgorithm() {
        return hashalgorithm;
    }
    
    public void addSmallModule(SmallModule sm,String hash)throws JSONException{
        JSONObject m = new JSONObject();
        m.put("name", sm.name());
        m.put("relpth", "sm/"+sm.name()+".zip");
        JSONArray exports = new JSONArray();
        for (Class i : sm.interfaces()) {
            exports.put(i.getName());
        }
        m.put("exports", exports);
        m.put("dead", sm.dead());
        m.put("hash", hash);
        smallmodules.put(m);
    }
    
    public void complete()throws JSONException,IOException{
        index.put("smallmodules", smallmodules);
        byte[]b=index.toString(3).getBytes();
        Files.write(location, b);
        
        Path zipedIndex = location.getParent().resolve("update.zip");
        Files.deleteIfExists(zipedIndex);
        
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI uri = URI.create("jar:" + zipedIndex.toUri());
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            Files.write(fs.getPath(location.getFileName().toString()), b);
            
            String versionsXML ="<root>\n" +
                "    <version>"+environment.version()
                    + "</version>\n" +
                "    <notificationdate1>"+environment.notification1Timestamp()
                    + "</notificationdate1>\n" +
                "    <notificationdate>"+environment.notificationTimestamp()
                    + "</notificationdate>\n" +
                "</root>";
            Files.write(fs.getPath("version.xml"), versionsXML.getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
