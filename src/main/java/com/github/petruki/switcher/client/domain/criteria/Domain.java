package com.github.petruki.switcher.client.domain.criteria;

import java.util.Arrays;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class Domain extends SwitcherElement {
	
	private Group[] group;

	public Group[] getGroup() {
		
		return group;
	}

	@Override
	public String toString() {
		
		return "Domain [group=" + Arrays.toString(group) + ", description=" + description + ", activated=" + activated
				+ "]";
	}
	
}