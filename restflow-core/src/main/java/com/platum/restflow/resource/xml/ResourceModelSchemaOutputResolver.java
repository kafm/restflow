package com.platum.restflow.resource.xml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.platum.restflow.RestflowModel;



public class ResourceModelSchemaOutputResolver extends SchemaOutputResolver {

	private File file;
	
	private OutputStream out;
	
	public ResourceModelSchemaOutputResolver(OutputStream out) {
		this.out = out;
	}
	
	public ResourceModelSchemaOutputResolver(File file) throws MalformedURLException {
		this.file = file;

	}
	
    public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {

        StreamResult result = null;
        String id = null;
        if(file != null) {
        	result = new StreamResult(file);;
        	id = file.toURI().toURL().toString();     	
        } else  {
        	result = new StreamResult(out);
        	id = UUID.randomUUID().toString();
        }
        result.setSystemId(id);
        return result;
    }
    
    public static void main(String[] args) throws Throwable {
		JAXBContext jaxbContext = JAXBContext.newInstance(RestflowModel.class);
		if(args == null || args.length == 0) {
			 jaxbContext.generateSchema(new ResourceModelSchemaOutputResolver(System.out));
		} else {
			File file = new File (args[0]);
			if(!file.exists()) {
				file.createNewFile();
			}
			jaxbContext.generateSchema(new ResourceModelSchemaOutputResolver(file));	
		}
    }
}
