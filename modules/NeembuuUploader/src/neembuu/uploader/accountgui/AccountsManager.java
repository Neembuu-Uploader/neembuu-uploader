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
package neembuu.uploader.accountgui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import neembuu.uploader.NeembuuUploader;
import neembuu.uploader.external.PluginDestructionListener;
import neembuu.uploader.external.UpdatesAndExternalPluginManager;
import neembuu.uploader.external.UploaderPlugin;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.interfaces.Account;
import neembuu.uploader.interfaces.Uploader;
import neembuu.uploader.theme.ThemeCheck;
import neembuu.uploader.translation.ToHtmlMultiLine;
import neembuu.uploader.utils.NULogger;
import neembuu.uploader.utils.NeembuuUploaderProperties;

/**
 *
 * @author dsivaji
 */
public class AccountsManager extends javax.swing.JDialog {

    //Singleton instance
    private static AccountsManager INSTANCE = new AccountsManager(NeembuuUploader.getInstance(), true);
    //This is the list of accounts to be displayed in Table
    private static Map<String, Account> accounts;
    //Reference to table model.
    private static DefaultTableModel model;
    //Reference to Column index. Use this instead of explicitly using index no.
    public static final int HOSTNAME = 0;
    public static final int USERNAME = 1;
    public static final int PASSWORD = 2;
    //This renderer is used only for decent look. Without this, table looks ugly.
    UsernameRenderer usernamerenderer = new UsernameRenderer("");
    //This editor is used to display *** instead of plain text "while" typing password
    TableCellEditor passwordeditor = new DefaultCellEditor(new JPasswordField(""));
    //This renderer is used to display *** instead of plain text "after" typing password
    PasswordRenderer passwordrenderer = new PasswordRenderer("");
    //This renderer+editor is to allow a "Register" button along with host name to exist in the table
    HostNameRendererEditor hostNameRendererEditor = new HostNameRendererEditor();

    public static Account getAccount(final String hostname) {
        Account a = accounts.get(hostname);
        if(a!=null)return a;
        
        UploaderPlugin plugin = uaepm.load(uaepm.getIndex().get(Uploader.class, hostname));
        Class<? extends Account> accountClass =  plugin.getAccount(new PluginDestructionListener() {
            @Override public void destroyed() {
                accounts.remove(hostname);
            }
        });
        if(accountClass!=null){
            boolean dead = AccountsManager.INSTANCE.loadAccountClass(accountClass,hostname);
            if(dead)return null;
        }else {
            NULogger.getLogger().log(Level.INFO, "no account for {0}", hostname);
            return null;
        }
        
        return getAccount(hostname);
    }
    
    private static volatile UpdatesAndExternalPluginManager uaepm;
    public static void uaepm(UpdatesAndExternalPluginManager uaepm){
        if(AccountsManager.uaepm!=null)throw new IllegalStateException();
        AccountsManager.uaepm = uaepm;
    }
    

    /**
     * Use this to get instance of the AccountsManager. It also updates the
     * language before returning.
     *
     * @return the singleton instance.
     */
    public static AccountsManager getInstance() {
        updateLanguage();
        return INSTANCE;
    }

    /**
     * This method is used to login enabled accounts.. Use this at startup or
     * after the save button in accounts table is clicked.
     */
    public static void loginEnabledAccounts() {

        //Create a separate thread for responsiveness of the save button
        new Thread() {
            @Override
            public void run() {
                //Iterate through each account
                for (Account account : accounts.values()) {
                    loginAccount(account);
                }
            }
        }.start();
    }
    
    public static void loginAccount(Account account){
        //May need to add additional conditions if premium accts have different login mechanism
        //But that'll be in future..
        if (account.getUsername().isEmpty() || account.getPassword().isEmpty()) {
            //If either one field is empty, disable the account if logged in already.
            //In fact it's enough to check one condition 
            //as the AccountsManager won't let you save with one field empty
            account.disableLogin();
        } else {
            //If both fields are present, login that account
            NULogger.getLogger().log(Level.INFO, "Logging in to {0}", account.getHOSTNAME());
            if (account.canLogin()) {
                account.login();
            }
        }
    }

