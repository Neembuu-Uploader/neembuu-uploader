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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shashank
 */
public final class InterfaceInstanceCreator implements InvocationHandler{
    public static final <E> E create(Class<E>clazz){
        return InterfaceInstanceCreator.create(initializeDefaults(clazz),clazz);
    }
    public static final <E> E create(Map values,Class<E>clazz){
        return (E)Proxy.newProxyInstance(InterfaceInstanceCreator.class.getClassLoader(),
                new Class[]{clazz}, new InterfaceInstanceCreator((Map<String,String>)values,clazz));
    }
    
    private final HashMap<String,String> values = new HashMap<>();
    private final HashMap<String,String> defaultValues;
    
    public static final Map<String,String> defaultValues(Class clazz){
        return (initializeDefaults(clazz));
    }

    public InterfaceInstanceCreator(Map<String,String> values,Class clazz) {
        defaultValues = initializeDefaults(clazz);
        if(values==null) {
        } else {
            this.values.putAll(values);
        }
    }
    
    public final String getByName(String mn){
        String val = values.get(mn);
        if(val==null){
             val = defaultValues.get(mn);
        }
        return val;
    }
    
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String mn = method.getName();
        return getByName(mn);
    }
    
    private static HashMap<String,String> initializeDefaults(Class clazz){
        HashMap<String,String> dvs = new HashMap<>();
        Method[]ms = clazz.getDeclaredMethods();
        for (Method m : ms) {
            DefaultValue dv = (DefaultValue)m.getAnnotation(DefaultValue.class);
            if(dv==null)continue;
            dvs.put(m.getName(), dv.s());
        }
        return dvs;
    }
}
