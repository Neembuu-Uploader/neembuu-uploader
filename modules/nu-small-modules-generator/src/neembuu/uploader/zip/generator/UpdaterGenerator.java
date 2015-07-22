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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FS;

/**
 * The runnable class that will do all the job.
 *
 * @author davidepastore
 */
public class UpdaterGenerator implements Runnable {    
    private File gitDirectory;
    private File outputDirectory;
    
    private final Environment env;
    private ArrayList<PluginToUpdate> pluginsToUpdate;
    private boolean diff = false;

    public UpdaterGenerator(Environment env) {
        this.env = env;
    }
    
    
    /**
     * The Git object.
     */
    private Git git;

    @Override public void run() {
        /*
         File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
         + System.currentTimeMillis());
         */
        /*gitDirectory = new File("C:\\xampp\\htdocs\\nutest");
        outputDirectory = new File("C:\\xampp\\htdocs\\nutestoutput");*/
        
        gitDirectory = new File(env.gitDirectory());
        outputDirectory = new File(env.outputDirectory());
        
        gitDirectory.mkdirs();
        outputDirectory.mkdirs();

        if(env.cleanUp()){
            deleteAndMkdirGit();
        }
        boolean gitUpdated = init_CheckDir_gitClone();
        if (gitUpdated) {} else {System.out.println("No need to update");}
        
        generate();
    }
    
    private void deleteAndMkdirGit(){
        FileUtils.deleteQuietly(gitDirectory);
        gitDirectory.mkdir();
    }
    
    private void generate(){
        System.out.println("Updating plugins zip");
        try {
            NUCompiler compiler = new NUCompiler(gitDirectory,
                    env.modulesFolderName(),env.srcFolderName());
            final String[]modules=env.sortedListOfModulesToCompile();
            //Compile components in order of dependency
            for (String module : modules) {
                compiler.compileDirectory(module);
            }
            
            NUZipFileGenerator zipFileGenerator = new NUZipFileGenerator(env);
                /*gitDirectory, outputDirectory, modules,
                env.modulesToCheckForExportibles());*/
            
            //Create the zip files
            zipFileGenerator.createZipFiles(/*pluginsToUpdate*/);
        } catch (Exception ex) {
            Logger.getLogger(UpdaterGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Davide you might notice that I have made functions smaller.
     * It makes code immensely more readable and you quickly are able
     * to resume the code from where you left.
     * Understanding takes lesser time, as number of lines are lesser.
     * @return 
     */
    private boolean init_CheckDir_gitClone(){
        //Check if the given directory exists
        boolean gitUpdated = true;
        if (RepositoryCache.FileKey.isGitRepository(new File(gitDirectory.getAbsolutePath(), ".git"), FS.DETECTED)) {
            // Already cloned. Just need to pull a repository here.
            System.out.println("git pull");
            gitUpdated = gitPull(gitDirectory);
        } else {
            // Not present or not a Git repository.
            System.out.println("git clone");
            gitClone(gitDirectory);
        }return gitUpdated;
    }
    
    /**
     * Clone GIT repository from sourceforge.
     *
     * @param gitDirectory The directory of Git.
     */
    private void gitClone(File gitDirectory) {
        try {
            git = Git.cloneRepository()
                    .setDirectory(gitDirectory)
                    .setURI(env.gitURI())
                    //.setURI("http://git.code.sf.net/p/neembuuuploader/gitcode")
                    .setProgressMonitor(new TextProgressMonitor()).call();

            for (Ref f : git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()) {
                git.checkout().setName(f.getName()).call();
                System.out.println("checked out branch " + f.getName()
                        + ". HEAD: " + git.getRepository().getRef("HEAD"));
            }
            // try to checkout branches by specifying abbreviated names
            git.checkout().setName("master").call();
        } catch (GitAPIException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Execute git pull command on the given repository.
     * It populates an ArrayList with all the updated files.
     * @param localPath The path where the project is.
     * @return Returns true if you should update plugins, false otherwise.
     */
    private boolean gitPull(File localPath) {
        try {
            Repository localRepo = new FileRepository(localPath.getAbsolutePath() + "/.git");
            git = new Git(localRepo);
            
            
            
            if(populateDiff()){
                PullCommand pullCmd = git.pull();
                pullCmd.call();
                return true;
            }
            else{
                return false;
            }
            
        } catch (GitAPIException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    /**
     * Populate all the files to update, if the system should update.
     */
    private boolean populateDiff() {
        try {
            git.fetch().call();
            Repository repo = git.getRepository();
            ObjectId fetchHead = repo.resolve("FETCH_HEAD^{tree}");
            ObjectId head = repo.resolve("HEAD^{tree}");
            
            ObjectReader reader = repo.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, head);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, fetchHead);
            List<DiffEntry> diffs = git.diff().setShowNameAndStatusOnly(true)
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();
            
            if (diffs.isEmpty()) {
                System.out.println("No diff");
                return false;
            }else{
                return true;
            }
        } catch (GitAPIException | IOException ex) {
            Logger.getLogger(UpdaterGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }return true;//assume true
    }
}
