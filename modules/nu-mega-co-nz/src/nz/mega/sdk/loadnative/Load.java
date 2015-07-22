/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.mega.sdk.loadnative;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;

/**
 *
 * @author Shashank
 */
public final class Load {

    private static volatile boolean loaded = false;

    public synchronized static void loadLibMega() throws Exception {
        if (loaded) {
            return;
        }
        Path p = Paths.get(System.getProperty("user.home"))
                .resolve(".neembuuuploader")
                .resolve("nativelibraries");

        // check OS, here we are assuming windows,
        // which is not correct
        InputStream is
                = Load.class.getResourceAsStream("libmega-0.dll");
        byte[]libasbytes = asByteArray(is);
        
        p = p.resolve("libmega.dll");
        if (!Files.exists(p)) {
            extract(p, libasbytes);
        }if(Files.size(p)!=libasbytes.length){
            extract(p, libasbytes);
        }
        System.load(p.toString());
        loaded = true;
    }
    
    private static void extract(Path dst,byte[]rawData)throws IOException{
        Files.write(dst, rawData, CREATE, WRITE, TRUNCATE_EXISTING);
    }

    private static byte[] asByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }
}
