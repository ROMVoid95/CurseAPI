package com.therandomlabs.curseapi.forgesvc;

import com.google.common.base.MoreObjects;
import com.therandomlabs.curseapi.CurseMember;
import okhttp3.HttpUrl;

final class ForgeSVCMember extends CurseMember {
	private int userId;
	private String name;
	private HttpUrl url;

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).
				add("id", userId).
				add("name", name).
				add("url", url).
				toString();
	}

	@Override
	public int id() {
		return userId;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public HttpUrl url() {
		return url;
	}
}
