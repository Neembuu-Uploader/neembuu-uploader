/*
 * Copyright 2015 Shashank Tulsyan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neembuu.rus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 * @author Shashank
 */
public class SeekableByteChannel_wrap implements SeekableByteChannel{
    private final byte[]b;
    SeekableByteChannel_wrap(String v) {
        this.b = v.getBytes();
    }

    public SeekableByteChannel_wrap(Object o) throws IOException{
        this.b = toByteArray(o);
    }
    
    private int pos = 0;
    @Override public int read(ByteBuffer dst) throws IOException {
        int l = Math.min(dst.capacity(), b.length-pos);
        dst.put(b,pos,l);
        pos += l;
        return l;
    }
    @Override public int write(ByteBuffer src) throws IOException { throw new UnsupportedOperationException("Not supported yet."); }
    @Override public long position() throws IOException { return pos; }
    @Override public SeekableByteChannel position(long newPosition) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); }
    @Override public long size() throws IOException { return b.length; }
    @Override public SeekableByteChannel truncate(long size) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");}
    @Override public boolean isOpen() {return true;}
    @Override public void close() throws IOException {}
    
    static ByteBuffer toByteBuffer(Object yourObject)throws IOException{
        return ByteBuffer.wrap(toByteArray(yourObject));
    }
    
    static  byte[] toByteArray(Object yourObject)throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(yourObject);
            byte[] yourBytes = bos.toByteArray();
            return yourBytes;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }
    
    static Object fromByteArray(byte[]buf)throws IOException,ClassNotFoundException{
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return in.readObject();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }
};
