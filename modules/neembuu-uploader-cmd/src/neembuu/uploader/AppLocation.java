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
package neembuu.uploader;

import neembuu.uploader.api.AppLocationProvider;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.logging.Level;
import neembuu.uploader.utils.NULogger;
import shashaank.EnvironmentHandler;
import shashaank.RootInfo;
import shashaank.RootInfoProvider;

/**
 *
 * @author vigneshwaran
 */
public class AppLocation {
    private AppLocation() {
    }
    
    private static final RootInfo ri = RootInfoProvider.getRootInfo(AppLocation.class, new EnvironmentHandler() {
        @Override public Path handleNonJar(Class type) {
            CodeSource src = AppLocation.class.getProtectionDomain().getCodeSource();
            String urlpth = src.getLocation().toString();
            urlpth = urlpth.substring(0, urlpth.lastIndexOf('/'));
            urlpth = urlpth.substring(0, urlpth.lastIndexOf('/'));
            urlpth = urlpth.substring(0, urlpth.lastIndexOf('/')); 
            urlpth = urlpth.substring(0, urlpth.lastIndexOf('/') + 1);
            try{return Paths.get(new URL(urlpth).toURI());}
            catch(Exception a){throw new IllegalStateException(a);}
        }
    }, "NeembuuUploader");
    
    private static final AppLocationProvider al = new AppLocationProvider() {
        @Override public File getPath() {
            return getLocation();
        }
    };
    
    public static File getLocation(){
        return ri.getRoot().toFile();
    }
    
    public static AppLocationProvider appLocationProvider(){
        NULogger.getLogger().log(Level.INFO, "jar="+ri.isJar()+" exe="
                +ri.isPackedExe()+" excelsior="+ri.isRuntimeExcelsior()
                +" root="+ri.getRoot());
        return al;
    }
}
