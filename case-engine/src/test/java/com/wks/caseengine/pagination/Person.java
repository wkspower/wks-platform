/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wks.caseengine.pagination;

import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Base entity for test.
 *
 * @author Davide Pedone
 */
public class Person {

	@Id
	String id;

	String name;

	Date birthday;

	int age;

	Long timestamp;

	String noGetterField;

	String exceptionGetterField;

	String nullField;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public void setNoGetterField(String noGetterField) {
		this.noGetterField = noGetterField;
	}

	public String getExceptionGetterField() {
		throw new NullPointerException();
	}

	public void setExceptionGetterField(String exceptionGetterField) {
		this.exceptionGetterField = exceptionGetterField;
	}

	public String getNullField() {
		return null;
	}

	public void setNullField(String nullField) {
		this.nullField = nullField;
	}

}
