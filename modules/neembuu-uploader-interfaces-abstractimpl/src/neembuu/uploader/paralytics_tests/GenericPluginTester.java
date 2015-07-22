/*
 * Copyright (C) 2015 RD
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
package neembuu.uploader.paralytics_tests;

import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import shashaank.smallmodule.SmallModule;
import javax.swing.JFileChooser;
import java.io.File;   

/**
 *
 * @author RD
 */
public class GenericPluginTester {
    /*public static void main(String[] args) throws Exception{
        Account account = new OneFichierAccount(); // for different plugins just change this line
        Uploader uploader;
        Utils.init("1fichier.com", account, null, null);
        // in case u are testing an uploader with account, u need to fill username and password
        uploader = new OneFichier();  // << for different plugins just change this line
        
        uploader.setFile(Utils.getMeATestFile());//u might want to change the code
        // so that you may test with bigger files.
        uploader.run();
    }*/
    
    public static void test(Class<? extends Uploader> uploaderClass,Class<? extends Account> accountsClass){
        try{
            Account account = accountsClass==null?null:accountsClass.newInstance();
            Uploader uploader;
            String name;
            
            SmallModule sm = uploaderClass.getAnnotation(SmallModule.class);
            
            Utils.init(sm.name(), account, /*username*/null, /*password*/null);
            if(account!=null)account.login();else{System.out.println("account is null");}
            uploader = uploaderClass.newInstance();

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = fileChooser.showOpenDialog(fileChooser);
            File selectedFile = null;
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                uploader.setFile(selectedFile);
            } else {
                uploader.setFile(Utils.getMeATestFile());
            }
            
            uploader.run();
        }catch(Exception a){
            throw new RuntimeException(a);
        }
    }

}
