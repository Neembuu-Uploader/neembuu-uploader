/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.translation;

import java.util.StringTokenizer;

/**
 *
 * @author Shashank
 */
public class ToHtmlMultiLine {    
    public static String splitToMultipleLines(String input, int maxLineLength) {
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken()+" ";

            if (lineLen + word.length() > maxLineLength) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word);
            lineLen += word.length();
        }
        return "<html>"+output.toString().replaceAll("\n", "<br/>")+"</html>";
    }
    
    static String tohtml(String n,int c){
        final double len = n.length();
        String[]a=n.split(" ");
        int[]idx = new int[c-1];
        for (int j = 0; j < idx.length; j++) {
            int ix = j==0?0:idx[j-1];
            double l = len*((j+1d)/c*1d);
            for (int i = 0; ix <l && i < a.length; i++) {
                ix+=a[i].length()+1;//1 for space character
            }idx[j]=ix;
        }
        
        String r = "<html>";
        
        for (int i = 0; i < idx.length + 1; i++) {
            int ix = i==0?0:idx[i-1];
            int iy = i==idx.length?((int)len):idx[i]; 
            //System.out.println("ix iy len "+ix+" "+iy+" "+len);
            r = r + (i==0?"":"<br/>")+ n.substring(ix,iy);
        }
        r = r + "</html>";
        r = r.replace("\n","<br/>");
        return r;
    }
}
