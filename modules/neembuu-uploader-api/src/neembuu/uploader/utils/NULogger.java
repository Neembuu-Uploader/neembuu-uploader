/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.utils;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**Use this class to log messages. Don't use System.out.println().
 *
 * @author vigneshwaran
 */
public class NULogger {

    //Root logger common for all classes(that's why the empty quotes).
    private static final Logger logger = Logger.getLogger("");
    
    public static void initializeFileHandler(String outputFileName){
        if(logger.getHandlers().length > 1)throw new IllegalStateException("already inited");
        try {
            //The Logger should log messages both to console and a log file
            FileHandler handler = new FileHandler(outputFileName, false);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            
        } catch (Exception ex) {
            System.out.println("Exception in creating Log file itself");
        }
        
    }
    
    //Noninstantiable
    private NULogger() {
    }

    /**
     * Use this with info or warning or severe to log messages. Don't use System.out.println().
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }
}
