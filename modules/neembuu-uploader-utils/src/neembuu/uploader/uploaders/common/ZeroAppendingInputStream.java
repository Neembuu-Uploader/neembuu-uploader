package neembuu.uploader.uploaders.common;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class ZeroAppendingInputStream extends InputStream {
    private final InputStream is;
    private final int n;
    private final long size;
    
    private int pos = 0;

    public ZeroAppendingInputStream(InputStream is, int n, long size) {
        this.is = is;
        this.n = n;
        this.size = size;
    }
   
    @Override
    public int read() throws IOException {
        // this is a really bad implementation might slow down uploading,
        // see http://stackoverflow.com/questions/760228/how-do-you-merge-two-input-streams-in-java
        // for better approach
        // basically read(byte[]b is more efficient and should be overrided.
        if(pos>size+n)throw new IOException("EOF");
        pos++;
        if(pos>size){
            return 0;
        }
        
        return is.read();
    }
    
}
