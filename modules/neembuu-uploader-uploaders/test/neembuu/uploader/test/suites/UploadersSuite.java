/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.test.suites;

import neembuu.uploader.uploaders.UploadingDotComTest;
/*import neembuu.uploader.uploaders.UptoboxTest;
import neembuu.uploader.uploaders.ZShareTest;
import neembuu.uploader.uploaders.ZippyShareTest;
import neembuu.uploader.uploaders.ZohoDocsTest;*/
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for uploaders.
 * @author davidepastore
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    UploadingDotComTest.class,
    /*UptoboxTest.class,
    ZShareTest.class,
    ZippyShareTest.class,
    ZohoDocsTest.class*/
})
public class UploadersSuite {

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
