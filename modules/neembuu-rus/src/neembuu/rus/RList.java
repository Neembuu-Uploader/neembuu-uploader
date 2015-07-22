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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import neembuu.rus.type.TypeHandlerProvider;

/**
 *
 * @author Shashank
 */
public class RList implements List{
    private final HashMap hm = new HashMap();
    
    private final Rus r;
    private final DefaultValue dv;
    private final TypeHandlerProvider thp;

    public RList(Rus r, DefaultValue dv, TypeHandlerProvider thp) {
        this.r = r;
        this.dv = dv;
        this.thp = thp;
        
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(((RusImpl)r).p)){
            for(Path p : ds){
                String n = p.getFileName().toString();
                if(n.contains("."))continue;
                if(Files.isDirectory(p)){
                    hm.put(Rusila.I(r, dv.subElementType(),thp),p);
                }else {
                    hm.put(Rusila.get(r, n).o(dv, r, n, thp),p);
                }
                
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
    }

    @Override
    public int size() {
        return hm.size();
    }

    @Override
    public boolean isEmpty() {
        return hm.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return hm.containsValue(o);
    }

    @Override public Iterator iterator() {
        final Iterator i = hm.keySet().iterator();
        return new Iterator() {
            Object last = null;
            @Override public boolean hasNext() {
                return i.hasNext();
            }@Override public Object next() {
                last = i.next();
                return last;
            }@Override public void remove() {
                Path p = (Path)hm.get(last);
                try{ delete(p); i.remove();}
                catch(IOException ioe){ throw new RuntimeException(ioe); }
            }};
    }
    
    private void delete(Path p)throws IOException{
        if(Files.isDirectory(p)){
            Files.walkFileTree(p, new FileVisitor<Path>() {
                @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }@Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }@Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }@Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }else { Files.delete(p); }
    }

    @Override
    public Object[] toArray() {
        Object[]s=new Object[size()]; int i = 0;
        for (Iterator it = this.iterator(); it.hasNext();) {
            Object object = it.next();
            s[i]=object; i++;
        }return s;
    }

    @Override
    public Object[] toArray(Object[] a) {
        return Arrays.copyOf(toArray(), size(), a.getClass());
    }

    @Override
    public boolean add(Object e) {
        Path p = makeRandomNew();
        //Files.createDirectory(p);
        //hm.put(e, p);
        //Rusila.
        throw new UnsupportedOperationException();
    }
    
    private Path makeRandomNew(){
        Path p = null;
        while(p==null || Files.exists(p)){
            p = ((RusImpl)r).p.resolve((int)(Math.random()*Integer.MAX_VALUE)+"");
        } return p;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object get(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object remove(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListIterator listIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ListIterator listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
