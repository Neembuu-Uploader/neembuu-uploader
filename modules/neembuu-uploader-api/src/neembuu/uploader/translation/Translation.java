package neembuu.uploader.translation;

import neembuu.rus.InterfaceInstanceCreator;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.api.AppLocationProvider;
//import neembuu.uploader.settings.SettingsManager;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author Shashank Tulsyan
 */
public final class Translation {

    //private final Properties properties;
    //private final String language;
    //guarded by this class
    private static TranslationValues singleton = null;
    private static final TranslationValues defaultprovider = InterfaceInstanceCreator.getDefault(TranslationValues.class);
    
    //private static AppLocationProvider appLocationProvider = null;
    private static LanguageChangedCallback lcc;
    private static Path /*home,*/updatedTranslationsLocation,defaultTranslationsLocation;
    
    
    public static final void init(AppLocationProvider alp,
            LanguageChangedCallback lcc,Path home){
        if(updatedTranslationsLocation!=null){
            throw new IllegalStateException("Already initialized");
        }
        if(Translation.lcc!=null){
            throw new IllegalStateException("Already initialized");
        }
        //Translation.appLocationProvider = alp;
        Translation.lcc = lcc;
        //Translation.home = home;
        
        updatedTranslationsLocation = home.resolve("external").resolve("translations");
        defaultTranslationsLocation = alp.getPath().toPath().resolve("translations");
    }

    /**
     * 
     * @return a translation provider based on default locale. If none exist, get Translation for English
     */
    public static synchronized TranslationValues T() {
        if (singleton == null) {
            return defaultprovider;
        }
        return singleton;
    }
    
    public static synchronized String T(String code) {
        TranslationValues t = T();
        InterfaceInstanceCreator tInstance = (InterfaceInstanceCreator)Proxy.getInvocationHandler(t);
        return tInstance.getByName(code);
    }

    /**
     * 
     * @return Translation with English language as default
     */
    public static synchronized TranslationValues getDefaultTranslation() {
        return defaultprovider;
    }
    
    public static synchronized String getDefaultTranslation(String code){
        TranslationValues t = defaultprovider;
        InterfaceInstanceCreator tInstance = (InterfaceInstanceCreator)Proxy.getInvocationHandler(t);
        return tInstance.getByName(code);
    }

    /**
     * 
     * @return language currently in use
     */
    public final String getLanguage() {
        return T().languageLocale();
    }

    /**This is the method used to initialize Translation and also to change language.
     * It also updates the GUI of NeembuuUploader and Table automatically.
     * @param languagecode locale 
     * @throws SecurityException The program is designed to allow
     * changes from only NeembuuUploader.class and Translation.class .
     * This is to prevent, malicious behavior.
     */
    public static synchronized void changeLanguage(String languagecode) {
        // getStackTrace()[0] will be getStackTrace()
        // getStackTrace()[1] will be changeLanguage()
        // getStackTrace()[2] will be the calling class which should be one of the three allowed classes.
        String name = Thread.currentThread().getStackTrace()[2].getClassName(); // takes around 228.3 microsec
        //name = sun.reflect.Reflection.getCallerClass(2).getName(); // takes around 40 microsecs*/
        if (!name.equals("neembuu.uploader.NeembuuUploader")
                && !name.equals("neembuu.uploader.Main")
                && !name.equals(Translation.class.getName())
                && !name.equals("neembuu.uploader.settings.SettingsManager")) {
            throw new SecurityException(name + " does not have rights to call this function");
        }
        
        TranslationValues res = locateTranslationFile(languagecode);
        if(res==null){
            NULogger.getLogger().log(Level.INFO, "{0}Could not change language", languagecode);
            return;
        }
        singleton = res;

        NULogger.getLogger().log(Level.INFO, "{0}Language Changed", Translation.class.getName());

        //Update GUI on NU
        lcc.updateGUI(); //NeembuuUploader.getInstance().languageChanged_UpdateGUI();

    }
    
    private static TranslationValues locateTranslationFile(String languagecode){
        File ret = locateTranslationFile(languagecode, updatedTranslationsLocation);
        if(ret==null){
            ret = locateTranslationFile(languagecode, defaultTranslationsLocation);
        }if(ret==null)return null;
        if(ret.exists()){
            //Properties p = new Properties();
            HashMap<String,String> p = new HashMap<>();
            //try (FileInputStream in = new FileInputStream(ret)) {
            try{
                List<String> lines = Files.readAllLines(ret.toPath(), Charset.forName("UTF-8"));
                for (String l : lines) {
                    int i = l.indexOf('=');
                    String val = l.substring(i+1,l.length()).trim();
                    String nam = l.substring(0,i).trim();
                    p.put(nam, val);
                }
                //p.load(in);
            } catch (IOException ex) {
                Logger.getLogger(Translation.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return InterfaceInstanceCreator.create(p,TranslationValues.class);
        }
        return null;
    }
    private static File locateTranslationFile(String languagecode,Path useLocation){
        final FilenameFilter ff = new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".translation");
            }};
        
        File[]trans=useLocation.toFile().listFiles(ff);
        
        for (File file : trans) {
            try{
                String l = file.getName();
                String name = l.substring(0,l.indexOf('.'));
                l = l.substring(l.indexOf('.')+1);
                l = l.substring(0,l.indexOf('.'));
                if(l.equalsIgnoreCase(languagecode)){
                    return file;
                }
            }catch(Exception a){
                Logger.getLogger(Translation.class.getName()).log(
                        Level.SEVERE, "Could not handle language file "+file.getName(), a);
            }
        }
        return null;
    }
}
