/* 
 * Copyright (C) 2015 Shashank Tulsyan <shashaank at neembuu.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package neembuu.uploader.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class UnsyncCopy {
    public static <K,V> Set<Entry<K,V>> unsyncCopyEntries(Map<K,V> m){
        final Set<Entry<K,V>> s = new HashSet<>();
        synchronized (m){
            for (Entry<K,V> e : m.entrySet()) {
                s.add(e);
            }
        }return s;
    }
    
    public static <K,V> Set<K> unsyncCopyKeys(Map<K,V> m){
        final Set<K> s = new HashSet<>();
        synchronized (m){
            for (K k : m.keySet()) {
                s.add(k);
            }
        }return s;
    }
    
    public static <K,V> Map<K,V> unsyncMap(Map<K,V> m){
        final Map<K,V> m2 = new HashMap<>();
        synchronized (m){
            for (Entry<K,V> e : m.entrySet()) {
                m2.put(e.getKey(), e.getValue());
            }
        }return m2;
    }
}
