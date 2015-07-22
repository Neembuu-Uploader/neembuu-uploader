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
package neembuu.uploader.external;

/**
 *
 * @author Shashank
 */
public class SmallModuleEntry {
    private final String name;
    private final String relpth;
    private final Class[]exports;
    private final String hash;
    private final boolean dead;
    
    private final Index index;
    
    volatile UploaderPlugin up;

    public SmallModuleEntry(String name, String relpth, Class[] exports, String hash, Index index,boolean dead) {
        this.name = name;
        this.relpth = relpth;
        this.exports = exports;
        this.hash = hash;
        this.index = index; this.dead = dead;
    } 

    public boolean isActivated() {
        return up!=null;
    }

    public String getName() {
        return name;
    }

    public String getRelpth() {
        return relpth;
    }

    public Class[] getExports() {
        return exports;
    }

    public String getHash() {
        return hash;
    }

    public Index getIndex(){
        return index;
    }

    public boolean isDead() {
        return dead;
    }
    
}
