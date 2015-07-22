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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import static java.nio.file.StandardOpenOption.*;


/**
 *
 * @author Shashank
 */
public  final class ClassRus implements Rus {
    private final Class c; private final String relPth;

    private ClassRus(Class c, String relPth) {
        this.c = c; 
        this.relPth = relPth;
    }

    public static final ClassRus I(Class c){
        return new ClassRus(c,"");
    }

    @Override public Rus r(String name) {
        return new ClassRus(c, name+"/");
    }

    @Override public SeekableByteChannel p(String name, OpenOption... openOptions) throws IOException {
        OpenOption[]whiteList = {READ};//create is ignored
        for (OpenOption openOption : openOptions) {
            boolean clean = false;
            INNER:
            for (OpenOption whiteOpt : whiteList) {
                if(openOption==whiteOpt){clean = true;break INNER;}
            }
            if(!clean)throw new IOException("Not supported "+openOption);
        }
        final String v = v(name);
        SeekableByteChannel_wrap sbc = new SeekableByteChannel_wrap(v);
        return sbc;
    }
    
    public final String v(String name)throws IOException{
        InputStream is = c.getResource(relPth+name).openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String ret="",s;
        while((s=br.readLine())!=null){
            ret+=s;
        }
        return ret;
    }

    @Override public boolean isDirectory(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public void close() throws IOException {}
    
}
