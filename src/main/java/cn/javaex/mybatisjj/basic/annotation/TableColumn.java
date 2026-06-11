package cn.javaex.mybatisjj.basic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表字段（主键以外的列）
 * 
 * @author 陈霓清
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableColumn {
	
	/**
	 * 数据库字段
	 */
	String value() default "";
	
}
