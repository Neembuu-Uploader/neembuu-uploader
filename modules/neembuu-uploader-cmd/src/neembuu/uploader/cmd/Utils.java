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

package neembuu.uploader.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import neembuu.uploader.api.accounts.AccountSelectionUI;
import neembuu.uploader.api.accounts.AccountsProvider;
import neembuu.uploader.api.accounts.EnableHostCallback;
import neembuu.uploader.api.accounts.HostsAccountUI;
import neembuu.uploader.api.accounts.UpdateSelectedHostsCallback;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.utils.Encrypter;
import neembuu.uploader.utils.NUProperties;
import neembuu.uploader.versioning.UserImpl;


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
        NUProperties nup = makeNUProperties(username,password);
        AbstractUploader.init(
            UserImpl.getUserProvider(),
            Main.getMainComponent(),
            makeAccountsProvider(hostname1, account),
            nup
        );
        
        /*AbstractAccount.init(nup, mc, getFakeAccountSelectionUI(), 
                getFakeUpdateSelectedHostsCallback(), getFakeHostsAccountUI(), null);*/
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
    
    static final HostsAccountUI getFakeHostsAccountUI(){
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
