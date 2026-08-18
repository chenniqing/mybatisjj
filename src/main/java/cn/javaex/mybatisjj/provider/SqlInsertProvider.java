package cn.javaex.mybatisjj.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

import cn.javaex.mybatisjj.model.entity.TableColumnEntity;
import cn.javaex.mybatisjj.model.entity.TableEntity;
import cn.javaex.mybatisjj.util.ReflectiveUtils;

/**
 * Insert构建
 * 
 * @author 陈霓清
 */
public class SqlInsertProvider extends EntityProvider implements ProviderMethodResolver {

	/**
	 * 插入实体信息
	 * @param providerContext
	 * @param entity
	 * @return
	 */
	public String insert(ProviderContext providerContext, Object entity) {
		Class<?> clazz = super.getEntityType(providerContext.getMapperType());
		
		// 获取表实体信息
		TableEntity tableEntity = super.getTableEntity(clazz);
		// 获取表的所有字段
		List<TableColumnEntity> tableColumnEntityList = tableEntity.getTableColumnEntityList();
		
		return new SQL() {{
			INSERT_INTO(tableEntity.getTableName());
			for (TableColumnEntity tableColumnEntity : tableColumnEntityList) {
				String column = tableColumnEntity.getColumn();
				String field = tableColumnEntity.getField();
				try {
					// 使用反射从实体对象中获取字段值
					Field entityField = ReflectiveUtils.findField(clazz, field);
					entityField.setAccessible(true);
					Object fieldValue = entityField.get(entity);
					
					// 如果字段值不为 null，则添加到插入语句
					if (fieldValue != null) {
						VALUES(column, "#{" + field + "}");
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}}.toString();
	}
	
	/**
	 * 批量插入实体信息
	 * @param providerContext
	 * @param list
	 * @return
	 */
	public String insertBatch(ProviderContext providerContext, @Param("list") List<?> list) {
		// 不能进行空或空列表的插入操作
		if (list == null || list.isEmpty()) {
			throw new IllegalArgumentException("Parameter 'list' must not be empty.");
		}

		Object firstEntity = list.get(0);
		if (firstEntity == null) {
			throw new IllegalArgumentException("Entity in insert batch must not be null.");
		}
		Class<?> clazz = firstEntity.getClass();
		for (Object entity : list) {
			if (entity == null) {
				throw new IllegalArgumentException("Entity in insert batch must not be null.");
			}
			if (!clazz.equals(entity.getClass())) {
				throw new IllegalArgumentException("All entities in the same insert batch must have the same type.");
			}
		}
		
		// 获取表实体信息
		TableEntity tableEntity = super.getTableEntity(clazz);
		// 获取表的所有字段
		List<TableColumnEntity> tableColumnEntityList = tableEntity.getTableColumnEntityList();
		
		// 仅保留当前批次至少有一条数据不为空的列，整批为空的列交给数据库默认值处理
		List<String> columnNames = new ArrayList<>(tableColumnEntityList.size());
		List<TableColumnEntity> insertColumnEntityList = new ArrayList<>(tableColumnEntityList.size());
		List<Field> insertFieldList = new ArrayList<>(tableColumnEntityList.size());
		for (TableColumnEntity tableColumnEntity : tableColumnEntityList) {
			String fieldName = tableColumnEntity.getField();
			try {
				Field entityField = ReflectiveUtils.findField(clazz, fieldName);
				entityField.setAccessible(true);
				boolean hasNonNullValue = false;
				for (Object entity : list) {
					if (entityField.get(entity) != null) {
						hasNonNullValue = true;
						break;
					}
				}
				if (!hasNonNullValue) {
					continue;
				}

				columnNames.add(tableColumnEntity.getColumn());
				insertColumnEntityList.add(tableColumnEntity);
				insertFieldList.add(entityField);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new IllegalStateException("Error processing field: " + fieldName, e);
			}
		}
		if (columnNames.isEmpty()) {
			throw new IllegalArgumentException("At least one field must have a non-null value for batch insert.");
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("INSERT INTO ").append(tableEntity.getTableName()).append(" ");
		sqlBuilder.append("(").append(String.join(", ", columnNames)).append(") VALUES ");
		
		// 构造每条记录的值
		List<String> valueRows = new ArrayList<>(list.size());
		for (int i = 0; i < list.size(); i++) {
			Object entity = list.get(i);
			List<String> valuePlaceholders = new ArrayList<>(insertColumnEntityList.size());
			for (int j = 0; j < insertColumnEntityList.size(); j++) {
				TableColumnEntity tableColumnEntity = insertColumnEntityList.get(j);
				String field = tableColumnEntity.getField();
				try {
					Object fieldValue = insertFieldList.get(j).get(entity);
					// 当前行字段为空时显式使用数据库默认值，保证所有行共用同一列集合
					if (fieldValue == null) {
						valuePlaceholders.add("DEFAULT");
					} else {
						valuePlaceholders.add("#{list[" + i + "]." + field + "}");
					}
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Error reading field: " + field, e);
				}
			}
			valueRows.add("(" + String.join(", ", valuePlaceholders) + ")");
		}
		
		sqlBuilder.append(String.join(", ", valueRows));
		return sqlBuilder.toString();
	}

}
