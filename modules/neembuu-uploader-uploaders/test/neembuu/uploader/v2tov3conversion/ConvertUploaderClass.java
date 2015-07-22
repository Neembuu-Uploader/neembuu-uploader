/*
 * Copyright (C) 2014 Shashank
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

package neembuu.uploader.v2tov3conversion;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import static neembuu.uploader.v2tov3conversion.ConvertAccountClass.r4;
import static neembuu.uploader.v2tov3conversion.ConvertAccountClass.r6;

/**
 *
 * @author Shashank
 */
public class ConvertUploaderClass {
    private final List<String> is,out_lines=new LinkedList<>();
    private final String myClassName;
    private String displayName=null,accountClassName = null;
    
    private String[]rejectList = {
        "import java.io.File;",
        //"import org.apache.http.client.CookieStore;",
        //"import org.apache.http.client.protocol.ClientContext;",
        //"import org.apache.http.impl.client.BasicCookieStore;",
        "import neembuuuploader.accountgui.AccountsManager;",
        "import neembuuuploader.utils.NeembuuUploaderProperties;",
        "import neembuuuploader.uploaders.common.MonitoredFileBody;",
        "import neembuuuploader.interfaces.Account;",
        "import neembuu.uploader.interfaces.Uploader;",
        //"import neembuu.uploader.TranslationProvider;"
    };

    public ConvertUploaderClass(Path in) throws IOException{
        String myClassName = in.getFileName().toString();
        myClassName = myClassName.substring(0,myClassName.indexOf("."));
        this.myClassName = myClassName;
        is = Files.readAllLines(in,Charset.defaultCharset());
    }
    
    
    public void convert(){
        findDetails();
        
        boolean consumed = false;
        for (String i : is) {
            consumed = handlePackageLine(i); if(consumed)continue;
            consumed = handleimport(i); if(consumed)continue;
            consumed = handleClassAnnotation(i); if(consumed)continue;
            consumed = replaceAccountManagerEntry(i); if(consumed)continue;
            consumed = handleConstructor(i); if(consumed)continue;
            consumed = handle_super(i); if(consumed)continue;
            consumed = r4(i,out_lines); if(consumed)continue;
            consumed = r6(i,out_lines); if(consumed)continue;
            consumed = r7(i); if(consumed)continue;
            
            out_lines.add(i);
        }
    }
    
    private void findDetails(){
        for (String i : is) {
            //if(i.matches("host?=?\".*\"")){
            String inz = i.replaceAll(" ", "");
            if(inz.contains("host=") && !inz.contains("_host")){
                if(displayName!=null){
                    System.err.println("possible double host name issue --->> "+i);
                    return;
                }
                try{
                    displayName = i.substring(i.indexOf("\"")+1,i.lastIndexOf("\""));
                }catch(Exception a){
                    displayName = i.substring(i.indexOf("=")+1,i.lastIndexOf(";"));
                }
                if(displayName.contains(" ")){
                    String displayName_new = displayName.substring(
                            displayName.lastIndexOf(" ")+1);
                    System.err.println("Display name contains issue --->>"+displayName+" fixing to "+displayName_new);
                    displayName = displayName_new;
                }
                System.out.println("found display name="+displayName);
            }else if(i.contains("AccountsManager.getAccount")){
                if(i.contains("//"))continue;
                i = i.replaceAll("private", "");
                i = i.trim();
                accountClassName = i.substring(0,i.indexOf(' '));
                System.out.println("found acccount name="+accountClassName);
            }
            if(displayName!=null && accountClassName!=null)return;
        }
    }
    
    public void writeTo(Path out)throws IOException{
        Files.write(out, out_lines, Charset.defaultCharset(), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }
    
    private boolean handlePackageLine(String i){
        if(i.startsWith("package")){
            out_lines.add("package neembuu.uploader.uploaders;");
            return true;
        }
        return false;
    }
    
    private boolean handleClassAnnotation(String i){
        if(i.startsWith("public class")){
            out_lines.add("@SmallModule(");
            if(accountClassName!=null){
                out_lines.add("    exports={"+myClassName+ ".class,"+accountClassName+ ".class},");
                out_lines.add("    interfaces={Uploader.class,Account.class},");
            }else {
                out_lines.add("    exports={"+myClassName+".class},");
                out_lines.add("    interfaces={Uploader.class},");
            }
            out_lines.add("    name=\""+displayName+ "\"");
            out_lines.add(")");
            out_lines.add(i);
            return true;
        }
        return false;
         
    }
    
    private boolean sminserted = false;
    private boolean handleimport(String i){
        if(i.startsWith("import")){
            if(!sminserted){
                sminserted = true;
                out_lines.add("import shashaank.smallmodule.SmallModule;");
                out_lines.add("import neembuu.uploader.interfaces.Uploader;");
                if(accountClassName!=null)
                    out_lines.add("import neembuu.uploader.interfaces.Account;");
            }
            for (String reject : rejectList) {
                if(i.matches(reject)){
                    //System.out.println("rejected "+i);
                    return true;
                }
            }
            if(i.contains("import neembuuuploader.TranslationProvider;")){
                i = "import neembuu.uploader.translation.TranslationProvider;";
            }
            i = i.replace("neembuuuploader","neembuu.uploader");
            out_lines.add(i);
            return true;
        }
        return false;
         
    }
    
    private boolean replaceAccountManagerEntry(String i){
        //AccountsManager.
        
        if(i.contains("AccountsManager.getAccount")){
            i = i.replace("AccountsManager.getAccount","getAccountsProvider().getAccount");
            out_lines.add(i);
            return true;
        }
        return false;
    }
    
    private boolean handle_super(String i){
        //File file
        return (i.contains("super(file);"));
    }
        
    private boolean handleConstructor(String i){
        if(i.contains("File file")){
            i = i.replace("File file","");
            out_lines.add(i);
            return true;
        }
        return false;
    }
    
    private boolean r7(String i){
        if(i.contains("MonitoredFileBody")){
            i = i.replace("MonitoredFileBody ", "neembuu.uploader.uploaders.common.MonitoredFileBody ");
            out_lines.add(i);
            return true;
        }return false;
    }
}
