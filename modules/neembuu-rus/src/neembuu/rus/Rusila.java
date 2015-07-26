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
import neembuu.rus.type.TypeImplementor;
import neembuu.rus.type.TypeHandlerProvider;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import neembuu.rus.type.DefaultTypeHandlerProvider;

/**
 *
 * @author Shashank
 */
public final class Rusila {
    private TypeHandlerProvider thp = new DefaultTypeHandlerProvider();
    private Rus r;private boolean cache;

    private Rusila() {}
    
    public static Rus create(Object base){
        if(base instanceof Path){
            return new RusImpl((Path)base);
        }if(base instanceof Class){
            return ClassRus.I((Class)base);
        }if(base instanceof Map){
            return new MapRus((Map)base,null);
        }
        throw new UnsupportedOperationException("Cannot extract rus out of "+base);
    }
    
    public Rusila r(Rus s){
        this.r = s;
        return this;
    }
    
    public Rusila thp(TypeHandlerProvider thp){
        this.thp = thp;
        return this;
    }
    
    public TypeHandlerProvider thp(){
        return thp;
    }
    
    public static Rusila newInstance(){
        return new Rusila();
    }
    
    public Rusila cache(boolean cache){
        this.cache = cache;
        return this;
    }
    
    public static <E> SyncMap<E> s(Rus r,Class<E> template){
        return new SyncedMapImpl(r,template);
    }
    
    public <E> E I(Class<E>interfaceDefinition){
        if(!interfaceDefinition.isInterface()){
            throw new IllegalStateException("Only interfaces supported " + interfaceDefinition);
        }
        return (E)Proxy.newProxyInstance(Rusila.class.getClassLoader(), new Class[]{interfaceDefinition}, 
                new TypeImplementor(r,thp));
    }
    
    public static <E> E I(Rus r,Class<E>interfaceDefinition){
        return I(r, interfaceDefinition, new DefaultTypeHandlerProvider());
    }
    
    public static <E> E put(Rus r,Class<E>interfaceDefinition,E value){
        Copier.overwrite(r, interfaceDefinition, value, new DefaultTypeHandlerProvider(),null);
        E e = I(r, interfaceDefinition);
        return e;
    }
    
    public static <E> E cast(Map m, Class<E>interfaceDefinition){
        return I(new MapRus(m, null), interfaceDefinition);
    }
    
    public static void copy(Rus src, Rus dest,Class interfaceDefinition){
        Copier.copy(src, dest, interfaceDefinition);
    }
    
    public static <E> E I(Rus r,Class<E>interfaceDefinition,TypeHandlerProvider thp){
        return I(r, interfaceDefinition, thp, true);
    }
    
    public static <E> E I(Rus r,Class<E>interfaceDefinition,TypeHandlerProvider thp,boolean cachingEnabled){
        if(!interfaceDefinition.isInterface()){
            throw new IllegalStateException("Only interfaces supported " + interfaceDefinition);
        }
        if(r instanceof MapRus){cachingEnabled = false;}
        return (E)Proxy.newProxyInstance(Rusila.class.getClassLoader(), new Class[]{interfaceDefinition}, 
                new TypeImplementor(r,thp,cachingEnabled));
    }
    
    public static Map<String,V> getMap(Rus r){
        HashMap<String,V> hm = new HashMap();
        try{
            Path p = ((RusImpl)r).p;
            
            DirectoryStream<Path> ds = Files.newDirectoryStream(p);
            for(Path pth : ds){
                if(Files.isDirectory(pth))continue;
                
                String n = pth.getFileName().toString();
                hm.put(n, get(r, n));
            }
        }catch(Exception a){
            a.printStackTrace();
        }
        return hm;
    }
    
    public static V[] getArray(Rus r){
        List<V> l = getList(r);
        return l.toArray(new V[l.size()]);
    }
    
    public static RIterable i(final Rus r,final DefaultValue dv,final TypeHandlerProvider thp){
        return new RIterable(r, dv, thp);
    }
    
