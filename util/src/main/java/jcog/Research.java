package jcog;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * suggests a research focus
 */
@Retention(RetentionPolicy.SOURCE)
@Research
public @interface Research {
    String title() default "";

    /** summary or abstract */
    String summary() default "";

    /** reference external URL or otherwise identifiable document (ex: DOI, ISBN etc) */
    String url() default "";
}