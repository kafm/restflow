package com.platum.restflow.auth;

import com.platum.restflow.AuthMetadata;
import com.platum.restflow.Restflow;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.ResourceObject;
import com.platum.restflow.utils.promise.Promise;

public interface AuthManager
{	
	public static final String AUTH_FILTER_IMPL_PROPERTY = "restflow.auth.filterImpl";
	
	public static final String USERNAME_PARAM = "userName";
	
	public static final String PASSWORD_PARAM = "password";
	
	public static final String OLD_PASSWORD_PARAM = "oldPassword";
	
	public static final String NEW_PASSWORD_PARAM = "newPassword";
	
	public static final String PASSWORD_CONFIRM_PARAM = "passwordConfirm";

	public void config(Restflow restflow, Resource authResource);
	
	public boolean authApplies();
	
	public Promise<AuthMetadata> authenticate(ResourceMethod method, AuthDetails authDetails);
	
	public Promise<Void> changePassword(ResourceMethod method, ResourceObject object, AuthDetails authDetails);
	
	public Promise<Void> changeUserInfo(ResourceMethod method, ResourceObject object);
		
	public Promise<Void> revoke(ResourceMethod method, String token);
	
	public Promise<String> getAuthorization(AuthMetadata object);
	
	public Promise<AuthMetadata> resolveAuthorization(String authCode);
		
}
