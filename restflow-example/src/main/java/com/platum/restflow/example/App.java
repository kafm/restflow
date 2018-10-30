package com.platum.restflow.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.platum.restflow.RestflowLauncher;
import org.h2.Driver;
/**
 * Hello world!
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
