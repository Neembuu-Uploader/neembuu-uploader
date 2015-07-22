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
package neembuu.uploader.uploadListFormatters;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import neembuu.uploader.api.SuccessfulUploadsListener;
import neembuu.uploader.interfaces.Uploader;

/**
 * actual date|Host|Download URL|Delete URL|File
 * http://www.neembuu.com/uploader/forum/suggestions-f5/log-file-with-all-completed-uploads-t54.html#p205
 * 2015-01-01 22:11:10|1fichier.com|https://1fichier.com/?xxx|https://1fichier.com/remove/xyz/xyz|Example filename with long name.rar
 * @author Shashank
 */
public class UploadListTextFile implements SuccessfulUploadsListener {
    private final Path destination;
    private final DateFormat df;
    public UploadListTextFile(Path root) {
        this.destination = root.resolve("successfullyUploadedFilesList.txt");
        df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        df.setCalendar(Calendar.getInstance(Locale.ENGLISH)); // we don't want weird characters in date
    }
    

    @Override public void success(Uploader u) throws IOException{
        String line="";
        line+= df.format(System.currentTimeMillis());
        line+="|"+u.getDisplayName();
        line+="|"+u.getDownloadURL();
        line+="|"+u.getDeleteURL();
        line+="|"+u.getFile().getName();
        
        List<String> l = Collections.singletonList(line);
        
        Files.write(destination, l, Charset.forName("UTF-8"),CREATE,WRITE,APPEND);
    }
    
}
