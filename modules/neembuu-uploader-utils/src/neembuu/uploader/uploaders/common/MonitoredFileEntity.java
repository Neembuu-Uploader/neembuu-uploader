/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.entity.FileEntity;

/**
 * This class overrides the FileEntity. Use this in place of FileEntity for uploading
 * when the upload is in the body without an associated key.
 * Maybe you will need to set the <b>Content-Type</b> of the request to <b>application/octet-stream</b>.
 * Otherwise you won't get the progress.
 * @author davidepastore
 */
public class MonitoredFileEntity extends FileEntity{
    private final File file;
    private final AtomicInteger jp;
    private StringBuffer speed;
    
    //Default value
    private static int bufferSize = MonitoredFileBody.DEFAULT_BUFFER_SIZE;//Increased default value to 20KB;
    
    public MonitoredFileEntity(final File file,AtomicInteger jp, StringBuffer speed){
        super(file);
        this.file = file;
        this.jp = jp;
        this.speed = speed;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new MonitoredInputStream(new FileInputStream(this.file),file.length(),jp,speed);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        MonitoredInputStream in = new MonitoredInputStream(new FileInputStream(this.file),file.length(),jp,speed);
        try {
            byte[] tmp = new byte[bufferSize];
            int l;
            while ((l = in.read(tmp)) != -1) {
                out.write(tmp, 0, l);
            }
            out.flush();
        } finally {
            in.close();
        }
    }
    
    
    /**
     * Set the buffer size.
     * @param newBufferSize the new buffer size.
     */
    public static void setBufferSize(int newBufferSize){
        bufferSize = newBufferSize;
    }
    
    
}
