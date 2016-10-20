package com.platum.restflow.components;

import com.platum.restflow.Restflow;

import io.vertx.ext.web.Router;

public interface Controller {
	
	void setRestflow(Restflow restflow);
	
	void setRouter(Router router);
	
	void createRoutes();
	
}
