/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shashaank.smallmodule;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shashank
 */
public final class NativeResourceLocator {
    private static final Map<String,Provider> resources 
            = new HashMap<>();
    
    public interface Provider {
        Path get();
    }
    
    public static void put(String resource,Provider p){
        resources.put(resource,p);
    }
    
    public static Path find(String name){
        Provider p = resources.get(name);
        if(p==null)return null;
        return p.get();
    }
}
