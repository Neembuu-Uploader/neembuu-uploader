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

import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Shashank
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
    long        l() default 0;
    int         i() default 0;
    double      d() default 0d;
    float       f() default 0f;
    String      s() default "";
    boolean     b() default false;
    
    long    []  la() default {};
    int     []  ia() default {};
    double  []  da() default {};
    float   []  fa() default {};
    String  []  sa() default {};
    boolean []  ba() default {};
    
    int maximumDataSize() default 4*1024;
    
    Class subElementType() default String.class;
    
    int PROPERTY_NOT_FOUND = -1;
}
