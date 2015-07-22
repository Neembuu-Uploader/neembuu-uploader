/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.zip.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import neembuu.uploader.zip.generator.utils.CompilerUtils;
import neembuu.uploader.zip.generator.utils.NUFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Compiler class. It compiles files from the packages.
 *
 * @author davidepastore
 */
public class NUCompiler {

    private File gitDirectory;

    /**
     * The directory for the api project.
     */
    public static final String API_DIRECTORY = "\\modules\\neembuu-uploader-api\\src";

    /**
     * The directory for the interface abstractimpl project.
     */
    public static final String INTERFACE_ABSTRACT_IMPL_DIRECTORY = "\\modules\\neembuu-uploader-interfaces-abstractimpl\\src";

    /**
     * The directory for the plugins project.
     */
    public static final String UPLOADERS_DIRECTORY = "\\modules\\neembuu-uploader-uploaders\\src";

    /**
     * The directory for the utils project.
     */
    public static final String UTILS_DIRECTORY = "\\modules\\neembuu-uploader-utils\\src";

    /**
     * The classpath.
     */
    private String classPath = "";

    /**
     * Constructor of the NUCompiler class.
     *
     * @param gitDirectory The git directory.
     */
    public NUCompiler(File gitDirectory) {
        this.gitDirectory = gitDirectory;
        addJarToClasspath();
    }

    /**
     * Compile all the api.
     *
     * @throws IOException
     */
    public void compileApi() throws IOException {
        File srcDir = new File(gitDirectory.getAbsolutePath() + API_DIRECTORY);
        compileFiles(NUFileUtils.listAllJavaFiles(srcDir));
    }

    /**
     * Compile all the interfaces and the abstract implementations.
     *
     * @throws IOException
     */
    public void compileIntAbsImpl() throws IOException {
        File srcDir = new File(gitDirectory.getAbsolutePath() + INTERFACE_ABSTRACT_IMPL_DIRECTORY);
        compileFiles(NUFileUtils.listAllJavaFiles(srcDir));
    }

    /**
     * Compile all the uploaders/accounts.
     *
     * @throws java.io.IOException
     */
    public void compileUploaders() throws IOException {
        File srcDir = new File(gitDirectory.getAbsolutePath() + UPLOADERS_DIRECTORY);
        compileFiles(NUFileUtils.listAllJavaFiles(srcDir));
    }

    /**
     * Compile all the utils.
     *
     * @throws IOException
     */
    public void compileUtils() throws IOException {
        File srcDir = new File(gitDirectory.getAbsolutePath() + UTILS_DIRECTORY);
        compileFiles(NUFileUtils.listAllJavaFiles(srcDir));
    }

    /**
     * Compile all the given files.
     *
     * @param files All the files.
     */
    private void compileFiles(Collection files) throws IOException {
        File[] resultFiles = (File[]) files.toArray(new File[files.size()]);
        File buildDir = CompilerUtils.getBuildDirectory(resultFiles[0]);
        compileFiles(resultFiles, buildDir);

        //Add to the classpath
        classPath += buildDir.getAbsolutePath() + ";";
    }

    /**
     * Compile all the given files in the given build directory.
     *
     * @param files The array of files to compiles.
     * @param buildDirectory The build directory in which put all the compiled
     * files.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void compileFiles(File[] files, File buildDirectory) throws FileNotFoundException, IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        buildDirectory.mkdir();

        /**/
        List<String> optionList = new ArrayList<String>();
        // set compiler's classpath to be same as the runtime's
        //optionList.addAll(Arrays.asList("-classpath", "C:\\neembuuuploader\\modules\\libs\\jsoup-1.7.2.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\commons-codec-1.6.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\commons-logging-1.1.1.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\httpclient-4.2.5.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\httpclient-cache-4.2.5.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\httpcore-4.2.4.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\httpmime-4.2.5.jar;C:\\neembuuuploader\\modules\\libs\\json-java.jar;C:\\neembuuuploader\\modules\\neembuu-uploader-utils\\build\\classes;C:\\neembuuuploader\\modules\\neembuu-uploader-api\\build\\classes;C:\\neembuuuploader\\modules\\neembuu-uploader-interfaces-abstractimpl\\build\\classes;C:\\neembuuuploader\\modules\\libs\\neembuu-now-api-ui.jar;C:\\neembuuuploader\\modules\\libs\\neembuu-release1-ui-mc.jar;C:\\neembuuuploader\\modules\\neembuu-uploader-uploaders\\build\\classes;C:\\neembuuuploader\\modules\\NeembuuUploader\\build\\classes"));
        optionList.addAll(Arrays.asList("-classpath", classPath));
        optionList.addAll(Arrays.asList("-d", buildDirectory.getAbsolutePath()));

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits
                = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, optionList, null, compilationUnits);
        boolean result = task.call();

        if (result) {
            System.out.println("Compilation was successful");
        } else {
            System.out.println("Compilation failed");

            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                System.out.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
            }
        }
    }

    /**
     * Add all the jar to the classpath.
     */
    private void addJarToClasspath() {
        Collection<File> filesList = NUFileUtils.listAllFilesWithExt(gitDirectory, "jar");

        for (File file : filesList) {
            classPath += file.getAbsolutePath() + ";";
        }

        System.out.println("Classpath: " + classPath);
    }

}
