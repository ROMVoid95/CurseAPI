package com.therandomlabs.curseapi;

import java.net.URL;

public class CurseUnavailableException extends CurseException {
	private static final long serialVersionUID = -5744500834687538210L;

	public CurseUnavailableException(URL url) {
		super("One of the websites CurseAPI relies on appears to be offline: " + url);
	}
}
