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
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;

/**
 *
 * @author Shashank Tulsyan
 */
public class SplitCsv {
    public static void main(String[] args) throws IOException{
        String src = "F:\\NeembuuUploader\\uploadstats.csv\\uploadstats.n.csv";
        String out = "F:\\NeembuuUploader\\uploadstats.csv\\uploadstats.n.1.csv";
        
        File f = new File(src);
        FileReader fr = new FileReader(f);
        fr.skip(f.length()/2);
        
        BufferedReader br = new BufferedReader(fr);
        
        FileChannel fc_out = FileChannel.open(Paths.get(out),CREATE,WRITE,APPEND);
        FileChannel fc_src = FileChannel.open(Paths.get(src),READ);

        fc_src.position(f.length()/2);
        fc_src.transferTo(f.length()/2, f.length()/2, fc_out);
        fc_out.force(true);
        fc_out.close();
        fc_src.close();
    }
}
