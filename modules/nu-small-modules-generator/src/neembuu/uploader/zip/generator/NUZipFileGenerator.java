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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuu.uploader.utils.HashUtil;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import shashaank.smallmodule.SmallModule;

/**
 * Make operations to create the zip files.
 * @author davidepastore
 */
public class NUZipFileGenerator {

    private final Path[]uploadersDirectories;
    private final Path outputDirectory;
    private final Index index;
    private final URLClassLoader classLoader;
    private final Environment env;

    /*public NUZipFileGenerator(File gitDirectory, File outputDirectory,String[]modules,
    String[]uploaderModuleName) {*/
    public NUZipFileGenerator(Environment env) {
        this.env = env;    
        uploadersDirectories = new Path[env.modulesToCheckForExportibles().length];
        Path gitDirectory = Paths.get(env.gitDirectory());
        for (int i = 0; i < env.modulesToCheckForExportibles().length; i++) {
            String uploadermod = env.modulesToCheckForExportibles()[i];
            uploadersDirectories[i] = gitDirectory
                    .resolve("modules/"+uploadermod+ "/build/");
        }
        
        this.outputDirectory = Paths.get(env.outputDirectory());
        classLoader = l(env.sortedListOfModulesToCompile(),gitDirectory);
        index = new Index(this.outputDirectory.resolve("index.json"),env);
    }
    
    private static URLClassLoader l(String[]modules,Path pth){
        try{
            URL[]u = new URL[modules.length];
            for (int i = 0; i < u.length; i++) {
                u[i]=pth.resolve("modules/"+modules[i]+ "/build/").toUri().toURL();
            }
            return new URLClassLoader(u);
        }catch(Exception a){
            throw new IllegalStateException(a);
        }
    }

    public void createZipFiles() {               
        Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.INFO, "Create the zip files");
        
        try {
            index.intialize();
            walkOverAllFiles();
            index.complete();
        } catch (Exception ex) {
            Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void walkOverAllFiles()throws IOException{
        for (final Path uploadersDirectory : uploadersDirectories) {
            Files.walkFileTree(uploadersDirectory, new FileVisitor<Path>() {
                @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {return FileVisitResult.CONTINUE;}
                @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException { exc.printStackTrace();return FileVisitResult.CONTINUE;}
                @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {return FileVisitResult.CONTINUE;}
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(file.getFileName().toString().endsWith(".class")){
                        visitClassFile(file, attrs,uploadersDirectory);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        
    }
    
    private Class load(Path f,Path uploadersDirectory)throws Exception{
        String p = uploadersDirectory.normalize().relativize(f.normalize()).toString();
        p = p.replace(File.separatorChar, '.');
        p = p.substring(0,p.lastIndexOf('.'));//to remove .class
        return classLoader.loadClass(p);
    }
    
    private void visitClassFile(Path file, BasicFileAttributes attrs, Path uploadersDirectory)throws IOException{
        Class c;
        try{
            c = load(file,uploadersDirectory);
        }catch(Throwable a){
            System.err.println("skipping "+file); a.printStackTrace(); return;
        }
        
        SmallModule moduleDescription = (SmallModule)c.getAnnotation(SmallModule.class);
        if(moduleDescription==null){
            System.out.println("not a small module"+file); return;
        }
        if(moduleDescription.ignore()){
            Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.INFO, "Ignoring : {0}", FilenameUtils.removeExtension(c.getName()));
            return;
        }
        handleSmallModule(moduleDescription, c,uploadersDirectory);
    }
    
    private void handleSmallModule(SmallModule moduleDescription,Class clzz,Path uploadersDirectory)throws IOException{
        Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.INFO, 
                "Create zip for: {0}", clzz.getName());
        Path outputModulePath = outputDirectory
                .resolve("sm")
                .resolve(moduleDescription.name()+".zip");
        Files.createDirectories(outputModulePath.getParent());
        while(Files.exists(outputModulePath)){
            try{Files.deleteIfExists(outputModulePath);}catch(Exception a){a.printStackTrace();}
        }

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");boolean destroyZipIsCorrupt = false;
        URI uri = URI.create("jar:" + outputModulePath.toUri());
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            smallModuleCreateZip(fs, moduleDescription, clzz,uploadersDirectory);
        }catch(Exception e){
            e.printStackTrace(); destroyZipIsCorrupt = true;
        }
        if(destroyZipIsCorrupt){
            Files.delete(outputModulePath);
        }else {
            String hash = HashUtil.hashFile(outputModulePath.toFile(), index.getHashalgorithm());
            try{
                index.addSmallModule(moduleDescription,hash);
            }catch(Exception a){
                a.printStackTrace();//ignore
            }
        }
    }

    private void smallModuleCreateZip(FileSystem fs,SmallModule moduleDescription,Class clzz,Path uploadersDirectory)throws IOException,JSONException{
        JSONObject metaData = makeMetaData(moduleDescription);
        Files.write(fs.getPath("SmallModule.json"),metaData.toString(3).getBytes());
        
        //zip(fs, moduleDescription.interfaces()); these are already
        //in the classpath of NU, so we need not repackage them in zip
        zip(fs, moduleDescription.exports(),uploadersDirectory);
        zip(fs, moduleDescription.dependsOn(),uploadersDirectory);
        
        if(moduleDescription.jarsRequired()!=null && 
                moduleDescription.jarsRequired().length > 0){
            throw new IllegalStateException("jars not supported as of now");
            /*Path extraJarRequiredPath = outputDirectory.resolve("jarsRequired");
            Files.createDirectories(extraJarRequiredPath);*/
        }
    }
    
    private void zip(FileSystem fs,Class[]z,Path uploadersDirectory)throws IOException{
        for (Class c : z) {
            String relClassPath = c.getName().replace('.', File.separatorChar);
            Path pathInZip = fs.getPath(relClassPath).getParent();
            Files.createDirectories(pathInZip);//creates all directory entires if required.
            
            handleClassEntry(pathInZip, c, fs,uploadersDirectory);
        }
    }
    
    private void handleClassEntry(Path pathInZip,final Class c,FileSystem fs,Path uploadersDirectory)throws IOException {
        Path classLocationOnDisk = uploadersDirectory.resolve(pathInZip.toString());
        DirectoryStream<Path> ds = Files.newDirectoryStream(classLocationOnDisk,new DirectoryStream.Filter<Path>() {
            @Override public boolean accept(Path entry) throws IOException {
                String fn = entry.getFileName().toString(); String cn = c.getSimpleName();
                return fn.equals(cn+".class") || fn.startsWith(cn+"$");
            }
        });
        for(Path p : ds){
            byte[]b=Files.readAllBytes(p);
            Files.write(pathInZip.resolve(p.getFileName().toString()), b);
        }
        
        // say we want to zie SomeClass.class
        // then we also need to zip SomeClass$1.class
        // That is, we also need to zip inner classes and inner annoymous classes 
        // into the zip as well
    }
    
    private JSONObject makeMetaData(SmallModule moduleDescription)throws JSONException{
        JSONObject metaData = new JSONObject();
        metaData.put("name", moduleDescription.name());
        JSONArray exports = new JSONArray();
        
        assert moduleDescription.exports().length!=moduleDescription.interfaces().length;
        
        for (int j = 0; j < moduleDescription.exports().length; j++) {
            JSONObject exportableItem = new JSONObject();
            exportableItem.put("implementation", moduleDescription.exports()[j].getName());
            exportableItem.put("interface", moduleDescription.interfaces()[j].getName());
            
            exports.put(exportableItem);
        }
        
        metaData.put("exports", exports);
        return metaData;
    }

}
