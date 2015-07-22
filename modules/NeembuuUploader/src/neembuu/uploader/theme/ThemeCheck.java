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
import java.awt.Component;
import java.awt.Container;
import javax.swing.JButton;
import javax.swing.JComponent;
import neembuu.uploader.settings.Application;
import neembuu.uploader.settings.Settings;

/**
 *
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class ThemeCheck {
    public static void apply(Component s){
        if(!Application.get(Settings.class).whitenBackground())return;
        apply0(s);
        if(Application.get(Settings.class).themeNm().trim().equalsIgnoreCase("nimbus")){
            WhiteBackgroundLookAndFeel.init(s);
        }
    }
    private static void apply0(Component s){
        if(s==null)return;
        
        if( 
                s instanceof javax.swing.JTable && false /*disabled*/
            ){
            return; // to allow alternate cell gray background
        }else { 
            s.setBackground(Color.WHITE);
        }
        if(s instanceof JComponent){
            JComponent jc = (JComponent)s;
            if(!(jc instanceof JButton)){
                try{
                    jc.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                }catch(Exception a){
                    //ignore
                }
            }
        }
        
        if(s instanceof Container){
            Container cc = (Container)s;
            for (Component component : cc.getComponents()) {
                apply0(component);
            }
        }
    }
}
