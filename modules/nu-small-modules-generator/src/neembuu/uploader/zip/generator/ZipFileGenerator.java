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
