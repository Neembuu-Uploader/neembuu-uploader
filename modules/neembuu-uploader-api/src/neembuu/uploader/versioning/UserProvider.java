/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.versioning;

/**
 *
 * @author Shashank
 */
public interface UserProvider {
    User getUserInstance();
    FileNameNormalizer getFileNameNormalizer();
}
