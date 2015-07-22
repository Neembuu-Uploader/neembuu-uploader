/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nu_javafx_sample;

import nu_javafx_sample.loadexternal.ExternalPluginsManager;
import java.io.File;
import neembuu.uploader.interfaces.Uploader;

/**
 *
 * @author Shashank
 */
public class UploaderServiceImpl implements UploaderService{

    private final UI ui;

    public UploaderServiceImpl(UI ui) {
        this.ui = ui;
    }
    
    @Override
    public void handleFile(final File file) {
        final Uploader u =  ExternalPluginsManager.getExternalPluginsCreator()
                .newUploader("180upload.com", file);
        // upload in a different thread. otherwise the UI will get blocked
        final Thread t = new Thread(u,"Upload Thread");
        Thread progressTracker = new Thread("ProgressTracker"){
            @Override public void run() {
                progressTracker(t, u, ui);
            }
        };
        progressTracker.start();
        t.start();
    }
    
    private void progressTracker(final Thread t,final Uploader u,final UI ui){
        while(t.isAlive()){
            ui.updateProgress(u.getProgress()*1d/100d);
            try{Thread.sleep(500);}catch(Exception a){/*ignore*/}
        }
        ui.setDownloadLink(u.getDownloadURL());
        ui.setDeleteLink(u.getDeleteURL());
    }
    
}
