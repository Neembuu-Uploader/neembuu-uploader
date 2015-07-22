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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import neembuu.uploader.external.UpdateProgressUI.Content;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.httpclient.httprequest.NUHttpGet;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

/**
 *
 * @author Shashank
 */
public class HttpUtil {
    public static void update(String url, Path localPath,final Content c)throws IOException{       
        NUHttpGet httpGet = new NUHttpGet(url);
        // you cannot use the same http client, or else the program will
        // simply get stuck. This update thing happens concurrently.
        // more than 10-20 plugins might update in parallel.
        // How? user may click button very fast, that's how.
        HttpResponse httpResponse = NUHttpClient.newHttpClient().execute(httpGet);
        
        try (FileChannel fc = FileChannel.open(localPath,
                StandardOpenOption.WRITE,StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            long contentLength =httpResponse.getEntity().getContentLength();
            fc.transferFrom(
                    wrap(httpResponse.getEntity().getContent(), 
                        httpResponse.getEntity(),c,contentLength),
                    0,contentLength);
        }
    }
    
    private static ReadableByteChannel wrap(final InputStream is, final HttpEntity he,
            final Content c, final double contentLength){
        return new ReadableByteChannel() {
            double total = 0;
            @Override public int read(ByteBuffer dst) throws IOException {
                byte[]b=new byte[dst.capacity()];
                int r = is.read(b); 
                //this sleep is just to slow down update to see, if the UI is working or not !
                // NU's update is very very very fast
                //try{Thread.sleep(1000);}catch(Exception a){}
                dst.put(b,0,r); total+=r; c.setProgress(total/contentLength);
                return r;
            }
            @Override public boolean isOpen() {
                return he.isStreaming();
            }
            @Override public void close() throws IOException {
                is.close();
            }
        };
    }
}
