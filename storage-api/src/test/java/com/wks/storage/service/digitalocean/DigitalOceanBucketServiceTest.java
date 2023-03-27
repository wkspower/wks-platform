package com.wks.storage.service.digitalocean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.storage.config.StorageConfig;
import com.wks.storage.driver.MinioClientDelegate;

@ExtendWith(MockitoExtension.class)
public class DigitalOceanBucketServiceTest {
	
	@InjectMocks
	private DigitalOceanBucketService service;
	
	@Mock
	private MinioClientDelegate client;
	
	@Mock
	private SecurityContextTenantHolder holder;

	private StorageConfig configs;

	@BeforeEach
	public void setup() {
		configs = new StorageConfig();
		configs.setBucketPrefix("wks");
		service.setConfig(configs);
	}

	@Test
	public void shouldCreateBucketNameWithTentant() throws Exception {
		when(holder.getTenantId()).thenReturn(Optional.of("app"));
		
		String bucket = service.createAssignedTenant();
		
		assertEquals("wks-app", bucket);
		verify(client).makeBucket(notNull());
	}

}
