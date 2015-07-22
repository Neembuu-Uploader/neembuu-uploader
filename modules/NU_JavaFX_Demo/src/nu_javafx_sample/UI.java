/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nu_javafx_sample;

/**
 *
 * @author Shashank
 */
public interface UI {
    void updateProgress(double d);
    void updateStatus(String status);
    void setDownloadLink(String link);
    void setDeleteLink(String link);
}
