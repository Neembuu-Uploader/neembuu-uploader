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
package neembuu.uploader.accountgui;

import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.settings.Application;
import neembuu.uploader.settings.Settings;

/**
 * This is simply to export the logic of creation of url for new account
 * registration/creation
 *
 * @author Shashank Tulsyan
 */
public class NewAccountURLUtil {

    public static String createNewAccountRegistrationURL(String hostName) {
        ////http://neembuuuploader.sourceforge.net/redirector.php?version=2.9&host=UpBooth.com&os=Linux&jre=1.7.0_25&locale=en
        String url = "http://neembuuuploader.sourceforge.net/redirector.php";

        url += "?version=" + NeembuuUploader.getVersionForProgam();
        url += "&host=" + hostName;
        url += "&os=" + removeSpaces(System.getProperty("os.name"));
        url += "&jre=" + System.getProperty("java.version");
        url += "&locale=" + Application.get(Settings.class).userlang();

        return url;
    }
    
    private static String removeSpaces(String text){
        return text.replace(" ","%20");
    }
}
