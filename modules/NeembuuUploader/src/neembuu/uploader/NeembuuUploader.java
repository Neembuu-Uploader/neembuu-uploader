/* 
 * Copyright (C) 2015 Shashank Tulsyan <shashaank at neembuu.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package neembuu.uploader;

import neembuu.uploader.utils.UnsyncCopy;
import neembuu.uploader.theme.ThemeCheck;
//import darrylbu.icon.StretchIcon;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import neembuu.reactivethread.CompletionCallback;
import neembuu.reactivethread.ReactiveThread;
import neembuu.release1.api.ui.MainComponent;
import neembuu.release1.ui.mc.MainComponentImpl;
import neembuu.rus.ClassRus;
import neembuu.rus.Rus;
import neembuu.rus.Rusila;
import neembuu.rus.V;
import neembuu.uploader.accountgui.AccountsManager;
import neembuu.uploader.exceptions.proxy.NUProxyException;
import neembuu.uploader.external.PluginDestructionListener;
import neembuu.uploader.external.SmallModuleEntry;
import neembuu.uploader.external.UpdatesAndExternalPluginManager;
import neembuu.uploader.external.UploaderPlugin;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.settings.SettingsManager;
import neembuu.uploader.settings.Application;
import neembuu.uploader.settings.Settings;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.updateprogress.UpdateProgressImpl;
import neembuu.uploader.uploaders.common.MonitoredFileBody;
import neembuu.uploader.uploaders.common.MonitoredFileEntity;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.FilePluginnamePair;
import neembuu.uploader.utils.FileDrop;
import neembuu.uploader.utils.JCheckBoxComparator;
import neembuu.uploader.utils.NUHttpClientUtils;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.utils.NeembuuUploaderLanguages;
import neembuu.uploader.utils.NeembuuUploaderProperties;
import neembuu.uploader.utils.ProxyChecker;
import neembuu.uploader.utils.SSLCertificateValidation;
import neembuu.uploader.utils.UploadStatusUtils;
import neembuu.uploader.versioning.UserImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Main class of this project. Everything starts from the main method in this
 * class.
 *
 * @author vigneshwaran
 */
public class NeembuuUploader extends javax.swing.JFrame {

    //Version variable
    private static final float version = initVersion();
    //ArrayList of files
    public List<File> files = new ArrayList<File>();
    //reference for NUTableModel singleton
    NUTableModel nuTableModel;
    //mapping of checkboxes with their associated hosts
    private final Map<JCheckBox, SmallModuleEntry> map = new TreeMap<JCheckBox, SmallModuleEntry>(new JCheckBoxComparator());   
    private final Map<JCheckBox, SmallModuleEntry> allmap = new TreeMap<JCheckBox, SmallModuleEntry>(new JCheckBoxComparator());
    
    //ImageIcons for 3 states of each of the 4 move rows buttons
    
    private ImageIcon selectFolderButtonIcon = new ImageIcon(getClass().getResource("/neembuuuploader/resources/add_folder.png"));
    private ImageIcon selectFileButtonIcon = new ImageIcon(getClass().getResource("/neembuuuploader/resources/add_file.png"));
    private ImageIcon getLogButtonIcon = new ImageIcon(getClass().getResource("/neembuuuploader/resources/nulog.png"));
    private ImageIcon topnormal = new ImageIcon(getClass().getResource("/neembuuuploader/resources/top24.png"));
    private ImageIcon topmouseover = new ImageIcon(getClass().getResource("/neembuuuploader/resources/top24mouseentered.png"));
    private ImageIcon topmousepressed = new ImageIcon(getClass().getResource("/neembuuuploader/resources/top24mousepressed.png"));
    private ImageIcon bottomnormal = new ImageIcon(getClass().getResource("/neembuuuploader/resources/bottom24.png"));
    private ImageIcon bottommouseover = new ImageIcon(getClass().getResource("/neembuuuploader/resources/bottom24mouseentered.png"));
    private ImageIcon bottommousepressed = new ImageIcon(getClass().getResource("/neembuuuploader/resources/bottom24mousepressed.png"));
    private ImageIcon upnormal = new ImageIcon(getClass().getResource("/neembuuuploader/resources/up24.png"));
    private ImageIcon upmouseover = new ImageIcon(getClass().getResource("/neembuuuploader/resources/up24mouseentered.png"));
    private ImageIcon upmousepressed = new ImageIcon(getClass().getResource("/neembuuuploader/resources/up24mousepressed.png"));
    private ImageIcon downnormal = new ImageIcon(getClass().getResource("/neembuuuploader/resources/down24.png"));
    private ImageIcon downmouseover = new ImageIcon(getClass().getResource("/neembuuuploader/resources/down24mouseentered.png"));
    private ImageIcon downmousepressed = new ImageIcon(getClass().getResource("/neembuuuploader/resources/down24mousepressed.png"));
    private JFileChooser f = null;
    private TrayIcon trayIcon = null;
    private Document doc;
    private String clickURL = "";
    private HttpsURLConnection con;
    
    private final MainComponent mainComponent;

    /**
     *
     * @return singleton instance
     */
    public static NeembuuUploader getInstance() {
        return LazyInitialize.lazy_singleton;
    }
    
    private volatile UpdatesAndExternalPluginManager uaepm;
    void uaepm(UpdatesAndExternalPluginManager uaepm){
        this.uaepm = uaepm;
    }
    
    public static MainComponent getMainComponent() {
        return getInstance().mainComponent;
    }
    //Reference for CheckBoxActionListener common for all the checkboxes
    private ActionListener checkBoxActionListener = new CheckBoxActionListener();

    private void setUpFileChooser() {
        f = new JFileChooser();
        //Enable selection of multiple files
        f.setMultiSelectionEnabled(true);
    }

