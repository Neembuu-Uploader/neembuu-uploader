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
package neembuu.uploader.cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.logging.Level;
import neembuu.release1.api.ui.MainComponent;
import neembuu.release1.ui.mc.NonUIMainComponent;
import neembuu.uploader.AppLocation;
import neembuu.uploader.FindVersion;
import neembuu.uploader.accountgui.AccountManagerWorker;
import neembuu.uploader.accountgui.Callbacks;
import neembuu.uploader.api.SuccessfulUploadsListener;
import neembuu.uploader.api.UserLanguageCodeProvider;
import neembuu.uploader.api.accounts.AccountSelectionUI;
import neembuu.uploader.api.accounts.AccountsProvider;
import neembuu.uploader.api.accounts.UpdateSelectedHostsCallback;
import neembuu.uploader.api.queuemanager.StartNextUploadIfAnyCallback;
import neembuu.uploader.captcha.Captcha;
import neembuu.uploader.captcha.CaptchaServiceProvider;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.external.SmallModuleEntry;
import neembuu.uploader.external.UpdatesAndExternalPluginManager;
import neembuu.uploader.external.UploaderPlugin;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.settings.Application;
import neembuu.uploader.settings.Settings;
import neembuu.uploader.translation.LanguageChangedCallback;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.uploaders.common.CommonUploaderTasks;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.utils.NeembuuUploaderProperties;
import neembuu.uploader.utils.PluginUtils;
import neembuu.uploader.versioning.ProgramVersionProvider;
import neembuu.uploader.versioning.ShowUpdateNotification;
import neembuu.uploader.versioning.UserImpl;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class Main {

    private static void logging(Settings settings) {
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
                //throw new IllegalStateException("gui update not required");
            }
        },Application.getNeembuuHome());
        //NeembuuUploaderLanguages.init(AppLocation.appLocationProvider(),Application.getNeembuuHome());
        //Update selected Language on GUI components
        Translation.changeLanguage("en");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        Application.init();
        NULogger.initializeFileHandler(Application.getNeembuuHome().resolve("nu.log").normalize().toString());
        Settings settings = Application.get(Settings.class);
        logging(settings);
        translation();
        
        try {
            nuInstanceAndRelated();
            updatesAndExternalPluginManager();
            //checkBoxes(); unessential
            firstLaunchAndAccountCheck();
            //checkTamil();
            //Finally start the update checking thread.
        } catch (Exception ex) {
            ex.printStackTrace();
            NULogger.getLogger().severe(ex.toString());
        }

        work(args);

    }

    private static void updatesAndExternalPluginManager() throws Exception {
        UpdatesAndExternalPluginManager uaepm
                = new UpdatesAndExternalPluginManager(
                        Application.getNeembuuHome(),
                        AppLocation.appLocationProvider(),
                        sun, null, new UpdateProgressCmdI());
        amw.uaepm(uaepm);
        PluginUtils.uaepm(uaepm);
        uaepm.initIndex();
        uaepm(uaepm);
        pa.checkBoxOperations();

        /*for (SmallModuleEntry sme : pa.getAllPlugins() ) {
         ap.getAccount(sme.getName());// so that something shows 
         // up in the accounts manager window
         }*/
        //amw.initAccounts();
        // since the ui is not there, nothing to initialize
    }

    private static void nuInstanceAndRelated() {
        //Initialize the instance..
        //Actually this statement was used to initialize for sometime.
        //But the TranslationProvider.changeLanguage() method few lines above will do that for us.
        //This will just return the already initialized instance. :)
        NeembuuUploaderProperties.setUp();

        // initialize httpclient
        NUHttpClient.getHttpClient();

        //initialize all who require access to NeembuuUploader instance
        init_CommonUploaderTasks();
        Main.mc = new NonUIMainComponent();
        NUException.init(mc);
        //NUException.init(NeembuuUploader.getInstance());
        initEnvironmentForPlugins();
    }
    private static MainComponent mc;
    
    static MainComponent getMainComponent(){
        return mc;
    }
    
    private static void initEnvironmentForPlugins() {
        initEnvironmentForPlugins(null);
    }

    private static void initEnvironmentForPlugins(MainComponent mainComponent) {
        NULogger.getLogger().info("Setting abstract uploader getaccount");
        /*AbstractUploader.init(UserImpl.getUserProvider(), mainComponent,
                ap, NeembuuUploaderProperties.getNUProperties());*/
        AbstractAccount.init(NeembuuUploaderProperties.getNUProperties(), mainComponent,
                new AccountSelectionUI() {
                    @Override
                    public void setVisible(boolean f) {
                        amw.setVisible(f);
                    }
                },
                new UpdateSelectedHostsCallback() {
                    @Override
                    public void updateSelectedHostsLabel() {
                        pa.updateSelectedHostsLabel();
                    }
                },
                Utils.getFakeHostsAccountUI(), new CaptchaServiceProvider() {
                    @Override
                    public Captcha newCaptcha() {
                        return DummyCaptcha.INSTANCE;
                    }
                }
        );
    }

    private static void firstLaunchAndAccountCheck() {
        Thread t = new Thread("First Launch Check") {
            @Override
            public void run() {
                try {
                    //If this is the firstlaunch(set by the NeembuuUploaderProperties class),
                    //then display AccountsManager
                    //and set the key back to false
                    if (NeembuuUploaderProperties.isPropertyTrue("firstlaunch")) {
                        NULogger.getLogger().info("First launch.. Display Language cannot be changed as cmd mode..");
                        //NeembuuUploader.displayLanguageOptionDialog();
                        NULogger.getLogger().info("First launch.. Display Accounts Manager.."
                                + " accounts manager cannot be shown in cmd mode");
                        //amw.setVisible(true);
                        NeembuuUploaderProperties.setProperty("firstlaunch", "false");
                    } else {
                        //If it is not the first launch, then
                        //start login process for enabled accounts
                        amw.loginEnabledAccounts();
                    }
                } catch (Exception ex) {
                    NULogger.getLogger().log(Level.WARNING, "{0}: Exception while logging in", getClass().getName());
                    System.err.println(ex);
                }
            }
        };
        t.start();
    }

    private static void init_CommonUploaderTasks() {
        //UploadListTextFile ultf = new UploadListTextFile(Application.getNeembuuHome());
        CommonUploaderTasks.init(
                new StartNextUploadIfAnyCallback() {
                    @Override
                    public void startNextUploadIfAny() {
                        System.out.println("THERE IS NO CONCEPT OF QUEUE IN COMMAND LINE, do nothing");

                    }
                }, pvp, UserImpl.getUserProvider(), new UserLanguageCodeProvider() {
                    @Override
                    public String getUserLanguageCode() {
                        return "en";//cmd version only in english! localization 
                        // would be complex to build up on cmd
                        //return NeembuuUploaderLanguages.getUserLanguageCode();
                    }
                },  new SuccessfulUploadsListener() {
                    @Override public void success(Uploader u) throws Exception {
                        System.out.println("successfully uploaded from u="+u+ 
                                " file-"+u.getFileName()+
                                " downloadurl="+u.getDownloadURL()+
                                " deleteurl="+u.getDeleteURL());
                    }
                }//,ultf
        );
    }

    private static ShowUpdateNotification sun = new ShowUpdateNotification() {
        @Override
        public void showNotification(final long notificationdate) {
            System.out.println("notification date " + notificationdate);
        }

        @Override
        public void showUpdate(float availablever) {
            System.out.println("update -> " + availablever + "/" + pvp.getVersion());
        }

        @Override
        public ProgramVersionProvider pvp() {
            return pvp;
        }
    };

    private static float version = -1f;

    static float version() {
        if (version < 0) {
            version = FindVersion.version();
        }
        return version;
    }

    private static volatile UpdatesAndExternalPluginManager uaepm;

    static void uaepm(UpdatesAndExternalPluginManager uaepm) {
        Main.uaepm = uaepm;
    }

    private static AccountManagerWorker amw = new AccountManagerWorker(new Callbacks() {
        @Override
        public void initAccounts() {
        }
    });

    private static final ProgramVersionProvider pvp
            = new ProgramVersionProvider() {
                @Override
                public String getVersionForProgam() {
                    return Float.toString(version());
                }

                @Override
                public float getVersion() {
                    return version();
                }
            };

    /*private static final AccountsProvider ap
            = new AccountsProvider() {
                @Override
                public Account getAccount(String hostname) {
                    return amw.getAccount(hostname);
                }

                @Override
                public Account getAccount(Class<Account> accountClass) {
                    throw new UnsupportedOperationException("this is how we could do it.");
                }

            };*/

    private static final PluginActivation pa
            = new PluginActivation(new PACallback() {
                @Override
                public UpdatesAndExternalPluginManager uaepm() {
                    return Main.uaepm;
                }
            });

    private static void work(String[] args) throws 
            InstantiationException, IllegalAccessException, 
            ParseException, URISyntaxException {
        Options options = new Options();
        Option file = new Option("f", true, "File to upload.");
        file.setRequired(true);
        Option filehost = new Option("h", true, "Filehost domain");
        filehost.setRequired(true);
        options.addOption(file);
        options.addOption(filehost);
        options.addOption("u", true, "Username");
        options.addOption("p", true, "Password");
        options.addOption("help", false, "Display help");

        String fileName = null, hostname = null, username = null, password = null;//,userName,password;
        if (args.length < 1) {
            System.out.println("syntax fileName hostname "
            //+ "userName password"
            );

            fileName = "F:\\tempimages\\mecon.png";
            hostname = //"mediafire.com"; 
                    "Solidfiles.com";
            //"4Shared.com";
            System.out.println("assuming for test purpose"
                    + fileName + " and host " + hostname);
        } else {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help") || args.length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("help", options);
                System.exit(0);
            } else {
                fileName = cmd.getOptionValue("f");
                hostname = cmd.getOptionValue("h"); //userName=args[2]; password=args[3];
                if (cmd.hasOption("u") && cmd.hasOption("p")) {
                    username = cmd.getOptionValue("u");
                    password = cmd.getOptionValue("p");
                }
            }
        }
        
        System.out.println("===Listing all active and non-active plugins===");
        for (SmallModuleEntry sme : pa.getAllPlugins()) {
            System.out.println(sme.getName());

        }
        System.out.println("===============================================");

        SmallModuleEntry sme = pa.getPluginByName(hostname);
        UploaderPlugin up = pa.activatePlugin(sme);

        Class<? extends Uploader> uClass = up.getUploader(DummyPluginDestructionListener
                .make(sme.getName() + "-uploader"));

        Class<? extends Account> aClass = up.getAccount(DummyPluginDestructionListener
                .make(sme.getName() + "-account"));

        Account a = aClass==null?null:aClass.newInstance();
        Utils.init(hostname, a, username, password);
        
        /*
         Call getAccounts twice because the first time is only registering the account into the manager and not returning the insance
         */
        //Account a = amw.getAccount(sme.getName());
        if (username != null && password != null) {
            a = amw.getAccount(sme.getName());
            a.setOverridingCredentials(username, password);
            a.login();
            System.out.println("account = " + a);

            if (!a.isLoginSuccessful()) {
                System.out.println("Login failed. Terminating.");
                System.exit(0);
            }
        }

        Uploader u = uClass.newInstance();

        u.setFile(new File(fileName));
        //u.startUpload();
        u.run(); // upload in same thread
        System.out.println("Delete URL=" + u.getDeleteURL());
        System.out.println("Download URL=" + u.getDownloadURL());

        System.exit(0);
    }
}
