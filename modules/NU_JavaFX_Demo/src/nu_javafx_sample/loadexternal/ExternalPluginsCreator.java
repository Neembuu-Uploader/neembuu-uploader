/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nu_javafx_sample.loadexternal;

import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;

/**
 *
 * @author Shashank
 */
public interface ExternalPluginsCreator {
    Uploader newUploader(String name,Object ... params);
    Account newAccount(String name);
}