    private void setUpTrayIcon() {
        if (SystemTray.isSupported()) {
            //trayIcon.setImageAutoSize(true); It renders the icon very poorly.
            //So we render the icon ourselves with smooth settings.
            {
                Dimension d = SystemTray.getSystemTray().getTrayIconSize();
                trayIcon = new TrayIcon(getIconImage().getScaledInstance(d.width, d.height, Image.SCALE_SMOOTH));
            }
            //trayIcon = new TrayIcon(getIconImage());
            //trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip(Translation.T().trayIconToolTip());
            trayIcon.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    NULogger.getLogger().info("System tray double clicked");

                    setExtendedState(JFrame.NORMAL);
                    setVisible(true);
                    repaint();
                    SystemTray.getSystemTray().remove(trayIcon);
                }
            });
        }
    }
    
    /**
     * Creates the initial instance of HttpClient.
     */
    private void setUpHttpClient() {
        NUHttpClient.getHttpClient();
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    private static final class LazyInitialize {

        private static final NeembuuUploader lazy_singleton = make();
        
        private static NeembuuUploader make(){
            NeembuuUploader nu = new NeembuuUploader();
            return nu;
        }
    }

    /**
     * Creates new form NeembuuUploader
     */
    private NeembuuUploader() {
        //An error message will be printed. 
        //Saying 
        //java.lang.ClassNotFoundException: neembuu.release1.ui.mc.EmotionIconProviderImpl
        //please ignore it.
                mainComponent = new MainComponentImpl(this); 
        //mainComponent = new NonUIMainComponent(); << When running in command line mode
        
        NULogger.getLogger().log(Level.INFO, "{0}: Starting up..", getClass().getName());

        //Display the splashscreen until the NeembuuUploader is initialized
        //NeembuuUploaderSplashScreen.getInstance().setVisible(true);

        //Setup NeembuuUploaderProperties.. Create the file if it doesn't exist.
        NeembuuUploaderProperties.setUp();

        //Initialize components
        initComponents();
        initSorting();
        initNotification();
        setUpTrayIcon();

        setUpFileChooser();
        
        setUpHttpClient();
        setupTabs();

        //map each checkbox to its class in the hashmap variable
        //NULogger.getLogger().info("Setting checkbox operations");
        //checkBoxOperations();

        //Load previously saved state
        //loadSavedState();

        //This 3rd party code is to enable Drag n Drop of files
        FileDrop fileDrop = new FileDrop(this, new FileDrop.Listener() {

            @Override
            public void filesDropped(java.io.File[] filesSelected) {
                //Ignore directories
                for (File file : filesSelected) {
                    if (file.isFile()) {
                        files.add(file);
                    }
                }
                if (files.isEmpty()) {
                    return;
                }
                //If one file is dropped, display its name.. If more than one dropped, display the number of files selected
                if (files.size() == 1) {
                    inputFileTextField.setText(files.get(0) + "");
                } else {
                    inputFileTextField.setText(files.size() + " " + Translation.T().nfilesselected());
                }
                NULogger.getLogger().info("Files Dropped");
            }
        });
        


        //Timer is used to periodically refresh the table so that the progress will appear smoothly.
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //Update every 1s
                Timer autoUpdate = new Timer(1000, new ActionListener() {
                    //Check if the queue is locked. If not, repaint.

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!QueueManager.getInstance().isQueueLocked()) {
                            //Should not call firetablerowsupdated as it'll lose the selection of rows.                             //
                            //So repaint is called.
                            neembuuUploaderTable.repaint();
                        }
                    }
                });
                //Unnecessary.. but waits for 3 seconds within which other threads will get more juice and initialize faster..
                //reduced from 10 to 3 as I moved to faster pc
                autoUpdate.setInitialDelay(3000);
                //Start the timer.
                autoUpdate.start();
                NULogger.getLogger().info("Timer started..");
            }
        });

        //By now everything is loaded, so no need of splashscreen anymore,, dispose it. :)
        //NeembuuUploaderSplashScreen.getInstance().dispose();
        NULogger.getLogger().info("Splash screen disposed..");

        //Make the NeembuuUploader appear in center of screen.
        setLocationRelativeTo(null);
        
        selectFileButton.requestFocus();
        
        setVisible(true);
        ThemeCheck.apply(this);
        
        selectFileButton.setBorder(BorderFactory.createEmptyBorder());
        selectFileButton.setContentAreaFilled(false);
        selectFileButton.setToolTipText(Translation.T().selectFileButton());
        selectFileButton.setFocusPainted(false);
        
        selectFolderButton.setBorder(BorderFactory.createEmptyBorder());
        selectFolderButton.setContentAreaFilled(false);
        selectFolderButton.setToolTipText("Select Folder");
        selectFolderButton.setFocusPainted(false);
        
        getLogButton.setBorder(BorderFactory.createEmptyBorder());
        getLogButton.setContentAreaFilled(false);
        getLogButton.setToolTipText("Copy \"nu.log\" to the clipboard");
        getLogButton.setFocusPainted(false);
        
        startNotificationRefreshingThread();
        // starting a thread from a constructor is a bad way to do it
        // however, we dont have enough options here.
    }
    
    private static final class NotificationRequest {
        final HttpGet image,redirect; 

        public NotificationRequest(HttpGet image, HttpGet redirect) {
            this.image = image;
            this.redirect = redirect;
        }
        
    }
    
    private NotificationRequest makeNotificationRequest()throws Exception{
        String params = "userid="+UserImpl.getUserProvider().getUserInstance().uidString()+
                "&os="+System.getProperty("os.name")+
                "&locale="+NeembuuUploaderLanguages.getUserLanguageCode()+
                "&version="+getVersionForProgam();
        
        URI uri1 = new URI("http", "neembuu.com", "/uploader/notification/image", params, null);
        URI uri2 = new URI("http", "neembuu.com", "/uploader/notification/redirect", params, null);
        System.out.println(uri1);
        System.out.println(uri2);
        HttpGet image = new HttpGet(uri1);
        HttpGet redirect = new HttpGet(uri2);
        
        return new NotificationRequest(image, redirect);
    }
    
    private String getAdType() throws Exception{
        StringBuilder content = new StringBuilder();
        String adHtmlPage = "", adImgURL = "", line = "";
        URL url;
        try {
            adHtmlPage = NUHttpClientUtils.getData("http://neembuu.com/uploader/donate/ads/location");
            url = new URL(adHtmlPage);
            SSLCertificateValidation.disable();
            con = (HttpsURLConnection)url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
            adHtmlPage = content.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        doc = Jsoup.parse(adHtmlPage);
        
        try {
            adImgURL = doc.select("img").attr("src");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return adImgURL;
    }
    
    private void prepareImgAd(String adImgURL){
        String imgAdClickURL = "";
        URL url;
        BufferedImage adImg = null;
        
        adImgURL = "https:" +adImgURL;
        
        try {
            url = new URL(adImgURL);
            SSLCertificateValidation.disable();
            con = (HttpsURLConnection)url.openConnection();
            adImg = ImageIO.read(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        notificationButton.setText("");
        notificationButton.setIcon(new ImageIcon(adImg));
        
        imgAdClickURL = doc.select("a[rel=nofollow]").first().attr("href");
        clickURL = imgAdClickURL;
    }
    
    private void prepareTextAd(){
        String titleName = "", textAdClickURL = "", textAdURLName = "", descR = "";
        titleName = doc.select("a[rel=nofollow]").first().text();
        textAdClickURL = doc.select("a[rel=nofollow]").first().attr("href");
        textAdURLName = doc.select("a[rel=nofollow]").eq(1).text();
        descR = doc.select("span").first().text();
        
        String adHtml = "<html><div style=\"text-align:center;\">";
        adHtml += "<a href=\"" +textAdClickURL+ "\" rel=\"nofollow\" target=\"_blank\" style=\"font-size:20px;\">";
        adHtml += titleName;
        adHtml += "</a><br />";
        adHtml += "<span style=\"font-size:17px;\">" +descR+ "</span><br />";
        adHtml += "<small><a href=\"" +textAdClickURL+ "\" rel=\"nofollow\" target=\"_blank\">";
        adHtml += textAdURLName;
        adHtml += "</a></small>";
        adHtml += "</div></html>";
        
        notificationButton.setIcon(null);
        notificationButton.setText(adHtml);
        clickURL = textAdClickURL;
    }
    
    private void initNotification(){
        //final StretchIcon si = new StretchIcon(getClass().getResource("/neembuuuploader/resources/say_yes_to_nu2.png"),true);
        //notificationButton.setIcon(si);
        
        try {
            String adImgURL = getAdType();
            if (!adImgURL.isEmpty()) {
                prepareImgAd(adImgURL);
            } else {
                prepareTextAd();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        final ReactiveThread rt = ReactiveThread.create(new Runnable() {
            @Override public void run() {
                try{
                    NotificationRequest nr = makeNotificationRequest();                    
                    HttpResponse hr = NUHttpClient.newHttpClient().execute(nr.image);
                    BufferedImage bi = ImageIO.read(hr.getEntity().getContent());
                    hr = NUHttpClient.newHttpClient().execute(nr.redirect);
                    final String redirect = EntityUtils.toString(hr.getEntity());
                    if(redirect!=null && redirect.length()>0){
                        if(bi==null)return;
                        //si.setImage(bi);
                        notificationButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                try{
                                    Desktop.getDesktop().browse(new URI(clickURL));
                                }catch(Exception a){a.printStackTrace();}
                            }
                        });
                    }else {
                        System.out.println("redirect was nullish "+redirect);
                    }
                }catch(Exception a){a.printStackTrace();}
            }
        }, CompletionCallback.DUMMY); rt.start();
    }
    
    Runnable adRunnable = new Runnable() {
        public void run() {
            try {
                String adImgURL = getAdType();
                if (!adImgURL.isEmpty()) {
                    prepareImgAd(adImgURL);
                } else {
                    prepareTextAd();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    
    Random rand = new Random();
    int min = 55, max = 85; //rand.nextInt((max - min) + 1) + min
    
    private void startNotificationRefreshingThread(){
        ReactiveThread rt = ReactiveThread.create(new Runnable() {
            @Override public void run() {
                for(;;){
                    try{
                        long sleepInterval = ((long)(min+rand.nextDouble()*(max-min))*1000) ;
                        Thread.sleep(sleepInterval);
                        adRunnable.run();
                    }catch(Exception a){
                        a.printStackTrace();
                    }
                }
            }
        }, CompletionCallback.DUMMY);
        rt.setName("Notification refresher");
        rt.setDaemon(true);
        // the application will exit even if this thread is running
        // if all non-daemon (non-background) threads have ended
        rt.start();
    }
    
    private void initSorting(){
        TableRowSorter<TableModel> sorter 
            = new TableRowSorter<TableModel>(neembuuUploaderTable.getModel());
        neembuuUploaderTable.setRowSorter(sorter);
    }
    
    static Set<Entry<JCheckBox, SmallModuleEntry>> unsyncEntries_map(){
        return UnsyncCopy.unsyncCopyEntries(NeembuuUploader.getInstance().map);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        sideOverlaidPanel = new javax.swing.JPanel();
        getLogButton = new javax.swing.JButton(getLogButtonIcon);
        forumButton = new javax.swing.JButton();
        donateButton = new javax.swing.JButton();
        fbbutton = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        neembuuUploaderTab = new javax.swing.JPanel();
        mainControlsPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        inputFileTextField = new javax.swing.JTextField();
        selectFileButton = new javax.swing.JButton(selectFileButtonIcon);
        addToQueueButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        selectHostsButton = new javax.swing.JButton();
        selectedHostsLabel = new javax.swing.JLabel();
        selectFolderButton = new javax.swing.JButton(selectFolderButtonIcon);
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        nuTableModel = NUTableModel.getInstance();
        neembuuUploaderTable = new javax.swing.JTable(nuTableModel);
        moveToTopButton = new javax.swing.JLabel();
        moveToBottomButton = new javax.swing.JLabel();
        moveUpButton = new javax.swing.JLabel();
        moveDownButton = new javax.swing.JLabel();
        startQueueButton = new javax.swing.JButton();
        stopFurtherButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        maxuploadspinner = new javax.swing.JSpinner();
        lowerNotificationPanel = new javax.swing.JPanel();
        notificationButton = new javax.swing.JButton();
        uploadHistoryTab = new javax.swing.JPanel();
        accountsTab = new javax.swing.JPanel();
        settingsTab = new javax.swing.JPanel();
        aboutTab = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Neembuu Uploader");
        setIconImage(Toolkit.getDefaultToolkit().getImage((getClass().getResource("/neembuuuploader/resources/Icon.png"))));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
        });

        sideOverlaidPanel.setMaximumSize(new java.awt.Dimension(400, 32767));
        sideOverlaidPanel.setMinimumSize(new java.awt.Dimension(204, 32));
        sideOverlaidPanel.setPreferredSize(new java.awt.Dimension(204, 36));

        getLogButton.setMaximumSize(new java.awt.Dimension(24, 24));
        getLogButton.setMinimumSize(new java.awt.Dimension(24, 24));
        getLogButton.setPreferredSize(new java.awt.Dimension(24, 24));
        getLogButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getLogButtonActionPerformed(evt);
            }
        });

        forumButton.setText("Forum");
        forumButton.setMaximumSize(new java.awt.Dimension(69, 32));
        forumButton.setPreferredSize(new java.awt.Dimension(69, 30));
        forumButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forumButtonActionPerformed(evt);
            }
        });

        donateButton.setText("Donate");
        donateButton.setMaximumSize(new java.awt.Dimension(73, 32));
        donateButton.setPreferredSize(new java.awt.Dimension(73, 30));
        donateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                donateButtonActionPerformed(evt);
            }
        });

        fbbutton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/fb3.png"))); // NOI18N
        fbbutton.setBorder(null);
        fbbutton.setBorderPainted(false);
        fbbutton.setContentAreaFilled(false);
        fbbutton.setMaximumSize(new java.awt.Dimension(25, 32));
        fbbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fbbuttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sideOverlaidPanelLayout = new javax.swing.GroupLayout(sideOverlaidPanel);
        sideOverlaidPanel.setLayout(sideOverlaidPanelLayout);
        sideOverlaidPanelLayout.setHorizontalGroup(
            sideOverlaidPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sideOverlaidPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(getLogButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(forumButton, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(donateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fbbutton, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                .addGap(5, 5, 5))
        );
        sideOverlaidPanelLayout.setVerticalGroup(
            sideOverlaidPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sideOverlaidPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(forumButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(donateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(fbbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(getLogButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        neembuuUploaderTab.setLayout(new java.awt.BorderLayout());

        mainControlsPanel.setPreferredSize(new java.awt.Dimension(713, 600));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Select File(s) to upload or Drag and drop files over this window:"));

        inputFileTextField.setEditable(false);
        inputFileTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                inputFileTextFieldMouseClicked(evt);
            }
        });

        selectFileButton.setMaximumSize(new java.awt.Dimension(25, 32));
        selectFileButton.setMinimumSize(new java.awt.Dimension(25, 32));
        selectFileButton.setPreferredSize(new java.awt.Dimension(25, 32));
        selectFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFileButtonActionPerformed(evt);
            }
        });

        addToQueueButton.setText("Add to Queue");
        addToQueueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToQueueButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Selected Host(s):");

        selectHostsButton.setText("Select Hosts");
        selectHostsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectHostsButtonActionPerformed(evt);
            }
        });

        selectedHostsLabel.setText("<html><i>None.. :(</i></html>");

        selectFolderButton.setMaximumSize(new java.awt.Dimension(25, 32));
        selectFolderButton.setMinimumSize(new java.awt.Dimension(25, 32));
        selectFolderButton.setPreferredSize(new java.awt.Dimension(25, 32));
        selectFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFolderButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(inputFileTextField)
                        .addGap(18, 18, 18)
                        .addComponent(selectFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(selectFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(selectedHostsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(selectHostsButton)
                        .addGap(18, 18, 18)
                        .addComponent(addToQueueButton)))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addToQueueButton, selectHostsButton});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectFolderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inputFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addToQueueButton)
                        .addComponent(selectHostsButton))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(selectedHostsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addToQueueButton, inputFileTextField, jLabel2, selectFileButton, selectHostsButton, selectedHostsLabel});

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Upload Queue:"));

        //Set up ProgressBar as renderer for progress column
        ProgressRenderer renderer = new ProgressRenderer(0,100);
        renderer.setStringPainted(true); //Show Progress text
        UploadStatusRenderer uploadStatusRenderer = new UploadStatusRenderer();
        neembuuUploaderTable.setDefaultRenderer(JProgressBar.class, renderer);
        neembuuUploaderTable.setDefaultRenderer(UploadStatus.class, uploadStatusRenderer);
        //Set table's row height large enough to fit JProgressBar.
        neembuuUploaderTable.setRowHeight((int)renderer.getPreferredSize().getHeight());

        UploaderHostNameRenderer uploaderHostNameRenderer = new UploaderHostNameRenderer();
        neembuuUploaderTable.setDefaultRenderer(Uploader.class, uploaderHostNameRenderer);
        neembuuUploaderTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        neembuuUploaderTable.getTableHeader().setReorderingAllowed(false);
        neembuuUploaderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                neembuuUploaderTableMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                neembuuUploaderTableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                neembuuUploaderTableMouseReleased(evt);
            }
        });
        neembuuUploaderTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                neembuuUploaderTableKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(neembuuUploaderTable);
        neembuuUploaderTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        moveToTopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/top24.png"))); // NOI18N
        moveToTopButton.setToolTipText("Move selected row(s) to Top of Queue");
        moveToTopButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                moveToTopButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                moveToTopButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                moveToTopButtonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                moveToTopButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                moveToTopButtonMouseReleased(evt);
            }
        });

        moveToBottomButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/bottom24.png"))); // NOI18N
        moveToBottomButton.setToolTipText("Move selected row(s) to Bottom of Queue");
        moveToBottomButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMouseReleased(evt);
            }
        });

        moveUpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/up24.png"))); // NOI18N
        moveUpButton.setToolTipText("Move selected row(s) Up in Queue");
        moveUpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                moveUpButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                moveUpButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                moveUpButtonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                moveUpButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                moveUpButtonMouseReleased(evt);
            }
        });

        moveDownButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/down24.png"))); // NOI18N
        moveDownButton.setToolTipText("Move selected row(s) Down in Queue");
        moveDownButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                moveDownButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                moveDownButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                moveDownButtonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                moveDownButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                moveDownButtonMouseReleased(evt);
            }
        });

        startQueueButton.setText("Start Queue");
        startQueueButton.setToolTipText("Start queued uploads if any");
        startQueueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startQueueButtonActionPerformed(evt);
            }
        });

        stopFurtherButton.setText("Stop Further");
        stopFurtherButton.setToolTipText("Stop when the current upload is finished");
        stopFurtherButton.setEnabled(false);
        stopFurtherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopFurtherButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Max. no. of simultaneous uploads:");

        maxuploadspinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(2), Integer.valueOf(1), null, Integer.valueOf(1)));
        maxuploadspinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxuploadspinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(maxuploadspinner, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(maxuploadspinner)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(moveToTopButton)
                        .addGap(18, 18, 18)
                        .addComponent(moveUpButton)
                        .addGap(18, 18, 18)
                        .addComponent(moveDownButton)
                        .addGap(18, 18, 18)
                        .addComponent(moveToBottomButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(startQueueButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(stopFurtherButton)))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {moveDownButton, moveToBottomButton, moveToTopButton, moveUpButton});

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {startQueueButton, stopFurtherButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(moveToTopButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(moveUpButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(moveDownButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(moveToBottomButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(stopFurtherButton)
                        .addComponent(startQueueButton))
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {moveDownButton, moveToBottomButton, moveToTopButton, moveUpButton, startQueueButton, stopFurtherButton});

        javax.swing.GroupLayout mainControlsPanelLayout = new javax.swing.GroupLayout(mainControlsPanel);
        mainControlsPanel.setLayout(mainControlsPanelLayout);
        mainControlsPanelLayout.setHorizontalGroup(
            mainControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainControlsPanelLayout.setVerticalGroup(
            mainControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainControlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );

        neembuuUploaderTab.add(mainControlsPanel, java.awt.BorderLayout.CENTER);

        lowerNotificationPanel.setPreferredSize(new java.awt.Dimension(754, 100));
        lowerNotificationPanel.setRequestFocusEnabled(false);

        notificationButton.setContentAreaFilled(false);

        javax.swing.GroupLayout lowerNotificationPanelLayout = new javax.swing.GroupLayout(lowerNotificationPanel);
        lowerNotificationPanel.setLayout(lowerNotificationPanelLayout);
        lowerNotificationPanelLayout.setHorizontalGroup(
            lowerNotificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 761, Short.MAX_VALUE)
            .addGroup(lowerNotificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(lowerNotificationPanelLayout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(notificationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
        lowerNotificationPanelLayout.setVerticalGroup(
            lowerNotificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
            .addGroup(lowerNotificationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(lowerNotificationPanelLayout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(notificationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );

        neembuuUploaderTab.add(lowerNotificationPanel, java.awt.BorderLayout.SOUTH);

        tabbedPane.addTab("NeembuuUploader", neembuuUploaderTab);

        uploadHistoryTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Upload History", uploadHistoryTab);

        accountsTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Accounts", accountsTab);

        settingsTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("Settings", settingsTab);

        aboutTab.setLayout(new java.awt.BorderLayout());
        tabbedPane.addTab("About", aboutTab);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addContainerGap(470, Short.MAX_VALUE)
                    .addComponent(sideOverlaidPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane1Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jLayeredPane1Layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(sideOverlaidPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(626, Short.MAX_VALUE)))
        );
        jLayeredPane1.setLayer(sideOverlaidPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane1.setLayer(tabbedPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLayeredPane1)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLayeredPane1)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private static final AtomicBoolean showedStartQueueMessage = new AtomicBoolean(false);
    
    
    private void maxuploadspinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxuploadspinnerStateChanged
        //Event listener for state changed event for the spinner        

        //getValue() returns as object.. So must convert to string and then to int
        int maxNoOfUploads = Integer.parseInt(maxuploadspinner.getValue() + "");

        // set this bufferSize to queuemanager's variable
        QueueManager.getInstance().setMaxNoOfUploads(maxNoOfUploads);

        //Update the queuing sequence so that more uploads may be started.
        QueueManager.getInstance().updateQueue();
    }//GEN-LAST:event_maxuploadspinnerStateChanged
    /**
     * Depending on OS, the right click menu may be triggered for one of these 3
     * methods. So better register event for all.
     *
     * @param evt
     */
private void neembuuUploaderTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neembuuUploaderTableMouseClicked
    openPopup(evt);
}//GEN-LAST:event_neembuuUploaderTableMouseClicked

private void neembuuUploaderTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neembuuUploaderTableMousePressed
    openPopup(evt);
}//GEN-LAST:event_neembuuUploaderTableMousePressed

private void neembuuUploaderTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neembuuUploaderTableMouseReleased
    openPopup(evt);
}//GEN-LAST:event_neembuuUploaderTableMouseReleased

    /**
     * The following methods set the icons for each state of mouse event of the
     * 4 move row buttons and also call the appropriate functions when clicked
     *
     * @param evt
     */
private void moveToTopButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMouseEntered
    moveToTopButton.setIcon(topmouseover);
}//GEN-LAST:event_moveToTopButtonMouseEntered

private void moveToTopButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMouseExited
    moveToTopButton.setIcon(topnormal);
}//GEN-LAST:event_moveToTopButtonMouseExited

private void moveToBottomButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMouseEntered
    moveToBottomButton.setIcon(bottommouseover);
}//GEN-LAST:event_moveToBottomButtonMouseEntered

private void moveToBottomButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMouseExited
    moveToBottomButton.setIcon(bottomnormal);
}//GEN-LAST:event_moveToBottomButtonMouseExited

private void moveUpButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMouseEntered
    moveUpButton.setIcon(upmouseover);
}//GEN-LAST:event_moveUpButtonMouseEntered

