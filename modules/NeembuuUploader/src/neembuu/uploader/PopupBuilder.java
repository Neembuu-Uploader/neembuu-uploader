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

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import neembuu.uploader.interfaces.UploadStatus;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.interfaces.abstractimpl.AbstractUploader;
import neembuu.uploader.theme.ThemeCheck;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.utils.UploadStatusUtils;

/**
 * This class is used by NeembuuUploader class for displaying Popup Menu
 * Don't mess with this class. (package private)
 * @author vigneshwaran
 */
class PopupBuilder implements ClipboardOwner {

    //Singleton instance
    private static final PopupBuilder INSTANCE = new PopupBuilder();
    //Member variables
    private Clipboard clipboard = null;
    private JPopupMenu popup = null;
    private JMenuItem copyDownloadURL = null;
    private JMenuItem copyDeleteURL = null;
    private JMenuItem exportLinksURL = null;
    private JMenuItem gotoDownloadURL = null;
    private JMenuItem removeFromQueue = null;
    private JMenuItem removeFinished = null;
    private JMenuItem stopUpload = null;
    private JMenuItem retryUpload = null;
    private JTable table = null;
    private int[] selectedrows = null;
    private boolean multiple = false;

    //Create one Popup object and reuse it everytime instead of creating new objects everytime
    private PopupBuilder() {
        NULogger.getLogger().info("Initializing PopupBuilder");

        //Get reference to main table for easy typing
        table = NeembuuUploader.getInstance().getTable();

        //Get a reference to System Clipboard
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        //Initialize popup menu
        popup = new JPopupMenu();

        //Initialize menuitem with only icons.. Add Locale specific text later whenever getInstance is called
        //Add its exclusive action listener
        //Add this menu item to the popupmenu
        copyDownloadURL = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/copydownloadurl.png")));
        copyDownloadURL.addActionListener(new CopyDownloadURLActionListener());
        popup.add(copyDownloadURL);

        copyDeleteURL = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/copydeleteurl.png")));
        copyDeleteURL.addActionListener(new CopyDeleteURLActionListener());
        popup.add(copyDeleteURL);

        exportLinksURL = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/exporturl.png")));
        exportLinksURL.addActionListener(new ExportLinksActionListener());
        popup.add(exportLinksURL);

        gotoDownloadURL = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/gotodownloadurl.png")));
        gotoDownloadURL.addActionListener(new GotoDownloadURLActionListener());
        popup.add(gotoDownloadURL);



        //Add a separator to differentiate menu items
        popup.add(new JSeparator());


        removeFromQueue = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/removefromlist.png")));
        removeFromQueue.addActionListener(new RemoveFromQueueActionListener());
        popup.add(removeFromQueue);

        removeFinished = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/removefinished.png")));
        removeFinished.addActionListener(new RemoveFinishedActionListener());
        popup.add(removeFinished);

        //Add a separator to differentiate menu items
        popup.add(new JSeparator());

        stopUpload = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/stopupload.png")));
        stopUpload.addActionListener(new StopUploadActionListener());
        popup.add(stopUpload);
        
        retryUpload = new JMenuItem(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/popup/retryupload.png")));
        retryUpload.addActionListener(new RetryUploadActionListener());
        popup.add(retryUpload);

    }

    /**
     * 
     * @return singleton instance of popupbuilder
     */
    public static PopupBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * Display popup menu at the specified parameters (component, x and y position)
     * @param invoker
     * @param x
     * @param y 
     */
    public void show(Component invoker, int x, int y) {
        ThemeCheck.apply(popup);
    
        selectedrows = table.getSelectedRows();
        if (table.getSelectedRowCount() > 1) {
            multiple = true;
        } else {
            multiple = false;
        }
        copyDownloadURL.setEnabled(canCopyDownloadURLEnable());
        copyDeleteURL.setEnabled(canCopyDeleteURLEnable());
        exportLinksURL.setEnabled(canExportLinksEnable());
        gotoDownloadURL.setEnabled(canGotoDownloadURLEnable());

        removeFromQueue.setEnabled(canRemoveFromQueueEnable());
        removeFinished.setEnabled(canRemoveFinishedEnable());

        stopUpload.setEnabled(canStopUploadEnable());
        retryUpload.setEnabled(canRetryUploadEnable());


        popup.show(invoker, x, y);
        NULogger.getLogger().info("Popup Menu displayed");
    }

    /**
     * Implemented method. Hope this method may never be called.
     * @param clipboard
     * @param contents 
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        NULogger.getLogger().log(Level.WARNING, "{0}: Lost clipboard ownersh ip", getClass().getName());
    }

    /**
     * 
     * @return whether the menuitem can be enabled or not depending on the status of selected rows
     */
    private boolean canCopyDownloadURLEnable() {
        //Popup shows only if there is one or more row selected so this is a safe code

        //Show only for finished rows
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.UPLOADFINISHED)) {
                i++;
            }
        }

        //Setting default text whether return value will be true or not
        copyDownloadURL.setText(Translation.T().copyDownloadURL());
        //Atleast one or more selected rows must be valid for the above condition
        if (i >= 1) {
            //If multiple rows selected and few or only one of them satisfies the condition, append (n) at the end even if it is 1.
            if (multiple) {
                copyDownloadURL.setText(Translation.T().copyDownloadURL() + " (" + i + ")");
            }
            return true;
        }

        return false;
    }

    private boolean canCopyDeleteURLEnable() {
        //Popup shows only if there is one or more row selected so this is a safe code
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.UPLOADFINISHED)
                    && table.getValueAt(selectedrow, NUTableModel.DELETEURL) != UploadStatus.NA) {
                i++;
            }
        }

        copyDeleteURL.setText(Translation.T().copyDeleteURL());
        if (i >= 1) {
            if (multiple) {
                copyDeleteURL.setText(Translation.T().copyDeleteURL() + " (" + i + ")");
            }
            return true;
        }

        return false;
    }

    private boolean canExportLinksEnable() {
        //Popup shows only if there is one or more row selected so no need to check that here.
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.UPLOADFINISHED)) {
                i++;
            }
        }

        exportLinksURL.setText(Translation.T().exportLinks());
        if (i >= 1) {
            if (multiple) {
                exportLinksURL.setText(Translation.T().exportLinks() + " (" + i + ")");
            }
            return true;
        }

        return false;
    }

    private boolean canGotoDownloadURLEnable() {
        //Popup shows only if there is one or more row selected so no need to check that here.
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.UPLOADFINISHED)) {
                i++;
            }
        }

        gotoDownloadURL.setText(Translation.T().gotoDownloadURL());
        if (i >= 1) {
            if (multiple) {
                gotoDownloadURL.setText(Translation.T().gotoDownloadURL() + " (" + i + ")");
            }
            return true;
        }

        return false;
    }

    private boolean canRemoveFromQueueEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.QUEUED, 
                    UploadStatus.UPLOADFINISHED, UploadStatus.UPLOADFAILED, 
                    UploadStatus.UPLOADSTOPPED, UploadStatus.UPLOADINVALID,
                    UploadStatus.RETRYFAILED)) {
                i++;
            }
        }


        removeFromQueue.setText(Translation.T().removeFromQueue());
        if (i >= 1) {
            if (multiple) {
                removeFromQueue.setText(Translation.T().removeFromQueue() + " (" + i + ")");
            }
            return true;
        }

        return false;
    }

    
    private boolean canRemoveFinishedEnable() {
        //Check all rows instead of selection
        int finishedrows = 0;
        synchronized(NUTableModel.uploadList){
            for (int i = 0; i < NUTableModel.uploadList.size(); i++) {
                if (UploadStatusUtils.isRowStatusOneOf(i, UploadStatus.UPLOADFINISHED)) {
                    finishedrows++;
                }
            }
        }

        removeFinished.setText(Translation.T().removeFinished());
        if (finishedrows > 0) {
            removeFinished.setText(Translation.T().removeFinished() + " (" + finishedrows + ")");
            return true;
        }

        return false;
    }

    private boolean canStopUploadEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.INITIALISING, 
                    UploadStatus.UPLOADING,
                    UploadStatus.REUPLOADING)) {
                i++;
            }
        }


        stopUpload.setText(Translation.T().stopUpload());
        if (i >= 1) {
            if (multiple) {
                stopUpload.setText(Translation.T().stopUpload() + " (" + i + ")");
            }
            return true;
        }

        return false;
    }
    
    private boolean canRetryUploadEnable() {
        int i = 0;
        for (int selectedrow : selectedrows) {
            if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.UPLOADFAILED, 
                    UploadStatus.UPLOADSTOPPED,
                    UploadStatus.RETRYFAILED,
                    UploadStatus.UPLOADFINISHED)) {
                i++;
            }
        }


        retryUpload.setText(Translation.T().retryUpload());
        if (i >= 1) {
            if (multiple) {
                retryUpload.setText(Translation.T().retryUpload() + " (" + i + ")");
            }
            return true;
        }

        return false;
    }

    /**
     * Action listener for CopyDownloadURL menu item
     */
    class CopyDownloadURLActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //Collect the set of urls and set the text in Clipboard
            NULogger.getLogger().info("Copy Download URL clicked");
            StringBuilder listofurls = new StringBuilder();
            for (int selectedrow : selectedrows) {
                listofurls.append(table.getValueAt(selectedrow, NUTableModel.DOWNLOADURL).toString()).append("\n");
            }
            setClipboardContent(listofurls.toString());
        }
    }

    class CopyDeleteURLActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //Collect the set of urls and set the text in Clipboard
            NULogger.getLogger().info("Copy Delete URL clicked");
            StringBuilder listofurls = new StringBuilder();
            for (int selectedrow : selectedrows) {
                listofurls.append(table.getValueAt(selectedrow, NUTableModel.DELETEURL).toString()).append("\n");
            }
            setClipboardContent(listofurls.toString());
        }
    }

    class ExportLinksActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //No need to check if rows are selected -- popup shown only if true
            //No need to check if atleast one row is finished -- menu item enabled only if true
            NULogger.getLogger().info("Export links clicked");
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setDialogTitle(Translation.T().exportLinkDialog());
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileFilter(new FileNameExtensionFilter("HTML File", new String[]{"html"}));
            if (fc.showSaveDialog(NeembuuUploader.getInstance()) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File htmlfile = fc.getSelectedFile();

            //Get the starting row to ending row
            int startingrow = table.getSelectedRow();
            int endingrow = startingrow + table.getSelectedRowCount() - 1;

            //This is a very big stringbuilder.. It contains text for html and css.. Rows will be appended programmatically.
            //Unfortunately I can do this only in English
            StringBuilder sb = new StringBuilder("<html>"
                    + "<head><title>Neembuu Uploader</title>"
                    + "<style type='text/css'>"
                    + " table { display:block; width:90%; border-top:1px solid #e5eff8; border-right:1px solid #e5eff8; margin:1em auto;border-collapse:collapse;}"
                    + " td { color:#7892a8;border-bottom:1px solid #e5eff8;border-left:1px solid #e5eff8;padding:.3em 1em;text-align:center;}"
                    + " tr.odd td { background:#f7fbff } tr.odd .column1 {background:#f4f9fe;} .column1 {background:#f9fcfe;}"
                    + " th, h3 { background:#f4f9fe; text-align:center; font:bold 1.2em/2em 'Century Gothic','Trebuchet MS',Arial,Helvetica,sans-serif; color:#66a3d3; }"
                    + "</style></head>"
                    + "<body><center><h3>Neembuu Uploader Exported Linkset</h3></center>"
                    + "<table><tr class='odd'><th class='column1'>File</th><th>Size</th><th>Host</th><th>Download URL</th><th>Delete URL if any</th></tr>");

            int i = 1;
            for (int row = startingrow; row <= endingrow; row++) {
                //ignore rows that are not "Upload Finished"
                if (!UploadStatusUtils.isRowStatusOneOf(row, UploadStatus.UPLOADFINISHED)) {
                    continue;
                }

                if (i % 2 == 0) {
                    sb.append("<tr class='odd'>");
                } else {
                    sb.append("<tr>");
                }

                for (int column = 0; column < table.getModel().getColumnCount(); column++) {
                    if (column == NUTableModel.STATUS || column == NUTableModel.PROGRESS) {
                        continue;
                    }

                    if (column == 0) {
                        sb.append("<td class='column1'>");
                    } else {
                        sb.append("<td>");
                    }
                    if (column == NUTableModel.DOWNLOADURL
                            && !(table.getModel().getValueAt(row, column).equals(UploadStatus.NA.getDefaultLocaleSpecificString())
                            || table.getModel().getValueAt(row, column).equals(UploadStatus.NA.getLocaleSpecificString()))) {
                        sb.append("<a target='_blank' href='").append(table.getModel().getValueAt(row, column)).append("'>").append(table.getModel().getValueAt(row, column)).append("</a>");
                    } else {
                        sb.append(table.getModel().getValueAt(row, column));
                    }

                    sb.append("</td>");
                }

                sb.append("</tr>");
                i++;
            }
            sb.append("</table></body></html>");
            try {
                NULogger.getLogger().log(Level.INFO, "{0}: Writing links to html file..", getClass().getName());
                //Add .html to the filename if the file doesn't already have .html or .htm extension
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
        }
    }

    class GotoDownloadURLActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            NULogger.getLogger().info("Goto Download URL clicked..");
            for (int selectedrow : selectedrows) {
                String url =
                        table.getValueAt(selectedrow, NUTableModel.DOWNLOADURL).toString();
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
        }
    }

    class RemoveFromQueueActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //Run this in a separate thread for responsiveness of GUI
            //Otherwise the popupmenu won't disappear until action completed. bad for slow pc's.
            SwingUtilities.invokeLater(new Runnable(){
            //new Thread() {

                @Override
                public void run() {
                    NULogger.getLogger().info("Remove from Queue menu item clicked");

                    //Must lock queue
                    QueueManager.getInstance().setQueueLock(true);

                    int selectedrow;
                    //Remove from the end
                    for (int i = selectedrows.length - 1; i >= 0; i--) {
                        selectedrow = selectedrows[i];
                        if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.QUEUED,
                                UploadStatus.UPLOADFINISHED, UploadStatus.UPLOADFAILED,
                                UploadStatus.UPLOADSTOPPED, UploadStatus.UPLOADINVALID,
                                UploadStatus.RETRYFAILED)) {

                            NUTableModel.getInstance().removeUpload(selectedrow);
                            NULogger.getLogger().log(Level.INFO, "{0}: Removed row no. {1}", new Object[]{getClass().getName(), selectedrow});
                        }

                    }


                    //Unlock Queue back
                    QueueManager.getInstance().setQueueLock(false);
                }
            //}.start();
            });
        }
    }

    class RemoveFinishedActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
            //new Thread() {

                @Override
                public void run() {
                    NULogger.getLogger().info("Remove Finished menu item clicked..");

                    //Must lock queue
                    QueueManager.getInstance().setQueueLock(true);

                    //Remove from end

                    for (int i = NUTableModel.uploadList.size() - 1; i >= 0; i--) {
                        if (UploadStatusUtils.isRowStatusOneOf(i, UploadStatus.UPLOADFINISHED)) {
                            NUTableModel.getInstance().removeUpload(i);
                            NULogger.getLogger().log(Level.INFO, "{0}: Removed row no. {1}", new Object[]{getClass().getName(), i});
                        }
                    }



                    //Unlock Queue back
                    QueueManager.getInstance().setQueueLock(false);
                }
            //}.start();
            });
        }
    }

    class StopUploadActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //Without thread, popup will not return
            SwingUtilities.invokeLater(new Runnable() {
            //new Thread() {

                @Override
                public void run() {
                    NULogger.getLogger().info("Stop Upload clicked..");
                    for (int selectedrow : selectedrows) {
                        if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.INITIALISING, UploadStatus.UPLOADING, UploadStatus.REUPLOADING)) {
                            NUTableModel.uploadList.get(selectedrow).stopUpload();
                            NULogger.getLogger().log(Level.INFO, "Stopped upload : {0}", selectedrow);
                        }
                    }
                }
            //}.start();
            });
        }
    }
    
    class RetryUploadActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //Without thread, popup will not return
            new Thread() {

                @Override
                public void run() {
                    NULogger.getLogger().info("Retry Upload clicked..");
                    for (int selectedrow : selectedrows) {
                        if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.UPLOADFAILED, UploadStatus.UPLOADSTOPPED, UploadStatus.RETRYFAILED, UploadStatus.UPLOADFINISHED)) {
                            try {
                                NULogger.getLogger().log(Level.INFO, "Retrying upload : {0}", selectedrow);
                                Uploader uploader = NUTableModel.uploadList.get(selectedrow);
                                File file = uploader.getFile();
                                Constructor<? extends Uploader> uploaderConstructor = uploader.getClass().getConstructor();
                                AbstractUploader retryingUploader = (AbstractUploader) uploaderConstructor.newInstance();
                                retryingUploader.setFile(file);
                                retryingUploader.setRetry(true);
                                NUTableModel.uploadList.set(selectedrow, retryingUploader);
                                QueueManager.getInstance().startNextUploadIfAny();
                            } catch (Exception ex) {
                                NULogger.getLogger().log(Level.INFO, "Retry failed for : {0}", selectedrow);
                            }
                            
                        }
                    }
                }
            }.start();

        }
    }

    /**
     * Place a String on the clipboard, and make this class the
     * owner of the clipboard contents.
     */
    public void setClipboardContent(String aString) {
        StringSelection stringSelection = new StringSelection(aString);
        clipboard.setContents(stringSelection, this);
        NULogger.getLogger().info("Copied to clipboard");
    }
}
