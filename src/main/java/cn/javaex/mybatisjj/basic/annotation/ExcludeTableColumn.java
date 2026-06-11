package cn.javaex.mybatisjj.basic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 排除表字段
 * 实体类中所有属性默认都会被当做数据库字段，
 * 该@ExcludeTableColumn注解用于排除指定属性，使其不被当做数据库字段
 * 
 * @author 陈霓清
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcludeTableColumn {
	
	/**
	 * 排除表字段
	 */
	String value() default "";
	
}