    /**
     * Private method to update the current language everytime the window is
     * about to be displayed
     */
    private static void updateLanguage() {
        NULogger.getLogger().log(Level.INFO, "{0}: Updating Language", AccountsManager.class);
        INSTANCE.setTitle(Translation.T().title());
        
        INSTANCE.infoLabel.setText(Translation.T().infoLabel());
        INSTANCE.infoLabel2.setText(
                ToHtmlMultiLine.splitToMultipleLines(
                    Translation.T().infoLabel2(),100));
        INSTANCE.infoLabel3.setText(
                ToHtmlMultiLine.splitToMultipleLines(
                    Translation.T().infoLabel3(),100));

        //This stupid code clears any editors or renderers...
        model.setColumnIdentifiers(new String[]{
            Translation.T().Hostname(),
            Translation.T().Username(),
            Translation.T().Password()
        });

        //... so have to set them again :'(
        INSTANCE.accountsTable.getColumnModel().getColumn(0).setCellEditor(INSTANCE.hostNameRendererEditor);
        INSTANCE.accountsTable.getColumnModel().getColumn(0).setCellRenderer(INSTANCE.hostNameRendererEditor);
        INSTANCE.accountsTable.getColumnModel().getColumn(1).setCellRenderer(INSTANCE.usernamerenderer);
        INSTANCE.accountsTable.getColumnModel().getColumn(2).setCellEditor(INSTANCE.passwordeditor);
        INSTANCE.accountsTable.getColumnModel().getColumn(2).setCellRenderer(INSTANCE.passwordrenderer);
        //Repaint the table.. // no.. wait.. no need to call repaint.
        //INSTANCE.accountsTable.repaint();

        INSTANCE.saveButton.setText(Translation.T().savebutton());

        //Pack the window as the font sizes may have changed.
        INSTANCE.pack();
    }

    /**
     * Creates new form AccountsManager
     */
    public AccountsManager(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        //First initialize the components.
        initComponents();

        //Assign reference to the table model for easy typing.
        model = (DefaultTableModel) accountsTable.getModel();

        //////////Dynamic add all accounts///////
        
        NULogger.getLogger().info("Adding accounts to accounts table()");
        accounts = new TreeMap<String, Account>();
        
    }
    
    public void initAccounts(){
        /*try{
            for (Class class1 : ClassUtils.getClasses("neembuu.uploader.accounts")) {
                Class<? extends Account> account = class1.asSubclass(Account.class);
                loadAccountClass();
            }
        } catch(ClassNotFoundException ex){
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        }*/
        
        
        //Then update the rows with values from the .nuproperties files.
        updateRows();

        //Pack the frame
        //    pack();
        pack();

        //Set the window relative to NU
        setLocationRelativeTo(NeembuuUploader.getInstance());
    }
    
    public void destroyAnythingFrom(UploaderPlugin up){
        accounts.remove(up.getSme().getName());
    }
    
