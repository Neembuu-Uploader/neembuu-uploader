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
package neembuu.uploader.utils;

import java.util.Comparator;
import javax.swing.JCheckBox;

/**
 * A custom comparator to use with checkboxes.
 * @author davidepastore
 */
public class JCheckBoxComparator implements Comparator<JCheckBox>{

    /**
     * Compare the two checkboxes.
     * @param jCheckBox1 The first checkbox.
     * @param jCheckBox2 The second checkbox.
     * @return Returns the compare between two checkboxes.
     */
    @Override
    public int compare(JCheckBox jCheckBox1, JCheckBox jCheckBox2) {
        return jCheckBox1.getText().compareTo(jCheckBox2.getText());
    }

    
}
