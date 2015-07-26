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
package neembuu.uploader.cmd;

import neembuu.uploader.external.UpdateProgressUI;

/**
 * Commandline interface to show progress of plugins updating
 * @author Shashank Tulsyan <shashaank at neembuu.com>
 */
public class UpdateProgressCmdI implements UpdateProgressUI {

    @Override
    public Content addContent(String str) {
        return new C(str, 0);
    }

    public static final class C implements Content {
        private String str; double progress;

        public C(String str, double progress) {
            this.str = str;
            this.progress = progress;
        }
        
        
        
        @Override
        public void setString(String str2) {
            this.str = str2;
            
        }
        
        private void reshow(){
            System.out.println(str+ " "+progress+" %");
        }

        @Override
        public void setProgress(double progress) {
            progress = Math.max(0d, Math.min(1d, progress));
            this.progress = progress;
            if (progress == 1d) {
                done();
            }
        }

        @Override
        public void done() {
            
        }
    }

}
