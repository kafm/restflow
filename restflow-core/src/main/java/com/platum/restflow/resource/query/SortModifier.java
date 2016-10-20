package com.platum.restflow.resource.query;

import org.apache.commons.lang3.StringUtils;

public class SortModifier {
	
	private String field;
	
	private SortOrder order;
	
	public static SortModifier fromString(String sortString) {
		SortModifier sort = null;
		if(StringUtils.isNotEmpty(sortString)) {
			sort  = new SortModifier();
			System.out.println(sortString);
			if(sortString.startsWith("\\+")) { //ASC
				System.out.println("ASC");
				sort.field(sortString.substring(1))
					.order(SortOrder.ASC);
			} else if(sortString.startsWith("-")) { //DESC
				System.out.println("AQUI");
				sort.field(sortString.substring(1))
				.order(SortOrder.DESC);				
			} else {
				System.out.println("HERE");
				sort.field(sortString)
				.order(SortOrder.ASC);	
			}
		}
		return sort;
	}

	public String field() {
		return field;
	}

	public SortModifier field(String field) {
		this.field = field;
		return this;
	}

	public SortOrder order() {
		return order;
	}

	public SortModifier order(SortOrder order) {
		this.order = order;
		return this;
	}

}
