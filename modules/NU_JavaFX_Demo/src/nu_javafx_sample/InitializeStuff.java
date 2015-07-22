/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nu_javafx_sample;

import nu_javafx_sample.loadexternal.ExternalPluginsManager;
import java.io.File;
import java.util.HashMap;
import neembuu.release1.api.ui.MainComponent;
import neembuu.release1.ui.mc.NonUIMainComponent;
import neembuu.uploader.api.AppLocationProvider;
import neembuu.uploader.versioning.ProgramVersionProvider;
import neembuu.uploader.api.UserLanguageCodeProvider;
import neembuu.uploader.api.accounts.AccountsProvider;
import neembuu.uploader.api.queuemanager.StartNextUploadIfAnyCallback;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.translation.LanguageChangedCallback;
import neembuu.uploader.translation.TranslationProvider;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.utils.NUProperties;
import neembuu.uploader.versioning.FileNameNormalizer;
import neembuu.uploader.versioning.User;
import neembuu.uploader.versioning.UserProvider;

/**
 *
 * @author Shashank
 */
public class InitializeStuff {
    
    static void intializeGlobalStuff(){
        UserProvider userProvider = makeUserProvider();
        AppLocationProvider alp = makeAppLocationProvider();
        initializeAbstractUploader(userProvider);
        initializeApplication(alp);
        initCommonUploaderTasks(userProvider);
        ExternalPluginsManager.initialize(alp);
    }
    
    private static void initCommonUploaderTasks(UserProvider userProvider){
        StartNextUploadIfAnyCallback snuiac = new StartNextUploadIfAnyCallback() {
            @Override public void startNextUploadIfAny() {}
        };
        ProgramVersionProvider pvp = new ProgramVersionProvider() {
            @Override public String getVersionForProgam() {
                return "3.0 (demo)";
            }
            @Override public float getVersion() {
                return 3f;
            }
        };
        UserLanguageCodeProvider ulcp = new UserLanguageCodeProvider() {
            @Override public String getUserLanguageCode() {
                return "en";
            }
        };
        CommonUploaderTasks.init(snuiac, pvp, userProvider, ulcp);
    }
    
    public static AppLocationProvider makeAppLocationProvider(){
        return new AppLocationProvider() {
            @Override public File getPath() {
                try{
                    File d = new File(InitializeStuff.class.getResource("/nu_javafx_sample/").toURI());;
                    return new File(d,"../../../");
                }catch(Exception a){
                    throw new RuntimeException(a);
                }
            }
        };
    }
    
    private static void initializeApplication(AppLocationProvider alp){
        LanguageChangedCallback lcc = new LanguageChangedCallback() {
            @Override public void updateGUI() {
                System.out.println("language changed :P");
            }
        };
        TranslationProvider.init(alp, lcc);
    }
    
    private static void initializeAbstractUploader(UserProvider userProvider){
        MainComponent mainComponent = new NonUIMainComponent();
        AccountsProvider accountsProvider = new AccountsProvider() {
            @Override public Account getAccount(String hostname) {
                return ExternalPluginsManager.getExternalPluginsCreator()
                        .newAccount(hostname);
            }
        };
        NUProperties nup = makeNUProperties();
        AbstractUploader.init(userProvider, mainComponent, accountsProvider, nup);
    }
    
    private static NUProperties makeNUProperties(){
        return new NUProperties() {
            private final HashMap<String,String> properties = new HashMap<String,String>();
            @Override public String getProperty(String key) {
                return properties.get(key);
            }

            @Override public String getEncryptedProperty(String key) {
                return properties.get(key);
            }

            @Override public void setProperty(String key, String value) {
                properties.put(key, value);
            }

            @Override public void setEncryptedProperty(String key, String value) {
                properties.put(key, value);
            }
        };
    }
    private static UserProvider makeUserProvider(){
        return new UserProvider() {
            @Override public User getUserInstance() {
                final long uid = (long)(Math.random()*Long.MAX_VALUE);
                return new User() {
                    @Override public long uid() {return uid; }
                    @Override public String uidString() { return Long.toString(uid); }
                    @Override public boolean canCustomizeNormalizing() { return true; }
                };
            }

            @Override public FileNameNormalizer getFileNameNormalizer() {
                return new FileNameNormalizer() {
                    @Override public String normalizeFileName(String fn, int fileNameLengthLimit) {return fn; }
                    @Override public String normalizeFileName(String fn) { return fn; }
                };
            }
        };
    }
}
