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
package neembuu.uploader.versioning;

import java.util.logging.Level;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author Shashank Tulsyan
 */
public class UserImpl implements User, FileNameNormalizer {
    
    public volatile String normalization = ".neembuu";
    private static UserImpl I = null;
    private final long uid;
    private volatile boolean canCustomizeNormalizing = true;
    
    public static UserProvider getUserProvider(){
        return new UserProvider() {
            @Override public User getUserInstance() {
                return UserImpl.I();}
            @Override public FileNameNormalizer getFileNameNormalizer() {
                return UserImpl.I();}
        };
    }
    
    @Override public String normalizeFileName(String fn, int fileNameLengthLimit) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override public String normalizeFileName(String fn) {
        String r = fn;
        int dotCnt = countof('.', fn);
        int insertionIndex = 0;
        if (dotCnt == 0) {
            return fn + "_" + normalization.substring(1);
        } else if (dotCnt == 1) {
            r = r.substring(0, r.lastIndexOf('.')) + normalization + r.substring(r.lastIndexOf('.'));
            return r;
        } else {
            String t = fn;
            t = t.substring(0, t.lastIndexOf('.'));
            int idx = t.lastIndexOf('.');
            r = t.substring(0, idx) + normalization + fn.substring(idx);
            return r;
        }
    }
    
    private static int countof(char c, String src){
        int cnt = 0;
        for (int i = 0; i < src.length(); i++) {
            if(src.charAt(i)==c)cnt++;
        }return cnt;
    }
    
    public static void init(long id){
        if(I!=null)throw new IllegalStateException("Already initialized");
        I = new UserImpl(id);
    }

    public static UserImpl I() {
        return I;
    }

    public UserImpl(long uid) {
        this.uid = uid;
    }
    
    @Override public long uid() {
        return uid;
    }
    
    @Override public String uidString() {
        return Long.toString(uid);
    }

    @Override public boolean canCustomizeNormalizing() {
        return canCustomizeNormalizing;
    }
    
    public void keepChecking(){
        new Thread("Check user"){
            @Override public void run(){
                for(;;){try{
                    CheckUser.getCanCustomizeNormalizing(new UserSetPriv() {
                        @Override public void setCanCustomizeNormalizing(boolean canCustomizeNormalizing) {
                            UserImpl.this.canCustomizeNormalizing = canCustomizeNormalizing;
                        }@Override public void setNormalization(String normalization) {
                            UserImpl.this.normalization = normalization;
                        }
                    });
                    
                    Thread.sleep(
                            5 * 60 * 1000// 5 minutes
                            //1000 //1 second
                    );
                }catch(Exception a){
                    NULogger.getLogger().log(Level.INFO,"Error in checking user",a);
                }
            }}
        }.start();
        
    }
    
}
