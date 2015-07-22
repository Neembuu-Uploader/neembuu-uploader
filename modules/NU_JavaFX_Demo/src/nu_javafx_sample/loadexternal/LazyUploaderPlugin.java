/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nu_javafx_sample.loadexternal;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;

/**
 *
 * @author Shashank
 */
public class LazyUploaderPlugin {
    private final PluginMetaData metaData;
    
    private Validate validate = Validate.NOT_VALIDATED;
    
    public enum Validate {
        NOT_VALIDATED, VALIDATED, CORRUPT
    }
    
    public String getName(){
        return metaData.getName();
    }
    
    private Class uploader;
    private Class account;
    private Close close; interface Close {void close()throws Exception;}
    
    public void validate(){
        synchronized (metaData){
            if(validate!=Validate.NOT_VALIDATED)return;
        }
        
        String uploadderClNm = metaData.getImplementation("neembuu.uploader.interfaces.Uploader");
        String accountClzzNm = metaData.getImplementation("neembuu.uploader.interfaces.Account");
        try{
            final FileSystem zipfs = FileSystems.newFileSystem(metaData.getModuleFile(),null);
            close = new Close() { @Override public void close()throws Exception { zipfs.close(); } };
            ZipClassLoader zcl = new ZipClassLoader(zipfs);
            uploader = zcl.loadClass(uploadderClNm);
            account = zcl.loadClass(accountClzzNm);
        }catch(Exception a){
            a.printStackTrace();
            validate = Validate.CORRUPT;
            try {close(); }catch(Exception a2){a2.printStackTrace();}
            return;
        }
        
        validate = Validate.VALIDATED;
    }
    
    public void close()throws Exception{
        close.close();
    }

    public LazyUploaderPlugin(PluginMetaData metaData) {
        this.metaData = metaData;
    }
    
    public PluginMetaData getMetaData() {
        return metaData;
    }
    
    public Uploader newUploader(Object... params)throws NoSuchMethodException,InstantiationException,IllegalAccessException,InvocationTargetException{
        validate();
        Uploader u = (Uploader)this.uploader.newInstance();
        u.setFile((File)params[0]);
        return u;
    }
    
    public Account newAccount()throws NoSuchMethodException,InstantiationException,IllegalAccessException,InvocationTargetException{
        validate();
        return (Account)account.newInstance();
    }
    
}
