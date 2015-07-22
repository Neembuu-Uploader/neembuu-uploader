package neembuu.uploader.uploaders.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class overrides FileInputStream to calculate the progress value and set
 * it on a referenced AtomicInteger
 * 
 * @author vigneshwaran
 * @author davidepastore
 */
public class MonitoredInputStream extends InputStream {

    private InputStream upperStream;
    private long totalSize=0;
    private long uploadedSize=0;
    private AtomicInteger jp = null;
    
    private int read;
    private int progressValue;
    
    /* Speed logic */
    private StringBuffer speed;
    private long currentTimeMillis = System.currentTimeMillis();
    private long byteCounter;

    public MonitoredInputStream(InputStream upperStream) {
        this(upperStream,Long.MAX_VALUE,null);
    }

    public MonitoredInputStream(InputStream upperStream, long totalSize) {
        this(upperStream,totalSize,null);
    }

    public MonitoredInputStream(InputStream upperStream, long totalSize, AtomicInteger jp) {
        this.upperStream = upperStream;
        this.totalSize = totalSize;
        this.jp = jp;
    }
    
    public MonitoredInputStream(InputStream upperStream, long totalSize, AtomicInteger jp, StringBuffer speed) {
        this.upperStream = upperStream;
        this.totalSize = totalSize;
        this.jp = jp;
        this.speed = speed;
    }



    @Override
    public int read() throws IOException {
//        NULogger.getLogger().info("simple read");
        return upperStream.read();
    }

    @Override
    public int read(byte[]b) throws IOException {
        read = upperStream.read(b);
        if (read == -1) return -1;
        uploadedSize += read;
        progressValue = (int)((float)((uploadedSize*100)/totalSize));
        jp.set(progressValue);
        addToSpeed(read);
//        NULogger.getLogger().info("byte read: " + read);
//        NULogger.getLogger().info("uploaded size: "+ uploadedSize);
//        NULogger.getLogger().info("Total size: "+ totalSize);
//        NULogger.getLogger().info("progress value: "+ progressValue);
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        read = upperStream.read(b, off, len);
        if (read == -1) return -1;
        uploadedSize += read;
        progressValue = (int)((float)((uploadedSize*100)/totalSize));
        jp.set(progressValue);
        addToSpeed(read);
//        NULogger.getLogger().info("off read: " + progressValue);
        return read;
    }

    @Override
    public void close() throws IOException {
        upperStream.close();
        super.close();
    }

    /**
     * Add a val to the speed of the upload.
     * @param val the value to add to speed.
     */
    private void addToSpeed(int val) {
        if(speed != null){
            //Reset the byteCounter and set the new speed value
            if(currentTimeMillis + 1000 < System.currentTimeMillis()){
                speed.setLength(0); //Reset the value
                speed.append(CommonUploaderTasks.getSpeed(byteCounter));
//                NULogger.getLogger().log(Level.INFO, "Byte Counter: {0}", byteCounter);
                currentTimeMillis = System.currentTimeMillis();
                byteCounter = 0;
            }
            byteCounter+=val;
        }
    }

}
