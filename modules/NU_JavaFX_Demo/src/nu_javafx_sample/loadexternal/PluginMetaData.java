/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nu_javafx_sample.loadexternal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Shashank
 */
public class PluginMetaData {
    private final Path moduleZipfile;
    
    private String name;
    private final Map<String,String> exports = new HashMap<String, String>();
    
    public static PluginMetaData make(Path zipfile,Path md)throws JSONException,IOException{
        JSONObject metaData = new JSONObject(new String(Files.readAllBytes(md)));
        
        if(!metaData.has("neembuuModule"))return null;
        if(!metaData.getBoolean("neembuuModule"))return null;
        
        PluginMetaData data = new PluginMetaData(zipfile);
        
        data.name = metaData.getString("name");
        JSONArray array = metaData.getJSONArray("exports");
        for (int i = 0; i < array.length(); i++) {
            JSONObject exportEntry = array.getJSONObject(i);
            String interfaceClass = exportEntry.getString("interface");
            String implementationClass = exportEntry.getString("implementation");
            data.exports.put(interfaceClass, implementationClass);
        }
        
        return data;
    }
    
    public PluginMetaData(Path moduleZipfile) {
        this.moduleZipfile = moduleZipfile;
        
    }

    public String getName() {
        return name;
    }

    public Path getModuleFile() {
        return moduleZipfile;
    }

    public String getImplementation(String interfaceName){
        return exports.get(interfaceName);
    }
    
    
}
