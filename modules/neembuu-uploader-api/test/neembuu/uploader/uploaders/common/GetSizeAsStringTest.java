/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuu.uploader.uploaders.common;

/**
 *
 * @author Shashank
 */
public class GetSizeAsStringTest {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            long x = (long)Math.pow(1024,i+1);
            System.out.println(GetSizeAsString.getSize(x));
            System.out.println(GetSizeAsString.getSize(x-1));
        }

    }
}
