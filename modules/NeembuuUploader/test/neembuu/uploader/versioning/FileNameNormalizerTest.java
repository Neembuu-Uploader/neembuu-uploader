/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.versioning;

import neembuu.uploader.versioning.UserImpl;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Shashank Tulsyan
 */
public class FileNameNormalizerTest {
    private UserImpl fd;
    @Before
    public void init(){
        fd = new UserImpl((long)(Math.random()*Long.MAX_VALUE));
    }
    
    @Test
    public void testall(){
        assertEquals(fd.normalizeFileName("a"),"a_neembuu");
        assertEquals(fd.normalizeFileName("aasa"),"aasa_neembuu");
        assertEquals(fd.normalizeFileName("aasaas.txt"),"aasaas.neembuu.txt");
        assertEquals(fd.normalizeFileName("aasaas.tar.gz"),"aasaas.neembuu.tar.gz");
        assertEquals(fd.normalizeFileName("aasaas.part078.rar"),"aasaas.neembuu.part078.rar");
        assertEquals(fd.normalizeFileName("."),".neembuu.");
        assertEquals(fd.normalizeFileName(".."),".neembuu..");
        assertEquals(fd.normalizeFileName("a."),"a.neembuu.");
        assertEquals(fd.normalizeFileName("a.a"),"a.neembuu.a");
        assertEquals(fd.normalizeFileName(".a"),".neembuu.a");
        assertEquals(fd.normalizeFileName(".a.a"),".neembuu.a.a");
        assertEquals(fd.normalizeFileName("..a.a"),"..neembuu.a.a");
    }

}
