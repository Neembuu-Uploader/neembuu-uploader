/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.api.accounts;

import neembuu.uploader.interfaces.Account;

/**
 *
 * @author Shashank
 */
public interface AccountsProvider {
    /**
     * Also used for activating accounts from recently loaded plugins.
     * Apart from that plugin Uploader classes can use this to
     * get instance of their account plugins
     * @param hostname
     * @return 
     */
    Account getAccount(String hostname);
    Account getAccount(Class<Account> accountClass);
}
