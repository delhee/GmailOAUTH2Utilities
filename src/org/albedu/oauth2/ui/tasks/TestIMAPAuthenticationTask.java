package org.albedu.oauth2.ui.tasks;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.albedu.oauth2.OAuth2;
import org.albedu.oauth2.Util;
import org.apache.commons.net.imap.IMAPCommand;
import org.apache.commons.net.imap.IMAPSClient;
import org.apache.log4j.Logger;

public class TestIMAPAuthenticationTask extends SwingWorker<Boolean, String> {

	private static final Logger logger = Logger.getLogger(TestIMAPAuthenticationTask.class);
	private BackgroundTaskListener backgroundTaskListener;
	private String user;
	private String accessToken;

	public TestIMAPAuthenticationTask(BackgroundTaskListener backgroundTaskListener, String user, String accessToken) {
		this.backgroundTaskListener = backgroundTaskListener;
		this.user = user;
		this.accessToken = accessToken;
	}

	@Override
	protected Boolean doInBackground() {
		boolean isTestSuccessul = false;
		
		try {
			OAuth2 objOAuth2 = new OAuth2();
			String oauth2String = objOAuth2.generateOAuth2String(user, accessToken);

			logger.debug("oauth2String: " + oauth2String);
			IMAPSClient imapClient = new IMAPSClient(true);
			imapClient.connect("imap.gmail.com", 993);

			String resp = imapClient.getReplyString();
			logger.debug(resp);
			publish(resp);
			Thread.sleep(100);

			imapClient.sendCommand(IMAPCommand.AUTHENTICATE, "XOAUTH2 " + oauth2String);

			resp = imapClient.getReplyString();
			logger.debug(resp);
			publish(resp);
			Thread.sleep(100);

			imapClient.sendCommand(IMAPCommand.SELECT, "INBOX");

			resp = imapClient.getReplyString();
			logger.debug(resp);
			publish(resp);
			Thread.sleep(100);

			imapClient.sendCommand(IMAPCommand.LOGOUT);

			resp = imapClient.getReplyString();
			logger.debug(resp);
			publish(resp);
			Thread.sleep(100);

			imapClient.disconnect();

			resp = imapClient.getReplyString();
			logger.debug(resp);
			publish(resp);
			Thread.sleep(100);
			
			isTestSuccessul = true;
		} catch (SocketException e) {
			logger.error(Util.stackTraceToString(e));
		} catch (IOException e) {
			logger.error(Util.stackTraceToString(e));
		} catch (InterruptedException e) {
			logger.error(Util.stackTraceToString(e));
		}

		return isTestSuccessul;
	}

	@Override
	protected void done() {
		boolean isTestSuccessul = false;
		
		try {
			isTestSuccessul = get();
		} catch (InterruptedException e) {
			logger.error(Util.stackTraceToString(e));
		} catch (ExecutionException e) {
			logger.error(Util.stackTraceToString(e));
		}
		
		this.backgroundTaskListener.updateUI(this, isTestSuccessul);
	}

	@Override
	protected void process(List<String> chunks) {
		if (chunks != null) {
			this.backgroundTaskListener.updateUI(this, chunks.get(chunks.size() - 1));
		}
	}
}
