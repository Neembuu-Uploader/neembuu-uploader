/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.versioning;

/**
 *
 * @author Shashank Tulsyan
 */
public interface FileNameNormalizer {

    String normalizeFileName(String fn, int fileNameLengthLimit);

    String normalizeFileName(String fn);
    
}
