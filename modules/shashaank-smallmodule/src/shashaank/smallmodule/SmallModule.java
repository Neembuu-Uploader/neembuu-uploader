/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shashaank.smallmodule;

import java.lang.annotation.Annotation;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Shashank
 */
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SmallModule {
    Class[]dependsOn() default {};
    Class[]exports();
    Class[]interfaces();
    String name();
    boolean dead() default false;
    boolean ignore() default false;
    String[]jarsRequired() default {};
    String description() default "";
    Class<? extends Annotation>[] metadataAnnotations() default {};
}