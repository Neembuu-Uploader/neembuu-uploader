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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import static neembuu.rus.Rusila.get;
import neembuu.rus.type.TypeHandlerProvider;

/**
 *
 * @author Shashank
 */
final class RIterable implements Iterable {
    private final Rus r;
    private final DefaultValue dv;
    private final TypeHandlerProvider thp;

    RIterable(Rus r, DefaultValue dv, TypeHandlerProvider thp) {
        this.r = r;
        this.dv = dv;
        this.thp = thp; 
    }

    public boolean add(Object e) {
        try {
            Rusila.set(r, String.valueOf(e.hashCode()), e);
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
        return new Iterator() {
            Path p = null;
            final Iterator<String> it = r.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Object next() {
                String n = it.next(); if(n==null)return null;
                Rus ri = r.r(it.next());
                if (Files.isDirectory(p)) {
                    Rusila r1 = Rusila.newInstance();
                    r1.thp(thp);
                    Class subElementType = dv==null?String.class:dv.subElementType();
                    return r1.r(ri).I(subElementType);
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
    }
}