/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.api;

import neembuu.uploader.interfaces.Uploader;

/**
 *
 * @author Shashank
 */
public interface SuccessfulUploadsListener {
    void success(Uploader u) throws Exception;
}
