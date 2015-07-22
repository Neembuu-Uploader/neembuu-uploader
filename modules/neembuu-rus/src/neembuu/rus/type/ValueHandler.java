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

import neembuu.rus.DefaultValue;
import neembuu.rus.Rus;

/**
 *
 * @author Shashank
 */
public interface ValueHandler {
    Class type();
    Object handle(String tempValue,Rus r,String name,DefaultValue dv);
}
