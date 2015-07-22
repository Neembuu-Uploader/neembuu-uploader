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

import neembuu.uploader.translation.Translation;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.theme.ThemeCheck;
import neembuu.uploader.utils.NULogger;

/**
 *
 * @author dsivaji
 */
public class UploadHistory extends javax.swing.JDialog implements ClipboardOwner {

    //Singleton instance
    private static UploadHistory INSTANCE = new UploadHistory(NeembuuUploader.getInstance(), true);
    //reference to system clipboard
    private Clipboard clipboard = null;
    //reference to tablemodel just for easy typing
    DefaultTableModel model;
    private static final int FILE = 0;
    private static final int HOST = 1;
    private static final int DOWNLOADURL = 2;
    private static final int DELETEURL = 3;

    /**
     * Creates new form UploadHistory
     */
    public UploadHistory(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        model = (DefaultTableModel) uploadTable.getModel();
    }

    /**
     * 
     * @return the singleton instance. Call setVisible(true) with it.
     */
    public static UploadHistory getInstance() {
        //Add the rows by reading the recent.log file
        //Use the INSTANCE dot when referring nonstatic from static
        INSTANCE.updateRows();

        //Update the language
        updateLanguage();

        //Different languages make the component in different size. So pack it finally..
        INSTANCE.pack();

        //After all these completed, finally return.
        return INSTANCE;
    }

    private static void updateLanguage() {
        NULogger.getLogger().log(Level.INFO, "{0}: updating language for components", UploadHistory.class.getName());
        INSTANCE.setTitle(Translation.T().title3());

        INSTANCE.copyDownloadURLButton.setToolTipText(Translation.T().copyDownloadURL());
        INSTANCE.copyDeleteURLButton.setToolTipText(Translation.T().copyDeleteURL());
        INSTANCE.gotoDownloadURLButton.setToolTipText(Translation.T().gotoDownloadURL());
        INSTANCE.exportButton.setToolTipText(Translation.T().exportLinks());

        INSTANCE.clearHistoryButton.setToolTipText(Translation.T().clearHistoryButton());
        INSTANCE.removeSelectedButton.setToolTipText(Translation.T().removeSelectedButton());

        //This code will clear any renderers or editors. Here this table has none so no problem.
        INSTANCE.model.setColumnIdentifiers(new String[]{
                    Translation.T().File(),
                    Translation.T().Host(),
                    Translation.T().Download_URL(),
                    Translation.T().Delete_URL()
                });
    }

