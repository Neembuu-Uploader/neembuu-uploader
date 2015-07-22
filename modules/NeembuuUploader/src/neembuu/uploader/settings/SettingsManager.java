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
package neembuu.uploader.settings;

import java.awt.SystemTray;
import java.awt.event.ItemEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.exceptions.proxy.NUProxyException;
import neembuu.uploader.httpclient.NUHttpClient;
import neembuu.uploader.uploaders.common.MonitoredFileBody;
import neembuu.uploader.uploaders.common.MonitoredFileEntity;
import neembuu.uploader.uploaders.common.StringUtils;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.utils.NeembuuUploaderLanguages;
import neembuu.uploader.utils.ProxyChecker;

/**
 *
 * @author dsivaji
 */
public class SettingsManager extends javax.swing.JDialog {
    //Singleton instance

    private static SettingsManager INSTANCE = null;
    //Whether nimbus theme is available in this system or not?
    private boolean nimbusavailable = false;
    //Whether system tray is available in this system or not?
    private boolean trayavailable = false;

    /**
     * 
     * @return the singleton instance. Call setVisible(true) after this.
     */
    public static SettingsManager getInstance() {


        if (INSTANCE == null) {
            INSTANCE = new SettingsManager(NeembuuUploader.getInstance(), true);
            
            //General tab
            
            //First update the language
            updateLanguage();

            // Set Language combo box selection based on the user selected settings.
            INSTANCE.languageComboBox.setSelectedItem(NeembuuUploaderLanguages.getUserLanguageName());

            // Set Theme radiobutton based on the user selection
            String theme = Application.get(Settings.class).themeNm();
            if (theme.equals("default")) {
                INSTANCE.systemThemeRadioButton.setSelected(true);
            } else if (theme.equals("nimbus")) {
                INSTANCE.systemThemeRadioButton.setSelected(false);
                INSTANCE.nimbusThemeRadioButton.setSelected(true);
            }INSTANCE.whitenBackgroundCheckBox.setSelected(Application.get(Settings.class).whitenBackground());

            Settings settings = Application.get(Settings.class);
            
            //Set State Checkbox
            INSTANCE.saveStateCheckBox.setSelected(settings.savecontrolstate());
            INSTANCE.saveQueuedLinksOnExit.setSelected(settings.savequeuedlinks());
            INSTANCE.minimizeToTray.setSelected(settings.minimizetotray());
            INSTANCE.saveCurrentPath.setSelected(settings.savecurrentpath());
            INSTANCE.autoRetryFailedUploads.setSelected(settings.autoretryfaileduploads());
            INSTANCE.showOverallProgress.setSelected(settings.showoverallprogress());

            //Set logging checkbox
            INSTANCE.loggingCheckBox.setSelected(settings.logging());
            
            
            //Proxy tab
            Settings s = Application.get(Settings.class);
            //Set radio buttons
            if(s.usingProxy()){
                INSTANCE.manualProxyRadioButton.setSelected(true);
                INSTANCE.noProxyRadioButton.setSelected(false);
                INSTANCE.setProxyEnabled(true);
            }
            else{
                INSTANCE.manualProxyRadioButton.setSelected(false);
                INSTANCE.noProxyRadioButton.setSelected(true);
            }
            
            // Set proxy Jtextfield based on the user selection
            String proxyAddress = Application.get(Settings.class).proxyAddress();
            String proxyPort = Application.get(Settings.class).proxyPort();
            INSTANCE.proxyAddress.setText(proxyAddress);
            INSTANCE.proxyPort.setText(proxyPort);
            
            
            //Connection tab
            
            // Set buffer size combo box selection based on the user selected settings.
            String bufferSize = Application.get(Settings.class).bufferSize();
            NULogger.getLogger().log(Level.INFO, "Read buffer size: {0}", bufferSize);
            INSTANCE.bufferSizeJComboBox.setSelectedItem(bufferSize.toString());
        }

        //Return instance
        return INSTANCE;
    }

