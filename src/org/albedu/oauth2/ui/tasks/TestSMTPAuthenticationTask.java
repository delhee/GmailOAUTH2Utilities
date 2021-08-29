package org.albedu.oauth2.ui.tasks;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.albedu.oauth2.OAuth2;
import org.albedu.oauth2.Util;
import org.apache.commons.net.smtp.SMTPCommand;
import org.apache.commons.net.smtp.SMTPSClient;
import org.apache.log4j.Logger;

public class TestSMTPAuthenticationTask extends SwingWorker<Boolean, String> {

	private static final Logger logger = Logger.getLogger(TestIMAPAuthenticationTask.class);
	private BackgroundTaskListener backgroundTaskListener;
	private String user;
	private String accessToken;

	public TestSMTPAuthenticationTask(BackgroundTaskListener backgroundTaskListener, String user, String accessToken) {
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
			
			SMTPSClient smtpClient = new SMTPSClient(false);			
			smtpClient.connect("smtp.gmail.com", 587);
			
			String resp = smtpClient.getReplyString();
			logger.debug(resp);
			publish(resp);
			Thread.sleep(100);
			
			//smtpClient.helo("test");
			smtpClient.sendCommand("ehlo test");
			
			resp = smtpClient.getReplyString();
			logger.debug("Sent: ehlo test\nReply: " + resp);
			publish("Sent: ehlo test\nReply: " + resp);
			Thread.sleep(100);

			if (smtpClient.execTLS()) {
				resp = smtpClient.getReplyString();
				logger.debug("Sent: STARTTLS\nReply: " + resp);
				publish("Sent: STARTTLS\nReply: " + resp);
				Thread.sleep(100);
				
				smtpClient.sendCommand("AUTH XOAUTH2 " + oauth2String);
				
				resp = smtpClient.getReplyString();
				logger.debug("Sent: AUTH XOAUTH2  " + oauth2String + "\nReply: " + resp);
				publish("Sent: AUTH XOAUTH2  " + oauth2String + "\nReply: " + resp);
				Thread.sleep(100);
				
				smtpClient.sendCommand(SMTPCommand.QUIT);
				
				resp = smtpClient.getReplyString();
				logger.debug("Sent: QUIT\nReply: " + resp);
				publish("Sent: QUIT\nReply: " + resp);
				Thread.sleep(100);
				
				smtpClient.disconnect();
				
				resp = smtpClient.getReplyString();
				logger.debug("Request: smtpClient.disconnect()\nReply: " + resp);
				//publish("Request: smtpClient.disconnect()\nReply: " + resp);
				Thread.sleep(100);
			} else {
				logger.error("smtpClient.execTLS() failed. ");
			}
			
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
