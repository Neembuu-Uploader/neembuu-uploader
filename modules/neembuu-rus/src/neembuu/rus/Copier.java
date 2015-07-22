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

/**
 *
 * @author Shashank
 */
final class Copier {
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
}
