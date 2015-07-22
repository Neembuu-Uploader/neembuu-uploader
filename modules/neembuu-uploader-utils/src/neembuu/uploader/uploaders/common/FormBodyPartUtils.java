/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.uploaders.common;

import java.io.UnsupportedEncodingException;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

/**
 * Utils for form body part.
 * @author davidepastore
 */
public class FormBodyPartUtils {
    
    /**
     * Create an empty form body part.
     * @param name the name of the field.
     * @param body the content of the field.
     * @return Return the new empty form body part.
     * @throws UnsupportedEncodingException 
     */
    public static FormBodyPart createEmptyFileFormBodyPart(final String name, StringBody body) throws UnsupportedEncodingException{
        return new FormBodyPart(name, body) {
            @Override
            protected void generateContentDisp(final ContentBody body) {
                StringBuilder buffer = new StringBuilder();
                buffer.append("form-data; name=\"").append(name).append("\"");
                buffer.append("; filename=\"\"");
                buffer.append("\n");
                addField(MIME.CONTENT_DISPOSITION, buffer.toString());
                addField(MIME.CONTENT_TYPE, "application/octet-stream");
            }

        };
    }
    
}