private void moveUpButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMouseExited
    moveUpButton.setIcon(upnormal);
}//GEN-LAST:event_moveUpButtonMouseExited

private void moveDownButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMouseEntered
    moveDownButton.setIcon(downmouseover);
}//GEN-LAST:event_moveDownButtonMouseEntered

private void moveDownButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMouseExited
    moveDownButton.setIcon(downnormal);
}//GEN-LAST:event_moveDownButtonMouseExited

private void moveToTopButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMousePressed
    moveToTopButton.setIcon(topmousepressed);
}//GEN-LAST:event_moveToTopButtonMousePressed

private void moveToTopButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMouseReleased
    moveToTopButton.setIcon(topmouseover);
}//GEN-LAST:event_moveToTopButtonMouseReleased

private void moveToTopButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMouseClicked
    QueueManager.getInstance().moveRowsTop();
}//GEN-LAST:event_moveToTopButtonMouseClicked

private void moveUpButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMousePressed
    moveUpButton.setIcon(upmousepressed);
}//GEN-LAST:event_moveUpButtonMousePressed

private void moveUpButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMouseReleased
    moveUpButton.setIcon(upmouseover);
}//GEN-LAST:event_moveUpButtonMouseReleased

private void moveDownButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMousePressed
    moveDownButton.setIcon(downmousepressed);
}//GEN-LAST:event_moveDownButtonMousePressed

