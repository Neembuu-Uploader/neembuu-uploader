/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shashaank.smallmodule;

import java.lang.annotation.Annotation;

/**
 *
 * @author Shashank
 */
public interface MetadataAnnotationHandler {
    Class<? extends Annotation> handlesType();
    void handle(Class<? extends Annotation> annotation);
}
