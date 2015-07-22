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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.exceptions.NUException;
import neembuu.uploader.exceptions.proxy.NUProxyException;

/**
 * Check if the proxy is correct and if it responds in a short time.
 * @author davidepastore
 */
public class ProxyChecker {
    
    private String proxyAddress;
    private String proxyPort;
    private int maxSeconds = 10; //Default value
    
    /**
     * Constructor.
     * @param proxyAddress the proxy address
     * @param proxyPort  the proxy port
     */
    public ProxyChecker(String proxyAddress, String proxyPort){
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
    }
    
    /**
     * Constructor.
     * @param proxyAddress the proxy address
     * @param proxyPort the proxy port
     * @param maxSeconds the maximum number of seconds required for the response
     */
    public ProxyChecker(String proxyAddress, String proxyPort, int maxSeconds){
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
        this.maxSeconds = maxSeconds;
    }
    
    /**
     * Check if it is a good proxy.
     * @return It is a good proxy?
     */
    public boolean control() throws NUProxyException, Exception{
        boolean result = true;
        try{
            ProxyCheckerRunnable proxyCheckerRunnable = new ProxyCheckerRunnable(proxyAddress, proxyPort);
            Thread proxyThread = new Thread(proxyCheckerRunnable);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(proxyThread).get(maxSeconds, TimeUnit.SECONDS);
            
            if(proxyCheckerRunnable.getException() == null){
                //Ok!
            }
            else{
                throw proxyCheckerRunnable.getException();
            }
            
        }catch (InterruptedException ex) {
            Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        } catch (ExecutionException ex) {
            Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        } catch (TimeoutException ex) {
            //Here we must call SettingsManager
            throw new NUProxyException(NUException.PROXY_TIMEOUT, proxyAddress, proxyPort);
        } catch (Exception ex) {
            //Here we must call SettingsManager
            throw ex;
        }
        return result;
    }
    
}
