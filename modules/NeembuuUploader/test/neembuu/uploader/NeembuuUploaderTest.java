/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader;

import neembuu.uploader.NeembuuUploader;
import static junit.framework.Assert.*;
/**
 *
 * @author Shashank Tulsyan
 */
public class NeembuuUploaderTest {
    public static void main(String[] args) {

        assertEquals("2.9", NeembuuUploader.getVersionNumber(2.9f));
        assertEquals("2.9.1",NeembuuUploader.getVersionNumber(2.91f));
        assertEquals("2.9.12212",NeembuuUploader.getVersionNumber(2.912212f));
        assertEquals("2.0",NeembuuUploader.getVersionNumber(2f));
        assertEquals("0.9",NeembuuUploader.getVersionNumber(.9f));
        assertEquals("0.0.9",NeembuuUploader.getVersionNumber(.09f));
        assertEquals("0.0",NeembuuUploader.getVersionNumber(0f));
        assertEquals("0.0",NeembuuUploader.getVersionNumber(0.0f));
        assertEquals("19.0",NeembuuUploader.getVersionNumber(19f));
        assertEquals("19.1",NeembuuUploader.getVersionNumber(19.1f));
        assertEquals("19.1.2",NeembuuUploader.getVersionNumber(19.12f));
        assertEquals("19.1.2121",NeembuuUploader.getVersionNumber(19.12121f));
    }
}
