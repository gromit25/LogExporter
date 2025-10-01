pacakge com.redeye.logexporter.workflow;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.TYPE)
public @interface ComponentConfig {
  String from() default "";
  String subscribe() default "";
  int threadCount() default 1;
}
