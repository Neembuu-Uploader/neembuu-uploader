/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nu_javafx_sample.loadexternal;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Shashank
 */
public class ZipClassLoader extends ClassLoader {
    private final FileSystem zipfs;

    public ZipClassLoader(FileSystem zipfs) {
        this.zipfs = zipfs;
    }

    @Override protected Class findClass(String name) throws ClassNotFoundException {
        Path entry = this.zipfs.getPath('/'+name.replace('.', '/') + ".class");
        if (entry == null) {
            throw new ClassNotFoundException(name);
        }
        try {
            /*byte[] array = new byte[1024];
            InputStream in = zipfs.provider().newInputStream(entry);
            ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
            int length = in.read(array);
            while (length > 0) {
                out.write(array, 0, length);
                length = in.read(array);
            }
            return defineClass(name, out.toByteArray(), 0, out.size());*/
            byte[]b=Files.readAllBytes(entry);
            return defineClass(name, b, 0, b.length);
        }
        catch (IOException exception) {
            throw new ClassNotFoundException(name, exception);
        }
    }
}
