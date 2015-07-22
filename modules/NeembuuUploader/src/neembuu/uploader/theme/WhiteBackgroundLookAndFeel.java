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
import java.awt.Graphics2D;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Painter;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.nimbus.ScrollBarButtonPainter_Modified;
import javax.swing.plaf.nimbus.ScrollBarThumbPainter_Modified;

/**
 *
 * @author Shashank Tulsyan
 */
public class WhiteBackgroundLookAndFeel {
    public static void init(){
        init(null);
    }
    
    public static void init(Component jc){
        try {
            colors();
            toolTip();
            scrollBar();

            if(jc==null){
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            }else {
                SwingUtilities.updateComponentTreeUI(jc);
            }
        } catch (Exception lookandfeelexception) {
            lookandfeelexception.printStackTrace(System.err);
        }
    }
    
    private static void colors(){
        UIManager.put("nimbusBase", new ColorUIResource(Colors.NIMBUS_BASE));
        UIManager.put("background",new ColorUIResource(Color.WHITE));
        UIManager.put("OptionPane.background",new ColorUIResource(Color.WHITE));
        UIManager.put("RootPane.background",new ColorUIResource(Color.WHITE));
        
        
UIManager.put("RootPane.disabled",new ColorUIResource(Color.WHITE));        
        //UIManager.put("Button.background",new ColorUIResource(Color.WHITE));
        UIManager.put("Label.background",new ColorUIResource(Color.WHITE));
        UIManager.put("Panel.background",new ColorUIResource(Color.WHITE));
UIManager.put("Panel.disabled",new ColorUIResource(Color.WHITE));
        UIManager.put("Separator.background",new ColorUIResource(Color.WHITE));
UIManager.put("OptionPane.disabled",new ColorUIResource(Color.WHITE));
        UIManager.put("control",new ColorUIResource(Color.WHITE));
        UIManager.put("info",new ColorUIResource(Color.WHITE));
        
        UIManager.put("nimbusBorder",new ColorUIResource(Color.WHITE));
        //UIManager.put("nimbusBlueGrey",Color.WHITE);
    }
    
    private static void toolTip(){
    }
    
    private static void scrollBar(){
        scrollBar(UIManager.getDefaults());
    }
    
    private static void scrollBar(UIDefaults uid){
        scrollBarButton(uid);
        
        uid.put("ScrollBar.background",new ColorUIResource(Color.WHITE));
        uid.put("ScrollBar.foreground",new ColorUIResource(Color.WHITE));
            
        scrollBarTrack(uid);
        scrollBarThumb(uid);
    }
    
    private static void scrollBarButton(UIDefaults uid){
        UIManager.put("ScrollBar:\"ScrollBar.button\"[Enabled].foregroundPainter", new ScrollBarButton());
    }
    
    private static void scrollBarTrack(UIDefaults uid){
        final ScrollBarTrack p1 = new ScrollBarTrack();
        final ScrollBarTrack p2 = new ScrollBarTrack();
        uid.put("ScrollBar:ScrollBarTrack[Enabled].backgroundPainter",p1);
        uid.put("ScrollBar:ScrollBarTrack[Disabled].backgroundPainter",p2);
    }
    
    private static void scrollBarThumb(UIDefaults uid){
        uid.put("ScrollBar:ScrollBarThumb[Enabled].backgroundPainter", 
                    new ScrollBarThumbPainter_Modified(ScrollBarThumbPainter_Modified.BACKGROUND_ENABLED));
        uid.put("ScrollBar:ScrollBarThumb[MouseOver].backgroundPainter", 
                    new ScrollBarThumbPainter_Modified(ScrollBarThumbPainter_Modified.BACKGROUND_MOUSEOVER));
        uid.put("ScrollBar:ScrollBarThumb[Pressed].backgroundPainter", 
                    new ScrollBarThumbPainter_Modified(ScrollBarThumbPainter_Modified.BACKGROUND_PRESSED));
    }
    
    public static void themeScrolls(JScrollPane jsp){
        if(jsp.getHorizontalScrollBar()!=null)
            themedScrollBar(jsp.getHorizontalScrollBar());
        if(jsp.getVerticalScrollBar()!=null)
            themedScrollBar(jsp.getVerticalScrollBar());
    }
    
    public static void themedScrollBar(JScrollBar a){
        UIDefaults uid = new UIDefaults();
        scrollBar(uid);
        a.putClientProperty("Nimbus.Overrides", uid);
    }
    
    private static final class ScrollBarTrack implements Painter,UIResource{
        @Override
        public void paint(Graphics2D g, Object object, int width, int height) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
        }
    }
    
    private static final class ScrollBarButton implements Painter, UIResource{
        private final Painter ScrollBar_button_Painter;

        public ScrollBarButton() {
            this.ScrollBar_button_Painter = 
                new ScrollBarButtonPainter_Modified(ScrollBarButtonPainter_Modified.FOREGROUND_ENABLED);;
        }
        
        @Override
        public void paint(Graphics2D g, Object object, int width, int height) {
            int wm=2,hm=2;
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, wm, height);
            g.fillRect(0, 0, width-8, hm);
            g.setClip(wm, hm, width-wm, height-hm);
            ScrollBar_button_Painter.paint(g, object, width, height);
        }
    }
    
    
    /*public static void main(String[] args) {
        WhiteBackgroundLookAndFeel.init();
        for (int i = 0; i < 100; i++) {
            JOptionPane.showConfirmDialog(null, "Start queued uploads if any","Start Queue",JOptionPane.YES_NO_OPTION);
            
        }
        System.exit(0);
        
    }*/
}
