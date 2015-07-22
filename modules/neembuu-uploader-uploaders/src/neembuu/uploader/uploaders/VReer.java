/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import shashaank.smallmodule.SmallModule;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.accounts.VReerAccount;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import neembuu.uploader.httpclient.httprequest.NUHttpPost;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.UploaderAccountNecessary;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author davidepastore
 */
@SmallModule(
    exports={VReer.class,VReerAccount.class},
    interfaces={Uploader.class,Account.class},
    name="VReer.com",
    ignore = true
)
public class VReer extends AbstractUploader implements UploaderAccountNecessary {
    
    VReerAccount vReerAccount = (VReerAccount) getAccountsProvider().getAccount("VReer.com");
    //Necessary variables
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    
    public VReer() {
        host = "VReer.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();

        if (vReerAccount.loginsuccessful) {
            host = vReerAccount.username + " | VReer.com";
        }

    }
    
    
    @Override
    public void run() {

        //Checking once again as user may disable account while this upload thread is waiting in queue
        if (vReerAccount.loginsuccessful) {
            host = vReerAccount.username + " | VReer.com";
        } else {
            host = "VReer.com";
            uploadInvalid();
            return;
        }
        
        //Check file type


        //uploadVReer();


    }
    
}
