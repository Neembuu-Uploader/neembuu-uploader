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
package neembuu.uploader.settings;

import neembuu.rus.DefaultValue;

/**
 *
 * @author Shashank
 */
public interface Settings {
    @DefaultValue(b=true)       boolean savecontrolstate();
    @DefaultValue(b=true)       boolean savequeuedlinks();    
    @DefaultValue(b=true)       boolean showoverallprogress();
    @DefaultValue(b=true)       boolean firstlaunch();
    @DefaultValue(b=false)      boolean minimizetotray();
    @DefaultValue(b=false)      boolean savecurrentpath();
    @DefaultValue(b=false)      boolean autoretryfaileduploads();
    @DefaultValue(b=true)       boolean logging();
    @DefaultValue(s="")         String  themeNm(); //renamed in v3.3 to force users to shift into new theme
    @DefaultValue(b=false)      boolean whitenBackground();
    @DefaultValue(i=2)          int     maxNoOfUploads();
    @DefaultValue(s="")         String  currentpath();
    @DefaultValue(b=false)      boolean usingProxy();
    @DefaultValue(s="")         String  proxyAddress();
    @DefaultValue(s="")         String  proxyPort();
    @DefaultValue(s="32 KB")    String  bufferSize();
    @DefaultValue(s="en")       String  userlang();
    
    void savecontrolstate       (boolean v);
    void savequeuedlinks        (boolean v);
    void showoverallprogress    (boolean v);
    void firstlaunch            (boolean v);
    void minimizetotray         (boolean v);
    void savecurrentpath        (boolean v);
    void autoretryfaileduploads (boolean v);
    void logging                (boolean v);
    void themeNm                (String  v);
    void whitenBackground       (boolean v);
    void maxNoOfUploads         (int     v);
    void currentpath            (String  v);
    void usingProxy             (boolean v);
    void proxyAddress           (String  v);
    void proxyPort              (String  v);
    void bufferSize             (String  v);
    void userlang               (String  v);
}
