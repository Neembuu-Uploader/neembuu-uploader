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
package neembuu.uploader.theme;

import java.awt.Color;

/**
 *
 * @author Shashank Tulsyan
 */
public final class Colors {

    public static Color BORDER          = hsl(135, 72*1.1f, 184, HSLSource.MSEXCEL), 
            TINTED_IMAGE                = hsl(136, 73*1.1f, 206, HSLSource.MSEXCEL),
            BUTTON_TINT                 = hsl(136, 73*1.2f, 216, HSLSource.MSEXCEL),
            PROGRESS_BAR_BACKGROUND     = hsl(135, 69*1.1f, 209, HSLSource.MSEXCEL),
            NIMBUS_BASE                 = hsl(135, 100*1.4f, 135, HSLSource.MSEXCEL), 
            TEXT_BACKGROUND             = hsl(135, 100*1.1f, 254, HSLSource.MSEXCEL), 
            PROGRESS_DOWNLOAD_LESS_MODE = hsl(135, 72*1.1f, 120, HSLSource.MSEXCEL), 
            OVERLAY                     = hsl(136, 71*1.1f, 232, 0.3f, HSLSource.MSEXCEL),
            //SIZ9_POST_BACKGROUND        = hsl(31, 73, 248, HSLSource.MSEXCEL),
            
            CONTROL_ICONS               = hsl(140, 255, 58, HSLSource.MSEXCEL), 
            PROGRESS_BAR_FILL_ACTIVE    = hsl(140, 209, 105, HSLSource.MSEXCEL),
            PROGRESS_BAR_FILL_BUFFER   = hsl(140, 85*1.5f, 212, HSLSource.MSEXCEL);
    
            
    

    private static Color rgb(int r, int g, int b) {
        return new Color(r, g, b);
    }
    
    private static Color hsl(float h, float s, float l,float alpha, HSLSource hSLSource){
        int base = hSLSource.getBase();
        return new Color(HSLColor.toRGB((float)h/base, (float)s/base, (float)l/base,alpha),true);
    }
    
    public static Color hsl(float h, float s, float l,HSLSource hSLSource){
        int base = hSLSource.getBase();
        return new Color(HSLColor.toRGB((float)h/base, (float)s/base, (float)l/base));
    }
    
    public static enum HSLSource {
        MSPAINT(240),
        MSEXCEL(255);
        private int base;

        private HSLSource(int base) {
            this.base = base;
        }

        public int getBase() {
            return base;
        }
        
    }
    
    
}
