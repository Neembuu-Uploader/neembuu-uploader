/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.utils;

import neembuu.uploader.utils.IntegerUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for IntegerUtils
 * @author davidepastore
 */
public class IntegerUtilsTest {
    
    public IntegerUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isInteger method, of class IntegerUtils.
     */
    @Test
    public void testIsInteger() {
        System.out.println(this.getClass().getName() + ".isInteger");
        
        assertEquals(true, IntegerUtils.isInteger("213"));
        assertEquals(true, IntegerUtils.isInteger("213231123"));
        assertEquals(false, IntegerUtils.isInteger("213.1"));
        assertEquals(false, IntegerUtils.isInteger("213l"));
        assertEquals(false, IntegerUtils.isInteger("qwerty"));
        assertEquals(false, IntegerUtils.isInteger(""));
        assertEquals(false, IntegerUtils.isInteger("   "));
    }
}
