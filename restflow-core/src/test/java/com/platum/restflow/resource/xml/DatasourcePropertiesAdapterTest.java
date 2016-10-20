package com.platum.restflow.resource.xml;

import java.util.Arrays;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Assert;
import org.junit.Test;

import com.platum.restflow.DatasourceDetails;
import com.platum.restflow.RestflowModel;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.impl.jdbc.JdbcRepository;
import com.platum.restflow.resource.property.ResourceProperty;
import com.platum.restflow.resource.property.ResourcePropertyType;
import com.platum.restflow.utils.ResourceUtils;



public class DatasourcePropertiesAdapterTest {

	@Test
	public void testMarshaller()  {
		RestflowModel model = new RestflowModel();
		Properties properties = new Properties();
		properties.put("jdbcUrl", "jdbc:hsqldb:mem:test?shutdown=true");
		properties.put("dataSourceClassName", "org.hsqldb.jdbcDriver");
		model.setDatasources(Arrays.asList(new DatasourceDetails()
												.setImplClass(JdbcRepository.class.getName())
												.setName("hsqldb")
												.setProperties(properties)));
		model.setResources(Arrays.asList(new Resource()
												.setName("test_resource")
												.setIdProperty("x")
												.setDatasource("hsqldb")
												.setResourceClass("a")
												.setTable("xx")
												.setProperties(Arrays.asList(
														new ResourceProperty()
														.setName("p1")
														.setType(ResourcePropertyType.STRING)
														))
												.setMethods(Arrays.asList(new ResourceMethod()
														.setName("test")
														.setUrl("POST /:id")
														.setQuery("select * from xpto")))));
		Throwable exception = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(RestflowModel.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(model, System.out);	
		} catch(Throwable e) {
			exception = e;
		}
		Assert.assertNull(exception);
	}
	
	@Test
	public void testUnmarshaller() throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(RestflowModel.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		RestflowModel model = (RestflowModel) jaxbUnmarshaller.unmarshal(ResourceUtils.getURL("classpath:models/test.xml").openStream());
		Assert.assertEquals(1, model.getResources().size());
	}
	
}
