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

import java.util.LinkedList;
import java.util.List;
import neembuu.rus.DefaultValue;

/**
 *
 * @author Shashank
 */
public final class DefaultTypeHandlerProvider implements TypeHandlerProvider {
    private final List<TypeHandler> t = new LinkedList<>();
    private final List<ValueHandler> v = new LinkedList<>();

    public DefaultTypeHandlerProvider() {
        register(new IterableHandler(this));
    }
    
    @Override public TypeHandler provideFor(Class m) {
        for (TypeHandler typeHandler : thandlers()) {
            if(m.isAssignableFrom(typeHandler.type())
                    || typeHandler.type().isAssignableFrom(m) ){
                return typeHandler;
            }
        }return null;
    }

    @Override public ValueHandler provideFor(Class m,DefaultValue dv) {
        for (ValueHandler valueHandler : vhandlers()) {
            if(m.isAssignableFrom(valueHandler.type())
                    || valueHandler.type().isAssignableFrom(m) ){
                return valueHandler;
            }
        }return null;
    }
    
    private TypeHandler[] thandlers(){
        TypeHandler[]ths;
        synchronized (t){
            ths = t.toArray(new TypeHandler[t.size()]);
        }return ths;
    }
    
    private ValueHandler[] vhandlers(){
        ValueHandler[]vhs;
        synchronized (v){
            vhs = v.toArray(new ValueHandler[v.size()]);
        }return vhs;
    }
    
    @Override public final void register(TypeHandler th) {
        synchronized (t){
            if(!th.type().isInterface()){
                throw new IllegalStateException("Only interfaces supported");
            }
            for (TypeHandler typeHandler : thandlers()) {
                if(typeHandler.type().equals(th.type())){
                    throw new IllegalStateException("Already registered "+typeHandler
                    + " to handle class "+typeHandler.type());
                }
            }
            t.add(th);
        }
    }

    @Override public void unregister(TypeHandler th) {
        synchronized (t){ t.remove(th);}
    }
    
    @Override public final void register(ValueHandler vh) {
        synchronized (v){
            for (ValueHandler valueHandler : vhandlers()) {
                if(valueHandler.type().equals(vh.type())){
                    throw new IllegalStateException("Already registered "+valueHandler
                    + " to handle class "+valueHandler.type());
                }
            }
            v.add(vh);
        }
    }

    @Override public void unregister(ValueHandler vh) {
        synchronized (v){ v.remove(vh);}
    }
    
}
