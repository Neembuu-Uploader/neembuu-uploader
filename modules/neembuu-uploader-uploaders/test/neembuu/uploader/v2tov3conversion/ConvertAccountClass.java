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

/**
 *
 * @author Shashank
 */
public class ConvertAccountClass {
    private final List<String> is,out_lines=new LinkedList<>();
    
    private String[]rejectList = {
        "import neembuuuploader.uploaders.common.FileUtils;",
        "import neembuuuploader.HostsPanel;",
        "import neembuuuploader.NeembuuUploader;",
        "import neembuuuploader.utils.NeembuuUploaderProperties;",
        //"import neembuuuploader.TranslationProvider;",
        "import neembuuuploader.accountgui.AccountsManager;",
        "import javax.swing.JOptionPane;",
        "<-----dummy place holder ------>"
    };

    public ConvertAccountClass(Path in) throws IOException{
        is = Files.readAllLines(in,Charset.defaultCharset());
    }
    
    
    public void convert(){
        boolean consumed = false;
        for (String i : is) {
            consumed = handlePackageLine(i);
            if(consumed)continue;
            
            consumed = handleimport(i);
            if(consumed)continue;
            
            consumed = r1(i); if(consumed)continue;
            consumed = r2(i); if(consumed)continue;
            consumed = r3(i); if(consumed)continue;
            consumed = r4(i,out_lines); if(consumed)continue;
            consumed = r5(i); if(consumed)continue;
            consumed = r6(i,out_lines); if(consumed)continue;
           
            out_lines.add(i);
        }
    }
    
    public void writeTo(Path out)throws IOException{
        Files.write(out, out_lines, Charset.defaultCharset(), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }
    
    private boolean handlePackageLine(String i){
        if(i.startsWith("package")){
            out_lines.add("package neembuu.uploader.accounts;");
            return true;
        }
        return false;
    }
    

    private boolean handleimport(String i){
        if(i.startsWith("import")){
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
    
    //HostsPanel.getInstance().ddlStorageCheckBox.setEnabled(false);
    //hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);    
    //HostsPanel.getInstance().ddlStorageCheckBox.setSelected(false);
    //hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
    private boolean r1(String i){
        if(i.contains("HostsPanel.getInstance().")){
            i = leadingSpaces(i)+"hostsAccountUI().hostUI(HOSTNAME)"+i.substring(i.lastIndexOf('.'));
            out_lines.add(i);
            return true;
        }
        return false;
    }
    
    private static String leadingSpaces(String i){
        String ret= "";
        for (int j = 0; j < i.length(); j++) {
            if(i.charAt(j)==' '){
                ret = ret+" ";
            }else break;
        }
        return ret;
    } 

    //NeembuuUploader.getInstance().updateSelectedHostsLabel();
    //updateSelectedHostsLabel();
    private boolean r2(String i){
        if(i.contains("NeembuuUploader.getInstance().updateSelectedHostsLabel();")){
            i = i.replace("NeembuuUploader.getInstance().updateSelectedHostsLabel();", 
                    "updateSelectedHostsLabel();");
            out_lines.add(i);
            return true;
        }
        return false;
    }
    
    //AccountsManager.getInstance().setVisible(true);
    //accountUIShow().setVisible(true);
    private boolean r3(String i){
        if(i.contains("AccountsManager.getInstance().setVisible(")){
            i = i.replace("AccountsManager.getInstance().setVisible(", 
                    "accountUIShow().setVisible(");
            out_lines.add(i);
            return true;
        }
        return false;
    }
    
    //JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
    //showWarningMessage( "<html>" + TranslationProvider.get("neembuu.uploader.accounts.loginerror") + "</html>", HOSTNAME);
    static boolean r4(String i,List<String>out_lines){
        if(i.contains("JOptionPane.show")){
            if(i.trim().startsWith("//"))return false;
            String p = i.substring(i.indexOf(",")+1);
            p = p.substring(0,p.lastIndexOf(","));
            String m = leadingSpaces(i)+"showWarningMessage("+p+");";
            out_lines.add(m);
            return true;
        }
        return false;
    }
    
    //captchaServiceProvider().newCaptcha();
    private boolean r5(String i){
        if(i.contains("new Captcha")){
            i = i.replace("new Captcha()", 
                    "captchaServiceProvider().newCaptcha()");
            out_lines.add(i);
            return true;
        }
        return false;
    }
    
    //properties().
    static boolean r6(String i,List<String>out_lines){
        if(i.contains("NeembuuUploaderProperties.")){
            i = i.replace("NeembuuUploaderProperties.", 
                    "properties().");
            out_lines.add(i);
            return true;
        }
        return false;
    }
}
