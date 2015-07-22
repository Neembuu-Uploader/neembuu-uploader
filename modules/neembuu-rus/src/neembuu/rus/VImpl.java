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

import neembuu.rus.type.TypeHandlerProvider;

/**
 *
 * @author Shashank
 */
public class VImpl implements V{
    private final String v;
    VImpl(String v) {this.v = v;}
    @Override public int i(int defaultValue) 
        { if(v==null)return defaultValue; return Integer.parseInt(v); }
    @Override public long l(long defaultValue) 
        { if(v==null)return defaultValue; return Long.parseLong(v); }
    @Override public double d(double defaultValue) 
        { if(v==null)return defaultValue; return Double.parseDouble(v);}
    @Override public String s(String defaultValue) 
        { if(v==null)return defaultValue; return v; }
    @Override public float f(float defaultValue) 
        {if(v==null)return defaultValue; return Float.parseFloat(v); }
    @Override public boolean b(boolean defaultValue) 
        {if(v==null)return defaultValue; return Boolean.parseBoolean(v); }

    @Override public boolean isNull() { return v == null; }

    @Override public Object o(DefaultValue dv,Rus r,String name,TypeHandlerProvider thp) {
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
        return thp.provideFor(dv.subElementType(),dv).handle(v, r,name, dv);
    }

    @Override public byte[] raw() {
        return v.getBytes();
    }

    @Override
    public <X> X o(Class<X> cast, X defaultValue) {
        if(String.class.isAssignableFrom(cast)){
            return v==null?defaultValue:(X)v;
        }else {
            
        }return defaultValue;
    }
    
    
    
}
