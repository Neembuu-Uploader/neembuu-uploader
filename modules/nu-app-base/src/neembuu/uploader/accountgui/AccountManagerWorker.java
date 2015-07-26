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

package neembuu.uploader.accountgui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import neembuu.uploader.external.PluginDestructionListener;
import neembuu.uploader.external.SmallModuleEntry;
import neembuu.uploader.external.UpdatesAndExternalPluginManager;
import neembuu.uploader.external.UploaderPlugin;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class AccountManagerWorker {
    private Map<String, Account> accounts;
    private final Callbacks c;

    public AccountManagerWorker(Callbacks c) {
        this.c = c;
        accounts = new TreeMap<String, Account>();
    }
    
    public void destroyAnythingFrom(UploaderPlugin up){
        accounts.remove(up.getSme().getName());
    }

    public Map<String, Account> getAccounts() {
        NULogger.getLogger().severe("Some part of is accessing account details. In future this portion may be secured.");
        return accounts;
    }
    
    private boolean loadAccountClass(Class<? extends Account> account,String hostname){
        if(accounts.containsKey(hostname))return true;
        
        Constructor<? extends Account> constructor;
        try {
            constructor = account.getConstructor();
            Account instance = constructor.newInstance();
            if(!instance.isDead()){
                accounts.put(instance.getHOSTNAME(), instance);
                return true;
            }else {
                return false;
            }
        } catch(IllegalAccessException ex){
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        }
        
        c.initAccounts();
        return true;
    }
    
    private volatile UpdatesAndExternalPluginManager uaepm;
    public void uaepm(UpdatesAndExternalPluginManager uaepm){
        if(this.uaepm!=null)throw new IllegalStateException();
        this.uaepm = uaepm;
    }
    
    public void loginEnabledAccounts() {
        //Create a separate thread for responsiveness of the save button
        new Thread() {
            @Override
            public void run() {
                //Iterate through each account
                for (Account account : accounts.values()) {
                    loginAccount(account);
                }
            }
        }.start();
    }
    
    public void loginAccount(Account account){
        //May need to add additional conditions if premium accts have different login mechanism
        //But that'll be in future..
        if (account.getUsername().isEmpty() || account.getPassword().isEmpty()) {
            //If either one field is empty, disable the account if logged in already.
            //In fact it's enough to check one condition 
            //as the AccountsManager won't let you save with one field empty
            account.disableLogin();
        } else {
            //If both fields are present, login that account
            NULogger.getLogger().log(Level.INFO, "Logging in to {0}", account.getHOSTNAME());
            if (account.canLogin()) {
                account.login();
            }
        }
    }

    
    public Account getAccount(final String hostname) {
        Account a = accounts.get(hostname);
        if(a!=null)return a;
        SmallModuleEntry sme = uaepm.getIndex().get(Uploader.class, hostname);
        UploaderPlugin plugin = uaepm.load(sme);
        System.out.println("plugin - "+plugin);
        Class<? extends Account> accountClass =  plugin.getAccount(new PluginDestructionListener() {
            @Override public void destroyed() {
                accounts.remove(hostname);
            }
        });
        if(accountClass!=null){
            boolean dead = loadAccountClass(accountClass,hostname);
            if(dead)return null;
        }else {
            NULogger.getLogger().log(Level.INFO, "no account for {0}", hostname);
            return null;
        }
        
        return getAccount(hostname);
    }
    
    public void setVisible(boolean f){
        // show accounts in command line using std out?
        // so unsafe and pointless
    }
}
