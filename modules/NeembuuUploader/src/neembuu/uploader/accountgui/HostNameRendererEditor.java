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

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.EventObject;
import java.util.logging.Level;
import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import neembuu.uploader.theme.ThemeCheck;
import neembuu.uploader.translation.Translation;
import neembuu.uploader.utils.NULogger;

/**
 * This renderer is used only for textfield look instead of default label look.
 *
 * @author shashanktulsyan
 */
public class HostNameRendererEditor extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor {

    private HostNameCellPanel renderer = new HostNameCellPanel();
    private HostNameCellPanel editor = new HostNameCellPanel();
    
    public HostNameRendererEditor() {

    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        renderer.setHostNameValue((String) value);
        return renderer;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editor.setHostNameValue((String) value);
        return editor;
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getHostNameValue();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    private static final class HostNameCellPanel extends JPanel {

        private final JLabel hostName = new JLabel();
        private final JButton registerButton = new JButton(
                Translation.T().registerButton());

        public HostNameCellPanel() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            
            registerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        
                        String url = NewAccountURLUtil.createNewAccountRegistrationURL(hostName.getText());
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        NULogger.getLogger().log(Level.SEVERE, null, ex);
                        ThemeCheck.apply(null);
                        JOptionPane.showMessageDialog(null, "Could not open registration page", hostName.getText(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            add(hostName);
            add(Box.createHorizontalStrut(10));
            add(registerButton);
        }
        
        String getHostNameValue(){
            return hostName.getText();
        }
        
        void setHostNameValue(String hostNameText){
            hostName.setText(hostNameText);
        }

    }

}
