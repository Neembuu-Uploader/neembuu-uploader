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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import static neembuu.rus.Rusila.get;
import neembuu.rus.type.TypeHandlerProvider;

/**
 *
 * @author Shashank
 */
public final class RIterable implements Iterable {
    private final Path p;
    private final Rus r;
    private final DefaultValue dv;
    private final TypeHandlerProvider thp;

    public RIterable(Rus r, DefaultValue dv, TypeHandlerProvider thp) {
        this.r = r;
        this.dv = dv;
        this.thp = thp; 
        p = ((RusImpl)r).p;
    }

    public boolean add(Object e) {
        try {
            Rusila.set(r, Integer.toString((int)(Math.random()*Integer.MAX_VALUE)), e);
            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean remove(Object o) {
        for (Iterator it = this.iterator(); it.hasNext();) {
            Object object = it.next();
            if(object.equals(o)){
                it.remove(); return true;
            }
        }return false;
    }

    public void clear() {
        for (Iterator it = this.iterator(); it.hasNext();) {
            it.remove();
        }
    }    

    @Override
    public Iterator iterator(){
        try (final DirectoryStream<Path> ds = Files.newDirectoryStream(p)) {
            return new Iterator() {
                Path p = null;
                final Iterator<Path> it = ds.iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Object next() {
                    p = it.next();
                    RusImpl ri = new RusImpl(p);
                    if (Files.isDirectory(p)) {
                        Rusila r1 = Rusila.newInstance();
                        r1.thp(thp);
                        return r1.r(ri).I(dv.subElementType());
                    } else {
                        return get(r, p.getFileName().toString())
                                .o(dv, r, p.getFileName().toString(), thp);
                    }
                }

                @Override
                public void remove() {
                    if(p!=null)try {
                        Files.delete(p);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
        }catch(IOException ioe){
            throw new RuntimeException(ioe);
        }
    }
}