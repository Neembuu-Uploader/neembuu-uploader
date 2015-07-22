/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.zip.generator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Make operations to create the zip files.
 * @author davidepastore
 */
public class NUZipFileGenerator {

    private String uploadersDirectory;
    private String accountsDirectory;
    private String gitDirectory;
    private File outputDirectory;

    public NUZipFileGenerator(File gitDirectory, File outputDirectory) {
        uploadersDirectory = gitDirectory.getAbsolutePath() + "\\modules\\neembuu-uploader-uploaders\\build\\neembuu\\uploader\\uploaders";
        accountsDirectory = gitDirectory.getAbsolutePath() + "\\modules\\neembuu-uploader-uploaders\\build\\neembuu\\uploader\\accounts";
        this.gitDirectory = gitDirectory.getAbsolutePath();
        this.outputDirectory = outputDirectory;
    }

    /**
     * Create all the zip files.
     * @param pluginsToUpdate The list of files to update.
     */
    public void createZipFiles(ArrayList<PluginToUpdate> pluginsToUpdate) {
        Collection<File> accountClasses = getAllAccounts();
        Collection<File> uploaderClasses = getAllUploaders();
        
        JSONObject metadataJson = new JSONObject();
        JSONArray pluginsList = new JSONArray();
        JSONObject singlePlugin;
        
        int index = 0;
        
        Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.INFO, "Create the zip files");
        Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.INFO, "Plugins size: {0}", pluginsToUpdate.size());
        
        //New method
        for (PluginToUpdate pluginToUpdate : pluginsToUpdate) {
            File uploaderClass = findUploaderClassForPlugin(pluginToUpdate, uploaderClasses);
            File accountClass = findAccountClassForUploader(uploaderClass, accountClasses);
            
            String fileName = FilenameUtils.removeExtension(uploaderClass.getName()) + ".zip";
            ZipOutputStream zip;
            Class cls = null;
            Package aPackage = null;
            singlePlugin = new JSONObject();
            
            Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.INFO, "Create zip for: {0}", FilenameUtils.removeExtension(uploaderClass.getName()));

            try {
                FileOutputStream f = new FileOutputStream(outputDirectory + File.separator + fileName);
                zip = new ZipOutputStream(new BufferedOutputStream(f));
                
                if(accountClass != null){
                    ZipEntry accountZipEntry = new ZipEntry("neembuu\\uploader\\accounts\\" + accountClass.getName());
                    zip.putNextEntry(accountZipEntry);
                    zip.write(Files.readAllBytes(Paths.get(accountClass.toURI())));
                    zip.closeEntry();
                }
                
                ZipEntry uploaderZipEntry = new ZipEntry("neembuu\\uploader\\uploaders\\" + uploaderClass.getName());
                zip.putNextEntry(uploaderZipEntry);
                zip.write(Files.readAllBytes(Paths.get(uploaderClass.toURI())));
                zip.closeEntry();
                
                zip.close();
                
                // Convert File to a URL
                URL url = uploaderClass
                        .getParentFile()
                        .getParentFile()
                        .getParentFile()
                        .getParentFile()
                        .toURI()
                        .toURL();
                URL[] urls = new URL[]{url};
                
                System.out.println("URL: " + url);
                
                // Create a new class loader with the directory
                ClassLoader cl = new URLClassLoader(urls);
                
                // Load in the class
                cls = cl.loadClass("neembuu.uploader.uploaders." + FilenameUtils.removeExtension(uploaderClass.getName()));
                
                System.out.println("Ehy man!");
                
                cls = cls.getSuperclass();
                
                //System.out.println("File name: " + outputDirectory + File.separator + fileName);
                
                singlePlugin.put("name", FilenameUtils.removeExtension(uploaderClass.getName()));
                singlePlugin.put("host", "host");
                singlePlugin.put("accountsupported", accountClass != null);
                singlePlugin.put("downloadurl", "http://neembuu.com/uploader/updates/" + fileName);
                singlePlugin.put("package", cls.getCanonicalName());
                singlePlugin.put("SHA", pluginToUpdate.getSha());

                pluginsList.put(index, singlePlugin);

                index++;
                
            } catch (Exception ex) {
                Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        metadataJson.put("plugins", pluginsList);
        
        //Write the plugins list in the metadata.json file
        try {
            FileUtils.writeStringToFile(new File(outputDirectory, "metadata.json"), metadataJson.toString(3));
        } catch (IOException ex) {
            Logger.getLogger(NUZipFileGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Get all accounts.
     * @return Returns all the account classes.
     */
    private Collection<File> getAllAccounts(){
        return FileUtils.listFiles(new File(accountsDirectory), new IOFileFilter() {

            @Override
            public boolean accept(File file) {
                return FilenameUtils.isExtension(file.getName(), "class");
            }

            @Override
            public boolean accept(File dir, String name) {
                return dir.getName().equals("accounts");
            }
        }, null);
    }
    
    
    /**
     * Get all uploaders.
     * @return Returns all the uploader classes.
     */
    private Collection<File> getAllUploaders(){
        return FileUtils.listFiles(new File(uploadersDirectory), new IOFileFilter() {

            @Override
            public boolean accept(File file) {
                return FilenameUtils.isExtension(file.getName(), "class");
            }

            @Override
            public boolean accept(File dir, String name) {
                return dir.getName().equals("uploaders");
            }
        }, null);
    }
    
    /**
     * Find all the uploader class for a given plugin.
     * @param pluginToUpdate The plugin to update.
     * @param uploaderFiles All the uploader files.
     * @return Returns the uploader associated with the given PluginToUpdate instance.
     */
    private static File findUploaderClassForPlugin(PluginToUpdate pluginToUpdate, Collection<File> uploaderFiles){
        for (File uploaderFile : uploaderFiles) {
            
            String name = uploaderFile.getName();
            name = FilenameUtils.removeExtension(name);
            
            String pluginName = pluginToUpdate.getName();
            pluginName = FilenameUtils.removeExtension(pluginName);

            if (pluginName.equalsIgnoreCase(name)) {
                return uploaderFile;
            }
        }
        System.out.printf("%s is null!", pluginToUpdate.getName());
        return null;
    }

    /**
     * Returns the account associated with the uploader.
     *
     * @param uploader The uploader.
     * @param accountFiles The list of accounts.
     * @return Returns the file associated with the account.
     */
    private static File findAccountClassForUploader(File uploader, Collection<File> accountFiles) {
        for (File accountFile : accountFiles) {
            
            String name = accountFile.getName();
            name = FilenameUtils.removeExtension(name);
            name = name.substring(0, name.indexOf("Account"));
            
            String uploaderName = uploader.getName();
            uploaderName = FilenameUtils.removeExtension(uploaderName);

            if (uploaderName.equalsIgnoreCase(name)) {
                return accountFile;
            }
        }
        return null;
    }

}