    /**
     * Creates new form SettingsManager
     */
    public SettingsManager(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        //Check if Nimbus theme is available or not..
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                nimbusavailable = true;
                nimbusThemeRadioButton.setEnabled(true);
                break;
            }
        }
        NULogger.getLogger().log(Level.INFO, "Nimbus theme available? : {0}", nimbusavailable);

        //Check if Nimbus theme is available or not..
        if (SystemTray.isSupported()) {
            trayavailable = true;
            minimizeToTray.setEnabled(true);
        }
        NULogger.getLogger().log(Level.INFO, "System Tray available? : {0}", trayavailable);
        
        //Temporary disabling for release 2.9
        settingsTabbedPanel.remove(1);

        //Set location relative to NU
        setLocationRelativeTo(NeembuuUploader.getInstance());
    }

    /**
     * private method that dynamically updates the language..
     */
    private static void updateLanguage() {
        NULogger.getLogger().log(Level.INFO, "{0}: Updating Language..", SettingsManager.class.getName());
        
        //General tab

        //Language Panel
        INSTANCE.setTitle(Translation.T().settings());
        INSTANCE.languagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Translation.T().languagePanel()));
        INSTANCE.languageLabel.setText(Translation.T().languageLabel());

        //Theme panel
        INSTANCE.themePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Translation.T().themePanel()));
        INSTANCE.systemThemeRadioButton.setText(Translation.T().systemThemeRadioButton());
        //////Set different text for this radio button depending on the availability of theme
        if (INSTANCE.nimbusavailable) {
            INSTANCE.nimbusThemeRadioButton.setText(Translation.T().nimbusThemeRadioButtonAvailableText());
        } else {
            INSTANCE.nimbusThemeRadioButton.setText(Translation.T().nimbusThemeRadioButtonNotAvailableText());
        }


        //State Panel
        INSTANCE.statePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Translation.T().statePanel()));
        INSTANCE.saveStateCheckBox.setText(Translation.T().saveStateCheckBox());
        INSTANCE.saveQueuedLinksOnExit.setText(Translation.T().saveQueuedLinksOnExit());
        INSTANCE.minimizeToTray.setText(Translation.T().minimizeToTray());
        INSTANCE.saveCurrentPath.setText(Translation.T().saveCurrentPath());
        INSTANCE.autoRetryFailedUploads.setText(Translation.T().autoRetryFailedUploads());
        INSTANCE.showOverallProgress.setText(Translation.T().showOverallProgress());
        
        //Diagnosis Panel
        INSTANCE.diagnosisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Translation.T().diagnosisPanel()));
        INSTANCE.loggingCheckBox.setText(Translation.T().loggingCheckBox());

        
        //Proxy tab
        
        INSTANCE.proxyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Translation.T().proxyPanel()));
        
        //JLabel
        INSTANCE.proxyAddressJLabel.setText(Translation.T().proxyaddressLabel());
        INSTANCE.proxyPortJLabel.setText(Translation.T().proxyportLabel());
        
        //Radio Button
        INSTANCE.noProxyRadioButton.setText(Translation.T().noProxy());
        INSTANCE.manualProxyRadioButton.setText(Translation.T().manualProxy());
        
        
        //Connection tab
        
        //Tabbed pane
        INSTANCE.settingsTabbedPanel.setTitleAt(0, Translation.T().general());
