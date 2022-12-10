package org.albedu.oauth2.ui.tasks;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.albedu.oauth2.OAuth2;
import org.albedu.oauth2.Util;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class GenerateOAUTH2TokenTask extends SwingWorker<JSONObject, Void> {
	private static final Logger logger = Logger.getLogger(GenerateOAUTH2TokenTask.class);
	private BackgroundTaskListener backgroundTaskListener;
	private String clientId;
	private String clientSecret;
	private String authorizationCode;
	private String redirectURI;

	public GenerateOAUTH2TokenTask(BackgroundTaskListener backgroundTaskListener, String clientId, String clientSecret, String authorizationCode, String redirectURI) {
		this.backgroundTaskListener = backgroundTaskListener;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.authorizationCode = authorizationCode;
		this.redirectURI = redirectURI;
	}
	
	@Override
	protected JSONObject doInBackground() throws Exception {
		OAuth2 oauth2Util = new OAuth2();
		JSONObject response = oauth2Util.authorizeTokens(this.clientId, this.clientSecret, this.authorizationCode, this.redirectURI);
		
		logger.debug("resp: " + (response == null ? response:response.toString()));
		
		return response;
	}

	@Override
	protected void done() {
		JSONObject response = null;
		
		try {
			response = get();
		} catch (InterruptedException e) {
			logger.error(Util.stackTraceToString(e));
		} catch (ExecutionException e) {
			logger.error(Util.stackTraceToString(e));
		}
		
		this.backgroundTaskListener.updateUI(this, response);
	}
}
