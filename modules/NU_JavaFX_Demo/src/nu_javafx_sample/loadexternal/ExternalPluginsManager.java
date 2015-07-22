/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nu_javafx_sample.loadexternal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.api.AppLocationProvider;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import nu_javafx_sample.InitializeStuff;

/**
 * @author Shashank
 */
public class ExternalPluginsManager {
    
    public static void main(String[] args) {
        AppLocationProvider alp = InitializeStuff.makeAppLocationProvider();
        initialize(alp);
    }
    
    private final List<LazyUploaderPlugin> plugins = new ArrayList<LazyUploaderPlugin>();
    private final ExternalPluginsCreator epc = new ExternalPluginsCreator() {
        @Override public Uploader newUploader(String name, Object... params) { return newUploaderImpl(name, params); }
        @Override public Account newAccount(String name) { return newAccountImpl(name);}
    };

    private Uploader newUploaderImpl(String name, Object... params) {
        LazyUploaderPlugin lup = find(name);
        Uploader u = null;
        try{
            u = lup.newUploader(params);
        }catch(Exception a){
            throw new RuntimeException(a);
        }return u;
    }
    private Account newAccountImpl(String name) {
        LazyUploaderPlugin lup = find(name);
        Account account = null;
        try{
            account = lup.newAccount();
        }catch(Exception a){
            throw new RuntimeException(a);
        }return account;
    }
    
    private LazyUploaderPlugin find(String name){
        for(LazyUploaderPlugin lup : plugins){
            if(lup.getName().equalsIgnoreCase(name))
                return lup;
        }return null;
    }
    
    // this should ideally be executed in a separate thread.
    public static void initialize(AppLocationProvider alp){
        ExternalPluginsManager lep = IK.I;
        lep.loadImpl(alp.getPath().toPath().resolve("external_plugins"));
    }
    
    private static final class IK {
        // this is actually a lazy classloader idiom
        private static final ExternalPluginsManager I = new ExternalPluginsManager();
    }
    
    public static ExternalPluginsCreator getExternalPluginsCreator() {
        return IK.I.epc;
    }
    
    
    
    
    private void loadImpl(Path externalPluginsDir){        
        DirectoryStream.Filter<Path> zipFileOnly = new DirectoryStream.Filter<Path>() {
                @Override public boolean accept(Path entry) throws IOException {
                    return entry.getFileName().toString().toLowerCase().endsWith(".zip");
                }
            };
        
        // try catch resource is a java7 feature which ensure
        // all IOExceptions are handled nicely and all resources are close at the end
        try(DirectoryStream<Path> ds = Files.newDirectoryStream(externalPluginsDir,zipFileOnly)){
            for(Path p : ds){
                handleZipFile(p);
            }
        } catch (IOException ex) {
            Logger.getLogger(ExternalPluginsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void handleZipFile(Path zipfile)throws MalformedURLException,IOException{
        try(FileSystem zipfs = FileSystems.newFileSystem(zipfile, null)){
            Path md = zipfs.getPath("metadata.json");
            if(!Files.exists(md)){ return; }
            PluginMetaData metaData;
            try{metaData = PluginMetaData.make(zipfile,md);}
            catch(Exception a){a.printStackTrace(); return;}
            
            LazyUploaderPlugin lp = new LazyUploaderPlugin(metaData);
            lp.validate();
            plugins.add(lp);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
}
