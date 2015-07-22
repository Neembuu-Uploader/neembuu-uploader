/*
 * Copyright (C) 2014 Shashank
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package neembuu.uploader.v2tov3conversion;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Shashank
 */
public class Main {
    public static void main(String[] args) throws IOException{
        
        
        //Path rel = Paths.get("uploaders\\DDLStorage.java");
        Path rel;
        DirectoryStream<Path> ds;
        String[]ignorelist = {
            "CrockoAccount","FourSharedAccount","HostrAccount",
            "Crocko","FourShared","Hostr",
            "WUpload" // << wierd plugin, difficult to handle
                // automatically. The website is also dead.
        };
        
        
        /*ConvertUploaderClass xc = new ConvertUploaderClass(nu_v2_uploaders_path().resolve("uploaders\\VipFile.java"));
        xc.convert();
        xc.writeTo(nu_v3_uploaders_output_path().resolve("uploaders\\VipFile.java"));
        if(true)return;*/
        
        rel = Paths.get("accounts");
        ds = Files.newDirectoryStream(nu_v2_uploaders_path().resolve(rel));
        for (Path src : ds) {
            if(fallsInIgnoreList(src, ignorelist))continue;
            if(Files.isDirectory(src))continue;
            ConvertAccountClass c = new ConvertAccountClass(src);
            c.convert();
            c.writeTo(nu_v3_uploaders_output_path().resolve(rel).resolve(src.getFileName()));
        }
        
        rel = Paths.get("uploaders");
        ds = Files.newDirectoryStream(nu_v2_uploaders_path().resolve(rel));
        for (Path src : ds) {
            if(fallsInIgnoreList(src, ignorelist))continue;
            if(Files.isDirectory(src))continue;
            ConvertUploaderClass c = new ConvertUploaderClass(src);
            c.convert();
            c.writeTo(nu_v3_uploaders_output_path().resolve(rel).resolve(src.getFileName()));
        }
    }
    
    private static boolean fallsInIgnoreList(Path p,String[]ignorelist){
        String fn = p.getFileName().toString();
        for (int i = 0; i < ignorelist.length; i++) {
            String string = ignorelist[i];
            if(fn.contains(string))return true;
        }return false;
    }
    
    private static final Path nu_v2_uploaders_path(){
        Path p = Paths.get("f:\\NeembuuUploader\\legacy\\svn_before_v3\\NeembuuUploader\\src\\neembuuuploader\\");
        assert Files.exists(p);
        return p;
    }
    
    private static final Path nu_v3_uploaders_output_path(){
        Path p = Paths.get("F:\\NeembuuUploader\\gitcode\\modules\\neembuu-uploader-uploaders\\src\\neembuu\\uploader\\");
        assert Files.exists(p);
        return p;
    }
    
    
}
