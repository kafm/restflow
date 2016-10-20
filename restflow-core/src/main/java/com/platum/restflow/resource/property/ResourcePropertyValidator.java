package com.platum.restflow.resource.property;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.platum.restflow.exceptions.InvalidValueValidationException;
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
					value = validate(property, value);	
				}
			} catch(RestflowValidationException e) {
				exceptions.add(e);
			} catch(Throwable e) {
				exceptions.add(
						new RestflowFieldConversionValidationException("Impossible to convert field ["+property.getName()+"]", e));
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
			throw new RequiredValidationException("Property "+ property.getLabel()+" is mandatory.")
						.setContextProperty(property);
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
	
	public static Integer validateInteger(ResourceProperty property, String value) {
		IntegerValidator validator = IntegerValidator.getInstance();
		String min = property.getMin();
		String max = property.getMax();
		Integer intVal = validator.validate(value);
		if(intVal == null && StringUtils.isNotEmpty(value)) {
			throw new InvalidValueValidationException("Invalid integer value for property "+property.getLabel());
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
			throw new InvalidValueValidationException("Invalid long value for property "+property.getLabel());
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
			throw new InvalidValueValidationException("Invalid long value for property "+property.getLabel());
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
			throw new InvalidValueValidationException("Invalid date value for property "+property.getLabel());
		}
		validateRange(property.getLabel(), dateValue, validator.validate(min), validator.validate(max));
		return dateValue;		
	}

	public static String validateCreditCard(ResourceProperty property, String value) {
		if(!GenericValidator.isCreditCard(value)) {
			throw new InvalidValueValidationException("Invalid credit card value for property "+property.getLabel());
		}
		return value;
	}

	public static String validateEmail(ResourceProperty property, String value) {
		if(!GenericValidator.isEmail(value)) {
			throw new InvalidValueValidationException("Invalid credit card value for property "+property.getLabel());
		}
		return validateString(property, value);		
	}
		
	public static String validateIpv4(ResourceProperty property, String value) {
		InetAddressValidator validator = InetAddressValidator.getInstance();
		if(!validator.isValidInet4Address(value)) {
			throw new InvalidValueValidationException("Invalid credit card value for property "+property.getLabel());
		}
		return validateString(property, value);				
	}
	
	public static String validateIpv6(ResourceProperty property, String value) {
		InetAddressValidator validator = InetAddressValidator.getInstance();
		if(!validator.isValidInet6Address(value)) {
			throw new InvalidValueValidationException("Invalid credit card value for property "+property.getLabel());
		}
		return validateString(property, value);				
	}
	
	public static String validateUrl(ResourceProperty property, String value) {
		if(!GenericValidator.isUrl(value)) {
			throw new InvalidValueValidationException("Invalid credit card value for property "+property.getLabel());
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
				throw new PatternValidationException("Property "+propertyLabel+" does not match with pattern expected "+pattern+".");	
			}
		}
	}
			
	protected static void validateLength(String propertyLabel, String value, Integer min, Integer max) {
		if(min != null && !GenericValidator.minLength(value, min)) {
			throw new MinLengthValidationException("Property length of "+propertyLabel+" is less than the minimum value allowed "+min+".");
		} 
		if(max != null && !GenericValidator.maxLength(value, max)) {
			throw new MaxLengthValidationException("Property length of "+propertyLabel+" is greater than the maximum value allowed "+max+".");
		}
	}

	protected static void validateRange(String propertyLabel, Integer value, Integer min, Integer max) {
		if(min != null && value < min) {
			throw new MinValueValidationException("Property value of "+propertyLabel+" is less than the minimum value allowed "+min+".");
		} 
		if(max != null && value > max) {
			throw new MaxValueValidationException("Property value of "+propertyLabel+" is greater than the maximum value allowed "+max+".");
		}
	}
	
	protected static void validateRange(String propertyLabel, Long value, Long min, Long max) {
		if(min != null && value < min) {
			throw new MinValueValidationException("Property value of "+propertyLabel+" is less than the minimum value allowed "+min+".");
		} 
		if(max != null && value > max) {
			throw new MaxValueValidationException("Property value of "+propertyLabel+" is greater than the maximum value allowed "+max+".");
		}
	}
	
	protected static void validateRange(String propertyLabel, BigDecimal value, BigDecimal min, BigDecimal max) {
		double dValue = value.doubleValue();
		if(min != null && dValue < min.doubleValue()) {
			throw new MinValueValidationException("Property value of "+propertyLabel+" is less than the minimum value allowed "+min+".");
		} 
		if(max != null && dValue > max.doubleValue()) {
			throw new MaxValueValidationException("Property value of "+propertyLabel+" is greater than the maximum value allowed "+max+".");
		}
	}
	
	protected static void validateRange(String propertyLabel, Date value, Date min, Date max) {
		if(min != null && !value.before(min)) {
			throw new MinValueValidationException("Property value of "+value+" is less than the minimum value allowed "+min+".");
		} 
		if(max != null && !value.after(max)) {
			throw new MaxValueValidationException("Property value of "+value+" is greater than the maximum value allowed "+max+".");
		}
	}		
	
}
