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

package neembuu.uploader.cmd;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.external.SmallModuleEntry;
import neembuu.uploader.external.UploaderPlugin;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class PluginActivation {
    private final List<SmallModuleEntry> activatePlugins = new LinkedList<>();   
    private final List<SmallModuleEntry> allPlugins = new LinkedList<>();

    private final PACallback pac;

    public PluginActivation(PACallback pac) {
        this.pac = pac;
    }
    
    public void updateSelectedHostsLabel(){
        
    }
    
    public void checkBoxOperations() {        
        NULogger.getLogger().info("Mapping Checkboxes");
        synchronized (activatePlugins){
            activatePlugins.clear();
        }
        synchronized (allPlugins){
            allPlugins.clear();
        }
        
        for(SmallModuleEntry sme : pac.uaepm().getIndex().get(Uploader.class)){
            synchronized (allPlugins){
                allPlugins.add(sme);
            }
            try {
                if ( UploaderPlugin.locallyPresent(pac.uaepm().getUpdateLocation(), sme)
                        == UploaderPlugin.LocallyPresent.PRESENT) {
                    // if user has 100s of plugins activated, and all of them
                    // suddenly 1 day are updated together, then 
                    // this can slow down our application, 
                    // loading may be deferred to keep the program super agile.
                    UploaderPlugin up = pac.uaepm().load(sme); // this sets activated = true
                }
            } catch (IOException ex) {
                Logger.getLogger(PluginActivation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
 
        checkBoxOperation_ActivatePlugins(); int size;
        synchronized (allPlugins){size = allPlugins.size();}
        NULogger.getLogger().log(Level.INFO, "{0}: Number of supported sites: {1}", new Object[]{getClass().getName(), size});
    }

    public List<SmallModuleEntry> getAllPlugins() {
        return allPlugins;
    }
    
    public SmallModuleEntry getPluginByName(String name){
        System.out.println("Please update this code to use fuzzy logic to match string"
                + " so the spelling mistakes are also accomodated");
        
        name = name.trim();
        for (SmallModuleEntry sme : allPlugins) {
            if(sme.getName().equalsIgnoreCase(name)){
                //activatePlugin(sme);
                return sme;
            }
        }
        return null;
    }

    public List<SmallModuleEntry> getActivatePlugins() {
        return activatePlugins;
    }
    
    public UploaderPlugin activatePlugin(SmallModuleEntry sme){
        // this makes classes :D Creation happens here.
        // this can be slow, and classes are compiled to machine code
        // in java vm
        return pac.uaepm().load(sme);
    }
    
    void checkBoxOperation_ActivatePlugins(){
        //Arrange the checkboxes in alphabetical order
        //not required
    }
    
    void checkBoxOperation_DeActivatePlugin(SmallModuleEntry sme){
        // rest ui stuff not required
        pac.uaepm().unloadAndDelete(sme);
    }
}
