package com.platum.restflow.auth.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;

import com.auth0.jwt.Algorithm;
import com.auth0.jwt.JWTSigner;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.platum.restflow.auth.AuthFilter;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.exceptions.RestflowUnauthorizedException;
import com.platum.restflow.resource.ResourceObject;

public class JwtResolver {
	
	public static final String PROPERTIES_PREFIX = "restflow.auth.jwt";
	
	public static final String ISSUER_PROPERTY = "restflow.auth.jwt.issuer";
	
	public static final String AUDIENCE_PROPERTY = "restflow.auth.jwt.audience";
	
	public static final String SECRET_PROPERTY = "restflow.auth.jwt.secret";
	
	public static final String EXPIRATION_INTERVAL_PROPERTY = "restflow.auth.jwt.expirationInterval";
	
	public static final String SIGN_ALGORITHM_PROPERTY = "restflow.auth.jwt.signingAlgorithm";
	
	public static final String PUBLIC_KEY_PATH_PROPERTY = "restflow.auth.jwt.publicKeyPath";

	public static final String PRIVATE_KEY_PATH_PROPERTY = "restflow.auth.jwt.privateKeyPath";

	private String issuer;
	
	private String audience;
	
	private String secret; 

	private long expirationInterval = 50000; //seconds
	
	private Algorithm signingAlgorithm = Algorithm.HS256; 
	
	private PublicKey publicKey;
	
	private PrivateKey privateKey;
	
	public String encode(ResourceObject object) {
		final long iat = System.currentTimeMillis() / 1000l; 
		final long exp = iat + expirationInterval;
		final HashMap<String, Object> claims = new HashMap<String, Object>();
		claims.put("iss", issuer);
		claims.put("aud", audience);
		claims.put("exp", exp);
		claims.put("iat", iat);
		claims.put("object", object);
		final JWTSigner signer = privateKey != null ? new JWTSigner(privateKey) : new JWTSigner(secret);
		return AuthFilter.BEARER_AUTH_HEADER+" "+signer.sign(claims);
	}
	
	public ResourceObject decode(String token) {
		if(StringUtils.isEmpty(token)) {
			throw new RestflowUnauthorizedException("No token provided."); 
		}
		System.out.println(token);
		String[] parts = token.split(" ");
		String jwt = parts.length == 1 ? token : parts[1];
		System.out.println(jwt);
		try {
			final JWTVerifier verifier = publicKey != null ? new JWTVerifier(publicKey)
					 : new JWTVerifier(secret);
			final Map<String,Object> claims= verifier.verify(jwt);
			@SuppressWarnings("unchecked")
			Map<String, Object> objectMap = (Map<String, Object>) claims.get("object");
			ResourceObject object = new ResourceObject();
			if(objectMap != null) {
				object.putAll(objectMap);
			}
			return object;
		} catch (InvalidKeyException | JWTVerifyException e) {
			throw new RestflowUnauthorizedException("Invalid token provided");
		}  catch(Throwable e) {
			throw new RestflowException("Error when reading token.", e);
		}
	}
	
	public JwtResolver load(Properties properties) {
		Validate.notNull(properties);
		try
		{
			issuer = properties.getProperty(ISSUER_PROPERTY);
			audience = properties.getProperty(AUDIENCE_PROPERTY);
			secret = properties.getProperty(SECRET_PROPERTY);
			String expirationIntervalStr = properties.getProperty(EXPIRATION_INTERVAL_PROPERTY);
			if(StringUtils.isNotEmpty(expirationIntervalStr) && 
					NumberUtils.isDigits(expirationIntervalStr)) {
				expirationInterval = Long.parseLong(expirationIntervalStr);	
			}	
			String signingAlgorithmStr = properties.getProperty(SIGN_ALGORITHM_PROPERTY);
			if(StringUtils.isNotEmpty(signingAlgorithmStr)) {
				signingAlgorithm = Algorithm.findByName(signingAlgorithmStr);
			}
			loadPublicKey(properties.getProperty(PUBLIC_KEY_PATH_PROPERTY));
			loadPrivateKey(properties.getProperty(PRIVATE_KEY_PATH_PROPERTY));
		} catch(Throwable e) {
			throw new RestflowException("Error loading jwt properties.", e);
		}
		return this;
	}
	
	public JwtResolver issuer(String issuer) {
		this.issuer = issuer;
		return this;
	}

	public JwtResolver audience(String audience) {
		this.audience = audience;
		return this;
	}
	
	public JwtResolver secret(String secret) {
		this.secret = secret;
		return this;
	}

	public JwtResolver expirationInterval(long expirationInterval) {
		this.expirationInterval = expirationInterval;
		return this;
	}

	public JwtResolver signingAlgorithm(Algorithm signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
		return this;
	}

	public JwtResolver publicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
		return this;
	}

	public JwtResolver privateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
		return this;
	}
	
	private void loadPublicKey(String path) throws Throwable {
		if(StringUtils.isEmpty(path)) return;
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(readKeyFile(path));
		KeyFactory keyFactory = KeyFactory.getInstance(signingAlgorithm.getValue());
		publicKey = keyFactory.generatePublic(publicKeySpec);
	}
	
	private void loadPrivateKey(String path) throws Throwable {
		if(StringUtils.isEmpty(path)) return;
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(readKeyFile(path));
		KeyFactory keyFactory = KeyFactory.getInstance(signingAlgorithm.getValue());
		privateKey = keyFactory.generatePrivate(privateKeySpec);
	}
	
	private byte[] readKeyFile(String path) throws IOException {
		File fileKey = new File(path);
		FileInputStream fis = new FileInputStream(path);
		byte[] encodedPublicKey = new byte[(int) fileKey.length()];
		fis.read(encodedPublicKey);
		fis.close();
		return encodedPublicKey;
	}

}
