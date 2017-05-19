package com.platum.restflow.resource.property;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import com.platum.restflow.resource.xml.PropertyTypeEnumAdapter;


@XmlRootElement(name = "property")
@XmlAccessorType (XmlAccessType.FIELD)
public class ResourceProperty implements Serializable {

	private static final long serialVersionUID = 789745601516071235L;

	@XmlAttribute
	private String name;
	
	@XmlJavaTypeAdapter(PropertyTypeEnumAdapter.class)
	private ResourcePropertyType type;
	
	private String label;
	
	private boolean labelFromMessages;
	
	@XmlAttribute
	private String column;

	private boolean required;
	
	private boolean repeating;
	
	private String max;
	
	private String min;
	
	private String pattern;
	
	private String precision;
	
	private String format;

	public String getName() {
		return name;
	}

	public ResourceProperty setName(String name) {
		this.name = name;
		return this;
	}

	public ResourcePropertyType getType() {
		return type;
	}

	public ResourceProperty setType(ResourcePropertyType type) {
		this.type = type;
		return this;
	}

	public String getLabel() {
		return StringUtils.isEmpty(label) ? name : label;
	}

	public ResourceProperty setLabel(String label) {
		this.label = label;
		return this;
	}

	public boolean isLabelFromMessages() {
		return labelFromMessages;
	}

	public ResourceProperty setLabelFromMessages(boolean labelFromMessages) {
		this.labelFromMessages = labelFromMessages;
		return this;
	}

	public String getColumn() {
		return StringUtils.isEmpty(column) ?  name : column;
	}

	public ResourceProperty setColumn(String column) {
		this.column = column;
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public ResourceProperty setRequired(boolean required) {
		this.required = required;
		return this;
	}
	
	public boolean isRepeating() {
		return repeating;
	}

	public ResourceProperty setRepeating(boolean repeating) {
		this.repeating = repeating;
		return this;
	}

	public String getMax() {
		return max;
	}

	public ResourceProperty setMax(String max) {
		this.max = max;
		return this;
	}

	public String getMin() {
		return min;
	}

	public ResourceProperty setMin(String min) {
		this.min = min;
		return this;
	}

	public String getPattern() {
		return pattern;
	}

	public ResourceProperty setPattern(String pattern) {
		this.pattern = pattern;
		return this;
	}

	public String getPrecision() {
		return precision;
	}

	public ResourceProperty setPrecision(String precision) {
		this.precision = precision;
		return this;
	}

	public String getFormat() {
		return format;
	}

	public ResourceProperty setFormat(String format) {
		this.format = format;
		return this;
	}

	@Override
	public String toString() {
		return "ResourceProperty [name=" + name + ", type=" + type + ", label=" + label + ", labelFromMessages="
				+ labelFromMessages + ", column=" + column + ", required=" + required + ", repeating=" + repeating
				+ ", max=" + max + ", min=" + min + ", pattern=" + pattern + ", precision=" + precision + ", format="
				+ format + "]";
	}

}
