package com.platum.restflow.resource.property;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.routines.BigDecimalValidator;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.commons.validator.routines.LongValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.exceptions.InvalidCreditCardValidationException;
import com.platum.restflow.exceptions.InvalidDateValidationException;
import com.platum.restflow.exceptions.InvalidDecimalValidationException;
import com.platum.restflow.exceptions.InvalidEmailValidationException;
import com.platum.restflow.exceptions.InvalidIntegerValidationException;
import com.platum.restflow.exceptions.InvalidIpv4ValidationException;
import com.platum.restflow.exceptions.InvalidIpv6ValidationException;
import com.platum.restflow.exceptions.InvalidLongValidationException;
import com.platum.restflow.exceptions.InvalidUrlValidationException;
import com.platum.restflow.exceptions.MaxLengthValidationException;
import com.platum.restflow.exceptions.MaxValueValidationException;
import com.platum.restflow.exceptions.MinLengthValidationException;
import com.platum.restflow.exceptions.MinValueValidationException;
import com.platum.restflow.exceptions.PatternValidationException;
import com.platum.restflow.exceptions.RequiredValidationException;
import com.platum.restflow.exceptions.RestflowFieldConversionValidationException;
import com.platum.restflow.exceptions.RestflowObjectValidationException;
import com.platum.restflow.exceptions.RestflowValidationException;
import com.platum.restflow.resource.ResourceObject;

public class ResourcePropertyValidator {
	
	public static final String DEFAULT_DATE_FORMAT  = "yyyy-MM-dd HH:mm:ss";
	
	private static final Logger logger = LoggerFactory.getLogger(ResourcePropertyValidator.class);
	
	/**
	 * Validates a {@link ResourceObject} object according to it's properties definition 
	 * @param property A list of {@link ResourceProperty} properties definition 
	 * @param object The {@link ResourceObject} instance to be validated 
	 */
	public static void validate(List<ResourceProperty> properties, ResourceObject object) {
		validate(properties, object, ResourceObject.class);
	}
		
	/**
	 * Validates a {@link ResourceObject} object according to the properties definition 
	 * @param property A list of {@link ResourceProperty} properties definition 
	 * @param object The object to be validated 
	 * @param clazz Class of the objected to be validated
	 */
	public static void validate(List<ResourceProperty> properties, Object object, Class<?> clazz) {
		validate(properties, object, clazz, false);
	}
	
	@SuppressWarnings("unchecked")
	public static void validate(List<ResourceProperty> properties, Object object, Class<?> clazz,
			boolean nonNullOnly) {
		Validate.notNull(properties);
		Validate.notNull(object);
		Validate.notNull(clazz);
		List<RestflowValidationException> exceptions = new ArrayList<>(); 
		properties.stream()
		.forEach(property -> {
			try {
				Object value  = object instanceof ResourceObject ? 
								((ResourceObject) object).getProperty(property.getName())
								: FieldUtils.readField(object, property.getName(), true);
				if(value != null || !nonNullOnly) {
					if(property.isRepeating()) {
						Collection<Object> valueList = new ArrayList<Object>();
						if(value != null) {
							if(value.getClass().isArray()) {
								valueList = Arrays.asList(value);
							} else if(Collection.class.isAssignableFrom(value.getClass())) {
								valueList = (Collection<Object>) value;
							} else {
								valueList.add(value);
							}							
						} 
						if(valueList.isEmpty() && property.isRequired()) {
							RequiredValidationException ex =  new RequiredValidationException("Property "+ property.getLabel()+" is mandatory.")
									.setContextProperty(property);
							ex.params(property.getLabel());
							throw ex;
						} else {
							valueList.stream().forEach(val -> validate(property, val));
						}						
					} else {
						value = validate(property, value);	
					}	
				}
			} catch(RestflowValidationException e) {
				exceptions.add(e);
			} catch(Throwable e) {
				RestflowFieldConversionValidationException ex = new RestflowFieldConversionValidationException("Impossible to convert field ["+property.getName()+"]", e);
				ex.params(property.getName());
				exceptions.add(ex);
			}
		});
		if(!exceptions.isEmpty()) {
			throw new RestflowObjectValidationException(exceptions);
		}
	}
	
