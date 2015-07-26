/*
 * Copyright (C) 2015 Shashank
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Properties;
import javax.swing.JFrame;
import neembuu.release1.api.ui.MainComponent;
import neembuu.release1.ui.mc.MainComponentImpl;
import neembuu.uploader.api.AppLocationProvider;
import neembuu.uploader.api.SuccessfulUploadsListener;
import neembuu.uploader.api.UserLanguageCodeProvider;
import neembuu.uploader.api.accounts.AccountSelectionUI;
import neembuu.uploader.api.accounts.AccountsProvider;
import neembuu.uploader.api.accounts.EnableHostCallback;
import neembuu.uploader.api.accounts.HostsAccountUI;
import neembuu.uploader.api.accounts.UpdateSelectedHostsCallback;
import neembuu.uploader.api.queuemanager.StartNextUploadIfAnyCallback;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.translation.LanguageChangedCallback;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.utils.Encrypter;
import neembuu.uploader.utils.NUProperties;
import neembuu.uploader.versioning.ProgramVersionProvider;
import neembuu.uploader.versioning.UserProvider;


/**
 *
 * @author Shashank
 */
public class Utils {
    public static File getMeATestFile(){
        return new File(getNUHome(),"nu.log");
    }
    
    private static File getNUHome(){
        return new File(System.getProperty("user.home")+File.separatorChar+".neembuuuploader");
    }
    
        
    public static void init1(){
        //Main.main(new String[]{});
        //NeembuuUploader.getInstance().setVisible(false);
    }
    
    public static void init(final String hostname1,final Account account,
            final String username,final String password)throws URISyntaxException{
        //TranslationProvider
        Translation.init(new AppLocationProvider() {
            @Override public File getPath() {
                try{return getNUFolderPath();}
                catch(Exception a){throw new RuntimeException(a);}}
        }, new LanguageChangedCallback() {
            @Override public void updateGUI() {
                throw new UnsupportedOperationException("Not supported yet.");}
        }, getNUFolderPath().toPath());
        NUProperties nup = makeNUProperties(username,password);
        MainComponent mc = new MainComponentImpl(new JFrame());
        UserProvider up = DummyUserImpl.getUserProvider();
        AbstractUploader.init(
            up,
            mc,
            makeAccountsProvider(hostname1, account),
            nup
        );
        
        AbstractAccount.init(nup, mc, getFakeAccountSelectionUI(), 
                getFakeUpdateSelectedHostsCallback(), getFakeHostsAccountUI(), null);
        
        CommonUploaderTasks.init(new StartNextUploadIfAnyCallback() {
            @Override public void startNextUploadIfAny() {
                System.out.println("asked to start next upload. So I will just quit");
                System.exit(0);
            }
        }, new ProgramVersionProvider() {
            @Override public String getVersionForProgam() {
                return "100";
            }@Override public float getVersion() {
                return 100;
            }
        }, up, new UserLanguageCodeProvider() {
            @Override public String getUserLanguageCode() {return "en";}
        }, new SuccessfulUploadsListener() {
            @Override public void success(Uploader u) throws Exception {
                System.out.println("successfully uploaded from u="+u+ 
                        " file-"+u.getFileName()+
                        " downloadurl="+u.getDownloadURL()+
                        " deleteurl="+u.getDeleteURL());
            }
        });
    }
    
    private static File getNUFolderPath()throws URISyntaxException{
        File f;
        CodeSource cs = Utils.class.getProtectionDomain().getCodeSource();
        f = new File(cs.getLocation().toURI()); // F:\NeembuuUploader\gitcode\modules\neembuu-uploader-uploaders\test\neembuu\ uploader\generate_plugins
        f = f.getParentFile().getParentFile().getParentFile(); //F:\NeembuuUploader\gitcode\modules\NeembuuUploader
        f = new File(f,"NeembuuUploader");// F:\NeembuuUploader\gitcode\modules\NeembuuUploader
        f = new File(f,"dist"); // F:\NeembuuUploader\gitcode\modules\NeembuuUploader\dist
        return f;
    }
    
    private static AccountsProvider makeAccountsProvider(final String hostname1,final Account account){
        return new AccountsProvider() {
            @Override public Account getAccount(String hostname) {
                if(hostname1.equals(hostname))return account;
                return null;
            }
            @Override public Account getAccount(Class<Account> accountClass) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }
    
    private static NUProperties makeNUProperties(final String username,final String password){
        final File propertyfile = new File(getNUHome(),".nuproperties");
        NUProperties nup = new NUProperties() {
            private final Properties properties = new Properties();
            {
                try{
                    properties.load(new FileInputStream(propertyfile));
                }catch(IOException fnfe){
                    throw new RuntimeException(fnfe);
                }
            }
            @Override public String getProperty(String key) {
                //return username;
                String r = (String)properties.get(key);
                if(r==null)return username;
                return r;
            }

            @Override public String getEncryptedProperty(String key) {
                //return password;
                String value = properties.getProperty(key, "");
                if(value.isEmpty()){
                    if(password!=null)return password;
                    return "";
                }
                return Encrypter.decrypt(value);
            }

            @Override public void setProperty(String key, String value) {
                properties.put(key, value);
            }

            @Override public void setEncryptedProperty(String key, String value) {
                properties.put(key, value);
            }
        };
        return nup;
    }
    
    private static final AccountSelectionUI getFakeAccountSelectionUI (){
        return new AccountSelectionUI() {

            @Override
            public void setVisible(boolean f) {
                System.out.println("user called account ui, expecting user "
                        + "to type username and password "
                        + "and presslogin button");
            }
        };
    }
    
    private static final UpdateSelectedHostsCallback getFakeUpdateSelectedHostsCallback(){
        return new UpdateSelectedHostsCallback() {
            @Override public void updateSelectedHostsLabel() {
                System.out.println("called UpdateSelectedHostsCallback.updateSelectedHostsLabel");
            }
        };
    }
    
    public static final HostsAccountUI getFakeHostsAccountUI(){
        return new HostsAccountUI() {
            @Override
            public EnableHostCallback hostUI(String name) {
                return new EnableHostCallback() {

                    @Override public void setEnabled(boolean f) {
                        new Throwable("this is not an error").printStackTrace();
                    }

                    @Override public void setSelected(boolean f) {
                        new Throwable("this is not an error").printStackTrace();
                    }
                };
            }
        };
    }
}
