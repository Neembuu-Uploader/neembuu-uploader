/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.uploaders;

import java.io.File;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.test.utils.files.FileCreator;
import neembuu.uploader.uploaders.UploadingDotCom;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * UploadingDotCom test.
 * @author davidepastore
 */
public class UploadingDotComTest {
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private File tempFile;
    
    private UploadingDotCom uploadingDotCom;
    private long maxFileSizeLimit;
    
    public UploadingDotComTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        uploadingDotCom = new UploadingDotCom(/*tempFile*/);
        maxFileSizeLimit = uploadingDotCom.getMaxFileSizeLimit();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test upload without account.
     */
    @Test
    public void testUploadWithoutAccount() {
        tempFile = FileCreator.createTemporaryFile(tempFolder, "txt", 1024);
        uploadingDotCom = new UploadingDotCom(/*tempFile*/);
        uploadingDotCom.run();
        
        assertEquals("The status is not correct", UploadStatus.UPLOADINVALID, uploadingDotCom.getStatus());
    }
}