	public static <T> T validate(ResourceProperty property, Object value, Class<T> clazz) {
		Validate.notNull(clazz);
		return clazz.cast(validate(property, value));
	}
	
	/**
	 * Validates an object according to the property definition object
	 * @param property A {@link ResourceProperty} property definition instance
	 * @param value Value to be validated (may be null)
	 */
	public static Object validate(ResourceProperty property, Object value) {
		String propertyName = property.getName();
		ResourcePropertyType propertyType = property.getType();
		Validate.notBlank(propertyName);
		Validate.notNull(propertyType);
		String strVal = value == null ? null : String.valueOf(value);
		if(property.isRequired() 
				&& GenericValidator.isBlankOrNull(strVal) 
				&& !property.getType().equals(ResourcePropertyType.BOOLEAN)) {
			RequiredValidationException ex =  new RequiredValidationException("Property "+ property.getLabel()+" is mandatory.")
												.setContextProperty(property);
			ex.params(property.getLabel());
			throw ex;
		} 
		else if(property.getType().equals(ResourcePropertyType.BOOLEAN)) {
			return validateBoolean(property, strVal);	
		} else if(value != null) {
		    switch (propertyType)
		    {	
		    	case INTEGER:
		    		return validateInteger(property, strVal);
		    	case LONG:
		    		return validateLong(property, strVal);	
		    	case DECIMAL:
		    		return validateDecimal(property, strVal);	
		    	case DATE:
		    		return validateDate(property, strVal);	
		    	case EMAIL:
		    		return validateEmail(property, strVal);			
		    	case IPV4:
		    		return validateIpv4(property, strVal);		
		    	case IPV6:
		    		return validateIpv6(property, strVal);	
		    	case URL:
		    		return validateUrl(property, strVal);	    
		    	case CREDITCARD:
		    		return validateCreditCard(property, strVal);
		    	default:
		    		return validateString(property, strVal);
		    }			
		} else {
			return null;
		}
	}
	
	public static Object getRepositoryPropertyValue(ResourceProperty property, Object value) {
		if(value != null) {
			String valueStr = value.toString();
			if(property == null || property.getType() == null) {
				return value;
			} else if(property.getType().equals(ResourcePropertyType.STRING)) {
				return valueStr;
			} else if(StringUtils.isNotEmpty(valueStr)) {
				Object rValue = null;
				switch (property.getType())
				{	
					case INTEGER:
				    	rValue = value instanceof Integer ? value
				    				: NumberUtils.createInteger(valueStr);
				    	break;
			    	case LONG:
			    		rValue = value instanceof Long || value instanceof Integer ? value
			    					: NumberUtils.createLong(valueStr);
			    		break;
			    	case DECIMAL:
			    		rValue = value instanceof Number ? value
			    					: NumberUtils.createBigDecimal(valueStr);
			    		break;
			    	case BOOLEAN:
			    		rValue = value instanceof Boolean ? value
			    					: BooleanUtils.toBoolean(valueStr);
			    		break;
			    	case DATE:
			    		if(value instanceof Date) {
			    			rValue = value;
			    		} else {
				    		String format = GenericValidator.isBlankOrNull(property.getFormat())? 
									DEFAULT_DATE_FORMAT : property.getFormat();
				    		DateFormat df = new SimpleDateFormat(format);	
				    		try {
				    			rValue = new Timestamp(df.parse(valueStr).getTime());
							} catch (ParseException e) {
								if(logger.isDebugEnabled()) {
									logger.debug("Cannot parse value ["+value+"] to date using format "+format);
								}
							}
			    		}
			    		break;
			    	default:
			    		break;
				}	
				return rValue;
			}
		} else if(property != null 
					&& property.getType() != null
					&& property.getType().equals(ResourcePropertyType.BOOLEAN)) {
			return false;
		}
		return null;
	}
	
