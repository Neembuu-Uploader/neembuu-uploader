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
package neembuu.uploader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import neembuu.release1.api.ui.MainComponent;
import neembuu.release1.api.ui.Message;
import static neembuu.uploader.NeembuuUploader.getMainComponent;
import neembuu.uploader.accountgui.AccountsManager;
import neembuu.uploader.versioning.ProgramVersionProvider;
import neembuu.uploader.api.UserLanguageCodeProvider;
import neembuu.uploader.api.accounts.AccountSelectionUI;
import neembuu.uploader.api.accounts.AccountsProvider;
import neembuu.uploader.api.accounts.UpdateSelectedHostsCallback;
import neembuu.uploader.api.queuemanager.StartNextUploadIfAnyCallback;
import neembuu.uploader.captcha.Captcha;
import neembuu.uploader.captcha.CaptchaImpl;
import neembuu.uploader.captcha.CaptchaServiceProvider;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.external.SmallModuleEntry;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.settings.Application;
import neembuu.uploader.settings.Settings;
import neembuu.uploader.translation.LanguageChangedCallback;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.utils.NeembuuUploaderLanguages;
import neembuu.uploader.utils.NeembuuUploaderProperties;
import neembuu.uploader.external.UpdatesAndExternalPluginManager;
import neembuu.uploader.updateprogress.UpdateProgressImpl;
import neembuu.uploader.uploadListFormatters.UploadListTextFile;
import neembuu.uploader.utils.PluginUtils;
import neembuu.uploader.versioning.Notification;
import neembuu.uploader.versioning.NotifyUpdate;
import neembuu.uploader.versioning.ShowUpdateNotification;
import neembuu.uploader.versioning.UserImpl;

/**
 *
 * @author Shashank
 */
public class Main {
    private static void logging(Settings settings){
        if (settings.logging()) {
            NULogger.getLogger().setLevel(Level.INFO);
            NULogger.getLogger().info("Logger turned on");
        } else {
            NULogger.getLogger().info("Turning off logger");
            NULogger.getLogger().setLevel(Level.OFF);
        }
    }
    
    private static void translation(){
        Translation/*Provider*/.init(AppLocation.appLocationProvider(),new LanguageChangedCallback() {

            @Override public void updateGUI() {
                NeembuuUploader.getInstance().languageChanged_UpdateGUI();
            }
        },Application.getNeembuuHome());
        NeembuuUploaderLanguages.init(AppLocation.appLocationProvider(),Application.getNeembuuHome());
        //Update selected Language on GUI components
        Translation.changeLanguage(NeembuuUploaderLanguages.getUserLanguageCode());
    }
    