private void moveDownButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMouseReleased
    moveDownButton.setIcon(downmouseover);
}//GEN-LAST:event_moveDownButtonMouseReleased

private void moveToBottomButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMousePressed
    moveToBottomButton.setIcon(bottommousepressed);
}//GEN-LAST:event_moveToBottomButtonMousePressed

private void moveToBottomButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMouseReleased
    moveToBottomButton.setIcon(bottommouseover);
}//GEN-LAST:event_moveToBottomButtonMouseReleased

private void moveUpButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMouseClicked
    QueueManager.getInstance().moveRowsUp();
}//GEN-LAST:event_moveUpButtonMouseClicked

private void moveDownButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMouseClicked
    QueueManager.getInstance().moveRowsDown();
}//GEN-LAST:event_moveDownButtonMouseClicked

private void moveToBottomButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMouseClicked
    QueueManager.getInstance().moveRowsBottom();
}//GEN-LAST:event_moveToBottomButtonMouseClicked

private void startQueueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startQueueButtonActionPerformed
    //Start the queued items if any.. This will set the stopfurther to false and update the queuing there.
    QueueManager.getInstance().setStopFurther(false);

    //Toggle the enabled state of these two buttons
    stopFurtherButton.setEnabled(true);
    startQueueButton.setEnabled(false);
    NULogger.getLogger().info("Start Queue Button clicked.");
}//GEN-LAST:event_startQueueButtonActionPerformed

private void stopFurtherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopFurtherButtonActionPerformed
    //Do not upload the further items in queue.. 
    //Stop with the current uploads.
    //This will set the stopfurther to false and update the queuing there.
    QueueManager.getInstance().setStopFurther(true);

    //Toggle the enabled state of these two buttons
    startQueueButton.setEnabled(true);
    stopFurtherButton.setEnabled(false);
    NULogger.getLogger().info("Stop Further Button clicked");
}//GEN-LAST:event_stopFurtherButtonActionPerformed

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    //If the user clicks the close button on NeembuuUploader, save the state before closing
    saveStateOnClosing();
}//GEN-LAST:event_formWindowClosing

