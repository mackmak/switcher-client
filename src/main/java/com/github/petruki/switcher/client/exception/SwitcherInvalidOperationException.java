package com.github.petruki.switcher.client.exception;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherInvalidOperationException extends SwitcherException {
	
	private static final long serialVersionUID = 4685056886357966951L;

	public SwitcherInvalidOperationException(final String operation, final String strategyName) {
		
		super(String.format("Invalid operation %s for %s", operation, strategyName), null);
	}

}
