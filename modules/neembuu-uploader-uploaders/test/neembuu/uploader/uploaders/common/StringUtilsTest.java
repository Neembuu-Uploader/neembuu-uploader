/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders.common;

import neembuu.uploader.uploaders.common.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for StringUtils class methods.
 * @author davidepastore
 */
public class StringUtilsTest {
    
    public StringUtilsTest() {
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
     * Test of stringBetweenTwoStrings method, of class StringUtils.
     */
    @Test
    public void testStringBetweenTwoStrings_3args() {
        System.out.println("stringBetweenTwoStrings (3 args)");
        String response = "asd<message>TEST</message>fgh";
        String stringStart = "<message>";
        String stringEnd = "</message>";
        String expResult = "TEST";
        String result = StringUtils.stringBetweenTwoStrings(response, stringStart, stringEnd);
        assertEquals(expResult, result);
    }

    /**
     * Test of stringBetweenTwoStrings method, of class StringUtils.
     */
    @Test
    public void testStringBetweenTwoStrings_4args() {
        System.out.println("stringBetweenTwoStrings (4 args)");
        String response = "asd<message><message>TEST</message>fgh";
        String stringStart = "<message>";
        String stringEnd = "</message>";
        boolean lastindexof = true;
        String expResult = "TEST";
        String result = StringUtils.stringBetweenTwoStrings(response, stringStart, stringEnd, lastindexof);
        assertEquals(expResult, result);
    }

    /**
     * Test of stringUntilString method, of class StringUtils.
     */
    @Test
    public void testStringUntilString() {
        System.out.println("stringUntilString");
        String string = "TEST</message>fgh";
        String stringEnd = "</message>";
        String expResult = "TEST";
        String result = StringUtils.stringUntilString(string, stringEnd);
        assertEquals(expResult, result);
    }

    /**
     * Test of stringStartingFromString method, of class StringUtils.
     */
    @Test
    public void testStringStartingFromString() {
        System.out.println("stringStartingFromString");
        String string = "asd<message>TEST";
        String stringStart = "<message>";
        String expResult = "TEST";
        String result = StringUtils.stringStartingFromString(string, stringStart);
        assertEquals(expResult, result);
    }
    
    
    /**
     * Test of stringStartingFromString method, of class StringUtils.
     */
    @Test
    public void testStringStartingFromString_3args() {
        System.out.println("stringStartingFromString (3 args)");
        fail("Not yet implemented.");
    }
}
