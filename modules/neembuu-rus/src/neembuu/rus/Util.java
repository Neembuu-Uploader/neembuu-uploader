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
public class Util {
    public static boolean isGetter(Method method,Object[] args){
        Class retType = method.getReturnType();
        if(args==null || args.length==0){
            return (retType != Void.TYPE);
        }return false;
    }
    
    public static boolean isSetter(Method method,Object... args){
        UnsupportedOperationException ns = new UnsupportedOperationException("Not supported yet.");
        Class retType = method.getReturnType();
        if(args.length != method.getParameterTypes().length)
            throw new IllegalStateException("Params don't match");
        if(args.length>1)throw ns;
            
        return (retType == Void.TYPE);
        //return true;
    }
}
