/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuu.uploader.translation;

import neembuu.rus.DefaultValue;

/**
 *
 * @author Shashank
 */
public interface TranslationValues {

    @DefaultValue(s = "About")
    String aboutButton();

    @DefaultValue(s = "Login failed: account not actived.")
    String accountnotactived();

    @DefaultValue(s = "Accounts")
    String accountsButton();

    @DefaultValue(s = "Hosts (Account only)")
    String acctonlyPanel();

    @DefaultValue(s = "Add to Queue")
    String addToQueueButton();

    @DefaultValue(s = "All uploads have been completed :)")
    String allUploadsCompleted();

    @DefaultValue(s = "Auto-retry failed uploads")
    String autoRetryFailedUploads();

    @DefaultValue(s = "The IP address has been banned.")
    String bannedip();

    @DefaultValue(s = "Login failed: user is banned.")
    String banneduser();

    @DefaultValue(s = "Buffer size:")
    String bufferSize();

    @DefaultValue(s = "Captcha control")
    String captchacontrol();

    @DefaultValue(s = "The captcha you entered is incorrect.")
    String captchaerror();

    @DefaultValue(s = "Clear History")
    String clearHistoryButton();

    @DefaultValue(s = "Download the latest version from here")
    String clicklabel();

    @DefaultValue(s = "Do you wish to clear upload history?")
    String confirmClear();

    @DefaultValue(s = "Connection")
    String connection();

    @DefaultValue(s = "Copy Delete URL")
    String copyDeleteURL();

    @DefaultValue(s = "Copy Download URL")
    String copyDownloadURL();

    @DefaultValue(s = "Your outdated Neembuu Uploader version")
    String currentversionlabel();

    @DefaultValue(s = "Daily upload limits exceeded. The limit is:")
    String dailyuploadlimit();

    @DefaultValue(s = "Delete URL")
    String Delete_URL();

    @DefaultValue(s = "Diagnosis")
    String diagnosisPanel();

    @DefaultValue(s = "username and password must be both filled or both empty")
    String dialogerror();

    @DefaultValue(s = "Download URL")
    String Download_URL();

    @DefaultValue(s = "Empty file")
    String emptyfile();

    @DefaultValue(s = "Exit")
    String exitButton();

    @DefaultValue(s = "Enter a name for the html file..")
    String exportLinkDialog();

    @DefaultValue(s = "Export Links")
    String exportLinks();

    @DefaultValue(s = "File")
    String File();

    @DefaultValue(s = "File is in black list")
    String fileinblacklist();

    @DefaultValue(s = "filetype not supported")
    String filetypenotsupported();

    @DefaultValue(s = "Hosts (Free and Account)")
    String freeandacctPanel();

    @DefaultValue(s = "General")
    String general();

    @DefaultValue(s = "Getting Cookie")
    String GETTINGCOOKIE();

    @DefaultValue(s = "Getting errors")
    String GETTINGERRORS();

    @DefaultValue(s = "Getting link")
    String GETTINGLINK();

    @DefaultValue(s = "Go ahead, Make more uploads.. :)")
    String goAheadMakeMoreUploads();

    @DefaultValue(s = "Go to Download URL")
    String gotoDownloadURL();

    @DefaultValue(s = "Host")
    String Host();

    @DefaultValue(s = "Host name")
    String Hostname();

    @DefaultValue(s = "HTTP")
    String http();

    @DefaultValue(s = "The server declined to allow the requested access.")
    String http403();

    @DefaultValue(s = "Enter your account details for the appropriate hosts..")
    String infoLabel();

    @DefaultValue(s = "Select whichever sites you would like to upload to.. You need not select again unless you want to change..")
    String infoLabel1();

    @DefaultValue(s = "If you don't have an account or if you want to disable an account or if a site has temporary login problems,leave both the fields blank and save..")
    String infoLabel2();

    @DefaultValue(s = "Get a free account from the appropriate sites if you don't have one so you can manage files on the cloud..")
    String infoLabel3();

    @DefaultValue(s = "Initializing")
    String INITIALISING();

    @DefaultValue(s = "Login failed: password or login is incorrect.")
    String invalidlogin();

