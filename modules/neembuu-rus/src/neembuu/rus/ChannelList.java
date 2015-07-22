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
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Shashank Tulsyan
 */
final class ChannelList<E extends SeekableByteChannel> implements Collection<E> {
    private final ArrayList<SBCRef> a = new ArrayList<SBCRef>();

    ChannelList() {
    }
    
    public void close()throws IOException{
        IOException ioe = null;
        for (SBCRef e : a) {
            if(!e.isOpen()) continue;
            try {
                e.onlyClose();
            } catch (IOException a) {
                if (ioe == null) {
                    ioe = new IOException();
                }
                ioe.addSuppressed(a);
            }
        }
        a.clear();
        if(ioe!=null){
            throw ioe;
        }
    }

    @Override public int size() {
        return a.size();
    }

    @Override public boolean isEmpty() {
        return a.isEmpty();
    }

    @Override public boolean contains(Object o) {
        return a.contains(o);
    }


    @Override public Object[] toArray() {
        return a.toArray();
    }

    @Override public <T> T[] toArray(T[] a) {
        return this.a.toArray(a);
    }

    @Override public boolean add(E e) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*SBCRef ref = new SBCRef(e, this);
        return a.add(ref);*/
    }
    
    public E addAndGet(E e) {
        SBCRef ref = new SBCRef(e, this);
        a.add(ref);
        return (E)ref;
    }

    @Override public boolean remove(Object o) {
        if(o instanceof  SBCRef){
            try{
                SBCRef  sbcr = ((SBCRef)o);
                if(sbcr.isOpen())
                    sbcr.onlyClose();
            }catch(IOException w){w.printStackTrace();}
        }
        return a.remove(o);
    }

    @Override public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public Iterator<E> iterator() {
        final Iterator it = a.iterator();
        return new Iterator<E>() {
            @Override public boolean hasNext() { return it.hasNext(); }
            @Override public E next() { return (E)it.next(); }
            @Override public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Override public boolean containsAll(Collection<?> c) {
        return a.containsAll(c);
    }

    
}
