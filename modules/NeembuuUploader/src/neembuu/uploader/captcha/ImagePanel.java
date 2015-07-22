/* 
 * Copyright (C) 2015 Shashank Tulsyan <shashaank at neembuu.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package neembuu.uploader.captcha;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author davidepastore
 */
public class ImagePanel extends JPanel{

    private volatile BufferedImage image;
    
    public ImagePanel(){
        
    }

    public final void update(URL imageFileUrl, HttpContext httpContext) {
        try {                
            HttpClient httpClient = NUHttpClient.getHttpClient();
            HttpGet httpGet = new HttpGet(imageFileUrl.toURI());
            HttpResponse httpresponse = httpClient.execute(httpGet, httpContext);
            byte[] imageInByte = EntityUtils.toByteArray(httpresponse.getEntity());
            InputStream in = new ByteArrayInputStream(imageInByte);
            image = ImageIO.read(in);
            //image = ImageIO.read(imageFileUrl);
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "ImagePanel exception: {0}", ex.getMessage());
        }
    }
    
    public ImagePanel(URL imageFileUrl, HttpContext httpContext){
        update(imageFileUrl, httpContext);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //int x = this.getWidth()/4;
        g.drawImage(image, 10, 10, null); // see javadoc for more info on the parameters            
    }
}
