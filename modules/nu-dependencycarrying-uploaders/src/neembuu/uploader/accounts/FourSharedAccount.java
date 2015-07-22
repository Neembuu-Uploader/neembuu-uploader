/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.accounts;

import java.util.logging.Level;
import javax.swing.JOptionPane;

//import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.translation.Translation;

import neembuu.uploader.interfaces.abstractimpl.AbstractAccount;
import neembuu.uploader.uploaders.api._4shared.DesktopAppJax2;
import neembuu.uploader.uploaders.api._4shared.DesktopAppJax2Service;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author dinesh
 */
public class FourSharedAccount extends AbstractAccount {

    public DesktopAppJax2 da = null;

    public FourSharedAccount() {

        KEY_USERNAME = "4susername";
        KEY_PASSWORD = "4spassword";
        HOSTNAME = "4Shared.com";
    }

    @Override
    public void disableLogin() {
        loginsuccessful = false;
        //These code are necessary for account only sites.
        hostsAccountUI().hostUI(HOSTNAME).setEnabled(false);
        hostsAccountUI().hostUI(HOSTNAME).setSelected(false);
        updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());

    }

    @Override
    public void login() {
        loginsuccessful = false;
        String loginRes = null;
        try {
            da = new DesktopAppJax2Service().getDesktopAppJax2Port();
            loginRes = da.login(getUsername(), getPassword());
            if (!loginRes.isEmpty()) {
                throw new Exception();
            } else {
                loginsuccessful = true;
                hostsAccountUI().hostUI(HOSTNAME).setEnabled(true);
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("4shared Login success :)");
            }

        } catch (Exception e) {
            NULogger.getLogger().log(Level.INFO, "4Shared Login failed: {0}", loginRes);
            loginsuccessful = false;
            username = "";
            password = "";
            showWarningMessage(Translation.T().loginerror(), HOSTNAME);
            accountUIShow().setVisible(true);
        }
    }
}
