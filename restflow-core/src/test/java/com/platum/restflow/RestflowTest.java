package com.platum.restflow;

import org.junit.Assert;
import org.junit.Test;

import com.platum.restflow.Restflow;

public class RestflowTest {
	
	@Test
	public void testLoadEnvironment() {
		Restflow r = new Restflow()
					.loadToEnvironment("classpath:config/restflow.properties");
		Assert.assertEquals("8080", r.getEnvironment().getProperty("restflow.http.port"));
	}
	
	@Test
	public void testLoadMessages() {
		Restflow r = new Restflow()
						.loadToEnvironment("classpath:conf/restflow.properties")
						.loadMessages();
		Assert.assertEquals("Forbidden resource operation."
				, r.getContext()
					.configMessageProvider("i18n/messages")
					.getMessageProvider()
					.getMessage("restflow.exceptions.forbidden"));
	}
	
	@Test
	public void testLoadModels() {
		Restflow r = new Restflow()
							.loadModels();
		Assert.assertNotNull(r.getResource("resource_test", "v1"));
	}
	
	@Test
	public void testInitVertx() {
		
	}
	
	
	@Test
	public void testResourceRoute() {
		
	}
	
	@Test
	public void testConfig() {
		
	}
	
	@Test
	public void testRun() {
		
	}
	
	
	@Test
	public void testGetRepositoryInstance() {
		
	}
	
	@Test
	public void testGetResource() {
	
	}
	
	@Test
	public void testCreateResource() {

		
	}
	
	@Test
	public void testreateResource() {
		
	}
}
