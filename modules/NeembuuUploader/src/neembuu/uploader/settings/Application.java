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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import neembuu.rus.Rus;
import neembuu.rus.Rusila;
import neembuu.uploader.versioning.UserImpl;

public class Application {
    
    private final Path neembuuHome;
    private final Rus nu_rus;
    
    private static final Application I = new Application();

    private Application() {
        neembuuHome = Paths.get(System.getProperty("user.home")).resolve(".neembuuuploader");
        nu_rus = Rusila.create(neembuuHome);
    }
     
    public static void init()throws IOException{
        Files.createDirectories(I.neembuuHome);
        UserImpl.init(getUserId());
        UserImpl.I().keepChecking();        
    }
    
    public static long getUserId(){
        String p = Application.getProperty("user_id");
        long user_id;
        if(p==null){
            user_id = generateNewId();
            Application.setProperty("user_id",Long.toString(user_id));
        }else  {
            try{
                user_id = Long.parseLong(p);
            }catch(Exception a){
                user_id = generateNewId();
                Application.setProperty("user_id",Long.toString(user_id));
            }
        }
        return user_id;
    }
    
    private static long generateNewId(){
        return (long)(Math.random()*Long.MAX_VALUE);
    }
    
    /**
     * Set the property with the specified key and value
     * @param key
     * @param value 
     */
    private static void setProperty(String key, String value) {
        try{
            Rusila.set(I.nu_rus, key,value);
        }catch(Exception a){
            throw new IllegalStateException(a);
        }
    }
    
    /**
     * Get the value for a specified key. Returns "" if no value is present
     * @param key The property key
     * @return The value of the given property if exists
     */
    private static String getProperty(String key) {
        return Rusila.get(I.nu_rus, key).s(null);
    }
    
    /**
     * Whether the value for a given key is true or not.
     * @param key The Key of the property
     * @return A boolean value that indicates whether the property has a true
     * value or not.. If no value present, false is returned.
     */
    /*public static V get(String key) {
        return Rusila.get(I.nu_rus, key);
    }*/
    
    public static <E> E get(Class<E> s){
        return Rusila.I(I.nu_rus.r(s.getSimpleName()), s);
    }

    public static Path getNeembuuHome() {
        return I.neembuuHome;
    }
    
    public static Rus getRoot(){
        return I.nu_rus;
    }
}

