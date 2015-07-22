package neembuu.uploader.translation;


import neembuu.rus.InterfaceInstanceCreator;
import com.gtranslate.Language;
import com.gtranslate.Translator;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Shashank
 */
public class TranslationFileModify {
    
    private static final String languageLocale = "languageLocale";
    private static final String languageDisplayName = "languageDisplayName";
    
    
    public static void main(String[] args) throws Exception {
        List<String> translated = new LinkedList<>();
        work(translated);
        translateRemaining(translated);
    }
    
    private static void translateRemaining(List<String> translated)throws IOException{
        Map<String,String> languages = new HashMap<>();
        Language.init(languages);
        
        for (String lng : translated) {
            languages.remove(lng);
        }
        
        for (Entry<String, String> e : languages.entrySet()) {
            String locale = e.getKey();
            String displayName = e.getValue();
            
            translateFully(locale, displayName);
        }
    }
    
    private static void translateFully(String locale,String displayName)throws IOException{
        if(true)return;
        System.out.println("-------------------Generating Full translation "+locale+" "+displayName+"-----------------------");
        Map<String,String> values = InterfaceInstanceCreator.defaultValues(TranslationValues.class);
        List<String>lines = new LinkedList<>();
        displayName = displayName.charAt(0)+displayName.substring(1).toLowerCase();
        for(Entry<String,String> es : values.entrySet()){
            String v = translate.translate(es.getValue(), "en", locale);
            String key = es.getKey().trim();
            if(key.equalsIgnoreCase(languageLocale)){
                lines.add(languageLocale+"="+locale);
            }else if(key.equalsIgnoreCase(languageDisplayName)){
                lines.add(languageDisplayName+"="+displayName+"(g)");
            }else{
                lines.add(es.getKey()+"=(g)"+v);
            }
            System.out.println(es.getKey()+"=(g)"+v+ " <-- "+es.getValue());
        }
        Path d = loc.resolve(
                displayName+"(g)."+locale+".translation");
        Files.write(d, lines, Charset.forName("UTF-8"), TRUNCATE_EXISTING,CREATE,WRITE);
        
    }
    
    private static final Translator translate = Translator.getInstance();
    private static Path loc;

    private static void work(final List<String> translated)throws Exception {
        URL cloc = TranslationFileModify.class.getProtectionDomain().getCodeSource().getLocation();
        loc = Paths.get(cloc.toURI());
        for (int i = 0; i < 3; i++) {
            loc = loc.getParent();
        }
        loc = loc.resolve("translations");
        System.out.println(loc);
        Files.walkFileTree(loc, new FileVisitor<Path>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(file.getFileName().toString().endsWith(".properties")){
                    try{modifyFile(file,translated);}catch(Exception a){throw new RuntimeException(a);}}
                return FileVisitResult.CONTINUE;
            }@Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }@Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    private static void modifyFile(Path p,List<String> translated)throws Exception{
        Map<String,String> values = InterfaceInstanceCreator.defaultValues(TranslationValues.class);
        Properties pr = new Properties();
        pr.load(Files.newInputStream(p,READ));
        String locale=pr.getProperty(languageLocale), 
                displayName=pr.getProperty(languageDisplayName);
        translated.add(locale);
        System.out.println("-------------------Modifying Full translation "+locale+" "+displayName+"-----------------------");
        //if(true)return;
        
        List<String>lines = new LinkedList<>();
        for(Entry<Object,Object> e : pr.entrySet()){
            String t = (String)e.getKey();
            try{t = t.substring(t.lastIndexOf('.')+1,t.length());}
            catch(StringIndexOutOfBoundsException ex){}
            lines.add(t+"="+e.getValue());
            values.remove(t);
        }
        //translate remaining
        for(Entry<String,String> es : values.entrySet()){
            String v = translate.translate(es.getValue(), "en", locale);
            lines.add(es.getKey()+"=(g)"+v);
            System.out.println(es.getKey()+"=(g)"+v+ " <-- "+es.getValue());
        }
        
        Path d = p.getParent().resolve(
                displayName+"."+locale+".translation");
        Files.write(d, lines, Charset.forName("UTF-8"), TRUNCATE_EXISTING,CREATE,WRITE);
        
        
    }
}
