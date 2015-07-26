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

import java.lang.reflect.Method;
import static neembuu.rus.Rusila.isWhiteList;
import static neembuu.rus.Rusila.isWhiteListAr;
import neembuu.rus.type.TypeHandler;
import neembuu.rus.type.TypeHandlerProvider;

/**
 *
 * @author Shashank
 */
final class Copier {
    static void overwrite(Rus dest, Class t,Object put,TypeHandlerProvider thp,DefaultValue dv){
        if(!t.isInterface()){
            throw new IllegalStateException("Only interfaces supported " + t);
        }
        TypeHandler th = thp.provideFor(t);
        if(th!=null){
            th.put(dest, put, dv);
            return ;
        }    
        Method[]ms=t.getMethods();
        for (Method method : ms) {
            if(Util.isGetter(method, null)){
                assert method.getReturnType().isAssignableFrom(t);
                merge(method, dest, t, put,thp);
            }
        }
    }
    
    static void merge(Method method,Rus dest, Class t, Object put,TypeHandlerProvider thp){
        try{
            Object v = method.invoke(put);
            if(isWhiteListAr(method.getReturnType())){
                setProperArray(dest, method, v, thp);
            }else {
                setProper(dest, method, v,thp);
            }
        }catch(Exception a){
            a.printStackTrace();
        }
    }
    
    static void copy(Rus src, Rus dest,Class t){
        if(!t.isInterface()){
            throw new IllegalStateException("Only interfaces supported " + t);
        }  
        Object sobj = Rusila.I(src, t);
        Object dobj = Rusila.I(dest, t);
        Method[]ms=t.getMethods();
        
        for (Method method : ms) {
            if(Util.isGetter(method, null)){
                copy(t,method, sobj, dobj);
            }
        }
    }
    
    private static void copy(Class t,Method m,Object sobj,Object dobj){
        try{
            Object v = m.invoke(sobj);
            Class cz = normalizeCz(v.getClass());
            Method w = t.getMethod(m.getName(), cz);
            
            if(Util.isSetter(w, v) ){
                invokeProper(w, dobj, v, cz);
                //w.invoke(dobj, cz.cast(v));
            }else {
                throw new IllegalStateException("Cannot copy, corresponding setter to "+m+" not found "+w);
            }
        }catch(Exception a){
            a.printStackTrace();
        }
    }
    
    private static Class normalizeCz(Class cz){
        if(cz == Integer.class)return int.class;
        if(cz == Long.class)return long.class;
        if(cz == Double.class)return double.class;
        if(cz == Float.class)return float.class;
        if(cz == Character.class)return char.class;
        if(cz == Boolean.class)return boolean.class;
        return cz;
    }
    
    private static void invokeProper(Method w,Object dobj,Object v,Class cz) throws Exception{
        if(cz == int.class){w.invoke(dobj, ((Integer)v).intValue());}
        else if(cz == long.class){w.invoke(dobj, ((Long)v).longValue());}
        else if(cz == double.class){w.invoke(dobj, ((Double)v).doubleValue());}
        else if(cz == float.class){w.invoke(dobj, ((Float)v).floatValue());}
        else if(cz == char.class){w.invoke(dobj, ((Character)v).charValue());}
        else if(cz == boolean.class){w.invoke(dobj, ((Boolean)v).booleanValue());}
        else w.invoke(dobj, v);
    }
    
    private static void setProper(Rus dest,Method method,Object v,
            TypeHandlerProvider thp) throws Exception{
        Class cz = method.getReturnType();
        
        if(isWhiteList(cz)){
            Rusila.set(dest, method.getName(), v);
        }else {
            // it is a Rusila
            Rus r2 = dest.r(method.getName());
            DefaultValue dv = null;
            
            try{  dv = method.getAnnotation(DefaultValue.class);} finally{}
            overwrite(r2, cz, v, thp,dv);
        }
    }
    
    
    private static void setProperArray(Rus dest,Method method,Object v,
            TypeHandlerProvider thp) throws Exception{
        Class cz = method.getReturnType();
        
        if(isWhiteListAr(cz)){
            String a = "";
            Object[]ax=(Object[])v;
            for (int i = 0; i < ax.length - 1; i++) {
                a = a+ax[i]+"\n";
            }
            if(ax.length>0){
                a = a+ax[ax.length - 1];
                Rusila.set(dest, method.getName(), a);
            }
        }else {
            throw new UnsupportedOperationException("this is getting complex");
        }
    }
}
