package com.github.petruki.switcher.client.exception;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherSnapshotLoadException extends SwitcherException {
	
	private static final long serialVersionUID = 5372308871473744595L;

	public SwitcherSnapshotLoadException(final String location, final Exception e) {
		
		super(String.format("Unable to load the snapshot from %s", location), e);
	}
}