    @DefaultValue(s = "Login failed: password is incorrect.")
    String invalidpassword();

    @DefaultValue(s = "The given proxy configuration is wrong.")
    String invalidproxy();

    @DefaultValue(s = "The given proxy host is wrong.")
    String invalidproxyhost();

    @DefaultValue(s = "The given proxy port is wrong.")
    String invalidproxyport();

    @DefaultValue(s = "Login failed: user is incorrect.")
    String invaliduser();

    @DefaultValue(s = "Selected Host(s):")
    String jLabel2();

    @DefaultValue(s = "Max. no. of simultaneous uploads:")
    String jLabel3();

    @DefaultValue(s = "Recently Uploaded Files")
    String label();

    @DefaultValue(s = "Choose your language: ")
    String languageLabel();

    @DefaultValue(s = "Language")
    String languagePanel();

    @DefaultValue(s = "For filesize limitations and other info on each site, click here")
    String limitationsLabel();

    @DefaultValue(s = "Login failed: account is temporarily locked.")
    String lockedaccount();

    @DefaultValue(s = "Logged in")
    String LOGGEDIN();

    @DefaultValue(s = "Enable logging reports to nu.log file")
    String loggingCheckBox();

    @DefaultValue(s = "Logging Failed")
    String LOGGINGFAILED();

    @DefaultValue(s = "Logging in")
    String LOGGINGIN();

    @DefaultValue(s = "Login failed due to server problem or plugin is out-of-date or invalid login details.. Please check in a browser and try again...")
    String loginerror();

    @DefaultValue(s = "Manual proxy configuration")
    String manualProxy();

    @DefaultValue(s = "Maximum file size limit:")
    String maxfilesize();

    @DefaultValue(s = "NeembuuUploader Loading...")
    String message();

    @DefaultValue(s = "Minimum file size limit:")
    String minfilesize();

    @DefaultValue(s = "Minimize to System Tray")
    String minimizeToTray();

    @DefaultValue(s = "Move selected row(s) Down in Queue")
    String moveDownButtonToolTip();

    @DefaultValue(s = "Move selected row(s) to Bottom of Queue")
    String moveToBottomButtonToolTip();

    @DefaultValue(s = "Move selected row(s) to Top of Queue")
    String moveToTopToolTip();

    @DefaultValue(s = "Move selected row(s) Up in Queue")
    String moveUpButtonToolTip();

    @DefaultValue(s = "NA")
    String NA();

    @DefaultValue(s = "Neembuu Uploader")
    String neembuuuploader();

    @DefaultValue(s = "Latest available version of Neembuu Uploader")
    String newversionlabel();

    @DefaultValue(s = "files selected..")
    String nfilesselected();

    @DefaultValue(s = "Nimbus Theme")
    String nimbusThemeRadioButtonAvailableText();

    @DefaultValue(s = "Nimbus Theme (Requires Java SString 6 Update 10 or later)")
    String nimbusThemeRadioButtonNotAvailableText();

    @DefaultValue(s = "No proxy")
    String noProxy();

    @DefaultValue(s = "No rows selected..")
    String noRowsSelected();

    @DefaultValue(s = "Password")
    String Password();

    @DefaultValue(s = "Please select some files..")
    String pleaseSelectAnyFiles();

    @DefaultValue(s = "Please wait..")
    String PLEASEWAIT();

    @DefaultValue(s = "Progress")
    String Progress();

    @DefaultValue(s = "Proxy")
    String proxy();

    @DefaultValue(s = "Http Proxy:")
    String proxyaddressLabel();

    @DefaultValue(s = "Proxy Configuration")
    String proxyPanel();

    @DefaultValue(s = "Port:")
    String proxyportLabel();

    @DefaultValue(s = "The given proxy has timed out.")
    String proxytimeout();

    @DefaultValue(s = "Queued")
    String QUEUED();

    @DefaultValue(s = "Upload History")
    String recentButton();

    @DefaultValue(s = "Register New")
    String registerButton();

    @DefaultValue(s = "Remove Finished")
    String removeFinished();

