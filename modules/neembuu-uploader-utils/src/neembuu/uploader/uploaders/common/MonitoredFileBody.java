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
import org.apache.http.entity.mime.content.FileBody;

/**
 * This class overrides the FileBody. Use this in place of FileBody for uploading.
 * Otherwise you won't get the progress.
 *
 * @author vigneshwaran
 */
public class MonitoredFileBody extends FileBody {
    private final File file;
    private final AtomicInteger jp;
    private StringBuffer speed;
    
    public static final int DEFAULT_BUFFER_SIZE = 32*1024;
    
    //default value
    private static int bufferSize = DEFAULT_BUFFER_SIZE;//Increased default value
    
    public MonitoredFileBody(final File file, AtomicInteger jp, StringBuffer speed) {
        super(file);
        this.file = file;
        this.jp = jp;
        this.speed = speed;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int n = 0;//number of zeros to append
        ZeroAppendingInputStream zais = new ZeroAppendingInputStream(fis,n,file.length());
        return new MonitoredInputStream(zais,file.length()+n,jp,speed);
        //return new MonitoredInputStream(new FileInputStream(this.file),file.length(),jp,speed);
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        MonitoredInputStream in = new MonitoredInputStream(new FileInputStream(this.file),file.length(),jp,speed);
        try {
            byte[] tmp = new byte[bufferSize];// this is 1 place we might try to increase buffer size
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
