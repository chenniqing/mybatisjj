package cn.javaex.mybatisjj.basic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表名
 * 
 * @author 陈霓清
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
	
	/**
	 * 默认值，为空时取属性名称
	 */
	String value() default "";
	
}
