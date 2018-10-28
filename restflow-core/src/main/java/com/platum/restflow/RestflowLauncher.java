package com.platum.restflow;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseFactory;

public class RestflowLauncher {
	
	/**
	 *  CLI options 
	 *  
	 *  -help
	 *  -version
	 *  -config 
	 *  
	 *  -- to define others properties as it appears....
	 */
	
	public static final String CURRENT_VERSION = RestflowLauncher.class.getPackage().getImplementationTitle();
	
	private static final String HELP_PREFIX = new StringBuilder()
												.append("NAME:")
												.append(System.lineSeparator())
												.append("	restflow - build restful applications as simple as yaml.")
												.append(System.lineSeparator())
												.append(System.lineSeparator())
												.append("DESCRIPTION:")
												.append(System.lineSeparator())
												.append("	Restflow is a light java framework that allows you to build high performance reactive Rest apis FAST.")
												.append(System.lineSeparator())
												.append("	We do this by automatizing the creating of repetitive tasks, e.g, CRUD operations")
												.append(System.lineSeparator())
												.append("	and API versioning so that you can focus on the business logic of your application.")
												.append(System.lineSeparator())
												.append("VERSION: ")
												.append(System.lineSeparator())
												.append("	"+CURRENT_VERSION)
												.append(System.lineSeparator())
												.append("COMMANDS: ")
												.append(System.lineSeparator())
												.toString();	
	private Restflow restflowInstance;

	
	public static void main(String[] args) {
		new RestflowLauncher().launch(args);
	}
	
	public Restflow getRestflowInstance() {
		return restflowInstance;
	}
	
	public Promise<Void> launch(String[] args) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		String configPath = null;
	    CommandLineParser parser = new DefaultParser();
	    Options options = getOptions();
	    try {
		    CommandLine cmdLine = parser.parse(options, args);
		    HelpFormatter formatter = new HelpFormatter();   
		    if(cmdLine.hasOption("help")) {
		        formatter.printHelp(HELP_PREFIX, options);
		    } else if (cmdLine.hasOption("version")) {
		    	System.out.println(CURRENT_VERSION);
		    } else {
		    	if (cmdLine.hasOption("config")) {
		    		configPath = cmdLine.getOptionValue("config");
		    		if(StringUtils.isEmpty(configPath)) {
		    			formatter.printHelp("restflow", options);
		    		}
		    	} 
		    	restflowInstance = new Restflow()
		    							.config(configPath);
		    	restflowInstance.run()
		    	.success(v -> {
		    		System.out.println("Server Started.");
		    		promise.resolve();
		    	}).error(err -> {
		    		err.printStackTrace();
			        System.out.println("Try \"-help\" option for details.");
			        promise.reject(err);
		    	});
		    } 
	    } catch (ParseException e) {
	        System.out.println(e.getMessage());
	        System.out.println("Try \"-help\" option for details.");
	        promise.reject(e);
	    }
	    return promise;
	}
	
	private Options getOptions() {
		return new Options()
					.addOption(Option.builder("help")
			                 .longOpt("help")
			                 .required(false)
			                 .desc("prints commands help and current restflow information")
			                 .build())
					.addOption(Option.builder("version")
			                 .longOpt("version")
			                 .required(false)
			                 .desc("prints current restflow version")
			                 .build())					
					.addOption(Option.builder("config")
			                 .longOpt("config")
			                 .required(false)
			                 .desc("specify a config restflow directory.")
                             .hasArg()
                             .argName("CONFIG_PATH")
			                 .build());			
	}
	

	
}
