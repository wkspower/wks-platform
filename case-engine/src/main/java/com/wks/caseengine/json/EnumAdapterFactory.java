package com.wks.caseengine.json;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class EnumAdapterFactory implements TypeAdapterFactory {

	@Override
	public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
		Class<? super T> rawType = type.getRawType();
		if (rawType.isEnum()) {
			return new EnumTypeAdapter<T>();
		}
		return null;
	}

	public class EnumTypeAdapter<T> extends TypeAdapter<T> {
		@Override
		public void write(JsonWriter out, T value) throws IOException {
			if (value == null || !value.getClass().isEnum()) {
				out.nullValue();
				return;
			}

			try {
				out.beginObject();
				out.name("value");
				out.value(value.toString());
				Arrays.stream(Introspector.getBeanInfo(value.getClass()).getPropertyDescriptors())
						.filter(pd -> pd.getReadMethod() != null && !"class".equals(pd.getName())
								&& !"declaringClass".equals(pd.getName()))
						.forEach(pd -> {
							try {
								out.name(pd.getName());
								out.value(String.valueOf(pd.getReadMethod().invoke(value)));
							} catch (IllegalAccessException | InvocationTargetException | IOException e) {
								e.printStackTrace();
							}
						});
				out.endObject();
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}
		}

		public T read(JsonReader in) throws IOException {
			// Properly deserialize the input
			return null;
		}
	}
}