    private boolean loadAccountClass(Class<? extends Account> account,String hostname){
        if(accounts.containsKey(hostname))return true;
        
        Constructor<? extends Account> constructor;
        try {
            constructor = account.getConstructor();
            Account instance = constructor.newInstance();
            if(!instance.isDead()){
                accounts.put(instance.getHOSTNAME(), instance);
                return true;
            }else {
                return false;
            }
        } catch(IllegalAccessException ex){
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            NULogger.getLogger().log(Level.SEVERE, null, ex);
        }
        
        initAccounts();
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        accountsManagerPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        accountsTable = new javax.swing.JTable();
        saveButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        infoLabel = new javax.swing.JLabel();
        infoLabel2 = new javax.swing.JLabel();
        infoLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Accounts Manager");
        setMinimumSize(new java.awt.Dimension(700, 450));

        accountsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Host name", "Username", "Password"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        accountsTable.setColumnSelectionAllowed(true);
        accountsTable.setRowHeight((int)usernamerenderer.getPreferredSize().getHeight());
        accountsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(accountsTable);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() | java.awt.Font.BOLD));
        infoLabel.setText("Enter your account details for the appropriate hosts..");

        infoLabel2.setText("If you don't have an account or if you want to disable an account or if a site has temporary login problems, leave both the fields blank and save..");

        infoLabel3.setFont(infoLabel3.getFont().deriveFont((infoLabel3.getFont().getStyle() | java.awt.Font.ITALIC), infoLabel3.getFont().getSize()-1));
        infoLabel3.setText("Get a free account from the appropriate sites if you don't have one so you can manage files on the cloud..");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(infoLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(infoLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(infoLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(infoLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(infoLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout accountsManagerPanelLayout = new javax.swing.GroupLayout(accountsManagerPanel);
        accountsManagerPanel.setLayout(accountsManagerPanelLayout);
        accountsManagerPanelLayout.setHorizontalGroup(
            accountsManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(accountsManagerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(accountsManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, accountsManagerPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveButton)
                        .addGap(56, 56, 56))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        accountsManagerPanelLayout.setVerticalGroup(
            accountsManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(accountsManagerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(saveButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(accountsManagerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(accountsManagerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(28, 28, 28))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        //The user may type into the field and while the cursor is still inside, 
        //he may click save button.
        //It will not set the value into that field and will cause error.
        //So when the user clicks save button, we have to stop that cellediting process
        //and set the value to the field.

        //Get the currently edited cell's celleditor
        TableCellEditor cellEditor = accountsTable.getCellEditor();

        //Call stopCellEditing() to stop the editing process
        //But if the selected cell is in first column which is non editable, then
        //calling stopCellEditing will throw nullpointer exception because there's
        //no editor there.. So check for null, before calling stopCellEditing().
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }

        //Iterate through each row..
        //int row = 0;
        for (Account account : accounts.values()) {
            //Declare local variables to store the username and password
            //If none present, empty "" is stored.
            
            String username = "", password = "";
            for (int i = 0; i < accountsTable.getModel().getRowCount(); i++) {
                if(accountsTable.getValueAt(i, HOSTNAME).toString().equals(account.getHOSTNAME())){
                    username = accountsTable.getValueAt(i, USERNAME).toString();
                    password = accountsTable.getValueAt(i, PASSWORD).toString();
                }
            }
            
            

            //The username and password field must be both filled or both empty
            //Only one field should not be filled.
            if (username.isEmpty() ^ password.isEmpty()) {
                NULogger.getLogger().info("The username and password field must be both filled or both empty");
                ThemeCheck.apply(null);
                JOptionPane.showMessageDialog(this,
                        account.getHOSTNAME() + " " + Translation.T().dialogerror(),
                        account.getHOSTNAME(),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            //Username and Password (encrypted) must be stored in the .nuproperties file in the user's home folder.
            NULogger.getLogger().info("Setting username and password(encrypted) to the .nuproperties file in user home folder.");
            NeembuuUploaderProperties.setProperty(account.getKeyUsername(), username);
            NeembuuUploaderProperties.setEncryptedProperty(account.getKeyPassword(), password);

           // row++;
        }

        //Separate thread to start the login process
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    loginEnabledAccounts();
                } catch (Exception ex) {
                    System.err.println("Exception while logging in.." + ex);
                    NULogger.getLogger().severe(ex.toString());
                }
            }
        });

        //Disposing the window
        NULogger.getLogger().info("Closing Accounts Manager..");
        
        try{
            if(isVisible())
                dispose();
        }catch(Exception a){
            System.err.println("Following error may be ignored");
            a.printStackTrace();
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    /**
     * Private method to update rows
     */
    private void updateRows() {
        model.setRowCount(0);
        
        //Iterate through each account
        for (Account account : accounts.values()) {
            //Get the values and update the rows.
            model.addRow(new Object[]{
                account.getHOSTNAME(),
                account.getUsername(),
                account.getPassword()
            });
        }
    }

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
            java.util.logging.Logger.getLogger(AccountsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AccountsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AccountsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AccountsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AccountsManager dialog = new AccountsManager(new javax.swing.JFrame(), true);
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

    @Override
    public void setVisible(boolean b) {
        ThemeCheck.apply(this);
        super.setVisible(b); //To change body of generated methods, choose Tools | Templates.
    }
    
    public JPanel getAccountsManagerPanel() {
        return accountsManagerPanel;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel accountsManagerPanel;
    private javax.swing.JTable accountsTable;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel infoLabel2;
    private javax.swing.JLabel infoLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
