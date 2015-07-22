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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Collection;

/**
 *
 * @author Shashank Tulsyan
 */
final class SBCRef implements SeekableByteChannel{
    private final SeekableByteChannel sbc;
    private final Collection parent;

    SBCRef(SeekableByteChannel sbc, Collection c) {
        this.sbc = sbc;
        this.parent = c;
    }
    
    @Override
    public int read(ByteBuffer dst) throws IOException {
        return sbc.read(dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return sbc.write(src);
    }

    @Override
    public long position() throws IOException {
        return sbc.position();
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        return sbc.position(newPosition);
    }

    @Override
    public long size() throws IOException {
        return sbc.size();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        return sbc.truncate(size);
    }

    @Override
    public boolean isOpen() {
        return sbc.isOpen();
    }

    void onlyClose() throws IOException {
        sbc.close();
    }
    
    @Override
    public void close() throws IOException {
        sbc.close();
        parent.remove(this);
    }
    
}
