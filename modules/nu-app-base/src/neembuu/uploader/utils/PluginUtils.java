/* 
 * Copyright (C) 2015 Shashank Tulsyan <shashaank at neembuu.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package neembuu.uploader.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.external.PluginDestructionListener;
import neembuu.uploader.external.SmallModuleEntry;
import neembuu.uploader.external.UpdatesAndExternalPluginManager;
import neembuu.uploader.interfaces.UploaderAccountNecessary;

/**
 * Plugin Utils.
 * @author davidepastore
 */
public class PluginUtils {
    
    /**
     * Test the methods.
     * @param args
     * @throws ClassNotFoundException
     * @throws IOException 
     */
    public static void main(String args[]) throws ClassNotFoundException, IOException{
        getAllExternalPlugins();
    }
    
    /**
     *
     * @return Returns all extenal plugins.
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Class[] getAllExternalPlugins() throws ClassNotFoundException, IOException{
        Class[] plugins = ClassUtils.getClasses("neembuu.uploader.uploaders");
        for (Class class1 : plugins) {
            NULogger.getLogger().log(Level.INFO, "{0} - UploaderAccountNecessary? {1}", new Object[]{class1.getName(), PluginUtils.isAccountNecessary(class1)});
        }
        return plugins;
    }
    
    
    /**
     * Check if the given plugin implements UploaderAccountNecessary.
     * @param classElement The plugin to check.
     * @return Returns true if the classElement implements UploaderAccountNecessary, false otherwise.
     */
    private static boolean isAccountNecessary(Class classElement){
        if(classElement==null)return false;
        Class[]interfaces = classElement.getInterfaces();
        if(interfaces==null || interfaces.length==0) return false;
        for (Class classInterface : interfaces) {
            if(classInterface==null)continue; // these null problems may happen when
            // the plugin Class has not been loaded.
            if(classInterface.getName().equals(UploaderAccountNecessary.class.getName())){
                return true;
            }
        }
        return false;
    }
    
    private static UpdatesAndExternalPluginManager uaepm;

    public static void uaepm(UpdatesAndExternalPluginManager uaepm) {
        if(PluginUtils.uaepm!=null)throw new IllegalStateException();
        PluginUtils.uaepm = uaepm;
    }
    
    
    
    public static boolean isAccountNecessary(SmallModuleEntry sme){
        return isAccountNecessary(sme,-1);
    }
    public static boolean isAccountNecessary(SmallModuleEntry sme,long startTime){
        Class exportible = null;
        while(exportible==null){
            exportible = uaepm.load(sme).getUploader(new PluginDestructionListener() {
            @Override public void destroyed() {}});
            if(startTime<0
                    || System.currentTimeMillis() - startTime > 10*1000)
                break;
            try {Thread.sleep(100);} catch (InterruptedException ex) {}
        }
        return isAccountNecessary(exportible);
    }
    
}