//        INSTANCE.jTabbedPane.setTitleAt(1, Translation.T().proxy());
        INSTANCE.settingsTabbedPanel.setTitleAt(1, Translation.T().connection());
        
        //Http Panel
        INSTANCE.httpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Translation.T().http()));
        
        //JLabel
        INSTANCE.bufferSizeJLabel.setText(Translation.T().bufferSize());
        
        //Pack() finally.. This is especially useful here 
        //as the settings panel is where user will change the language and it
        //has to update dynamically here..
        INSTANCE.pack();
    }
    
    private void setProxyEnabled(boolean enabled){
        //JtextField
        proxyAddress.setEnabled(enabled);
        proxyPort.setEnabled(enabled);

        //JLabel
        proxyAddressJLabel.setEnabled(enabled);
        proxyPortJLabel.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        proxyButtonGroup = new javax.swing.ButtonGroup();
        themeButtonGroup = new javax.swing.ButtonGroup();
        settingsTabbedPanel = new javax.swing.JTabbedPane();
        generalPanel = new javax.swing.JPanel();
        languagePanel = new javax.swing.JPanel();
        languageLabel = new javax.swing.JLabel();
        languageComboBox = new javax.swing.JComboBox();
        themePanel = new javax.swing.JPanel();
        systemThemeRadioButton = new javax.swing.JRadioButton();
        nimbusThemeRadioButton = new javax.swing.JRadioButton();
        whitenBackgroundCheckBox = new javax.swing.JCheckBox();
        statePanel = new javax.swing.JPanel();
        saveStateCheckBox = new javax.swing.JCheckBox();
        saveQueuedLinksOnExit = new javax.swing.JCheckBox();
        minimizeToTray = new javax.swing.JCheckBox();
        saveCurrentPath = new javax.swing.JCheckBox();
        autoRetryFailedUploads = new javax.swing.JCheckBox();
        showOverallProgress = new javax.swing.JCheckBox();
        diagnosisPanel = new javax.swing.JPanel();
        loggingCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        proxyPanel = new javax.swing.JPanel();
        noProxyRadioButton = new javax.swing.JRadioButton();
        manualProxyRadioButton = new javax.swing.JRadioButton();
        proxyAddressJLabel = new javax.swing.JLabel();
        proxyPortJLabel = new javax.swing.JLabel();
        proxyAddress = new javax.swing.JTextField();
        proxyPort = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        httpPanel = new javax.swing.JPanel();
        bufferSizeJLabel = new javax.swing.JLabel();
        bufferSizeJComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        languagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Language"));

        languageLabel.setText("Choose your language: ");

        languageComboBox.setModel(new javax.swing.DefaultComboBoxModel(NeembuuUploaderLanguages.getLanguageNames(true)));
        languageComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                languageComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout languagePanelLayout = new javax.swing.GroupLayout(languagePanel);
        languagePanel.setLayout(languagePanelLayout);
        languagePanelLayout.setHorizontalGroup(
            languagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, languagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(languageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(languageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        languagePanelLayout.setVerticalGroup(
            languagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(languagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(languagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(languageLabel)
                    .addComponent(languageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        themePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Theme (requires restart)"));

        themeButtonGroup.add(systemThemeRadioButton);
        systemThemeRadioButton.setSelected(true);
        systemThemeRadioButton.setText("System Default Theme");
        systemThemeRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                systemThemeRadioButtonItemStateChanged(evt);
            }
        });

        themeButtonGroup.add(nimbusThemeRadioButton);
        nimbusThemeRadioButton.setText("Nimbus Theme (Requires Java SE 6 Update 10 or later)");
        nimbusThemeRadioButton.setEnabled(false);
        nimbusThemeRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                nimbusThemeRadioButtonItemStateChanged(evt);
            }
        });

        whitenBackgroundCheckBox.setText("Whiten Background");
        whitenBackgroundCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                whitenBackgroundCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout themePanelLayout = new javax.swing.GroupLayout(themePanel);
        themePanel.setLayout(themePanelLayout);
        themePanelLayout.setHorizontalGroup(
            themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, themePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, themePanelLayout.createSequentialGroup()
                        .addComponent(systemThemeRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(whitenBackgroundCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(89, 89, 89))
                    .addComponent(nimbusThemeRadioButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE))
                .addContainerGap())
        );
        themePanelLayout.setVerticalGroup(
            themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(themePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(systemThemeRadioButton)
                    .addComponent(whitenBackgroundCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(nimbusThemeRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("State"));

        saveStateCheckBox.setSelected(true);
        saveStateCheckBox.setText("Save controls state on exit");
        saveStateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveStateCheckBoxActionPerformed(evt);
            }
        });

        saveQueuedLinksOnExit.setSelected(true);
        saveQueuedLinksOnExit.setText("Save queued links on exit");
        saveQueuedLinksOnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveQueuedLinksOnExitActionPerformed(evt);
            }
        });

        minimizeToTray.setSelected(true);
        minimizeToTray.setText("Minimize to System Tray");
        minimizeToTray.setEnabled(false);
        minimizeToTray.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeToTrayActionPerformed(evt);
            }
        });

        saveCurrentPath.setSelected(true);
        saveCurrentPath.setText("Save current path on exit");
        saveCurrentPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveCurrentPathActionPerformed(evt);
            }
        });

        autoRetryFailedUploads.setSelected(true);
        autoRetryFailedUploads.setText("Auto-retry failed uploads");
        autoRetryFailedUploads.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoRetryFailedUploadsActionPerformed(evt);
            }
        });

        showOverallProgress.setSelected(true);
        showOverallProgress.setText("Show an overall progress");
        showOverallProgress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showOverallProgressActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout statePanelLayout = new javax.swing.GroupLayout(statePanel);
        statePanel.setLayout(statePanelLayout);
        statePanelLayout.setHorizontalGroup(
            statePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveStateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveQueuedLinksOnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minimizeToTray, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveCurrentPath, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoRetryFailedUploads, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showOverallProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 498, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        statePanelLayout.setVerticalGroup(
            statePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(saveStateCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveQueuedLinksOnExit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(minimizeToTray)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveCurrentPath)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(autoRetryFailedUploads, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(showOverallProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        diagnosisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Diagnosis"));

        loggingCheckBox.setText("Enable logging reports to nu.log file");
        loggingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loggingCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout diagnosisPanelLayout = new javax.swing.GroupLayout(diagnosisPanel);
        diagnosisPanel.setLayout(diagnosisPanelLayout);
        diagnosisPanelLayout.setHorizontalGroup(
            diagnosisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(diagnosisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loggingCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        diagnosisPanelLayout.setVerticalGroup(
            diagnosisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(diagnosisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loggingCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(languagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(themePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addComponent(statePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(diagnosisPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(languagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(themePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(statePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(diagnosisPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        settingsTabbedPanel.addTab("General", generalPanel);

        proxyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Proxy Configuration"));

        proxyButtonGroup.add(noProxyRadioButton);
        noProxyRadioButton.setText("No proxy");
        noProxyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noProxyRadioButtonActionPerformed(evt);
            }
        });

        proxyButtonGroup.add(manualProxyRadioButton);
        manualProxyRadioButton.setText("Manual proxy configuration");
        manualProxyRadioButton.setToolTipText("");
        manualProxyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualProxyRadioButtonActionPerformed(evt);
            }
        });

        proxyAddressJLabel.setText("Http Proxy:");
        proxyAddressJLabel.setEnabled(false);

        proxyPortJLabel.setText("Port:");
        proxyPortJLabel.setEnabled(false);

        proxyAddress.setEnabled(false);

        proxyPort.setEnabled(false);

        javax.swing.GroupLayout proxyPanelLayout = new javax.swing.GroupLayout(proxyPanel);
        proxyPanel.setLayout(proxyPanelLayout);
        proxyPanelLayout.setHorizontalGroup(
            proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proxyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proxyPanelLayout.createSequentialGroup()
                        .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(noProxyRadioButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(manualProxyRadioButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proxyPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(proxyAddressJLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(proxyAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                        .addGap(10, 10, 10)
                        .addComponent(proxyPortJLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(proxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28))))
        );
        proxyPanelLayout.setVerticalGroup(
            proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proxyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noProxyRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(manualProxyRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(proxyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyAddressJLabel)
                    .addComponent(proxyPortJLabel)
                    .addComponent(proxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(proxyAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(proxyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(proxyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(335, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 562, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 494, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        settingsTabbedPanel.addTab("Proxy", jPanel2);

        httpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("HTTP"));

        bufferSizeJLabel.setText("Buffer Size:");

        bufferSizeJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "4 KB", "8 KB", "16 KB", "32 KB", "64 KB", "128 KB", "256 KB", "512 KB", "1024 KB", "2048 KB", "4096 KB", "8192 KB" }));
        bufferSizeJComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bufferSizeJComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout httpPanelLayout = new javax.swing.GroupLayout(httpPanel);
        httpPanel.setLayout(httpPanelLayout);
        httpPanelLayout.setHorizontalGroup(
            httpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(httpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bufferSizeJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(bufferSizeJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(280, 280, 280))
        );
        httpPanelLayout.setVerticalGroup(
            httpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(httpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(httpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bufferSizeJLabel)
                    .addComponent(bufferSizeJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(httpPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(httpPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(408, Short.MAX_VALUE))
        );

        settingsTabbedPanel.addTab("Connection", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(settingsTabbedPanel))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(settingsTabbedPanel)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void languageComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_languageComboBoxItemStateChanged
        //The item state changed listener executes two times
        //one for selection and one for deselection
        //We must execute code only one.
        //So use this code to run for only "selected" event
        if (evt.getStateChange() == ItemEvent.SELECTED) {

            //Set the user language by the index (starts from 0)
            NeembuuUploaderLanguages.setUserLanguageByIndex(languageComboBox.getSelectedIndex());

            //Then call this method to update language on the main window and table
            Translation.changeLanguage(NeembuuUploaderLanguages.getUserLanguageCode());

            //Finally call this to update self window.
            updateLanguage();
        }
    }//GEN-LAST:event_languageComboBoxItemStateChanged

    private void systemThemeRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_systemThemeRadioButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {

            //Set the 'theme' property to 'default'
            Application.get(Settings.class).themeNm("default");
            nimbusThemeRadioButton.setSelected(false); 
            systemThemeRadioButton.setSelected(true); 
        }
    }//GEN-LAST:event_systemThemeRadioButtonItemStateChanged

    private void nimbusThemeRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_nimbusThemeRadioButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {

            //Set the 'theme' property to 'nimbus'
            Application.get(Settings.class).themeNm("nimbus");
            nimbusThemeRadioButton.setSelected(true); 
            systemThemeRadioButton.setSelected(false); 
        }
    }//GEN-LAST:event_nimbusThemeRadioButtonItemStateChanged

    private void saveStateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveStateCheckBoxActionPerformed

        //Set the 'savecontrolstate' property to true or false
        //depending on the state of the checkbox
        Application.get(Settings.class).savecontrolstate(saveStateCheckBox.isSelected());
    }//GEN-LAST:event_saveStateCheckBoxActionPerformed

    private void saveQueuedLinksOnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveQueuedLinksOnExitActionPerformed

        //Set the 'savequeuedlinks' property to true or false
        //depending on the state of the checkbox
        Application.get(Settings.class).savequeuedlinks(saveQueuedLinksOnExit.isSelected());
    }//GEN-LAST:event_saveQueuedLinksOnExitActionPerformed

    private void minimizeToTrayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeToTrayActionPerformed
        //Set the 'minimizetotray' property to true or false
        //depending on the state of the checkbox
        Application.get(Settings.class).minimizetotray(minimizeToTray.isSelected());
    }//GEN-LAST:event_minimizeToTrayActionPerformed

    private void saveCurrentPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCurrentPathActionPerformed
        //Set the 'savecurrentpath' property to true or false
        //depending on the state of the checkbox
        Application.get(Settings.class).savecurrentpath(saveCurrentPath.isSelected());
    }//GEN-LAST:event_saveCurrentPathActionPerformed

    private void loggingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loggingCheckBoxActionPerformed

        //Set the 'logging' property to true or false
        //depending on the state of the checkbox
        Application.get(Settings.class).logging(loggingCheckBox.isSelected());

        //Turn the Logger on or off depending on the state of the checkbox
        if (loggingCheckBox.isSelected()) {
            NULogger.getLogger().setLevel(Level.INFO);
            NULogger.getLogger().info("Logger turned on");
        } else {
            NULogger.getLogger().info("Turning off logger");
            NULogger.getLogger().setLevel(Level.OFF);
        }
    }//GEN-LAST:event_loggingCheckBoxActionPerformed

    private void manualProxyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualProxyRadioButtonActionPerformed
        setProxyEnabled(true);
    }//GEN-LAST:event_manualProxyRadioButtonActionPerformed

    private void noProxyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noProxyRadioButtonActionPerformed
        setProxyEnabled(false);
    }//GEN-LAST:event_noProxyRadioButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        //Here we must store proxy info
        Application.get(Settings.class).proxyAddress(proxyAddress.getText());
        Application.get(Settings.class).proxyPort(proxyPort.getText());
        
        if(noProxyRadioButton.isSelected()){
            NULogger.getLogger().info("Disabling proxy.");
            Application.get(Settings.class).usingProxy(false);
            NUHttpClient.resetProxy();
            
            //Close the window
            dispose();
        }
        else{
            try {
                NULogger.getLogger().info("Activating proxy.");
                Application.get(Settings.class).usingProxy(true);
                
                ProxyChecker proxyChecker = new ProxyChecker(proxyAddress.getText(), proxyPort.getText());
                
                if(proxyChecker.control()){
                    NULogger.getLogger().info("Proxy activated");
                    
                    //Close the window
                    dispose();
                }
                else{
                    NULogger.getLogger().log(Level.INFO, "Proxy isn't ok!");
                    
                }
            } catch (NUProxyException ex) {
                Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
                ex.printError();
            } catch (Exception ex) {
                Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }//GEN-LAST:event_formWindowClosing

    private void autoRetryFailedUploadsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoRetryFailedUploadsActionPerformed
        //Set the 'autoretryfaileduploads' property to true or false
        //depending on the state of the checkbox
        Application.get(Settings.class).autoretryfaileduploads(autoRetryFailedUploads.isSelected());
    }//GEN-LAST:event_autoRetryFailedUploadsActionPerformed

    private void bufferSizeJComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bufferSizeJComboBoxItemStateChanged
        //The item state changed listener executes two times
        //one for selection and one for deselection
        //We must execute code only one.
        //So use this code to run for only "selected" event
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            int value = (int) StringUtils.getSizeFromString(evt.getItem().toString());
            NULogger.getLogger().log(Level.INFO, "Selected element: {0}, in bytes: {1}", new Object[]{evt.getItem().toString(), value});
            
            //Set the property
            Application.get(Settings.class).bufferSize(evt.getItem().toString());
            
            MonitoredFileBody.setBufferSize(value);
            MonitoredFileEntity.setBufferSize(value);
        }
    }//GEN-LAST:event_bufferSizeJComboBoxItemStateChanged

    private void showOverallProgressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showOverallProgressActionPerformed
        //Set the 'showoverallprogress' property to true or false
        //depending on the state of the checkbox
        Application.get(Settings.class).showoverallprogress(showOverallProgress.isSelected());
        
        //Reset the title
        if(!showOverallProgress.isSelected()){
            NeembuuUploader.getInstance().resetTitle();
        }
        
    }//GEN-LAST:event_showOverallProgressActionPerformed

    private void whitenBackgroundCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whitenBackgroundCheckBoxActionPerformed
        Application.get(Settings.class).whitenBackground(whitenBackgroundCheckBox.isSelected());
    }//GEN-LAST:event_whitenBackgroundCheckBoxActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SettingsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SettingsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SettingsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SettingsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SettingsManager dialog = new SettingsManager(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    public JTabbedPane getSettingsTabbedPanel() {
        return settingsTabbedPanel;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoRetryFailedUploads;
    private javax.swing.JComboBox bufferSizeJComboBox;
    private javax.swing.JLabel bufferSizeJLabel;
    private javax.swing.JPanel diagnosisPanel;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JPanel httpPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JComboBox languageComboBox;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JPanel languagePanel;
    private javax.swing.JCheckBox loggingCheckBox;
    private javax.swing.JRadioButton manualProxyRadioButton;
    private javax.swing.JCheckBox minimizeToTray;
    private javax.swing.JRadioButton nimbusThemeRadioButton;
    private javax.swing.JRadioButton noProxyRadioButton;
    private javax.swing.JTextField proxyAddress;
    private javax.swing.JLabel proxyAddressJLabel;
    private javax.swing.ButtonGroup proxyButtonGroup;
    private javax.swing.JPanel proxyPanel;
    private javax.swing.JTextField proxyPort;
    private javax.swing.JLabel proxyPortJLabel;
    private javax.swing.JCheckBox saveCurrentPath;
    private javax.swing.JCheckBox saveQueuedLinksOnExit;
    private javax.swing.JCheckBox saveStateCheckBox;
    private javax.swing.JTabbedPane settingsTabbedPanel;
    private javax.swing.JCheckBox showOverallProgress;
    private javax.swing.JPanel statePanel;
    private javax.swing.JRadioButton systemThemeRadioButton;
    private javax.swing.ButtonGroup themeButtonGroup;
    private javax.swing.JPanel themePanel;
    private javax.swing.JCheckBox whitenBackgroundCheckBox;
    // End of variables declaration//GEN-END:variables
}
