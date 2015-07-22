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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Shashank
 */
public class Index {
    private final Path indexFile;

    private final JSONObject index;
    
    private final String basepath, hashalgorithm;
    private final List<SmallModuleEntry> smallModuleEntrys 
            = new LinkedList<SmallModuleEntry>(); 
    
    public Index(Path indexFile) throws IOException,JSONException,
            ClassNotFoundException{
        this.indexFile = indexFile;
        index = new JSONObject(new String(Files.readAllBytes(indexFile),Charset.forName("UTF-8")));
        
        basepath = index.getString("basepath");
        JSONArray sms = index.getJSONArray("smallmodules");
        hashalgorithm = index.getString("hashalgorithm");
        
        for (int i = 0; i < sms.length(); i++) {
            JSONObject sm = sms.getJSONObject(i);
            JSONArray exportsJA = sm.getJSONArray("exports");
            Class[]exportsC = new Class[exportsJA.length()];
            for (int j = 0; j < exportsJA.length(); j++) {
                exportsC[j] = Class.forName(exportsJA.getString(j));
            }
            SmallModuleEntry sme = new SmallModuleEntry(
                    sm.getString("name"), sm.getString("relpth"),
                    exportsC, sm.getString("hash"),this,sm.getBoolean("dead"));
            smallModuleEntrys.add(sme);
        }
    }

    public String getBasepath() {
        return basepath;
    }

    public String getHashalgorithm() {
        return hashalgorithm;
    }

    public List<SmallModuleEntry> getSmallModuleEntrys() {
        return smallModuleEntrys;
    }
    
    public List<SmallModuleEntry> get(Class type){
        LinkedList<SmallModuleEntry> r = new LinkedList<>();
        for (SmallModuleEntry sme : smallModuleEntrys) {
            for(Class c  :  sme.getExports()){
                if(c.isAssignableFrom(type)){
                    r.add(sme);
                }
            }
        }
        return r;
    }
    
    public SmallModuleEntry get(Class type,String name){
        List<SmallModuleEntry> r = get(type);
        for (SmallModuleEntry sme : r) {
            if(sme.getName().equals(name))return sme;
        }
        return null;
    }
    
    
}
