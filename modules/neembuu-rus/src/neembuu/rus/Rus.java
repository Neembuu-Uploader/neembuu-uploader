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

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;

/**
 * Rus is a wrapper for properties whoes name, type & source are subject to rapid evolution.
 * Properties can be accessed via their String names.
 * Or by mapping Rus objects to based on an interface ( template ).
 * Corresponding data manipulated by Rus may be saved to disk/HashMap/Class resource.
 * Rus is not meant to be used with or replace databases.
 * However this is so lo level that you very well may do so.
 * 
 * Typical usage of Rus would me.
 * <pre>
 * Rus r = .... obtain rus from a resource 
 * SomeDataInterfaceTemplate obj = Rusila.I(r,SomeDataInterfaceTemplate.class);
 * obj....call functions
 * </pre>
 * @author Shashank
 */
public interface Rus extends Closeable,Iterable<String> {
    Rus r(String name);
    SeekableByteChannel p(String name,OpenOption ... openOptions)throws IOException;
    boolean isDirectory(String name);
    /**
     * @return true if empty. False if unknown, or not empty.
     */
    boolean isEmpty();
}
