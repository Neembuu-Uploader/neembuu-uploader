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

import neembuu.rus.DefaultValue;

/**
 *
 * @author Shashank
 */
public interface Environment {
    @Deprecated
    @DefaultValue(s="")             String inputDirectoryName();
    /**
     * Example : http://neembuu.com/uploader/updates/v3.1/
     * @return This is the location where the client will
     * check for new plugins. 
     */
    @DefaultValue(s="")             String baseUpdateURL(); 
    /**
     * Example : /home/neemsb/nu/gitcode
     * @return This is where all git code will be checked out
     * and kept for compilation and plugin extraction & generation.
     */
    @DefaultValue(s="")             String gitDirectory();
    /**
     * Example : /var/www/html/neembuu.com/uploader/updates/v3.1/
     * @return This is where all the small module zip plugins
     * will be written and saved.
     */
    @DefaultValue(s="")             String outputDirectory();
    /**
     * @return This is the name of the sub-folder inside the git-repo
     * where the modules are present. For both NU and NeembuuNow project
     * the value of this is modules.
     */
    @DefaultValue(s="modules")      String modulesFolderName();
    /**
     * @return This is the folder inside each module folder
     * where the source files are located. This may be seen as a constant.
     */
    @DefaultValue(s="src")          String srcFolderName();
    /**
     * @return The list of folder names (or modules) which should be compiled.
     * This should not contain names of modules which are no way related to
     * plugins. Plugin modules such as mega-co-nz which are under-development
     * may be removed from this list.
     */
    @DefaultValue(sa={})            String[]sortedListOfModulesToCompile();
    /**
     * @return The list of folder names (modules) which should be checked
     * for plugins. Plugins are identified by searching classes with the tag.
     * @SmallModule
     */
    @DefaultValue(sa={})            String[]modulesToCheckForExportibles();
    /**
     * @return URI/URL of the git repository from where the latest code
     * may be obtained.
     */
    @DefaultValue(s="")             String gitURI();
    /**
     * Example 3.10
     * @return The major version of the program. This needs to be 
     * changed after every new version is released. 
     */
    @DefaultValue(s="")             String version();
    @Deprecated
    @DefaultValue(s="")             String notification1Timestamp();
    /**
     * @return Used for showing arbitrary notifications
     */
    @DefaultValue(s="")             String notificationTimestamp();
    /**
     * @return If this is true, the entire git repo is deleted, each and ever ytime
     * the plugin generator is run. This option helps in solving problems
     * when the local git repo has corrupted, OR some random problems
     * are showing up, who's reason is not known.
     */
    @DefaultValue(b=true)           boolean cleanUp();
}