    @DefaultValue(s = "Remove from queue")
    String removeFromQueue();

    @DefaultValue(s = "Remove Selected")
    String removeSelectedButton();

    @DefaultValue(s = "Retry failed")
    String RETRYFAILED();

    @DefaultValue(s = "Retrying..")
    String RETRYING();

    @DefaultValue(s = "Retry Upload")
    String retryUpload();

    @DefaultValue(s = "Re-Uploading..")
    String REUPLOADING();

    @DefaultValue(s = "Save")
    String savebutton();

    @DefaultValue(s = "Save current path on exit")
    String saveCurrentPath();

    @DefaultValue(s = "Save queued links on exit")
    String saveQueuedLinksOnExit();

    @DefaultValue(s = "Save controls state on exit")
    String saveStateCheckBox();

    @DefaultValue(s = "Saving state...")
    String savingstate();

    @DefaultValue(s = "Select atleast one host..")
    String selectAtleastOneHost();

    @DefaultValue(s = "None.. :(")
    String selectedHostsLabel();

    @DefaultValue(s = "Select File(s)")
    String selectFileButton();

    @DefaultValue(s = "Select Hosts")
    String selectHostsButton();

    @DefaultValue(s = "Select File(s) to upload or Drag and drop files over this window:")
    String jPanel2_setBorder();

    @DefaultValue(s = "Upload Queue:")
    String setBorder();

    @DefaultValue(s = "Settings")
    String settings();

    @DefaultValue(s = "Show an overall progress")
    String showOverallProgress();

    @DefaultValue(s = "Size")
    String Size();

    @DefaultValue(s = "Speed")
    String Speed();

    @DefaultValue(s = "Start Queue")
    String startQueueButton();

    @DefaultValue(s = "Start queued uploads if any")
    String startQueueButtonToolTip();

    @DefaultValue(s = "State")
    String statePanel();

    @DefaultValue(s = "Status")
    String Status();

    @DefaultValue(s = "Stop Further")
    String stopFurtherButton();

    @DefaultValue(s = "Stop when the current upload is finished")
    String stopFurtherButtonToolTip();

    @DefaultValue(s = "Stop Upload")
    String stopUpload();

    @DefaultValue(s = "System Default Theme")
    String systemThemeRadioButton();

    @DefaultValue(s = "Theme (requires restart)")
    String themePanel();

    @DefaultValue(s = "Accounts Manager")
    String title();

    @DefaultValue(s = "Select favorite hosts to upload")
    String title2();

    @DefaultValue(s = "Upload History")
    String title3();

    @DefaultValue(s = "Too many tries, try again later.")
    String toomanytries();

    @DefaultValue(s = "To Retry")
    String TORETRY();

    @DefaultValue(s = "Double click to restore")
    String trayIconToolTip();

    @DefaultValue(s = "english")
    @Deprecated
    String unicodeString();

    @DefaultValue(s = "Upload Failed")
    String UPLOADFAILED();

    @DefaultValue(s = "Upload failed")
    String uploadfailed();

    @DefaultValue(s = "Upload Finished")
    String UPLOADFINISHED();

    @DefaultValue(s = "Uploading")
    String UPLOADING();

    @DefaultValue(s = "Upload Invalid")
    String UPLOADINVALID();

    @DefaultValue(s = "Upload limit exceeded")
    String uploadlimit();

    @DefaultValue(s = "Upload Stopped")
    String UPLOADSTOPPED();

    @DefaultValue(s = "Username")
    String Username();
    
    @DefaultValue(s = "en")
    String languageLocale();
    
    @DefaultValue(s = "English(Default)")
    String languageDisplayName();
    
    @DefaultValue(s = "Version 3.0 onwards, users need to activate plugins by selecting them in the activate plugins tab.")
    String version3activateManuallyMessage();
    
    @DefaultValue(s = "Activate Plugins")
    String activatePlugins();

    @DefaultValue(s = "Updating Plugins")
    String updatingPlugins();

    @DefaultValue(s = "Uploader's Forum")
    String forumButton();
    
    @DefaultValue(s = "Donate")
    String donateButton();
    
}
