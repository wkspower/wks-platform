package com.wks.caseengine.pagination.mongo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

import com.wks.caseengine.pagination.PaginationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PropertyUtils {

	public static Object getId(MongoPersistentEntity<?> persistentEntity, Object a) {
		MongoPersistentProperty prop = persistentEntity.getIdProperty();
		return getValue(prop, a);
	}

	public static Object getProperty(MongoPersistentEntity<?> persistentEntity, String key, Object a) {
		MongoPersistentProperty prop = persistentEntity.getPersistentProperty(key);
		return getValue(prop, a);
	}

	private static <T> Object getValue(MongoPersistentProperty key, T target) {
		if (key == null) {
			throw new PaginationException("PersistentProperty is null");
		}

		Method getter = key.getGetter();
		if (getter == null) {
			throw new PaginationException("No getter found for property " + key.getFieldName());
		}

		Object object;
		try {
			object = getter.invoke(target);
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error("Error invoking getter " + getter.getName() + " for property " + key.getFieldName(), e);
			throw new PaginationException(
					"Error invoking getter " + getter.getName() + " for property " + key.getFieldName());
		}

		if (object == null) {
			throw new PaginationException("Null value not allowed for property " + key.getFieldName());
		}

		return (object instanceof Date) ? ((Date) object).getTime() : object;
	}

}
