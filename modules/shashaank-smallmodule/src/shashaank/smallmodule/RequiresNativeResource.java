/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shashaank.smallmodule;

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
public @interface RequiresNativeResource {
    String name(); 
    String win() default "";
    String lin() default "";
    String mac() default "";
}
