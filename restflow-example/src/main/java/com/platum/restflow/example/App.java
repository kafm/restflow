package com.platum.restflow.example;

import com.platum.restflow.RestflowLauncher;

/**
 * Restflow App
 */
public class App  {
	public final static String PRODUCT_TABLE = "CREATE TABLE product ("+ 
		" id INT PRIMARY KEY,"+ 
		" name VARCHAR(255)"+ 
	")";

	public static void main(String[] args) {
    	RestflowLauncher app = new RestflowLauncher();
    	app.launch(args)
    	.success(v -> {
    	});
    }
}
