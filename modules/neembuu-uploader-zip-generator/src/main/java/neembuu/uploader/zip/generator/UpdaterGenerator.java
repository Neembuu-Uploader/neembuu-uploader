/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.zip.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    /**
     * The list that contains all the files to update.
     */
    private ArrayList<PluginToUpdate> pluginsToUpdate;
    
    private File gitDirectory;
    private File outputDirectory;
    
    /**
     * The Git object.
     */
    private Git git;

    @Override
    public void run() {
        /*
         File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp"
         + System.currentTimeMillis());
         */
        gitDirectory = new File("C:\\xampp\\htdocs\\nutest");
        outputDirectory = new File("C:\\xampp\\htdocs\\nutestoutput");
        boolean shouldUpdate = true;
        gitDirectory.mkdirs();
        outputDirectory.mkdirs();

        //Check if the given directory exists
        if (RepositoryCache.FileKey.isGitRepository(new File(gitDirectory.getAbsolutePath(), ".git"), FS.DETECTED)) {
            // Already cloned. Just need to pull a repository here.
            System.out.println("git pull");
            shouldUpdate = gitPull(gitDirectory);

        } else {
            // Not present or not a Git repository.
            System.out.println("git clone");
            gitClone(gitDirectory);
        }

        if (shouldUpdate) {
            System.out.println("Updating plugins zip");
            try {
                NUCompiler compiler = new NUCompiler(gitDirectory);
                NUZipFileGenerator zipFileGenerator = new NUZipFileGenerator(gitDirectory, outputDirectory);

                //Compile components in order of dependency
                compiler.compileApi();
                compiler.compileUtils();
                compiler.compileIntAbsImpl();
                compiler.compileUploaders();

                //Create the zip files
                zipFileGenerator.createZipFiles(pluginsToUpdate);
            } catch (Exception ex) {
                Logger.getLogger(UpdaterGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                //removeDirectory(tmpDir);
            }
        } else {
            System.out.println("No need to update");
        }
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
                    .setURI("http://git.code.sf.net/p/neembuuuploader/gitcode")
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
            
            populateDiff();
            
            if(!pluginsToUpdate.isEmpty()){
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
    private void populateDiff() {
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
            
            pluginsToUpdate = new ArrayList<PluginToUpdate>();
            
            checkDiffEmpty(diffs);
            
        } catch (GitAPIException | IOException ex) {
            Logger.getLogger(UpdaterGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void checkDiffEmpty(List<DiffEntry> diffs){
        if (diffs.isEmpty()) {
            System.out.println("No diff");
        } else {
            System.out.println("Check if there are plugins to update");
            for (DiffEntry entry : diffs) {
                String editFilePath = entry.getPath(DiffEntry.Side.NEW);
                if (editFilePath.contains("neembuu-uploader-uploaders/src/neembuu/uploader/uploaders")) {
                    AbbreviatedObjectId newId = entry.getNewId();
                    String sha = newId.name();
                    pluginsToUpdate.add(new PluginToUpdate(new File(gitDirectory, editFilePath), sha));
                    System.out.println(sha + " -> " + editFilePath);
                }
            }
        }
    }

}
