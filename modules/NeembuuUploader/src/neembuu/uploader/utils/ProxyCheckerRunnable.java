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

import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.exceptions.proxy.NUProxyException;
import neembuu.uploader.exceptions.proxy.NUProxyHostException;
import neembuu.uploader.exceptions.proxy.NUProxyPortException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.settings.SettingsManager;

/**
 * Code to execute to control the proxy
 * @author davidepastore
 */
public class ProxyCheckerRunnable implements Runnable {

    private String proxyAddress;
    private String proxyPort;
    private Exception ex;
    
    
    public ProxyCheckerRunnable(String proxyAddress, String proxyPort){
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
    }

    @Override
    public void run() {
        //This code must be exec in another thread
        try {
            //Checking and setting proxy on HttpClient
            NUHttpClient.setProxy(proxyAddress, proxyPort);
        } catch (NUProxyPortException ex) {
            this.ex = ex;
        } catch (NUProxyHostException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
            this.ex = ex;
        } catch (NUProxyException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
            this.ex = ex;
        }
    }

    public Exception getException(){
        return ex;
    }
    
}
