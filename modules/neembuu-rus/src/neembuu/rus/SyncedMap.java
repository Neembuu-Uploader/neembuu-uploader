/*
 * Copyright 2015 Shashank Tulsyan <shashaank at neembuu.com>.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class SyncedMap implements Map<String,V>{
    private final RusImpl r;

    public SyncedMap(RusImpl r) {
        this.r = r;
    }
    
    private final HashMap<String,V> hm = new HashMap<>();

    @Override public int size() {throw new UnsupportedOperationException();}
    @Override public boolean isEmpty() {
        boolean x = hm.isEmpty();
        if(x)return true;
        try {
            return Files.newDirectoryStream(r.p).iterator().hasNext();
            //return Files.list(r.p).findAny().isPresent();
        } catch (IOException ex) {
            Logger.getLogger(SyncedMap.class.getName()).log(Level.SEVERE, null, ex);
        }return false;
    }
    
    private String asStr(Object key){
        String s;
        if(key instanceof String)s=(String)key;
        else s = key.toString();
        return s;
    }
    
    @Override public V get(Object key) {
        if(key==null)return null;
        String s = asStr(key);
        
        V v = hm.get(key);
        if(v!=null)return v;
        
        return Rusila.get(r, s);
    }
    @Override public boolean containsKey(Object key) {
        if(key==null)return false;
        String s = asStr(key);
        
        boolean x = hm.containsKey(key);
        if(x)return true;
        V v = Rusila.get(r, s);
        return !v.isNull();
    }
    @Override public V put(String key, V value) {
        V x = hm.put(key, value);
        try{Rusila.set(r, key, value);}
        catch(Exception a){
            a.printStackTrace();
        }
        return x;
    }

    @Override public void putAll(Map<? extends String, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override public Set<Entry<String, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
    
}
