package dk.dma.msinm.common.settings.annotation;

import dk.dma.msinm.common.settings.Source;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Annotation used to inject settings
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface Setting {
    @Nonbinding String value() default "";
    @Nonbinding String defaultValue() default "";
    @Nonbinding Source source() default Source.DATABASE;
}
