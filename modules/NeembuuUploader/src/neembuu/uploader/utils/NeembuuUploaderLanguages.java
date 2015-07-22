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
package neembuu.uploader.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.api.AppLocationProvider;
import neembuu.uploader.settings.Application;
import neembuu.uploader.settings.Settings;
import neembuu.uploader.translation.Translation;

/**This class is used for Language/Locale related operations.
 * It is closely dependant on operations of SettingsProperties class. So that class will be instantiated if not already
 * @author vigneshwaran
 */
public class NeembuuUploaderLanguages {

    //This is a mapping of language code with it's full English name
    //static final Map<String, String> languagemap = new LinkedHashMap<String, String>();
    private static final List<L> languageList = new LinkedList<>();
    
    //This is a file object that refers to the location of the fallback font..
    //This is used only for Tamil language..
    private static final File fallbackfont = new File(System.getProperty("java.home") + File.separator
            + "lib" + File.separator + "fonts" + File.separator + "fallback"
            + File.separator + "LATHA.TTF");

    private static Path updatedTranslationsLocation,defaultTranslationsLocation;
    
    public static void init(AppLocationProvider alp, Path home){
        if(updatedTranslationsLocation!=null || defaultTranslationsLocation!=null){
            throw new IllegalStateException("Already intialized!");
        }
        updatedTranslationsLocation = home.resolve("external").resolve("translations");
        defaultTranslationsLocation = alp.getPath().toPath().resolve("translations");
        refresh();
    }
    public static void refresh(){
        checkUpdatedCopy(updatedTranslationsLocation, defaultTranslationsLocation);
        
        File[]trans=updatedTranslationsLocation.toFile().listFiles(new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".translation");
            }});
        languageList.clear();
        for (File file : trans) {
            try{
                String l = file.getName();
                String name = l.substring(0,l.indexOf('.'));
                l = l.substring(l.indexOf('.')+1);
                l = l.substring(0,l.indexOf('.'));
                if(isSupportedAndAreFontsAvailable(l)){
                    languageList.add(L.newInst(name, l));
                    //languagemap.put(l, name);
                }
            }catch(Exception a){
                Logger.getLogger(NeembuuUploaderLanguages.class.getName()).log(
                        Level.SEVERE, "Could not handle language file "+file.getName());
            }
        }
        Collections.sort(languageList, new Comparator<L>() {
            @Override public int compare(L o1, L o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        
    }
    
    private static void checkUpdatedCopy(final Path updatedTranslationsLocation,final Path defaultTranslationsLocation){
        try {
            if(!Files.exists(updatedTranslationsLocation)){
                Files.createDirectories(updatedTranslationsLocation);
            }
            Files.walkFileTree(defaultTranslationsLocation, new FileVisitor<Path>() {
                @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path abs = abs(dir);
                    if(!Files.exists(abs)){
                        Files.createDirectory(abs);
                    }return FileVisitResult.CONTINUE;}
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path abs = abs(file);
                    if(!Files.exists(abs)){
                        Files.copy(file,abs,StandardCopyOption.COPY_ATTRIBUTES);
                    }return FileVisitResult.CONTINUE;}
                @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;}
                @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;}
                private Path abs(Path dir){
                    return updatedTranslationsLocation.resolve(
                            defaultTranslationsLocation.relativize(dir).toString());
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(NeembuuUploaderLanguages.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean isSupportedAndAreFontsAvailable(String code){
        if (code.equalsIgnoreCase("ta")) {
            NULogger.getLogger().log(Level.INFO, "{0}: Tamil available=", fallbackfont.exists() );
            //languagemap.put(TAMIL, "Tamil");
            return fallbackfont.exists();
        }return true;
    }
    
    
    /**
     * 
     * @return the list of all language short codes as a String array[]
     */
    public synchronized static String[] getLanguageCodes() {
        final List<String> a = new LinkedList<>();
        for (L l : languageList) {
            a.add(l.code);
        }
        return a.toArray(new String[a.size()]);
        //return languagemap.keySet().toArray(new String[0]);
    }
    
    /**
     * 
     * @return the list of all full language names in English as a String array[]
     */
    public synchronized static String[] getLanguageNames(boolean refresh) {
        if(refresh)refresh();
        return getLanguageNames();
    }
    public synchronized static String[] getLanguageNames() {
        final List<String> a = new LinkedList<>();
        for (L l : languageList) {
            a.add(l.name);
        }
        return a.toArray(new String[a.size()]);
        //return languagemap.values().toArray(new String[0]);
    }
    
    /**
     * Get the language full name for the specified code
     * @param code
     * @return 
     */    
    /*public static String getLanguageNameByCode(String code) {
        return languagemap.get(code);
    }*/

    //Non instantiable.. Use getInstance()..
    private NeembuuUploaderLanguages() {
    }

    /**
     * 
     * @return the language code for the language set by the user. 
     * If none available, return "en"
     */
    public static String getUserLanguageCode() {
        return Application.get(Settings.class).userlang();
    }
    
    /**
     * 
     * @return the full language name for the language set by the user
     */
    public static String getUserLanguageName() {
        return Translation.T().languageDisplayName();
        //return getLanguageNameByCode(getUserLanguageCode());
    }

    /**
     * Set the user language with the specified code.
     * @param langcode 
     */
    public static void setUserLanguageCode(String langcode) {
        Application.get(Settings.class).userlang(langcode);
    }
    
    /**To be used by the SettingsManager class. 
     * Set the user language by the index of the languages combobox.
     * 
     * @param i 
     */
    public static void setUserLanguageByIndex(int i) {
        setUserLanguageCode(getLanguageCodes()[i]);
    }
    
    /**To be used by the SettingsManager class. 
     * Set the user language by the index of the languages combobox.
     * 
     * @param i 
     */
    public static void setUserLanguageByName(String selectedlanguage) {
        int i = 0;
        for(String language : getLanguageNames()) {
            if(language.equals(selectedlanguage)) {
                Application.get(Settings.class).userlang(
                    getLanguageCodes()[i]
                );break;
            }
            i++;
        }
    }
    
    private static final class L {
        private final String name, code;

        public L(String name, String code) {
            this.name = name;
            this.code = code;
        }
        private static L newInst(String name, String code){return new L(name, code);}
    }
}
