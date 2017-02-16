package com.lody.virtual.client.hook.delegate;

import java.util.Map;

public interface IORedirectDelegate {
	Map<String, String> getIORedirect();

	Map<String, String> getIOReversedRedirect();

	Map<String, String> getContentReversedRedirect();
}
