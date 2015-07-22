/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.mega.sdk;

import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import shashaank.smallmodule.SmallModule;

/**
 *
 * @author Shashank
 */
@SmallModule(
        exports = {MegaCoNz.class, MegaCoNzAccount.class},
        interfaces = {Uploader.class, Account.class},
        name = "mega.co.nz",
        dependsOn = {
            DelegateMegaGlobalListener.class,
            DelegateMegaListener.class,
            DelegateMegaLogger.class,
            DelegateMegaRequestListener.class,
            DelegateMegaTransferListener.class,
            DelegateMegaTreeProcessor.class,
            DelegateOutputMegaTransferListener.class,
            mega.class, // i guess these classes are not required, 
            // we may reduce download size of this plugin by investigating 
            // dependencies in detail. This is entirely an optional task,
            // and required only if optimization is desired.
            MegaAccountDetails.class,
            MegaApi.class,
            MegaApiJava.class,
            MegaError.class,
            MegaGfxProcessor.class,
            MegaGlobalListener.class,
            MegaGlobalListenerInterface.class,
            megaJNI.class,
            MegaListener.class,
            MegaListenerInterface.class,
            MegaLogger.class,
            MegaLoggerInterface.class,
            MegaNode.class,
            MegaNodeList.class,
            MegaPricing.class,
            MegaProxy.class,
            MegaRequest.class,
            MegaRequestListener.class,
            MegaRequestListenerInterface.class,
            MegaShare.class,
            MegaShareList.class,
            MegaTransfer.class,
            MegaTransferList.class,
            MegaTransferListener.class,
            MegaTransferListenerInterface.class,
            MegaTreeProcessor.class,
            MegaTreeProcessorInterface.class,
            MegaUser.class,
            MegaUserList.class,}
)
public class MegaCoNz extends AbstractUploader {

    @Override
    public void run() {
        String appKey = null;
        if(appKey==null){
            throw new UnsupportedOperationException("Please get the app key as explained in the java doc");
        }
        // you will also need to have libmega.so and mega.dll
        // find it http://megatools.megous.com/
        MegaApi ma = new MegaApi(appKey);
        MegaNode destinationUploadDirectoryOnMegaDrive = ma.getInboxNode();
        ma.startUpload(file.getAbsolutePath(), destinationUploadDirectoryOnMegaDrive);
    }

}
