/* 
 * Copyright 2015 Shashank Tulsyan <shashaank at neembuu.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package neembuu.uploader.zip.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

/**
 * Compiler class. It compiles files from the packages.
 *
 * @author davidepastore
 */
public class NUCompiler {

    private final File gitDirectory;

    /**
     * The classpath.
     */
    private String classPath = "";
    private final String modulesFolderName,srcFolderName;

    /**
     * Constructor of the NUCompiler class.
     *
     * @param gitDirectory The git directory.
     * @param modulesFolderName the name of the folder in git that contains all modules
     * @param srcFolderName default value is src, but still this has been kept a variable
     */
    public NUCompiler(File gitDirectory,String modulesFolderName,String srcFolderName) {
        this.gitDirectory = gitDirectory; this.modulesFolderName = modulesFolderName;
        this.srcFolderName = srcFolderName;
        addJarToClasspath();
    }

    public void compileDirectory(String moduleName)throws IOException{
        // I prefer using java7 new io api :-) 
        Path srcPth = Paths.get(gitDirectory.getAbsolutePath())
                .resolve(modulesFolderName).resolve(moduleName).resolve(srcFolderName).normalize();
        compileFiles(NUFileUtils.listAllJavaFiles(srcPth.toFile()));
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
        classPath += buildDir.getAbsolutePath() + File.pathSeparator;
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
        // set compiler's classpath to be same as the runtime's
        //optionList.addAll(Arrays.asList("-classpath", "C:\\neembuuuploader\\modules\\libs\\jsoup-1.7.2.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\commons-codec-1.6.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\commons-logging-1.1.1.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\httpclient-4.2.5.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\httpclient-cache-4.2.5.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\httpcore-4.2.4.jar;C:\\neembuuuploader\\modules\\libs\\ApacheHttpComponent-4.2.5\\httpmime-4.2.5.jar;C:\\neembuuuploader\\modules\\libs\\json-java.jar;C:\\neembuuuploader\\modules\\neembuu-uploader-utils\\build\\classes;C:\\neembuuuploader\\modules\\neembuu-uploader-api\\build\\classes;C:\\neembuuuploader\\modules\\neembuu-uploader-interfaces-abstractimpl\\build\\classes;C:\\neembuuuploader\\modules\\libs\\neembuu-now-api-ui.jar;C:\\neembuuuploader\\modules\\libs\\neembuu-release1-ui-mc.jar;C:\\neembuuuploader\\modules\\neembuu-uploader-uploaders\\build\\classes;C:\\neembuuuploader\\modules\\NeembuuUploader\\build\\classes"));
        
        List<String> optionList = new ArrayList<String>();
        optionList.addAll(Arrays.asList("-classpath", classPath));
        optionList.addAll(Arrays.asList("-d", buildDirectory.getAbsolutePath()));
        optionList.addAll(Arrays.asList("-source","1.7","-target","1.7"));

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits
                = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files));

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, optionList, null, compilationUnits);
        boolean result = task.call();

        handleDiagnotics(result, diagnostics);
    }
    
    private void handleDiagnotics(boolean result,DiagnosticCollector<JavaFileObject> diagnostics){
        if (result) {
            System.out.println("Compilation was successful");
        } else {
            System.out.println("Compilation failed, cp "+classPath);

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
            classPath += file.getAbsolutePath() + File.pathSeparatorChar;
        }

        System.out.println("Classpath: " + classPath);
    }

}
