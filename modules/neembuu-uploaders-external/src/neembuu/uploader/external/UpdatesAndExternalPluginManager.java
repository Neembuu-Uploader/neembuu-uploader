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
import neembuu.reactivethread.CompletionCallback;
import neembuu.reactivethread.ReactiveThread;
import static neembuu.reactivethread.Utils.waitTill;
import neembuu.uploader.api.AppLocationProvider;
import neembuu.uploader.api.accounts.AccountsProvider;
import neembuu.uploader.external.UpdateProgressUI.Content;
import neembuu.uploader.external.UploaderPlugin.LocallyPresent;
import neembuu.uploader.versioning.ShowUpdateNotification;

/**
 *
 * @author Shashank
 */
public class UpdatesAndExternalPluginManager {
    private final Path updateLocation;
    private final AppLocationProvider alp;
    private final ShowUpdateNotification sun;
    private final AccountsProvider ap;
    private final UpdateProgressUI upui;
    
    private volatile Index i;
    
    public UpdatesAndExternalPluginManager(
            Path home, AppLocationProvider alp,
            ShowUpdateNotification sun,final AccountsProvider ap,
            final UpdateProgressUI upui) {
        this.updateLocation = home.resolve("external");
        this.sun = sun; this.alp = alp; this.ap = ap;
        this.upui = upui;
    }
    
    private void updateIndex()throws Exception{
        final Content c = upui.addContent("Updating list of supported file host");
        final ReactiveThread rt1= ReactiveThread.create(new Runnable() {
            @Override public void run() {
                try{
                    HttpUtil.update("http://www.neembuu.com/uploader/updates/v3.1/update.zip",
                            updateLocation.resolve("update.zip"),c);
                    ReactiveThread.get().updateProgress(0.7d);
                    ZipFSClassLoader.quickExtract(updateLocation.resolve("update.zip"));
                    CheckMajorUpdate cu = new CheckMajorUpdate(updateLocation, sun);
                    cu.check();
                }catch(Exception a){throw new IllegalStateException(a);}
            }
        }, new CompletionCallback() {
            @Override public void completed(ReactiveThread rt) {c.done();}
            @Override public void canceled(ReactiveThread rt) {c.done();}
            @Override public void progressed(ReactiveThread rt) {c.setProgress(rt.getProgress());}
        });
        rt1.start();
        
        if(!waitTill(rt1, 20*1000, 300, 0.6)){
            rt1.cancel();
        }
    }
    
    public void initIndex()throws Exception{
        try{
            Files.createDirectories(updateLocation);
        }catch(Exception a){
            //ignore
        }
        long startUpdate = System.currentTimeMillis();
        System.out.println("fetching updates ");
        updateIndex();
        long span = System.currentTimeMillis() - startUpdate;
        System.out.println("done updates index - time taken = "+span);
        i = new Index(updateLocation.resolve("index.json"));
        
        LinkedList<Runnable> updateOperations = new LinkedList<>();
        for (SmallModuleEntry sme : i.getSmallModuleEntrys()) {
            LocallyPresent locallyPresent = UploaderPlugin.locallyPresent(updateLocation, sme);
            if(locallyPresent!=LocallyPresent.ABSENT){
                if(sme.isDead()){
                    deleteLocal(sme); // dead plugins get cleaned. :D
                }else {
                    updateOperations.add(loadInNewThread(sme));
                }
            }
        }
        ReactiveThread rt = ReactiveThread.create(CompletionCallback.DUMMY, "Updating all plugins", 
                updateOperations.toArray(new Runnable[updateOperations.size()]));
        rt.setDaemon(true);
        rt.start();
    }
    
    private Runnable loadInNewThread(final SmallModuleEntry sme){
        return new Runnable() {
            @Override public void run() {
                load(sme);
            }
        };
    }

    public Path getUpdateLocation() {
        return updateLocation;
    }

    public Index getIndex() {
        if(i==null)throw new IllegalStateException("initIndex() first");
        return i;
    }
    
    private void deleteLocal(SmallModuleEntry sme){
        Path localCopyPath = UploaderPlugin.getLocalPath(updateLocation, sme);
        try{
            Files.deleteIfExists(localCopyPath);
        }catch(IOException a){
            a.printStackTrace();
        }
    }
    
    public final UploaderPlugin load(SmallModuleEntry sme){
        return load(sme,0);
    }
    private UploaderPlugin load(SmallModuleEntry sme,int cnt){
        synchronized (sme){
            if(sme.up!=null){
                if(!sme.up.intitalized()){
                    ReactiveThread rt = sme.up.rt;
                    if(rt==null){
                        if(cnt > 3)return sme.up;
                        return load(sme,cnt+1);
                    }
                    try {
                        waitTill(rt, 3000, 100, 1d);
                    } catch (InterruptedException ex) {}
                }
                return sme.up;
            }

            UploaderPlugin up = new UploaderPlugin(sme, updateLocation);
            
            sme.up = up;
        }
        final Content c = upui.addContent(sme.getName());
        try{sme.up.create(c);}catch(Exception a){
            System.out.println("for plugin "+sme.getName());
            a.printStackTrace();
            c.done();
            return null;
            //if this happens, the check box should get unchecked
            //map.remove(entry.getKey()); //<< this is not going to work
        }
        return load(sme,cnt+1);
        
    }
    
    public void unloadAndDelete(SmallModuleEntry sme) {
        UploaderPlugin up;
        synchronized (this){
            up = sme.up; sme.up = null;// deactivate initiated here
            if(up==null){
                Path zipPath = UploaderPlugin.getLocalPath(updateLocation, sme);
                if(Files.exists(zipPath)){ 
                    try{
                        Files.delete(zipPath);
                    } catch(Exception a){
                        System.out.println("could not delete unactive plugin "+sme.getName());
                    }
                }
                return;
            }            
        }
        
        try{up.destroy();}catch(Exception a){
            System.out.println("could not destory plugin "+sme.getName());
            a.printStackTrace();
        } 
    }
    
    
    
}
