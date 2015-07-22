/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.zip.generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The application starts here.
 * @author davidepastore
 */
public class Main {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private static final long period = 5;

    public static void main(String args[]) {
        scheduler.scheduleAtFixedRate(new UpdaterGenerator(), 0, period, TimeUnit.MINUTES);
    }
}
