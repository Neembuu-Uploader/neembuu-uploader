/* 
 * Copyright 2015 Shashank Tulsyan <shashaank at neembuu.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package neembuu.uploader.external;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.reactivethread.CompletionCallback;
import neembuu.reactivethread.ReactiveThread;
import static neembuu.reactivethread.Utils.waitTill;
import neembuu.uploader.external.UpdateProgressUI.Content;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.utils.HashUtil;
import org.json.JSONException;

/**
 *
 * @author Shashank
 */
public class UploaderPlugin {
    private final SmallModuleEntry sme;
    private final Path root;
    
    private volatile Class<? extends Uploader> uploader;
    private volatile Class<? extends Account> account;
    
    private volatile ZipFSClassLoader zfscl;
    
    private final LinkedList<PluginDestructionListener> listeners = new LinkedList<PluginDestructionListener>();

    public UploaderPlugin(SmallModuleEntry sme, Path root) {
        this.sme = sme;
        this.root = root;
    }

    public SmallModuleEntry getSme() {
        return sme;
    }
    
    boolean intitalized(){
        synchronized (sme){return uploader!=null;}
    }
    
    public Class<? extends Uploader> getUploader(PluginDestructionListener pdl) {
        if(pdl==null)throw new NullPointerException("If you are taking a reference of "
                + " uploader class, also make sure you remove reference once destory is called.");
        if(!listeners.contains(pdl))
            listeners.add(pdl);
        return uploader;
    }

    public Class<? extends Account> getAccount(PluginDestructionListener pdl) {
        if(pdl==null)throw new NullPointerException("If you are taking a reference of "
                + " account class, also make sure you remove reference once destory is called.");
        
        if(!listeners.contains(pdl))
            listeners.add(pdl);
        
        return account;
    }

    volatile ReactiveThread rt=null;
    private final double httpDone = 0.6d;
    void create(final Content c)throws IOException,JSONException,ClassNotFoundException{
        synchronized (sme){
            if(uploader!=null)return; // already created
            if(locallyPresent(root,sme)!=LocallyPresent.PRESENT){
                downloadPlugin(c);
            }else{
                try {
                    postCreateImpl(getLocalPath(root,sme),c);
                } catch (Exception ex) {
                    Logger.getLogger(UploaderPlugin.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private void downloadPlugin(final Content c){
        final String url = sme.getIndex().getBasepath()+sme.getRelpth();
        final Path zipPath = getLocalPath(root,sme);
        rt = ReactiveThread.create(new Runnable() {
            @Override public void run() {
                try{
                    //System.out.println("doing "+sme.getName());
                    HttpUtil.update(url, zipPath,c);
                    ReactiveThread.get().updateProgress(httpDone);
                    postCreateImpl(zipPath,c);
                    //System.out.println("done "+sme.getName());
                }catch(Exception a){throw new IllegalStateException(a);}                        
            }
        }, new CompletionCallback() {
            @Override public void completed(ReactiveThread rt) {rt = null; c.done();}
            @Override public void canceled(ReactiveThread rt) {rt = null; c.done();}
            @Override public void progressed(ReactiveThread rt) {c.setProgress(rt.getProgress());}
        });
        rt.setName(sme.getName()+" updating");
        rt.start();
        try{ 
            if(!waitTill(rt, 8000, 100, httpDone)){
                rt.cancel();
            }
            if(!waitTill(rt, 24000, 100, 1d)){
                rt.cancel();
            }
        }catch(Exception ie){/*ignore*/}
    }
    
    public enum LocallyPresent {
        ABSENT, HASH_FAIL, PRESENT
    }
    
    public static LocallyPresent locallyPresent(Path root,SmallModuleEntry sme)throws IOException{
        Path zipPath = getLocalPath(root, sme);
        if(!Files.exists(zipPath.getParent())){
            Files.createDirectories(zipPath.getParent());
            return LocallyPresent.ABSENT;
        }
        return (checkHash(zipPath,sme));
    }
    
    private void postCreateImpl(Path zipPath,Content c)throws Exception{
        zfscl = new ZipFSClassLoader(zipPath);
        SmallModuleMetadata metadata = new SmallModuleMetadata(zfscl.getFs().getPath("SmallModule.json"));

        uploader = (Class<Uploader>)zfscl.findClass(metadata.getUploaderClassName());
        account = metadata.getAccountsClassName()==null?null:
                (Class<Account>)zfscl.findClass(metadata.getAccountsClassName());
        c.done();
    }
    
    public void destroy()throws Exception{
        Exception total = new Exception("Destory failed");
        try{zfscl.getFs().close();}catch(Exception a){total.addSuppressed(a);}
        try{Files.delete(zfscl.getZipPath());}catch(Exception a){total.addSuppressed(a);}
        
        try {
            for (PluginDestructionListener pdl : listeners) {
                try {
                    pdl.destroyed();
                } catch (Exception a) {
                    total.addSuppressed(a);
                }
            }
        } catch (NullPointerException e) { total.addSuppressed(e);}
        
        listeners.clear();
        
        if(total.getSuppressed().length > 0){
            throw total;
        }
    }
    
    static Path getLocalPath(Path root,SmallModuleEntry sme){
        return root.resolve(sme.getRelpth());
    }
    
    private static LocallyPresent checkHash(Path localCopy,SmallModuleEntry sme){
        if(!Files.exists(localCopy))return LocallyPresent.ABSENT;
        String hash = HashUtil.hashFile(localCopy.toFile(), sme.getIndex().getHashalgorithm());
        return (hash.equals(sme.getHash()))?LocallyPresent.PRESENT:LocallyPresent.HASH_FAIL;
    }
    
    
    
    
}
