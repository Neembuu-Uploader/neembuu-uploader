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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Shashank
 */
public class SmallModuleMetadata {
    private final Path metadatafile;
    
    private final String name;
    private final String uploaderClassName, accountsClassName;

    public SmallModuleMetadata(Path metadatafile)throws IOException,JSONException {
        this.metadatafile = metadatafile;
        String uploaderClassName = null, accountsClassName= null;
        JSONObject m = new JSONObject(
                new String(Files.readAllBytes(metadatafile),
                Charset.forName("UTF-8")));
        
        name = m.getString("name");
        JSONArray e = m.getJSONArray("exports");
        
        for (int i = 0; i < e.length(); i++) {
            JSONObject ee = e.getJSONObject(i);
            String impl = ee.getString("implementation");
            String intr = ee.getString("interface");
            
            if(intr.equals(Uploader.class.getName())){
                uploaderClassName = impl;
            }else if(intr.equals(Account.class.getName())){
                accountsClassName = impl;
            }
        }
        this.uploaderClassName = uploaderClassName;
        this.accountsClassName = accountsClassName;
    }

    public String getName() {
        return name;
    }

    public String getUploaderClassName() {
        return uploaderClassName;
    }

    public String getAccountsClassName() {
        return accountsClassName;
    }
    
    
    
    
}