    private static void lookAndFeel(Settings settings){
        try {
            //Read the settings file and set up the user preferred theme
            String theme = settings.themeNm();
            if (theme.equals("nimbus")) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        NULogger.getLogger().info("Setting Nimbus Look and Feel");
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } else {
                //Else set the System look and feel.
                NULogger.getLogger().info("Setting System Look and Feel");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            try {
                // In case any exception occured, try to set the System Look and feel again. It must not give any problems
                NULogger.getLogger().info("Setting System Look and Feel (under Exception)");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println(ex);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception{
        Application.init();
        NULogger.initializeFileHandler(Application.getNeembuuHome().resolve("nu.log").normalize().toString());
        Settings settings = Application.get(Settings.class);
        logging(settings);
        lookAndFeel(settings);
        translation();

        try {
            nuInstanceAndRelated();
            updatesAndExternalPluginManager();
            checkBoxes();
            firstLaunchAndAccountCheck();
            checkTamil();
            //Finally start the update checking thread.
        } catch (Exception ex) {
            ex.printStackTrace();
            NULogger.getLogger().severe(ex.toString());
        }
    }
        
    private static void updatesAndExternalPluginManager()throws Exception{
        UpdatesAndExternalPluginManager uaepm
                 = new UpdatesAndExternalPluginManager(
                        Application.getNeembuuHome(),
                        AppLocation.appLocationProvider(), 
                        sun, ap,UpdateProgressImpl.INSTANCE.upui());
        AccountsManager.uaepm(uaepm);PluginUtils.uaepm(uaepm);
        uaepm.initIndex();        
        NeembuuUploader.getInstance().uaepm(uaepm);
        NeembuuUploader.getInstance().checkBoxOperations();
        NeembuuUploader.getInstance().checkBox_selectFromPreviousState();
        
        for (Map.Entry<JCheckBox, SmallModuleEntry> entry : NeembuuUploader.unsyncEntries_map() ) {
            JCheckBox jCheckBox = entry.getKey();
            SmallModuleEntry sme = entry.getValue();
            ap.getAccount(sme.getName());// so that something shows 
            // up in the accounts manager window
        }
        
        
        AccountsManager.getInstance().initAccounts();
    }
    
    private static void checkBoxes(){
        NULogger.getLogger().info("Setting checkbox operations");
        
        //Load previously saved state
        NeembuuUploader.getInstance().loadSavedState();
        HostsPanel.getInstance().arrangeCheckBoxes(NeembuuUploader.getInstance().getActivatePluginsMap());
        HostsPanel.getInstance().arrangeCheckBoxesForAllPlugins(NeembuuUploader.getInstance().getAllPluginsMap());
    }
    
    private static  void nuInstanceAndRelated(){
        //Initialize the instance..
        //Actually this statement was used to initialize for sometime.
        //But the TranslationProvider.changeLanguage() method few lines above will do that for us.
        //This will just return the already initialized instance. :)
        NeembuuUploader.getInstance();
        //initialize all who require access to NeembuuUploader instance
        init_CommonUploaderTasks();

        NUException.init(getMainComponent());
        initEnvironmentForPlugins();
    }
    
    private static void initEnvironmentForPlugins(){
        initEnvironmentForPlugins(getMainComponent());
    }
    private static void initEnvironmentForPlugins(MainComponent mainComponent){
        NULogger.getLogger().info("Setting abstract uploader getaccount");
        AbstractUploader.init(UserImpl.getUserProvider(),mainComponent,
            ap,NeembuuUploaderProperties.getNUProperties());
        AbstractAccount.init(NeembuuUploaderProperties.getNUProperties(),mainComponent,
            new AccountSelectionUI() { @Override public void setVisible(boolean f) { 
                AccountsManager.getInstance().setVisible(f); }},
            new UpdateSelectedHostsCallback() {
                @Override public void updateSelectedHostsLabel() {
                    NeembuuUploader.getInstance().updateSelectedHostsLabel();}},
            HostsPanel.getInstance().hostsAccountUI(),new CaptchaServiceProvider() {
                @Override public Captcha newCaptcha() { return new CaptchaImpl(); }}
        );
    }
    
    private static void checkTamil()throws FileNotFoundException{
        //The following code is to write the fallback location to the Readme_for_Tamil_Locale.txt file
        File readmetamil = new File(AppLocation.getLocation(), "Readme_for_Tamil_Locale.txt");
        if (!readmetamil.exists()) {
            NULogger.getLogger().info("Writing Readme_for_Tamil_Locale.txt");
            PrintWriter out = new PrintWriter(readmetamil);
            out.write("If you don't use Tamil language, ignore this file."
                    + "\r\n\r\nTamil is not one of the officially supported locale by Java. But there is a workaround for this."
                    + "\r\n\r\nIf you wish to use Neembuu Uploader in Tamil, kindly copy the included 'LATHA.TTF' font to"
                    + "\r\n\"<JRE_INSTALL_DIR>/jre/lib/fonts/fallback\""
                    + "\r\n\r\nLocation to paste the fallback font for your pc is:\r\n\"" + System.getProperty("java.home") + File.separator
                    + "lib" + File.separator + "fonts" + File.separator + "fallback"
                    + File.separator + "\"");
            out.close();
            NULogger.getLogger().log(Level.INFO, "Fallback location: {0}{1}lib{2}fonts{3}fallback", new Object[]{System.getProperty("java.home"), File.separator, File.separator, File.separator});
        }
    }
    
    private static void firstLaunchAndAccountCheck(){
        Thread t = new Thread("First Launch Check") {
            @Override public void run() {
                try {
                    //If this is the firstlaunch(set by the NeembuuUploaderProperties class),
                    //then display AccountsManager
                    //and set the key back to false
                    if (NeembuuUploaderProperties.isPropertyTrue("firstlaunch")) {
                        NULogger.getLogger().info("First launch.. Display Language Dialog..");
                        NeembuuUploader.displayLanguageOptionDialog();
                        NULogger.getLogger().info("First launch.. Display Accounts Manager..");
                        AccountsManager.getInstance().setVisible(true);
                        NeembuuUploaderProperties.setProperty("firstlaunch", "false");
                    } else {
                        //If it is not the first launch, then
                        //start login process for enabled accounts
                        AccountsManager.loginEnabledAccounts();
                    }
                } catch (Exception ex) {
                    NULogger.getLogger().log(Level.WARNING, "{0}: Exception while logging in", getClass().getName());
                    System.err.println(ex);
                }
            }
        }; t.start();
    }
    
    private static void init_CommonUploaderTasks(){
        UploadListTextFile ultf = new UploadListTextFile(Application.getNeembuuHome());
        CommonUploaderTasks.init(
            new StartNextUploadIfAnyCallback() {
                @Override public void startNextUploadIfAny() {
                    QueueManager.getInstance().startNextUploadIfAny();
                }
            },pvp,UserImpl.getUserProvider()
            ,new UserLanguageCodeProvider() {
                @Override public String getUserLanguageCode() {
                    return NeembuuUploaderLanguages.getUserLanguageCode();
                }
            },ultf
        );
    }
    
    private static ShowUpdateNotification sun = new ShowUpdateNotification() {
        @Override public void showNotification(final long notificationdate) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    new Notification(notificationdate).setVisible(true);
                }
            });
        }
        @Override public void showUpdate(float availablever) {
            new NotifyUpdate(availablever,pvp.getVersion()).setVisible(true);
        }
        @Override public ProgramVersionProvider pvp() {return pvp;}
    };

    private static final ProgramVersionProvider pvp
            = new ProgramVersionProvider() {
                @Override public String getVersionForProgam() {
                    return NeembuuUploader.getVersionForProgam();
                }@Override public float getVersion() { return NeembuuUploader.version(); }
            };
    
    private static final AccountsProvider ap =
            new AccountsProvider() {
                @Override public Account getAccount(String hostname) {
                    return AccountsManager.getAccount(hostname);
                }@Override public Account getAccount(Class<Account> accountClass) {
                    throw new UnsupportedOperationException("this is how we could do it.");
                }
                
            };

}
