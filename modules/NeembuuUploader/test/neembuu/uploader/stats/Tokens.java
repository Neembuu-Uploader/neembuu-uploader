/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * 
 * @author Shashank Tulsyan
 */
public class Tokens {
    public static void main(String[] args) throws IOException{
        //String src = "F:\\NeembuuUploader\\uploadstats.csv\\uploadstats.n.1_analytics_token.txt";
        String src = "F:\\NeembuuUploader\\uploadstats.csv\\uploadstats.n.1_analytics_token.txt";
        String out = "F:\\NeembuuUploader\\uploadstats.csv\\token.txt";
        
        File f = new File(src);
        FileReader fr = new FileReader(f);
        fr.skip(f.length()/2);
        
        BufferedReader br = new BufferedReader(fr);
        
        //List<String> lines = Files.readAllLines(Paths.get(src), Charset.defaultCharset());
        
        HashMap<String,Integer> hm = new HashMap<String, Integer>();
        
        int x = 0, y =0;String q="";
        for(String s; (s=br.readLine())!=null; q = s){
        //for (String s : lines) {
            analyzeLine(s, hm);
            x++;
            
            if(x>1000){
                y++;
                System.out.println(y*1000);
                x=0;
            }
            
        }System.out.println("x="+x);
        System.out.println("last line-"+q);
        
        PrintWriter pw = new PrintWriter(new File(out));
        Set<String> keys = hm.keySet();
        
        for (String key : keys) {
            //System.out.println(key+"="+hm.get(key));
            pw.println(key+"\t"+hm.get(key));
        }
        pw.flush();
        pw.close();
    }
    
    private static void analyzeLine(String l,HashMap<String,Integer> hm){
        ArrayList<String> tokens = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(l);
        while(st.hasMoreElements()){
            StringTokenizer st2 = new StringTokenizer(st.nextToken(),".");
            while(st2.hasMoreElements()){
                addToken(st2.nextToken(),hm);
            }
        }
    }
    
    private static void addToken(String t,HashMap<String,Integer> hm){
        if(!hm.containsKey(t)){
            hm.put(t, 1);
            return;
        }
        int x = hm.get(t);
        x++;
        hm.put(t, x);
    }
    
}