    /**
     * private method that updates the rows
     */
    private void updateRows() {
        NULogger.getLogger().info("Updating rows..");
        model.setRowCount(0);

        try {
            //Get a reader object to this file.
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.home") + File.separator + "recent.log"));

            //Update the rows by reading file
            String data;
            while ((data = reader.readLine()) != null) {
                try {
                    Vector columns = new Vector();
                    String[] columnarray = data.split("<>");
                    //Validating url
                    //new URL(columnarray[DOWNLOADURL]); //This prevents filesonic NA rows to be showed.
                    columns.addAll(0, Arrays.asList(columnarray));
                    model.addRow(columns);
                } catch (Exception e) {
                    NULogger.getLogger().log(Level.INFO, "{0}: {1}", new Object[]{getClass().getName(), e});
                    //If it's  not a correct url, ignore it and loop next
                    continue;
                }
            }
            //It is necessary to close. Otherwise delete() method somewhere else won't work
            reader.close();
        } catch (Exception ex) {
            NULogger.getLogger().severe(ex.toString());
            System.err.println(ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        uploadHistoryPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        uploadTable = new javax.swing.JTable();
        clearHistoryButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        copyDownloadURLButton = new javax.swing.JButton();
        copyDeleteURLButton = new javax.swing.JButton();
        gotoDownloadURLButton = new javax.swing.JButton();
        removeSelectedButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        uploadTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "File", "Host", "Download URL", "Delete URL if any"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        uploadTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPane1.setViewportView(uploadTable);

        clearHistoryButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/clear_history.png"))); // NOI18N
        clearHistoryButton.setToolTipText("Clear History");
        clearHistoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearHistoryButtonActionPerformed(evt);
            }
        });

        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/exporturl.png"))); // NOI18N
        exportButton.setToolTipText("Export links");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        copyDownloadURLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/copydownloadurl.png"))); // NOI18N
        copyDownloadURLButton.setToolTipText("Copy Download URL");
        copyDownloadURLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyDownloadURLButtonActionPerformed(evt);
            }
        });

        copyDeleteURLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/copydeleteurl.png"))); // NOI18N
        copyDeleteURLButton.setToolTipText("Copy Delete URL");
        copyDeleteURLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyDeleteURLButtonActionPerformed(evt);
            }
        });

        gotoDownloadURLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/gotodownloadurl.png"))); // NOI18N
        gotoDownloadURLButton.setToolTipText("Goto Download URL");
        gotoDownloadURLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoDownloadURLButtonActionPerformed(evt);
            }
        });

        removeSelectedButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/removefromlist.png"))); // NOI18N
        removeSelectedButton.setToolTipText("Remove selected");
        removeSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedButtonActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout uploadHistoryPanelLayout = new javax.swing.GroupLayout(uploadHistoryPanel);
        uploadHistoryPanel.setLayout(uploadHistoryPanelLayout);
        uploadHistoryPanelLayout.setHorizontalGroup(
            uploadHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(uploadHistoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(uploadHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 882, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, uploadHistoryPanelLayout.createSequentialGroup()
                        .addComponent(copyDownloadURLButton)
                        .addGap(18, 18, 18)
                        .addComponent(copyDeleteURLButton)
                        .addGap(18, 18, 18)
                        .addComponent(gotoDownloadURLButton)
                        .addGap(18, 18, 18)
                        .addComponent(exportButton)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(removeSelectedButton)
                        .addGap(18, 18, 18)
                        .addComponent(clearHistoryButton)))
                .addContainerGap())
        );
        uploadHistoryPanelLayout.setVerticalGroup(
            uploadHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(uploadHistoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(uploadHistoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(copyDeleteURLButton)
                    .addComponent(copyDownloadURLButton)
                    .addComponent(gotoDownloadURLButton)
                    .addComponent(exportButton)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearHistoryButton)
                    .addComponent(removeSelectedButton))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(uploadHistoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(uploadHistoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearHistoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearHistoryButtonActionPerformed
        ThemeCheck.apply(null);
        if (JOptionPane.showConfirmDialog(this, Translation.T().
                confirmClear(), "", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }

        NULogger.getLogger().info("Clearing the upload history");
        try {
            //delete the file
            new File(System.getProperty("user.home") + File.separator + "recent.log").delete();
        } catch (Exception e) {
            NULogger.getLogger().warning(e.toString());
            System.err.println(e);
        }

        //What a cool way to clear rows?
        model.setRowCount(0);
    }//GEN-LAST:event_clearHistoryButtonActionPerformed

private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
    NULogger.getLogger().log(Level.INFO, "{0}: Export Button clicked", getClass().getName());

    //Return if no rows selected.
    if (uploadTable.getSelectedRowCount() < 1) {
        ThemeCheck.apply(null);
        JOptionPane.showMessageDialog(this, Translation.T().noRowsSelected());
        return;
    }

    //Ask for a file to save linklist
    JFileChooser fc = new JFileChooser();
    //Set File selection only mode
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    //Set Dialog Title
    fc.setDialogTitle(Translation.T().exportLinkDialog());
    //Disable All Files option
    fc.setAcceptAllFileFilterUsed(false);
    //Enable HTML file extension only
    fc.setFileFilter(new FileNameExtensionFilter("HTML File", new String[]{"html"}));
    //Show save dialog
    if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
        return;
    }

    File htmlfile = fc.getSelectedFile();
    //Get selected rows

    int[] rows = uploadTable.getSelectedRows();

    StringBuilder sb = new StringBuilder("<html>"
            + "<head><title>Neembuu Uploader</title>"
            + "<style type='text/css'>"
            + " table { display:block; width:90%; border-top:1px solid #e5eff8; border-right:1px solid #e5eff8; margin:1em auto;border-collapse:collapse;}"
            + " td { color:#7892a8;border-bottom:1px solid #e5eff8;border-left:1px solid #e5eff8;padding:.3em 1em;text-align:center;}"
            + " tr.odd td { background:#f7fbff } tr.odd .column1 {background:#f4f9fe;} .column1 {background:#f9fcfe;}"
            + " th, h3 { background:#f4f9fe; text-align:center; font:bold 1.2em/2em 'Century Gothic','Trebuchet MS',Arial,Helvetica,sans-serif; color:#66a3d3; }"
            + "</style></head>"
            + "<body><center><h3>Neembuu Uploader Exported Linkset</h3></center>"
            + "<table><tr class='odd'><th class='column1'>File</th><th>Host</th><th>Download URL</th><th>Delete URL if any</th></tr>");

    int i = 1;
    for (int row : rows) {
        if (i % 2 == 0) {
            sb.append("<tr class='odd'>");
        } else {
            sb.append("<tr>");
        }

        for (int column = 0; column < uploadTable.getModel().getColumnCount(); column++) {
            if (column == 0) {
                sb.append("<td class='column1'>");
            } else {
                sb.append("<td>");
            }
            if (column == DOWNLOADURL
                    && !(uploadTable.getModel().getValueAt(row, column).equals(UploadStatus.NA.getDefaultLocaleSpecificString())
                    || uploadTable.getModel().getValueAt(row, column).equals(UploadStatus.NA.getLocaleSpecificString()))) {
                sb.append("<a target='_blank' href='").append(uploadTable.getModel().getValueAt(row, column)).append("'>").append(uploadTable.getModel().getValueAt(row, column)).append("</a>");
            } else {
                sb.append(uploadTable.getModel().getValueAt(row, column));
            }

            sb.append("</td>");
        }

        sb.append("</tr>");
        i++;
    }
    sb.append("</table></body></html>");
    try {
        NULogger.getLogger().log(Level.INFO, "{0}: Writing links to html file..", getClass().getName());
        //Check if file name ends with html
        if (!(htmlfile.getName().toLowerCase().endsWith(".html") || htmlfile.getName().toLowerCase().endsWith(".htm"))) {
            htmlfile = new File(htmlfile.getAbsolutePath() + ".html");
        }
        PrintWriter writer = new PrintWriter(new FileWriter(htmlfile));
        writer.write(sb.toString());
        writer.close();
    } catch (Exception ex) {
        NULogger.getLogger().log(Level.INFO, "{0}: Error while writing html file\n{1}", new Object[]{getClass().getName(), ex});
        System.err.println(ex);
    }
}//GEN-LAST:event_exportButtonActionPerformed

    private void copyDownloadURLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyDownloadURLButtonActionPerformed
        NULogger.getLogger().log(Level.INFO, "{0}: CopyDownloadURL Button clicked", getClass().getName());

        //Return if no rows selected.
        if (uploadTable.getSelectedRowCount() < 1) {
            ThemeCheck.apply(null);
            JOptionPane.showMessageDialog(this, Translation.T().noRowsSelected());
            return;
        }

        //initialize a StringBuilder
        StringBuilder listofurls = new StringBuilder();

        //get selected rows
        int[] selectedrows = uploadTable.getSelectedRows();

        //Collect the set of urls and set the text in Clipboard
        for (int selectedrow : selectedrows) {
            listofurls.append(uploadTable.getValueAt(selectedrow, DOWNLOADURL).toString()).append("\n");
        }
        setClipboardContent(listofurls.toString());
    }//GEN-LAST:event_copyDownloadURLButtonActionPerformed

    private void copyDeleteURLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyDeleteURLButtonActionPerformed
        NULogger.getLogger().log(Level.INFO, "{0}: CopyDeleteURL Button clicked", getClass().getName());

        //Return if no rows selected.
        if (uploadTable.getSelectedRowCount() < 1) {
            ThemeCheck.apply(null);
            JOptionPane.showMessageDialog(this, Translation.T().noRowsSelected());
            return;
        }

        //initialize a StringBuilder
        StringBuilder listofurls = new StringBuilder();

        //get selected rows
        int[] selectedrows = uploadTable.getSelectedRows();

        //Collect the set of urls and set the text in Clipboard
        for (int selectedrow : selectedrows) {
            try {
                String deleteurl = uploadTable.getValueAt(selectedrow, DELETEURL).toString();
                //Validate url
                new URL(deleteurl);
                listofurls.append(deleteurl).append("\n");
            } catch (Exception e) {
                NULogger.getLogger().log(Level.INFO, "{0}: {1}", new Object[]{getClass().getName(), e});
                //If it's  not a correct url, ignore it and loop next
                continue;
            }
        }
        setClipboardContent(listofurls.toString());
    }//GEN-LAST:event_copyDeleteURLButtonActionPerformed

    private void gotoDownloadURLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoDownloadURLButtonActionPerformed
        NULogger.getLogger().log(Level.INFO, "{0}: GotoDownloadURL Button clicked", getClass().getName());

        //Return if no rows selected.
        if (uploadTable.getSelectedRowCount() < 1) {
            ThemeCheck.apply(null);
            JOptionPane.showMessageDialog(this, Translation.T().noRowsSelected());
            return;
        }

        //get selected rows
        int[] selectedrows = uploadTable.getSelectedRows();

        //Collect the set of urls and set the text in Clipboard
        //Remember it's a multiple selection interval
        for (int selectedrow : selectedrows) {
            String url =
                    uploadTable.getValueAt(selectedrow, DOWNLOADURL).toString();
            if (!Desktop.isDesktopSupported()) {
                return;
            }
            try {
                NULogger.getLogger().log(Level.INFO, "Opening url in browser: {0}", url);
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                NULogger.getLogger().log(Level.WARNING, "{0}: Cannot load url: {1}", new Object[]{getClass().getName(), url});
                System.err.println(ex);
            }
        }
    }//GEN-LAST:event_gotoDownloadURLButtonActionPerformed

    private void removeSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedButtonActionPerformed
        NULogger.getLogger().log(Level.INFO, "{0}: Remove Selected Button clicked", getClass().getName());

        //Return if no rows selected.
        if (uploadTable.getSelectedRowCount() < 1) {
            ThemeCheck.apply(null);
            JOptionPane.showMessageDialog(this, Translation.T().noRowsSelected());
            return;
        }

        deleteRowsAndReprintFile();
    }//GEN-LAST:event_removeSelectedButtonActionPerformed
    /**
     * Place a String on the clipboard, and make this class the owner of the
     * clipboard contents.
     */
    public void setClipboardContent(String aString) {
        StringSelection stringSelection = new StringSelection(aString);
        clipboard.setContents(stringSelection, this);
        NULogger.getLogger().info("Copied to clipboard");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearHistoryButton;
    private javax.swing.JButton copyDeleteURLButton;
    private javax.swing.JButton copyDownloadURLButton;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton gotoDownloadURLButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton removeSelectedButton;
    javax.swing.JPanel uploadHistoryPanel;
    private javax.swing.JTable uploadTable;
    // End of variables declaration//GEN-END:variables

    /**
     * Implemented method. Hope this method may never be called.
     *
     * @param clipboard
     * @param contents 
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        NULogger.getLogger().log(Level.WARNING, "{0}: Lost clipboard ownership", getClass().getName());
    }

    private void deleteRowsAndReprintFile() {
        //get selected rows
        int selectedrow;
        int[] selectedrows = uploadTable.getSelectedRows();

        //Remove the rows from tablemodel from the end..
        for (int i = selectedrows.length - 1; i >= 0; i--) {
            selectedrow = selectedrows[i];
            NULogger.getLogger().log(Level.INFO, "Removing row: {0}", selectedrow);
            model.removeRow(selectedrow);
        }
        uploadTable.repaint();

        //Reprint the file.

        try {
            //Overwrite the file instead of appending..
            NULogger.getLogger().info("Reprinting recent.log");
            PrintWriter writer = new PrintWriter(new FileWriter(System.getProperty("user.home") + File.separator + "recent.log", false));
            for (int row = 0; row < model.getRowCount(); row++) {
                writer.write(model.getValueAt(row, FILE) + "<>" + model.getValueAt(row, HOST) + "<>" + model.getValueAt(row, DOWNLOADURL) + "<>" + model.getValueAt(row, DELETEURL) + "\n");
            }
            writer.close();
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "Error while writing recent.log\n{0}", ex);
        }
    }
}
