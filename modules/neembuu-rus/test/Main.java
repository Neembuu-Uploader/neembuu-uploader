
import java.awt.Color;
import java.nio.file.Paths;
import java.util.Date;
import neembuu.rus.DefaultValue;
import neembuu.rus.Rus;
import neembuu.rus.Rusila;
import neembuu.rus.type.TypeHandlerProvider;
import neembuu.rus.type.ValueHandler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Shashank
 */
public class Main {
    public static void main(String[] args) throws Exception{
        Rus r = Rusila.create(Paths.get("j:\\jt_data\\t"));
        
        Rusila rusila = Rusila.newInstance().r(r);
        TypeHandlerProvider thp = rusila.thp();
        thp.register(new ValueHandler() {
            @Override public Class type() { return Color.class; }
            @Override public Object handle(String tempValue, Rus r, String name, DefaultValue dv) {
                String[]v=tempValue.split(",");
                int x[]=new int[v.length];
                for (int i = 0; i < x.length; i++) {
                    x[i] = Integer.parseInt(v[i]);
                }
                return new Color(x[0],x[1],x[2]);
            }
        });
        thp.register(new ValueHandler() {
            @Override public Class type() { return Date.class; }
            @Override public Object handle(String tempValue, Rus r, String name, DefaultValue dv) {
                return new Date(Long.parseLong(tempValue));
            }
        });
        t t = rusila.I(t.class);
        System.out.println(t.a1());
        System.out.println(t.a2());
        System.out.println(t.t2());
        System.out.println(t.t2().a1());
        System.out.println(t.t2().a2());
        
        Iterable<i1> t1 = t.t1();
        for(i1 i11 : t1){
            System.out.println("==i1==");
            System.out.println(i11.a1());
            System.out.println(i11.a2());
            System.out.println(i11.t2().a1());
            System.out.println(i11.t2().a2());
            System.out.println("++i1++");
        }
    }
}
