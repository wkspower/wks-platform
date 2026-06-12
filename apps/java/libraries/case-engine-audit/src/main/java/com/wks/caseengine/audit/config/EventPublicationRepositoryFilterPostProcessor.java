/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.audit.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Filter out duplicate EventPublicationRepository bean definitions registered by
 * JpaEventPublicationAutoConfiguration and MongoDbEventPublicationAutoConfiguration
 * based on the active WKS database type (database.type).
 */
@Component
public class EventPublicationRepositoryFilterPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

	private Environment environment;

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		String dbType = environment.getProperty("database.type");
		if (dbType == null) {
			dbType = "mongo"; // Default database type in WKS
		}

		boolean isJpa = "jpa".equalsIgnoreCase(dbType)
				|| "postgres".equalsIgnoreCase(dbType)
				|| "h2".equalsIgnoreCase(dbType);

		if (isJpa) {
			if (registry.containsBeanDefinition("mongoDbEventPublicationRepository")) {
				registry.removeBeanDefinition("mongoDbEventPublicationRepository");
			}
		} else {
			if (registry.containsBeanDefinition("jpaEventPublicationRepository")) {
				registry.removeBeanDefinition("jpaEventPublicationRepository");
			}
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// No-op
	}
}
