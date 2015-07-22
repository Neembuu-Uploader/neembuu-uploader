/*
 * Copyright (C) 2014 davidepastore
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package neembuu.uploader.zip.generator;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author davidepastore
 */
public class ZipFileGenerator implements Runnable{
    
    public static List<Plugin> buildPluginList() {
        List<Plugin> l = new LinkedList<Plugin>();
        
        // this list should be empty
        List<Class> accountResidue = new LinkedList<Class>();
        
        Class[]uc=new Class[0],ac=new Class[0];
        try{
            uc = ClassUtils.getClasses("neembuu.uploader.uploaders");
            ac = ClassUtils.getClasses("neembuu.uploader.accounts");
        }catch(Exception a){
            a.printStackTrace();
        }

        accountResidue.addAll(Arrays.asList(ac));
        
        for (Class uploader : uc) {
            Class accountClass = findAccountClassForUploader(uploader, ac);
            
            Plugin p = new Plugin(uploader, accountClass);
            l.add(p);
            accountResidue.remove(accountClass);
        }
        
        if(!accountResidue.isEmpty()){
            System.err.println("--------Some account could not be liked to their uploaders-----");
            for (Class a : accountResidue) {
                System.err.println(a);
            }
            System.err.println("--------Some account could not be liked to their uploaders-----");
        }
        
        return l;
    }
    
    private static Class findAccountClassForUploader(Class uploader, Class[]allAccounts){
        for (Class accountClass : allAccounts) {
            String name = accountClass.getSimpleName();
            name = name.substring(0,name.indexOf("Account")).toLowerCase();
            String uploaderName = uploader.getSimpleName().toLowerCase();
            
            if(uploaderName.equalsIgnoreCase(name)){
                return accountClass;
            }
        }
        return null;
    }

    @Override
    public void run() {
        //Get the last plugins from git
        
        
        List<Plugin> l = buildPluginList();
        
        //sample 
        try{
            Plugin p = l.get(0);
            System.out.println("Converting for plugin "+p.getPluginName());
            p.generatePluginFile("C:\\neembuuuploader\\generatedtest", null);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
}