	public static Integer validateInteger(ResourceProperty property, String value) {
		IntegerValidator validator = IntegerValidator.getInstance();
		String min = property.getMin();
		String max = property.getMax();
		Integer intVal = validator.validate(value);
		if(intVal == null && StringUtils.isNotEmpty(value)) {
			InvalidIntegerValidationException ex = new InvalidIntegerValidationException("Invalid integer value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;
		}
		validateRange(property.getLabel(), intVal, validator.validate(min), validator.validate(max));
		return intVal;
	}
		
	public static Long validateLong(ResourceProperty property, String value) {
		LongValidator validator = LongValidator.getInstance();
		String min = property.getMin();
		String max = property.getMax();
		Long longVal = validator.validate(value);
		if(longVal == null  && StringUtils.isNotEmpty(value)) {
			InvalidLongValidationException ex = new InvalidLongValidationException("Invalid long value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;
		}
		validateRange(property.getLabel(), longVal, validator.validate(min), validator.validate(max));
		return longVal;		
	}
	
	public static BigDecimal validateDecimal(ResourceProperty property, String value) {
		BigDecimalValidator validator = BigDecimalValidator.getInstance();
		String min = property.getMin();
		String max = property.getMax();
		BigDecimal decimalVal = validator.validate(value);
		if(decimalVal == null  && StringUtils.isNotEmpty(value)) {
			InvalidDecimalValidationException ex = new InvalidDecimalValidationException("Invalid decimal value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;
		}
		validateRange(property.getLabel(), decimalVal, validator.validate(min), validator.validate(max));
		return decimalVal;		
	}
	
	public static Boolean validateBoolean(ResourceProperty property, String value) {
		Boolean bValue = (value == null)? false : BooleanUtils.toBooleanObject(value);
		return (bValue == null)? bValue : false;
	}
	
	public static Date validateDate(ResourceProperty property, String value) {
		DateValidator validator = DateValidator.getInstance();
		String format = GenericValidator.isBlankOrNull(property.getFormat())? 
												DEFAULT_DATE_FORMAT : property.getFormat();
		String pattern = new SimpleDateFormat(format).toPattern();
		String min = property.getMin();
		String max = property.getMax();
		Date dateValue = validator.validate(value, pattern);
		if(dateValue == null  && StringUtils.isNotEmpty(value)) {
			InvalidDateValidationException ex = new InvalidDateValidationException("Invalid date value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;
		}
		validateRange(property.getLabel(), dateValue, validator.validate(min), validator.validate(max));
		return dateValue;		
	}

	public static String validateCreditCard(ResourceProperty property, String value) {
		if(!GenericValidator.isCreditCard(value)) {
			InvalidCreditCardValidationException ex = new InvalidCreditCardValidationException("Invalid credit card value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;
		}
		return value;
	}

	public static String validateEmail(ResourceProperty property, String value) {
		if(!GenericValidator.isEmail(value)) {
			InvalidEmailValidationException ex = new InvalidEmailValidationException("Invalid email value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;
		}
		return validateString(property, value);		
	}
		
	public static String validateIpv4(ResourceProperty property, String value) {
		InetAddressValidator validator = InetAddressValidator.getInstance();
		if(!validator.isValidInet4Address(value)) {
			InvalidIpv4ValidationException ex = new InvalidIpv4ValidationException("Invalid ipv4 value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;
		}
		return validateString(property, value);				
	}
	
	public static String validateIpv6(ResourceProperty property, String value) {
		InetAddressValidator validator = InetAddressValidator.getInstance();
		if(!validator.isValidInet6Address(value)) {
			InvalidIpv6ValidationException ex = new InvalidIpv6ValidationException("Invalid ipv6 value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;			
		}
		return validateString(property, value);				
	}
	
	public static String validateUrl(ResourceProperty property, String value) {
		if(!GenericValidator.isUrl(value)) {
			InvalidUrlValidationException ex = new InvalidUrlValidationException("Invalid url value for property "+property.getLabel());
			ex.params(property.getLabel());
			throw ex;				
		}
		return validateString(property, value);				
	}
	
	public static String validateString(ResourceProperty property, String value) {
		Integer min = NumberUtils.isNumber(property.getMin())? 
							NumberUtils.toInt(property.getMin()) : null;
		Integer max = NumberUtils.isNumber(property.getMax())? 
							NumberUtils.toInt(property.getMax()) : null;
		validateLength(property.getLabel(), value, min, max);
		validatePattern(property.getLabel(), property.getPattern(), value);
		return value;
	}
	
	protected static void validatePattern(String propertyLabel, String pattern, String value) {
		if(!GenericValidator.isBlankOrNull(pattern)) {
			if(!Pattern.matches(pattern, value)) {
				PatternValidationException ex = new PatternValidationException("Property "+propertyLabel+" does not match with pattern expected "+pattern+".");	
				ex.params(propertyLabel, pattern);
				throw ex;
			}
		}
	}
			
	protected static void validateLength(String propertyLabel, String value, Integer min, Integer max) {
		if(min != null && !GenericValidator.minLength(value, min)) {
			MinLengthValidationException ex = new MinLengthValidationException("Property length of "+propertyLabel+" is less than the minimum value allowed "+min+".");
			ex.params(propertyLabel, min);
			throw ex;
		} 
		if(max != null && !GenericValidator.maxLength(value, max)) {
			MaxLengthValidationException ex = new MaxLengthValidationException("Property length of "+propertyLabel+" is greater than the maximum value allowed "+max+".");
			ex.params(propertyLabel, max);
			throw ex;
		}
	}

	protected static void validateRange(String propertyLabel, Integer value, Integer min, Integer max) {
		if(min != null && value < min) {
			MinValueValidationException ex = new MinValueValidationException("Property value of "+propertyLabel+" is less than the minimum value allowed "+min+".");
			ex.params(propertyLabel, min);
			throw ex;
		} 
		if(max != null && value > max) {
			MaxValueValidationException ex = new MaxValueValidationException("Property value of "+propertyLabel+" is greater than the maximum value allowed "+max+".");
			ex.params(propertyLabel, max);
			throw ex;
		}
	}
	
	protected static void validateRange(String propertyLabel, Long value, Long min, Long max) {
		if(min != null && value < min) {
			MinValueValidationException ex = new MinValueValidationException("Property value of "+propertyLabel+" is less than the minimum value allowed "+min+".");
			ex.params(propertyLabel, min);
			throw ex;
		} 
		if(max != null && value > max) {
			MaxValueValidationException ex = new MaxValueValidationException("Property value of "+propertyLabel+" is greater than the maximum value allowed "+max+".");
			ex.params(propertyLabel, max);
			throw ex;
		}
	}
	
	protected static void validateRange(String propertyLabel, BigDecimal value, BigDecimal min, BigDecimal max) {
		double dValue = value.doubleValue();
		if(min != null && dValue < min.doubleValue()) {
			MinValueValidationException ex = new MinValueValidationException("Property value of "+propertyLabel+" is less than the minimum value allowed "+min+".");
			ex.params(propertyLabel, min);
			throw ex;
		} 
		if(max != null && dValue > max.doubleValue()) {
			MaxValueValidationException ex = new MaxValueValidationException("Property value of "+propertyLabel+" is greater than the maximum value allowed "+max+".");
			ex.params(propertyLabel, max);
			throw ex;
		}
	}
	
	protected static void validateRange(String propertyLabel, Date value, Date min, Date max) {
		if(min != null && !value.before(min)) {
			MinValueValidationException ex = new MinValueValidationException("Property value of "+value+" is less than the minimum value allowed "+min+".");
			ex.params(propertyLabel, min);
			throw ex;
		} 
		if(max != null && !value.after(max)) {
			MaxValueValidationException ex = new MaxValueValidationException("Property value of "+value+" is greater than the maximum value allowed "+max+".");
			ex.params(propertyLabel, max);
			throw ex;
		}
	}		
	
}
