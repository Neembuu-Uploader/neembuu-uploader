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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import neembuu.rus.type.TypeHandlerProvider;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class VObj implements V{
    private final Object w;

    public VObj(Object w) {
        this.w = w;
    }
    
    @Override
    public int i(int defaultValue) {
        if(isNull())return defaultValue;
        if(w instanceof Integer)return (Integer)w;
        String s;
        if(w instanceof String)s = (String)w;
        else s = w.toString();
        return Integer.parseInt(s);
    }

    @Override
    public long l(long defaultValue) {
        if(isNull())return defaultValue;
        if(w instanceof Long)return (Long)w;
        String s;
        if(w instanceof String)s = (String)w;
        else s = w.toString();
        return Long.parseLong(s);
    }

    @Override
    public double d(double defaultValue) {
        if(isNull())return defaultValue;
        if(w instanceof Double)return (Double)w;
        String s;
        if(w instanceof String)s = (String)w;
        else s = w.toString();
        return Double.parseDouble(s);
    }

    @Override
    public float f(float defaultValue) {
        if(isNull())return defaultValue;
        if(w instanceof Float)return (Float)w;
        String s;
        if(w instanceof String)s = (String)w;
        else s = w.toString();
        return Float.parseFloat(s);
    }

    @Override
    public String s(String defaultValue) {
        if(isNull())return defaultValue;
        String s;
        if(w instanceof String)s = (String)w;
        else s = w.toString();
        return s;
    }

    @Override
    public boolean b(boolean defaultValue) {
        if(isNull())return defaultValue;
        if(w instanceof Boolean)return (Boolean)w;
        String s;
        if(w instanceof String)s = (String)w;
        else s = w.toString();
        return Boolean.parseBoolean(s);
    }

    @Override
    public boolean isNull() {
        return w==null || w.toString()==null;
    }

    @Override
    public Object o(DefaultValue dv, Rus r, String name, TypeHandlerProvider thp) {
        if(dv.subElementType().isAssignableFrom(int.class)){
            return i(dv.i());
        }else if(dv.subElementType().isAssignableFrom(double.class)){
            return d(dv.d());
        }else if(dv.subElementType().isAssignableFrom(float.class)){
            return f(dv.f());
        }else if(dv.subElementType().isAssignableFrom(boolean.class)){
            return b(dv.b());
        }else if(dv.subElementType().isAssignableFrom(long.class)){
            return l(dv.l());
        }else if(dv.subElementType().isAssignableFrom(String.class)){
            return s(dv.s());
        }
        return thp.provideFor(dv.subElementType(),dv).handle(s(null), r,name, dv);
    }

    @Override
    public byte[] raw() {
        try{
            if(w instanceof String)return ((String)w).getBytes();
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(w);
            os.flush();
            return os.toByteArray();
        }catch(IOException ioe){
            return w.toString().getBytes();
        }
    }

    @Override
    public <X> X o(Class<X> cast, X defaultValue) {
        if(cast.isAssignableFrom(w.getClass())){
            return w==null?defaultValue:((X)w);
        }return defaultValue;
    }
    
}