private void neembuuUploaderTableKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_neembuuUploaderTableKeyTyped
    //If the delete key is pressed, then selected rows must be deleted.

    //Must be delete key and minimum of one row must be selected.
    if ((evt.getKeyChar() != KeyEvent.VK_DELETE) || (neembuuUploaderTable.getSelectedRowCount() < 0)) {
        return;
    }

    NULogger.getLogger().info("Delete Key event on Main window");

    //Must lock queue
    QueueManager.getInstance().setQueueLock(true);

    int selectedrow;
    int[] selectedrows = neembuuUploaderTable.getSelectedRows();

    //Remove from the end.. This is the correct way.
    //If you remove from top, then index will change everytime and it'll be stupid to try to do that way.
    for (int i = selectedrows.length - 1; i >= 0; i--) {
        selectedrow = selectedrows[i];

        //Remove only if the selected upload is in one of these states. For others, there is stop method.
        if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.QUEUED, UploadStatus.UPLOADFINISHED,
                UploadStatus.UPLOADFAILED, UploadStatus.UPLOADSTOPPED)) {

            NUTableModel.getInstance().removeUpload(selectedrow);
            NULogger.getLogger().log(Level.INFO, "{0}: Removed row no. {1}", new Object[]{getClass().getName(), selectedrow});
        }

    }


    //Unlock Queue back
    QueueManager.getInstance().setQueueLock(false);
}//GEN-LAST:event_neembuuUploaderTableKeyTyped