    public static List<V> getList(Rus r){
        List<V> l = new LinkedList<>();
        try{
            Path p = ((RusImpl)r).p;
            
            DirectoryStream<Path> ds = Files.newDirectoryStream(p);
            for(Path pth : ds){
                if(Files.isDirectory(pth))continue;
                String n = pth.getFileName().toString();
                l.add(get(r, n));
            }
        }catch(Exception a){
            a.printStackTrace();
        }
        return Collections.unmodifiableList(l);
    }
    
    public static V get(Rus r,String n){
        return get(r, n, -1); //4*1024
    }
        
    public static V get(Rus r,String n, int maxDataSize){
        try{
            return new VImpl(getVStr(r, n, maxDataSize));
        }catch(java.nio.file.NoSuchFileException nsfe){
            // ignore
        }catch(Exception a){
            a.printStackTrace();
        }
        return new VImpl(null);
    }
    
    public static V v(Object o){
        return new VObj(o);
    }
    
    private static String getVStr(Rus r,String n, int maxDataSize)throws IOException{
        String v;
        if(r instanceof ClassRus){
            return ((ClassRus)r).v(n);
        }else {
            SeekableByteChannel dp = r.p(n, StandardOpenOption.READ,StandardOpenOption.CREATE);
            if(maxDataSize < 0) {
                v = getVStrF(dp);
            }else{
                ByteBuffer bb=ByteBuffer.allocate(Math.min((int)dp.size(),maxDataSize));
                dp.read(bb);
                v = new String(bb.array(),Charset.forName("UTF-8"));
            }
            try{dp.close();}catch(Exception a){a.printStackTrace();}
        }
        return v;
    }
    
    private static String getVStrF(SeekableByteChannel sbc)throws IOException{
        StringBuilder sb = new StringBuilder();
        ByteBuffer bf = ByteBuffer.allocate(4*1024);
        int i = 0;
        while ((i = sbc.read(bf)) > 0) {
            bf.flip();
            sb.append(Charset.forName("UTF-8").decode(bf));
            bf.clear();
        } return sb.toString();
    }
    
    public static void set(Rus r,String n,V v)throws Exception{
        SeekableByteChannel dp = r.p(
                        n, 
                        StandardOpenOption.WRITE,StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
        dp.write(ByteBuffer.wrap(v.raw()));
        try{dp.close();}catch(Exception a){a.printStackTrace();}
    }
    
    public static void set(Rus r,String n,Object v)throws Exception{
        SeekableByteChannel dp = r.p(
                        n, 
                        StandardOpenOption.WRITE,StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
        ByteBuffer bb;
        if(v instanceof Integer  || v instanceof Long || v instanceof Double || v instanceof Float 
                || v instanceof Boolean || v instanceof Character){
            // explicitly specifying charset to avoid localization 
            bb=ByteBuffer.wrap(v.toString().getBytes(Charset.forName("UTF-8")));
        }else if(v instanceof String){
            bb=ByteBuffer.wrap(((String)v).getBytes(Charset.forName("UTF-8")));
        }else {
            bb=SeekableByteChannel_wrap.toByteBuffer(v);
        }
        dp.write(bb);
        try{dp.close();}catch(Exception a){a.printStackTrace();}
    }
    
    
    public static boolean isWhiteList(Class x){
        return checkContains(x, whiteList);
    }
    
    public static boolean isWhiteListAr(Class x){
        return checkContains(x, whiteListAr);
    }
    
    private static boolean checkContains(Class x, Class[]s){
        if(x==null)return false;
        for (Class cz : s) {
            if(cz == x || cz.equals(x) || x.isAssignableFrom(cz) ){
                return true;
            }
        }
        return false;
    }
    
    static final Class[] whiteList = 
        new Class[]{int.class,long.class,double.class,
            float.class,char.class,boolean.class,String.class};
    
    static final Class[] whiteListAr = 
        new Class[]{int[].class,long[].class,double[].class,
            float[].class,char[].class,boolean[].class,String[].class};
}
