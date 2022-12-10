package org.albedu.oauth2.ui.views;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.albedu.oauth2.Util;
import org.albedu.oauth2.ui.tasks.BackgroundTaskListener;
import org.albedu.oauth2.ui.tasks.CountdownTimerTask;
import org.albedu.oauth2.ui.tasks.RefreshOAUTH2TokenTask;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class RefreshOAUTH2TokenPanel extends JPanel implements DocumentListener, ActionListener, BackgroundTaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(RefreshOAUTH2TokenPanel.class);
	private JTextArea taClientId;
	private JTextArea taClientSecret;
	private JTextArea taRefreshToken;
	private JLabel lAccessTokenValue;
	private JLabel lAccessTokenExpirationSecondsValue;
	private JButton btnCopyAccessToken;
	private JButton btnContinue;

	private Timer timer;

	private long lastExitTime;
	private long timerOffset;
	private long accessTokenRequestStartTime;

	public RefreshOAUTH2TokenPanel(long lastExitTime, long timerOffset) {
		this.timerOffset = timerOffset;
		this.lastExitTime = lastExitTime;

		this.setLayout(null);

		JLabel lClientId = new JLabel("Client Id:");
		lClientId.setBounds(50, 20, 130, 40);

		this.taClientId = new JTextArea();
		this.taClientId.addMouseListener(new ContextMenuMouseListener(this.taClientId));
		this.taClientId.getDocument().addDocumentListener(this);
		Util.setFocusTraversalKeys(this.taClientId);

		JScrollPane scrollClientId = new JScrollPane(this.taClientId);
		scrollClientId.setBounds(175, 20, 250, 40);

		JLabel lClientSecret = new JLabel("Client Secret:");
		lClientSecret.setBounds(50, 80, 130, 40);

		this.taClientSecret = new JTextArea();
		this.taClientSecret.addMouseListener(new ContextMenuMouseListener(this.taClientSecret));
		this.taClientSecret.getDocument().addDocumentListener(this);
		Util.setFocusTraversalKeys(this.taClientSecret);

		JScrollPane scrollClientSecret = new JScrollPane(this.taClientSecret);
		scrollClientSecret.setBounds(175, 80, 250, 40);

		JLabel lRefreshToken = new JLabel("Refresh Token:");
		lRefreshToken.setBounds(50, 140, 130, 40);

		this.taRefreshToken = new JTextArea();
		this.taRefreshToken.addMouseListener(new ContextMenuMouseListener(this.taRefreshToken));
		this.taRefreshToken.getDocument().addDocumentListener(this);
		Util.setFocusTraversalKeys(this.taRefreshToken);

		JScrollPane scrollRefreshToken = new JScrollPane(this.taRefreshToken);
		scrollRefreshToken.setBounds(175, 140, 250, 40);

		JLabel lAccessToken = new JLabel("Access Token:");
		lAccessToken.setBounds(50, 200, 130, 40);

		this.lAccessTokenValue = new JLabel();

		JScrollPane scrollAccessToken = new JScrollPane(this.lAccessTokenValue);
		scrollAccessToken.setBounds(175, 200, 250, 40);

		this.btnCopyAccessToken = new JButton("Copy");
		this.btnCopyAccessToken.setBounds(445, 200, 80, 40);
		this.btnCopyAccessToken.addActionListener(this);
		Util.registerEnterKeyAction(this.btnCopyAccessToken);

		JLabel lAccessTokenExpirationSeconds = new JLabel("<html>Access Token<br> Expiration Seconds:</html>");
		lAccessTokenExpirationSeconds.setBounds(50, 247, 120, 80);

		this.lAccessTokenExpirationSecondsValue = new JLabel();

		JScrollPane scrollAccessTokenExpirationSeconds = new JScrollPane(this.lAccessTokenExpirationSecondsValue);
		scrollAccessTokenExpirationSeconds.setBounds(175, 270, 250, 40);

		this.btnContinue = new JButton("Refresh Token");
		this.btnContinue.setBounds(50, 360, 150, 40);
		this.btnContinue.addActionListener(this);
		this.btnContinue.setEnabled(false);
		Util.registerEnterKeyAction(this.btnContinue);

		this.add(lClientId);
		this.add(scrollClientId);
		this.add(lClientSecret);
		this.add(scrollClientSecret);
		this.add(lRefreshToken);
		this.add(lAccessToken);
		this.add(scrollAccessToken);
		this.add(scrollRefreshToken);
		this.add(lAccessTokenExpirationSeconds);
		this.add(scrollAccessTokenExpirationSeconds);
		this.add(this.btnCopyAccessToken);
		this.add(this.btnContinue);
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		refreshViews();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		refreshViews();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Util.buttonClickDelay(logger);

		if (arg0.getSource().equals(this.btnContinue)) {
			logger.info("btnContinue clicked. ");

			String clientId = this.taClientId.getText();
			String clientSecret = this.taClientSecret.getText();
			String refreshToken = this.taRefreshToken.getText();

			logger.debug("clientId: " + clientId);
			logger.debug("clientSecret: " + clientSecret);
			logger.debug("refreshToken: " + refreshToken);

			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.taClientId.setEnabled(false);
			this.taClientSecret.setEnabled(false);
			this.taRefreshToken.setEnabled(false);
			this.btnContinue.setEnabled(false);

			RefreshOAUTH2TokenTask refreshOAUTH2TokenTask = new RefreshOAUTH2TokenTask(this, clientId, clientSecret, refreshToken);
			refreshOAUTH2TokenTask.execute();

			this.accessTokenRequestStartTime = (new Date()).getTime();
		} else if (arg0.getSource().equals(this.btnCopyAccessToken)) {
			logger.info("btnCopyAccessToken clicked. ");

			String accessToken = this.lAccessTokenValue.getText();

			if (accessToken != null) {
				accessToken = accessToken.trim();
			}

			if (accessToken != null && !accessToken.isEmpty()) {
				Util.copyToClipboard(this.lAccessTokenValue.getText());
			}
		}
	}

	@Override
	public void updateUI(Object source, Object output) {
		if (source instanceof RefreshOAUTH2TokenTask) {
			JSONObject response = (JSONObject) output;

			if (response != null) {
				String accessToken = "";

				try {
					accessToken = response.getString("access_token");
				} catch (Exception e) {
					logger.warn("Failed to get accessToken from JSON. ");
					logger.warn(Util.stackTraceToString(e));
				}

				int accessTokenLifeSpanInSeconds = 0;

				try {
					accessTokenLifeSpanInSeconds = response.getInt("expires_in");
				} catch (Exception e) {
					logger.warn("Failed to get accessTokenLifeSpanInSeconds from JSON. ");
					logger.warn(Util.stackTraceToString(e));
				}

				logger.debug("Access Token: " + accessToken);
				logger.debug("Access Token Expiration Seconds: " + accessTokenLifeSpanInSeconds);

				this.lAccessTokenValue.setText(accessToken);

				if (accessTokenLifeSpanInSeconds == 0) {
					startCountdownTimer(0);
				} else {
					long accessTokenRequestTimeElapsedEstimated = ((new Date().getTime()) - this.accessTokenRequestStartTime) / 1000;

					logger.info("accessTokenRequestTimeElapsedEstimated: " + accessTokenRequestTimeElapsedEstimated);

					startCountdownTimer(accessTokenLifeSpanInSeconds - ((int) accessTokenRequestTimeElapsedEstimated));
				}
			} else {
				logger.error("Error implementing authorizeTokens(), response is null");
			}

			this.setCursor(Cursor.getDefaultCursor());
			refreshViews();
			this.taClientId.setEnabled(true);
			this.taClientSecret.setEnabled(true);
			this.taRefreshToken.setEnabled(true);
		} else if (source instanceof CountdownTimerTask) {
			Integer timeRemaining = (Integer) output;

			this.lAccessTokenExpirationSecondsValue.setText(Integer.toString(timeRemaining));

			if (timeRemaining <= 0) {
				this.timer.cancel();
			}
		}
	}

	private void refreshViews() {
		if (this.taClientId != null && this.taClientSecret != null && this.taRefreshToken != null) {
			String clientId = this.taClientId.getText();
			String clientSecret = this.taClientSecret.getText();
			String refreshToken = this.taRefreshToken.getText();

			if (clientId != null) {
				clientId = clientId.trim();
			}

			if (clientSecret != null) {
				clientSecret = clientSecret.trim();
			}

			if (refreshToken != null) {
				refreshToken = refreshToken.trim();
			}

			// logger.debug("clientId: " + clientId);
			// logger.debug("clientSecret: " + clientSecret);
			// logger.debug("refreshToken: " + refreshToken);

			if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty() && refreshToken != null && !refreshToken.isEmpty()) {
				this.btnContinue.setEnabled(true);
			} else {
				this.btnContinue.setEnabled(false);
			}
		}
	}

	public String getClientId() {
		return taClientId.getText().trim();
	}

	public void setClientId(String clientId) {
		this.taClientId.setText(clientId);
	}

	public String getClientSecret() {
		return taClientSecret.getText().trim();
	}

	public void setClientSecret(String clientSecret) {
		this.taClientSecret.setText(clientSecret);
	}

	public String getRefreshToken() {
		return taRefreshToken.getText().trim();
	}

	public void setRefreshToken(String refreshToken) {
		this.taRefreshToken.setText(refreshToken);
	}

	public String getAccessToken() {
		String accessToken = lAccessTokenValue.getText();

		if (accessToken != null) {
			accessToken = accessToken.trim();
		} else {
			accessToken = "";
		}

		logger.debug("getAccessToken(): " + accessToken);

		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.lAccessTokenValue.setText(accessToken);
	}

	public String getAccessTokenExpirationSeconds() {
		return lAccessTokenExpirationSecondsValue.getText().trim();
	}

	public void setAccessTokenExpirationSeconds(String accessTokenExpirationSeconds) {
		if (accessTokenExpirationSeconds == null || accessTokenExpirationSeconds.isEmpty()) {
			accessTokenExpirationSeconds = "0";
		}

		long now = (new Date()).getTime();

		long timeRemaining = Long.parseLong(accessTokenExpirationSeconds) - (now - this.lastExitTime) / 1000 - this.timerOffset;

		logger.debug("accessTokenExpirationSeconds: " + accessTokenExpirationSeconds);
		logger.debug("now: " + now);
		logger.debug("lastExitTime: " + this.lastExitTime);
		logger.debug("timeRemaining: " + timeRemaining);

		if (timeRemaining < 0) {
			timeRemaining = 1;
		}

		startCountdownTimer(((int) timeRemaining));
	}

	private void startCountdownTimer(int secondsRemaining) {
		if (this.timer != null) {
			this.timer.cancel();
		}

		this.timer = new Timer();

		CountdownTimerTask countdownTimer = new CountdownTimerTask(this, secondsRemaining);

		this.timer.schedule(countdownTimer, 0, 1000);
	}
}
