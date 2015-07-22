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

package neembuu.rus.type;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import neembuu.rus.DefaultValue;
import neembuu.rus.MapRus;
import neembuu.rus.Rus;
import neembuu.rus.Rusila;
import neembuu.rus.Util;
import neembuu.rus.V;
import neembuu.rus.VImpl;
import neembuu.rus.VObj;

/**
 *
 * @author Shashank
 */
public final class TypeImplementor<E> implements InvocationHandler{
    private final Rus r; private final TypeHandlerProvider thp;
    private final boolean cachingEnabled;
    private final HashMap cache = new HashMap();//reading each time from disk 
    // is more expensive, un-necessary and weird HOWEVER
    // caching introduces clear possibility of not having the latest value
    // if two separate objects of this are being used in different thread 
    // at the same time.

    public TypeImplementor(Rus r, TypeHandlerProvider thp) {
        this(r, thp, true);
    }
    public TypeImplementor(Rus r, TypeHandlerProvider thp,boolean cachingEnabled) {
        this.r = r; 
        this.cachingEnabled = cachingEnabled; 
        this.thp = thp;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(!cachingEnabled)return invoke2(proxy, method, args);
        Class retType = method.getReturnType();
        synchronized (cache){
            if(Util.isGetter(method, args)){
                Object result = cache.get(method.getName());
                if(result==null){
                    result = handleGettters(proxy, method, retType);
                    cache.put(method.getName(),result);
                }return result;
            }
            if(Util.isSetter(method, args)){
                handleSettters(proxy, method, args[0]);
                cache.put(method.getName(),args[0]);
            }
        }
        return null;
    }
    
    private Object invoke2(Object proxy, Method method, Object[] args) throws Throwable {
        Class retType = method.getReturnType();
        if(Util.isGetter(method, args)){
            return handleGettters(proxy, method, retType);
        }
        if(Util.isSetter(method, args)){
            handleSettters(proxy, method, args[0]);
        }return null;
    }
    
    private Object handleGettters(Object proxy, Method m,Class retType){
        DefaultValue dv = (DefaultValue)m.getAnnotation(DefaultValue.class);
        
        if(r.isDirectory(m.getName())){
            TypeHandler th = thp.provideFor(retType);
            if(th!=null)return th.handle(r.r(m.getName()), dv);
            else {
                Rusila r1 = Rusila.newInstance();
                r1.r(r.r(m.getName()));
                r1.thp(thp);
                return r1.I(m.getReturnType());
            }
        }
        V v;
        if(r instanceof MapRus){
            Object val = ((MapRus)r).get(m.getName());
            v= new VObj(val);
        }else{
            v = Rusila.get(r, m.getName(),dv==null?4*1024:dv.maximumDataSize());
        }
        
        if(retType.isAssignableFrom(Double.TYPE)){
            return v.d(dv==null?0d:dv.d());
        }else if(retType.isAssignableFrom(Integer.TYPE)){
            return v.i(dv==null?0:dv.i());
        }else if(retType.isAssignableFrom(Long.TYPE)){
            return v.l(dv==null?0:dv.l());
        }else if(retType.isAssignableFrom(Boolean.TYPE)){
            return v.b(dv==null?false:dv.b());
        }else if(retType.isAssignableFrom(String.class)){
            return v.s(dv==null?"":dv.s());
        }else if(retType.isArray()){
            return handleGettersArrays(proxy, m, retType,dv,v);
        }
        ValueHandler vh = thp.provideFor(retType,dv);
        return vh.handle(v.s(dv==null?"":dv.s()), r, m.getName(), dv);
    }
    
    private Object handleGettersArrays(Object proxy, Method m,Class retType,
            DefaultValue dv,V v){
        String values = v.s("");
        String[]valA = values.replaceAll("\r", "").split("\n");
        
        if(retType.isAssignableFrom(double[].class)){
            if(v.isNull())return dv.da();
            double[]array = (double[]) Array.newInstance(double.class, valA.length);
            for (int i = 0; i < valA.length; i++) {
                array[i] = Double.parseDouble(valA[i]);
            }
            return array;
        }else if(retType.isAssignableFrom(int[].class)){
            if(v.isNull())return dv.ia();
            int[]array = (int[]) Array.newInstance(int.class, valA.length);
            for (int i = 0; i < valA.length; i++) {
                array[i] = Integer.parseInt(valA[i]);
            }
            return array;
        }else if(retType.isAssignableFrom(long[].class)){
            if(v.isNull())return dv.la();
            long[]array = (long[]) Array.newInstance(long.class, valA.length);
            for (int i = 0; i < valA.length; i++) {
                array[i] = Long.parseLong(valA[i]);
            }
            return array;
        }else if(retType.isAssignableFrom(boolean[].class)){
            if(v.isNull())return dv.ba();
            boolean[]array = (boolean []) Array.newInstance(boolean.class, valA.length);
            for (int i = 0; i < valA.length; i++) {
                array[i] = Boolean.parseBoolean(valA[i]);
            }
            return array;
        }else if(retType.isAssignableFrom(String[].class)){
            if(v.isNull())return dv.sa();
            return valA;
        }
        return null;
    }
    
    
    
    private void handleSettters(Object proxy, Method m, Object arg)throws Exception{
        if(r instanceof MapRus){
            ((MapRus)r).put(m.getName(),arg);
            return;
        }
        Rusila.set(r, m.getName(), arg);
    }
}