private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
    if (!Application.get(Settings.class).minimizetotray() || !SystemTray.isSupported() || trayIcon == null || !isActive()) {
        return;
    }
    NULogger.getLogger().info("Minimizing to Tray");
    setVisible(false);
    try {
        SystemTray.getSystemTray().add(trayIcon);
    } catch (AWTException ex) {
        setVisible(true);
        Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
    }
}//GEN-LAST:event_formWindowIconified

    private void forumButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forumButtonActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("http://neembuu.com/uploader/forum/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_forumButtonActionPerformed

    private void donateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_donateButtonActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("http://neembuu.com/uploader/donate/"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }//GEN-LAST:event_donateButtonActionPerformed

    private void fbbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fbbuttonActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("https://facebook.com/NeembuuUploader"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_fbbuttonActionPerformed

    private void getLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getLogButtonActionPerformed
        Path nuLogPath = Paths.get(System.getProperty("user.home")+File.separatorChar+".neembuuuploader"+File.separatorChar+"nu.log"); 
        try {
            String contents = new String(Files.readAllBytes(nuLogPath), StandardCharsets.UTF_8);
            StringSelection stringSelection = new StringSelection (contents);
            Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
            clpbrd.setContents (stringSelection, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_getLogButtonActionPerformed

    private void selectFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFolderButtonActionPerformed
        //Open up the Open File dialog
        //If the user clicks cancel or close, do not continue.
        f.setMultiSelectionEnabled(false);
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        f.setAcceptAllFileFilterUsed(false);
        if (f.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File folder = f.getSelectedFile();

        //getSelectedFiles() returns as File array.
        //We need ArrayList for efficiency. So convert array to ArrayList
        this.files = new ArrayList<File>(FileUtils.listFiles(folder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));

        //Same stuff as in FileDrop code in constructor
        if (files.size() == 1) {
            inputFileTextField.setText(files.get(0) + "");
        } else {
            inputFileTextField.setText(files.size() + " " + Translation.T().nfilesselected());
        }
        NULogger.getLogger().info("Files selected");
    }//GEN-LAST:event_selectFolderButtonActionPerformed

    private void selectHostsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectHostsButtonActionPerformed
        //Display HostsPanel
        NULogger.getLogger().info("Opening HostsPanel window");
        NULogger.getLogger().info("Updating language");
        HostsPanel.updateLanguage();
        HostsPanel.getInstance().setVisible(true);
    }//GEN-LAST:event_selectHostsButtonActionPerformed

    private void addToQueueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToQueueButtonActionPerformed
        // If no files are selected, do not continue. Show error msg.
        if (files.isEmpty()) {
            ThemeCheck.apply(null);
            JOptionPane.showMessageDialog(this, Translation.T().pleaseSelectAnyFiles());
            return;
        }

        //Declare an empty list for adding selected classes into it.
        List<Class<? extends Uploader>> selectedUploaderClasses = new ArrayList<Class<? extends Uploader>>();

        //Iterate throught the map. If a checkbox is selected, add it's associated class into the above list.
        for (final Map.Entry<JCheckBox, SmallModuleEntry> entry : unsyncEntries_map()) {
            if (entry.getKey().isSelected()) {
                selectedUploaderClasses.add(uaepm.load(entry.getValue()).getUploader(pdl));
            }
        }

        //If class list is empty, that is no checkbox selected, show error msg and go back.
        if (selectedUploaderClasses.isEmpty()) {
            ThemeCheck.apply(null);
            JOptionPane.showMessageDialog(this, Translation.T().selectAtleastOneHost());
            return;
        }

        //If everything is okay, then we must lock the queue to prevent removal(will cause conflict with indices) or starting next upload
        //Lock Queue
        QueueManager.getInstance().setQueueLock(true);

        //Iterate through each file in selected files list
        for (File file : files) {

            //For each file, iterate through each class in selected host classes
            for (Class<? extends Uploader> uploaderClass : selectedUploaderClasses) {
                try {
                    //Get the constructor of that class which has one File parameter.. Infact there is only one constructor and that's that..
                    Constructor<? extends Uploader> uploaderConstructor = uploaderClass.getConstructor();

                    //Use the constructor to create a new instance by passing the file parameter.
                    //Pass that instance to tablemodel's addUpload method
                    Uploader uploader = uploaderConstructor.newInstance();
                    uploader.setFile(file);
                    nuTableModel.addUpload(uploader);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        //We must always unlock queue if we had locked before so that the queuing sequence may start again.
        //Unlock Queue
        QueueManager.getInstance().setQueueLock(false);

        //Set a friendly text for fun.. :)
        inputFileTextField.setText(Translation.T().goAheadMakeMoreUploads());

        //Clear the list of files // May change this in future..
        files.clear();

        NULogger.getLogger().info("Files added to queue");

        if(!showedStartQueueMessage.compareAndSet(false, true))return;

        String title = Translation.T().startQueueButton();
        String message = Translation.T().startQueueButtonToolTip();
        ThemeCheck.apply(null);
        int x = JOptionPane.showConfirmDialog(mainComponent.getJFrame(), message, title, JOptionPane.YES_NO_OPTION);
        if(x == JOptionPane.YES_OPTION){
            startQueueButtonActionPerformed(evt);
        }
    }//GEN-LAST:event_addToQueueButtonActionPerformed

    private void selectFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFileButtonActionPerformed

        f.setMultiSelectionEnabled(true);
        f.setFileSelectionMode(JFileChooser.FILES_ONLY);
        f.setAcceptAllFileFilterUsed(true);
        
        //Open up the Open File dialog
        //If the user clicks cancel or close, do not continue.
        if (f.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        //getSelectedFiles() returns as File array.
        //We need ArrayList for efficiency. So convert array to ArrayList
        this.files = new ArrayList<File>(Arrays.asList(f.getSelectedFiles()));

        //Same stuff as in FileDrop code in constructor
        if (files.size() == 1) {
            inputFileTextField.setText(files.get(0) + "");
        } else {
            inputFileTextField.setText(files.size() + " " + Translation.T().nfilesselected());
        }
        NULogger.getLogger().info("Files selected");
    }//GEN-LAST:event_selectFileButtonActionPerformed

    private void inputFileTextFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inputFileTextFieldMouseClicked
        //if the textfield is clicked, do the same operations as when the select button is clicked.
        selectFileButtonActionPerformed(null);
    }//GEN-LAST:event_inputFileTextFieldMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aboutTab;
    private javax.swing.JPanel accountsTab;
    private javax.swing.JButton addToQueueButton;
    private javax.swing.JButton donateButton;
    private javax.swing.JButton fbbutton;
    private javax.swing.JButton forumButton;
    private javax.swing.JButton getLogButton;
    private javax.swing.JTextField inputFileTextField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel lowerNotificationPanel;
    private javax.swing.JPanel mainControlsPanel;
    private javax.swing.JSpinner maxuploadspinner;
    private javax.swing.JLabel moveDownButton;
    private javax.swing.JLabel moveToBottomButton;
    private javax.swing.JLabel moveToTopButton;
    private javax.swing.JLabel moveUpButton;
    private javax.swing.JPanel neembuuUploaderTab;
    public javax.swing.JTable neembuuUploaderTable;
    private javax.swing.JButton notificationButton;
    private javax.swing.JButton selectFileButton;
    private javax.swing.JButton selectFolderButton;
    private javax.swing.JButton selectHostsButton;
    private javax.swing.JLabel selectedHostsLabel;
    private javax.swing.JPanel settingsTab;
    private javax.swing.JPanel sideOverlaidPanel;
    private javax.swing.JButton startQueueButton;
    private javax.swing.JButton stopFurtherButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel uploadHistoryTab;
    // End of variables declaration//GEN-END:variables

    /**
     * Opens up the Popup Menu
     *
     * @param evt
     */
    private void openPopup(MouseEvent evt) {
        //The above three mouse events call this method.
        //So check which one will trigger Rightclick menu depending on os and use it.
        if (evt.isPopupTrigger()) {
            //Check if it is right click.
            if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                NULogger.getLogger().info("RightClick event");
                //if already 2 or more rows selected, leave it.
                //EDIT: After some days I forgot what this code does,.. This is copied from a stackoverflow post actually..
                //But it's an unchangeable code and will just work forever, so I'm not gonna mess with it.
                if (neembuuUploaderTable.getSelectedRowCount() < 2) {
                    int r = neembuuUploaderTable.rowAtPoint(evt.getPoint());
                    if (r >= 0 && r < neembuuUploaderTable.getRowCount()) {
                        neembuuUploaderTable.setRowSelectionInterval(r, r);
                    } else {
                        neembuuUploaderTable.clearSelection();
                    }

                    int rowindex = neembuuUploaderTable.getSelectedRow();
                    if (rowindex < 0) {
                        return;
                    }
                }
                //Display the popup menu on the exact point of right click.
                PopupBuilder.getInstance().show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }

    void checkBoxOperations() {        
        NULogger.getLogger().info("Mapping Checkboxes");
        synchronized (map){
            map.clear();
        }
        synchronized (allmap){
            allmap.clear();
        }
        
        for(SmallModuleEntry sme : uaepm.getIndex().get(Uploader.class)){
            synchronized (allmap){
                allmap.put(new JCheckBox(sme.getName()), sme);
            }
            try {
                if ( UploaderPlugin.locallyPresent(uaepm.getUpdateLocation(), sme)
                        == UploaderPlugin.LocallyPresent.PRESENT) {
                    UploaderPlugin up = uaepm.load(sme); // this sets activated = true
                }
            } catch (IOException ex) {
                Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Iterator<JCheckBox> it = UnsyncCopy.unsyncCopyKeys(allmap).iterator();
        final ActionListener all = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Thread t = new Thread("allmap_checkBoxAction from ActionListener"){
                    @Override public void run() {
                        allmap_checkBoxAction();
                    }
                }; t.setDaemon(true); t.start();
            }
        };
        while (it.hasNext()) {
            it.next().addActionListener(all);
        }
        
        checkBoxOperation_ActivatePlugins(); int size;
        synchronized (allmap){size = allmap.size();}
        NULogger.getLogger().log(Level.INFO, "{0}: Number of supported sites: {1}", new Object[]{getClass().getName(), size});
    }
    
    private void allmap_checkBoxAction(){
        for (final Map.Entry<JCheckBox, SmallModuleEntry> entry : UnsyncCopy.unsyncCopyEntries(allmap)) {
            if (entry.getKey().isSelected()) {
                // this makes classes :D Creation happens here.
                // this can be slow, and classes are compiled to machine code
                // in java vm
                UploaderPlugin up = uaepm.load(entry.getValue());
                if(up==null){
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            entry.getKey().setSelected(false);
                        }});// failed so ignore this
                }else {
                    checkBoxOperation_ActivatePlugins();
                    Account a = AccountsManager.getAccount(up.getSme().getName());
                    if(a!=null)AccountsManager.loginAccount(a);
                }
            }else {
                checkBoxOperation_DeActivatePlugin(entry.getValue());
            }
        }AccountsManager.getInstance().initAccounts(); // refresh account window
    }
    
    void checkBoxOperation_ActivatePlugins(){
        //Arrange the checkboxes in alphabetical order
        //HostsPanel.getInstance().arrangeCheckBoxes(map);

        //Add action listeners common to all the checkboxes.
        
        for(SmallModuleEntry sme : uaepm.getIndex().get(Uploader.class)){
            if(sme.isActivated()){
                synchronized (map){
                    map.put(new JCheckBox(sme.getName()), sme);
                }
            }
        }
        
        Iterator<JCheckBox> it = unsyncKey_map().iterator();
        while (it.hasNext()) {
            it.next().addActionListener(checkBoxActionListener);
        }HostsPanel.getInstance().arrangeCheckBoxes(UnsyncCopy.unsyncMap(map));

        // so that if this plugin requires account, the account settings show up
    }
    
    private static Set<JCheckBox> unsyncKey_map(){
        return UnsyncCopy.unsyncCopyKeys(NeembuuUploader.getInstance().map);
    }
    
    void checkBoxOperation_DeActivatePlugin(SmallModuleEntry sme){
        JCheckBox cb = findByName(UnsyncCopy.unsyncMap(map), sme.getName());
        if(cb!=null){
            synchronized (map){map.remove(cb);}
            HostsPanel.getInstance().arrangeCheckBoxes(UnsyncCopy.unsyncMap(map)); // visually remove as well
        }

        uaepm.unloadAndDelete(sme);
        checkBoxActionListener.actionPerformed(null);
    }
    
    static JCheckBox findByName(Map<JCheckBox,SmallModuleEntry> m,String name){
        Iterator<JCheckBox> it = m.keySet().iterator();
        while (it.hasNext()) {
            JCheckBox cb = it.next(); if(cb==null || cb.getText()==null){
                continue;
            }
            if(cb.getText().equalsIgnoreCase(name)){
                return cb;
            }
            //.addActionListener(checkBoxActionListener);
        }return null;
    }
    
    void checkBox_selectFromPreviousState(){
        try {
            List<String> lines =
                Files.readAllLines(Application.getNeembuuHome()
                        .resolve("selectedhostslist"), Charset.forName("UTF-8"));
        
            OUTER_LOOP:
            for (String host : lines) {
                INNER_LOOP:
                for (Map.Entry<JCheckBox, SmallModuleEntry> entry : unsyncEntries_map()) {
                    JCheckBox jCheckBox = entry.getKey();
                    SmallModuleEntry smallModuleEntry = entry.getValue();
                    if(smallModuleEntry.getName().equalsIgnoreCase(host)){
                        jCheckBox.setSelected(true);
                        checkBoxActionListener.actionPerformed(null);
                        continue OUTER_LOOP;
                    }
                }
                
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    Profile[]p = null;
    void rusilaExampleForParalytic(){
        // all profiles are saved in a folder called listOfProfiles
        Rus r = Application.getRoot().r("listOfProfiles");
        V[]profiles = Rusila.getArray(r);
        p=new Profile[profiles.length];
        for (int i = 0; i < p.length; i++) {
            V v = profiles[i];
            String[]list = v.s("").replaceAll("\r", "").split("\n");
            p[i] = new Profile(list);
        }
    }
    
    void saveAProfileToDisk()throws Exception{
        Rus r = Application.getRoot().r("listOfProfiles");
        if(p==null || p.length==0)return; // empty
        for (int i = 0; i < p.length; i++) {
            Profile profile = p[i];
            String listOfHosts = ""; 
            for (int j = 0; j < profile.getFileHosts().length; j++) {
                listOfHosts+= ( j==0?"":"\n")+profile.getFileHosts()[j];
            }
            Rusila.set(r, ""+i, listOfHosts); // each profile is saved as a different file.
            // in the folder <user.home>\.neembuuuploader\listofProfiles\
            // with the name such as 0, 1, ,2 etc ...
            // name of the file doesn't matter, it can be anything.
            // if a profile has a name, the name can be used instead of these numbers
        }
        
    }
    
    final class Profile {
        private final String[]nameOfFileHosts;
        public Profile(String[] nameOfFileHosts) {
            this.nameOfFileHosts = nameOfFileHosts;
        }
        public String[] getNameOfFileHosts() {
            return nameOfFileHosts;
        }
        /**
         * Here u have access to all filehoss, along with their check boxes
         * from list of all plugins
         * Similarly u can get access to all ACTIVE plugins,
         * @return 
         */
        public Entry<JCheckBox, SmallModuleEntry> []getFileHosts(){
            // avoid race, do not directly use all_map
            return getFileHosts_fromMap(getAllPluginsMap());
        }
        public Entry<JCheckBox, SmallModuleEntry> []getFileHosts_fromActive(){
            return getFileHosts_fromMap(getActivatePluginsMap());
        }
        private Entry<JCheckBox, SmallModuleEntry> []getFileHosts_fromMap(Map<JCheckBox, SmallModuleEntry> pluginsMap){
            List<Entry<JCheckBox, SmallModuleEntry>> fileHosts = new LinkedList<>();
            OuterLoop:
            for (String host : nameOfFileHosts) {
                Iterator<Entry<JCheckBox,SmallModuleEntry>> it = pluginsMap.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<JCheckBox,SmallModuleEntry> e = it.next();
                    JCheckBox checkBox = e.getKey(); 
                    String text = checkBox.getText().replace("Checkbox", "").trim();
                    if(text.equalsIgnoreCase(host)){
                        fileHosts.add(e);
                        continue OuterLoop;
                    }
                }
            }
            //allPluginsMap.
            return fileHosts.toArray(new Entry[fileHosts.size()]);
        }
    }

    public JTable getTable() {
        return neembuuUploaderTable;
    }

    /**
     * Save the state on exit (unless the user specified otherwise)
     */
    private void saveStateOnClosing() {
        //Do the operations if user settings are enabled..
        Settings settings = Application.get(Settings.class);
        if (settings.savecontrolstate()) {
            settings.maxNoOfUploads(
                    QueueManager.getInstance().getMaxNoOfUploads());
            NULogger.getLogger().log(Level.INFO, "{0}: Maxnoofuploads saved", getClass().getName());
        }
        
        saveListOfSelectedHosts();
        saveQueuedFiles();

        /////////////Save current path//////////////
        if (settings.savecurrentpath()) {
            settings.currentpath(f.getCurrentDirectory().getAbsolutePath());
            NULogger.getLogger().log(Level.INFO, "{0}: currentpath saved: "+f.getCurrentDirectory(), getClass().getName());
        }
        
        //After all over finally exit.. :)
        NULogger.getLogger().log(Level.INFO, "{0}: Exiting..", getClass().getName());
        System.exit(0);
    }
    
    private void saveListOfSelectedHosts(){
        final List<String> l = new LinkedList<String>();
        for (Map.Entry<JCheckBox, SmallModuleEntry> entry : unsyncEntries_map()) {
            JCheckBox jCheckBox = entry.getKey();
            SmallModuleEntry smallModuleEntry = entry.getValue();
            if(jCheckBox.isSelected()){
                l.add(smallModuleEntry.getName());
            }
        }
        
        try {
            Files.write(Application.getNeembuuHome().resolve("selectedhostslist"), l, Charset.forName("UTF-8"),
                    StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        } catch (IOException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveQueuedFiles(){
        ////////////////////////Save queued files////////////////////////
        if (Application.get(Settings.class).savequeuedlinks()) {
            ///IMPORTANT PART - Serialize upload list to disk

            //We need only two parameters.. File and Class. So created a class called FileClassPair and made it serializable
            //Now Declared an empty arraylist of FileClassPair
            List<FilePluginnamePair> savelist = new ArrayList<FilePluginnamePair>();

            //Iterate through each row in the queue
            synchronized(NUTableModel.uploadList){
                for (Uploader uploader : NUTableModel.uploadList) {
                    //Add only if the status is Queued
                    if (uploader.getStatus() == UploadStatus.QUEUED) {                                      

                        savelist.add(new FilePluginnamePair(uploader.getFile(), uploader.getDisplayName()));
                    }
                }
            }


            //Continue if the list is not empty.
            if (!savelist.isEmpty()) {
                try {
                    //Write object to that file and close the stream.
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
                            Application.getNeembuuHome().resolve("nu.dat").toFile()));
                    oos.writeObject(savelist);
                    oos.close();
                    NULogger.getLogger().log(Level.INFO, "{0}: Queued List saved", getClass().getName());
                } catch (IOException ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println(ex);
                }

            }
        }
    }

    /**
     * Loads previously saved state (based on user preference).
     */
    void loadSavedState() {
        //Check user preference
        Settings s = Application.get(Settings.class);
        if (s.savecontrolstate()) {

            //////////////////////////////Set Max no of uploads///////////////////////////////////////////////
            NULogger.getLogger().log(Level.INFO, "{0}: Loading maxnoofuploads value..", getClass().getName());
            maxuploadspinner.setValue(s.maxNoOfUploads());
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////save queued links//////////////////////////////////////////////////
        if (s.savequeuedlinks()) {
            ///Important.. Deserialize listofuploads
            NULogger.getLogger().log(Level.INFO, "{0}: Adding previously queued uploads", getClass().getName());
            File savefile = Application.getNeembuuHome().resolve("nu.dat").toFile();
            //If this file exists, continue. It may not exist if there was no queued upload on previous exit or if it is the first launch.
            if (savefile.exists()) {

                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(savefile));
                    //Get Map
                    List<FilePluginnamePair> savelist = (List<FilePluginnamePair>) ois.readObject();

                    //Add files if they exist to uploadlist
                    //already initcomponents executed. so no need to worry if uploadlist is created or not.

                    for (FilePluginnamePair pair : savelist) {
                        //The user may have deleted some files. So we must check whether each file exists before adding.
                        if (pair.getFile().exists()) {
                            SmallModuleEntry sme = uaepm.getIndex().get(Uploader.class,pair.getHostPluginName());                            
                            Constructor<? extends Uploader> uploaderConstructor = uaepm.load(sme).getUploader(pdl).getConstructor();
                            Uploader uploader = uploaderConstructor.newInstance();
                            uploader.setFile(pair.getFile());
                            nuTableModel.addUpload(uploader);
                        }
                    }

                    //Delete the file after closing all streams
                    ois.close();
                    savefile.delete();

                } catch (Exception ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println(ex);
                }

            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        /////////////////////////////////////Save current path//////////////////////////////////////////////////
        if (s.savecurrentpath()) {
            f.setCurrentDirectory(new File(s.currentpath()));
            NULogger.getLogger().log(Level.INFO, "{0}: Loading currentpath value: "+f.getCurrentDirectory(), getClass().getName() );
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        /////////////////////////////////////Using Proxy//////////////////////////////////////////////////
        if (s.usingProxy()) {
            try {
                String proxyAddress = s.proxyAddress();
                String proxyPort = s.proxyPort();
                
                ProxyChecker proxyChecker = new ProxyChecker(proxyAddress, proxyPort);
                
                if(proxyChecker.control()){
                    NULogger.getLogger().log(Level.INFO, "Proxy is ok!");
                }
                else{
                    NULogger.getLogger().log(Level.INFO, "Proxy isn't ok!");
                }
            }
            catch (NUProxyException ex) {
                Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                ex.printError();
            } catch (Exception ex) {
                Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        NULogger.getLogger().log(Level.INFO, "Route is: {0}", NUHttpClient.getHttpClient().getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY));
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        /////////////////////////////////////Buffer Size//////////////////////////////////////////////////
        String bufferSizeString = s.bufferSize();
        if (bufferSizeString!=null && bufferSizeString.length() > 0) {
            int bufferSize = (int) StringUtils.getSizeFromString(s.bufferSize());
            MonitoredFileBody.setBufferSize(bufferSize);
            MonitoredFileEntity.setBufferSize(bufferSize);
            
            NULogger.getLogger().log(Level.INFO, "Buffer size is: {0}", bufferSize);
        }
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    /**
     * Update the hosts label to show the list of checkboxes selected
     */
    public void updateSelectedHostsLabel() {
        StringBuilder sb = new StringBuilder();
        List<UploaderPlugin> uploaderPluginList = new ArrayList<UploaderPlugin>();
        for (final Map.Entry<JCheckBox, SmallModuleEntry> entry : unsyncEntries_map() ) {
            if (entry.getKey().isSelected()) {
                UploaderPlugin up = uaepm.load(entry.getValue());
                uploaderPluginList.add(up);
            }
        }

        //If no checkboxes selected, show None and a sad smiley
        if (uploaderPluginList.isEmpty()) {
            selectedHostsLabel.setText(Translation.T().selectedHostsLabel());
            return;
        }

        //If the hosts are upto 3, display their names separated by comma
        //If more than 3, display the first three and "and n more"
        //Why 3? There's more space.. For some language, the label to the left can become long. So 3 is safe..
        if (uploaderPluginList.size() <= 3) {
            for (UploaderPlugin cl : uploaderPluginList) {
                /*System.out.println("cl="+cl);
                System.out.println("sme="+cl.getSme());
                System.out.println("sme="+cl.getSme().getName());*/
                sb.append(cl.getSme().getName() ).append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append(uploaderPluginList.get(0).getSme().getName()).append(", ").append(uploaderPluginList.get(1).getSme().getName()).append(", ").append(uploaderPluginList.get(2).getSme().getName()).append(" and ");
            sb.append((uploaderPluginList.size() - 3)).append(" more..");
        }
        selectedHostsLabel.setText(sb.toString());
        NULogger.getLogger().fine(sb.toString());
    }

    /**
     * Update the language at runtime Also updates the column of tablemodel and
     * repaints the table.
     */
    final void languageChanged_UpdateGUI() { /*
         * package private
         */
        NULogger.getLogger().log(Level.INFO, "{0} Calling languageChanged_UpdateGUI", getClass().getName());
        setTitle(Translation.T().neembuuuploader());
        jLabel2.setText(Translation.T().jLabel2());
        selectHostsButton.setText(Translation.T().selectHostsButton());
        
        //selectFileButton.setText(Translation.T().selectFileButton());
        // Paralytic ----- 12/07/2015
        // Since the select file(s) button has now been replaced with an image, there is no need
        // for a translation string to be displayed on the button itself.
        // Moving this translation string to be instead displayed as the tooltiptext.
        
        addToQueueButton.setText(Translation.T().addToQueueButton());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(Translation.T().jPanel2_setBorder()));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(Translation.T().setBorder()));

        startQueueButton.setText(Translation.T().startQueueButton());
        stopFurtherButton.setText(Translation.T().stopFurtherButton());
        jLabel3.setText(Translation.T().jLabel3());
        /*recentButton.setText(Translation.T().recentButton());
        accountsButton.setText(Translation.T().accountsButton());
        settingsButton.setText(Translation.T().settings());
        aboutButton.setText(Translation.T().aboutButton());
        exitButton.setText(Translation.T().exitButton());*/



        moveToTopButton.setToolTipText(Translation.T().moveToTopToolTip());
        moveUpButton.setToolTipText(Translation.T().moveUpButtonToolTip());
        moveDownButton.setToolTipText(Translation.T().moveDownButtonToolTip());
        moveToBottomButton.setToolTipText(Translation.T().moveToBottomButtonToolTip());

        startQueueButton.setToolTipText(Translation.T().startQueueButtonToolTip());
        stopFurtherButton.setToolTipText(Translation.T().stopFurtherButtonToolTip());


        //Set selected hosts label
        updateSelectedHostsLabel();

        //Update table columns
        nuTableModel.languageChanged_UpdateColumnNames();
        neembuuUploaderTable.repaint();
        
        UpdateProgressImpl.INSTANCE.updateTranslation();

    }

    /**
     * This displays a small dialog for user to choose the language from a set
     * of available options
     */
    static void displayLanguageOptionDialog() {
        NeembuuUploaderLanguages.refresh();
        //This code returns the selected language if and only if the user selects the Ok button
        ThemeCheck.apply(null);
        String selectedlanguage = (String) JOptionPane.showInputDialog(NeembuuUploader.getInstance(),
                "Choose your language: ",
                "Language",
                JOptionPane.PLAIN_MESSAGE, null,
                NeembuuUploaderLanguages.getLanguageNames(),
                NeembuuUploaderLanguages.getUserLanguageName());

        //selectedlanguage will be null if the user clicks the cancel or close button
        //so check for that.
        if (selectedlanguage != null) {
            //Set the language to settings file
            NeembuuUploaderLanguages.setUserLanguageByName(selectedlanguage);
            //Change the GUI to new language.
            Translation.changeLanguage(NeembuuUploaderLanguages.getUserLanguageCode());
        }
    }

    /*
     * package/*
     * package
     */ Map<JCheckBox, SmallModuleEntry> getActivatePluginsMap() {
         return UnsyncCopy.unsyncMap(map);
    }
     
    Map<JCheckBox, SmallModuleEntry> getAllPluginsMap() {
        return UnsyncCopy.unsyncMap(allmap);
    }
     
     /**
      * For peace of mind, the version number used for properties file,
      * version.xml and other stuf is Float.toString(version).
      * For display in user interface 
      * @see #getVersionNumberForUI() 
      * @return version number for usage for non-ui part
      */
     public static String getVersionForProgam(){
         return Float.toString(version);
}
     
     /**
      * This should be used only for user interface.
      * It converts float into a stylised version
      * Like if version = 2.912213 this return 2.9.12213
      * @return version number for user interface
      */
     public static String getVersionNumberForUI(){
         return getVersionNumber(version);
     }
     
     static String getVersionNumber(float ver){
         String v = "";
         int major = (int)ver;
         int minor = (int)((ver*10)-major*10);
         String sub_minor = "";
         
         v = major+"."+minor;
         
         if(ver> major+minor*0.1f){
            sub_minor = Float.toString(ver);
            sub_minor = sub_minor.substring(sub_minor.indexOf(".")+2);
            v = v + "." + sub_minor;
         }
         
         return v;
     }
     
     /**
      * Reset the title of the frame.
      */
     public void resetTitle(){
          setTitle(Translation.T().neembuuuploader());
    }
     
    private List<String> deactivatedPluginNames(){
        List<String> deactivatedPluginNames = new LinkedList<String>();
        for(Entry<JCheckBox,SmallModuleEntry> e : UnsyncCopy.unsyncCopyEntries(allmap) ){
            if(!e.getValue().isActivated()){
                deactivatedPluginNames.add(e.getValue().getName());
            }
        }
        return deactivatedPluginNames;
    }
         
    private final PluginDestructionListener pdl = new PluginDestructionListener() {
        @Override public void destroyed() throws Exception{
            List<String> deactivatedPluginNames = deactivatedPluginNames();            
            List<Uploader> uls_toRemove = new LinkedList<>();
            synchronized(NUTableModel.uploadList){
                outer_loop:
                for (Uploader uploader : NUTableModel.uploadList) {
                    String nm = uploader.getDisplayName();
                    inner_loop:
                    for(String p : deactivatedPluginNames){
                        if(p.equalsIgnoreCase(nm)){
                            uls_toRemove.add(uploader);
                            continue outer_loop;
                        }
                    }
                }
                NUTableModel.uploadList.removeAll(uls_toRemove);
            }
            NUTableModel.getInstance().fireTableStructureChanged();
            
            Exception total = new Exception();
            for (Uploader uploader : uls_toRemove) {
                try{ uploader.stopUpload(); }catch(Exception a){ total.addSuppressed(a);}
            }
            if(total.getSuppressed().length > 0){ throw total; }
        }
    };
    static final float version(){return version;}//package-private

    public static float initVersion(){
        float version_tmp = Rusila.get(ClassRus.I(NeembuuUploader.class), "version").f(-1);
        if(version_tmp<0){
            ThemeCheck.apply(null);
            JOptionPane.showMessageDialog(null, "Severe error",
                    "Could not find version of NeembuuUploader",JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("Could not find version");
        }System.out.println("Version = "+version_tmp);
        return version_tmp;
    }
    
    private static int neembuuuploadertab,uploadhistorytab,accountstab,settingstab,abouttab;
    
    private void setupTabs(){
        forumButton.setText(Translation.T().forumButton());
        
        neembuuuploadertab=it(neembuuUploaderTab);    
        uploadhistorytab=it(uploadHistoryTab);
        accountstab=it(accountsTab);
        settingstab=it(settingsTab);
        abouttab=it(aboutTab);
        
        tabbedPane.setTitleAt(neembuuuploadertab, Translation.T().neembuuuploader());
        tabbedPane.setTitleAt(uploadhistorytab, Translation.T().recentButton());
        tabbedPane.setTitleAt(accountstab, Translation.T().accountsButton());
        tabbedPane.setTitleAt(settingstab, Translation.T().settings());
        tabbedPane.setTitleAt(abouttab, Translation.T().aboutButton());
        
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int ti = tabbedPane.getSelectedIndex();
                System.out.println("Tab: " + ti);
                
                if(ti==neembuuuploadertab){
                    // do nothing
                }else if(ti==uploadhistorytab){
                    uploadHistoryTab.removeAll();
                    UploadHistory foo = UploadHistory.getInstance();
                    uploadHistoryTab.add(foo.uploadHistoryPanel);
                    NULogger.getLogger().info("Upload history tab opened");
                }else if(ti==accountstab){
                    if(accountsTab.getComponentCount()<=0){
                        accountsTab.add(AccountsManager.getInstance().getAccountsManagerPanel());
                    }NULogger.getLogger().info("Accounts Manager tab opened");
                }else if(ti==settingstab){
                    if(settingsTab.getComponentCount()<=0){
                        settingsTab.add(SettingsManager.getInstance().getSettingsTabbedPanel());
                    }NULogger.getLogger().info("Settings tab opened");
                }else if(ti==abouttab){
                    if(aboutTab.getComponentCount()<=0){
                        aboutTab.add(AboutNeembuuUploader.getInstance().aboutPanel);
                    }NULogger.getLogger().info("About tab opened");
                }else {
                    throw new AssertionError("Unknown tab");
                }
                
                ThemeCheck.apply(NeembuuUploader.this);
            }
        });                                     
    }
    
    private int it(Component c){
        return tabbedPane.indexOfComponent(c);
    }
    
}
