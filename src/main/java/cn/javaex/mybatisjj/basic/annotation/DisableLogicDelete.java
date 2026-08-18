package cn.javaex.mybatisjj.basic.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 禁用当前实体的逻辑删除能力
 *
 * 当实体继承的父类字段带有 {@link TableLogic} 时，可在实体类上添加本注解，
 * 使通用查询、更新和删除 SQL 忽略该逻辑删除字段，其中删除方法将生成物理
 * {@code DELETE} 语句，而不是更新逻辑删除标记
 *
 * 如果实体的整个继承链中本来就没有 {@link TableLogic} 字段，本注解仅表示
 * 显式关闭逻辑删除，解析结果仍为空，不会产生冲突或异常
 *
 * @author 陈霓清
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisableLogicDelete {

}
