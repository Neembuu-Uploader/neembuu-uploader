/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.suites;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite for all the project.
 * @author davidepastore
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    neembuu.uploader.test.suites.UploadersCommonSuite.class,
    neembuu.uploader.test.suites.UploadersSuite.class,
    neembuu.uploader.test.suites.UtilsSuite.class
})
public class AllSuites {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
