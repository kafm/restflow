package com.platum.restflow;

import org.junit.Assert;
import org.junit.Test;

public class RestflowLauncherTest 
{
	private RestflowLauncher launcher;
	
	public RestflowLauncherTest() {
		launcher = new RestflowLauncher();
	}

	@Test
    public void testRunWithoutArgs()
    {
		String[] args = {};
		Assert.assertTrue(launcher.launch(args));
    }
	
	@Test
    public void testRunWithConfigArg()
    {
		String[] args = {"-config ABC"};
		Assert.assertTrue(launcher.launch(args));      
    }
	
	@Test
    public void testHelp()
    {
		String[] args = {"-help"};
		Assert.assertTrue(launcher.launch(args));      
    }
	
	@Test
    public void testVersion()
    {
		String[] args = {"-version"};
		Assert.assertTrue(launcher.launch(args));
    }
	

}
