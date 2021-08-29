package org.albedu.oauth2.ui.views;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.albedu.oauth2.OAuth2;
import org.albedu.oauth2.Util;
import org.albedu.oauth2.ui.tasks.BackgroundTaskListener;
import org.albedu.oauth2.ui.tasks.CountdownTimerTask;
import org.albedu.oauth2.ui.tasks.GenerateOAUTH2TokenTask;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class GenerateOAUTH2TokenPanel extends JPanel implements DocumentListener, ActionListener, BackgroundTaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GenerateOAUTH2TokenPanel.class);
	private OAuth2 oauth2Util;
	private JTextArea taClientId;
	private JTextArea taClientSecret;
	private JTextArea taAuthorizationCode;
	private JTextArea taScope;
	private JLabel lAccessTokenValue;
	private JLabel lRefreshTokenValue;
	private JLabel lAccessTokenExpirationSecondsValue;
	private JButton btnCopyAccessToken;
	private JButton btnCopyRefreshToken;
	private JButton btnContinue1;
	private JButton btnContinue2;
	private AuthTokenDialog authTokenDialog;
	
	private Timer timer;
	
	private long lastExitTime;
	private long timerOffset;
	private long accessTokenRequestStartTime;
	
	public GenerateOAUTH2TokenPanel(long lastExitTime, long timerOffset) {
		this.timerOffset = timerOffset;
		this.lastExitTime = lastExitTime;
		
		this.oauth2Util = new OAuth2();
		setLayout(null);
		
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

		JLabel lScope = new JLabel("Scope (Optional): ");
		lScope.setBounds(50, 140, 130, 40);

		this.taScope = new JTextArea();
		this.taScope.addMouseListener(new ContextMenuMouseListener(this.taScope));
		Util.setFocusTraversalKeys(this.taScope);

		JScrollPane scrollScope = new JScrollPane(this.taScope);
		scrollScope.setBounds(175, 140, 250, 40);
		
		JLabel lAuthorizationCode = new JLabel("Verification Code:");
		lAuthorizationCode.setBounds(50, 200, 130, 40);

		this.taAuthorizationCode = new JTextArea();
		this.taAuthorizationCode.addMouseListener(new ContextMenuMouseListener(this.taAuthorizationCode));
		this.taAuthorizationCode.getDocument().addDocumentListener(this);
		Util.setFocusTraversalKeys(this.taAuthorizationCode);

		JScrollPane scrollAuthorizationCode = new JScrollPane(this.taAuthorizationCode);
		scrollAuthorizationCode.setBounds(175, 200, 250, 40);

		JLabel lAccessToken = new JLabel("Access Token:");
		lAccessToken.setBounds(50, 260, 130, 40);

		this.lAccessTokenValue = new JLabel();

		JScrollPane scrollAccessToken = new JScrollPane(this.lAccessTokenValue);
		scrollAccessToken.setBounds(175, 260, 250, 40);

		this.btnCopyAccessToken = new JButton("Copy");
		this.btnCopyAccessToken.setBounds(445, 260, 80, 40);
		this.btnCopyAccessToken.addActionListener(this);
		this.btnCopyAccessToken.setEnabled(true);
		Util.registerEnterKeyAction(this.btnCopyAccessToken);
		
		JLabel lRefreshToken = new JLabel("Refresh Token:");
		lRefreshToken.setBounds(50, 320, 130, 40);

		this.lRefreshTokenValue = new JLabel();

		JScrollPane scrollRefreshToken = new JScrollPane(this.lRefreshTokenValue);
		scrollRefreshToken.setBounds(175, 320, 250, 40);

		this.btnCopyRefreshToken = new JButton("Copy");
		this.btnCopyRefreshToken.setBounds(445, 320, 80, 40);
		this.btnCopyRefreshToken.addActionListener(this);
		this.btnCopyRefreshToken.setEnabled(true);
		Util.registerEnterKeyAction(this.btnCopyRefreshToken);
		
		JLabel lAccessTokenExpirationSeconds = new JLabel("<html>Access Token<br> Expiration Seconds:</html>");
		lAccessTokenExpirationSeconds.setBounds(50, 367, 120, 80);

		this.lAccessTokenExpirationSecondsValue = new JLabel();

		JScrollPane scrollAccessTokenExpirationSeconds = new JScrollPane(this.lAccessTokenExpirationSecondsValue);
		scrollAccessTokenExpirationSeconds.setBounds(175, 390, 250, 40);

		this.btnContinue1 = new JButton("Get Verification Code");
		this.btnContinue1.setBounds(50, 480, 175, 40);
		this.btnContinue1.addActionListener(this);
		this.btnContinue1.setEnabled(false);
		Util.registerEnterKeyAction(this.btnContinue1);

		this.btnContinue2 = new JButton("Generate OAUTH2 Token");
		this.btnContinue2.setBounds(250, 480, 175, 40);
		this.btnContinue2.addActionListener(this);
		this.btnContinue2.setEnabled(false);
		Util.registerEnterKeyAction(this.btnContinue2);
				
		add(lClientId);
		add(scrollClientId);
		add(lClientSecret);
		add(scrollClientSecret);
		add(lAuthorizationCode);
		add(scrollAuthorizationCode);
		add(lScope);
		add(scrollScope);
		add(lAccessToken);
		add(scrollAccessToken);
		add(lRefreshToken);
		add(scrollRefreshToken);
		add(lAccessTokenExpirationSeconds);
		add(scrollAccessTokenExpirationSeconds);
		add(this.btnCopyAccessToken);
		add(this.btnCopyRefreshToken);
		add(this.btnContinue1);
		add(this.btnContinue2);
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {}

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
		
		if (arg0.getSource().equals(this.btnContinue1)) {
			logger.info("btnContinue1 clicked. ");

			String clientId = this.taClientId.getText();
			String scope = this.taScope.getText();

			logger.debug("clientId: " + clientId);
			logger.debug("scope: " + scope);

			String permissionUrl = this.oauth2Util.generatePermissionUrl(clientId, scope);

			logger.debug("permissionUrl: " + permissionUrl);
			
			if (this.authTokenDialog == null) {
				JFrame rootFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
				this.authTokenDialog = new AuthTokenDialog(rootFrame);
			}
			
			this.authTokenDialog.setPermissionUrl(permissionUrl);
			this.authTokenDialog.setVisible(true);
		} else if (arg0.getSource().equals(this.btnContinue2)) {
			logger.info("btnContinue2 clicked. ");

			String clientId = this.taClientId.getText();
			String clientSecret = this.taClientSecret.getText();
			String authorizationCode = this.taAuthorizationCode.getText();

			logger.debug("clientId: " + clientId);
			logger.debug("clientSecret: " + clientSecret);
			logger.debug("authorizationCode: " + authorizationCode);
			
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.btnContinue2.setEnabled(false);
			this.taClientId.setEnabled(false);
			this.taClientSecret.setEnabled(false);
			this.taAuthorizationCode.setEnabled(false);
			this.taScope.setEnabled(false);
			
			GenerateOAUTH2TokenTask generateOAUTH2TokenTask = new GenerateOAUTH2TokenTask(this, clientId, clientSecret, authorizationCode);
			generateOAUTH2TokenTask.execute();
			
			this.accessTokenRequestStartTime = (new Date()).getTime();
		} else if (arg0.getSource().equals(this.btnCopyAccessToken)) {
			logger.info("btnCopyAccessToken clicked. ");
			
			String accessToken = this.lAccessTokenValue.getText();
			
			if (accessToken != null) {
				accessToken = accessToken.trim();
			}
			
			if(accessToken != null && !accessToken.isEmpty()) {
				Util.copyToClipboard(this.lAccessTokenValue.getText());
			}
		} else if (arg0.getSource().equals(this.btnCopyRefreshToken)) {
			logger.info("btnCopyRefreshToken clicked. ");
			
			String refreshToken = this.lRefreshTokenValue.getText();
			
			if (refreshToken != null) {
				refreshToken = refreshToken.trim();
			}
			
			if(refreshToken != null && !refreshToken.isEmpty()) {
				Util.copyToClipboard(this.lRefreshTokenValue.getText());
			}
		}
	}
	
	@Override
	public void updateUI(Object source, Object output) {
		if (source instanceof GenerateOAUTH2TokenTask) {
			JSONObject response = (JSONObject) output;
			
			if(response != null) {
				logger.debug("Refresh Token: " + response.getString("refresh_token"));
				logger.debug("Access Token: " + response.getString("access_token"));
				logger.debug("Access Token Expiration Seconds: " + response.getInt("expires_in"));
				
				this.lAccessTokenValue.setText(response.getString("access_token"));
				this.lRefreshTokenValue.setText(response.getString("refresh_token"));
				
				long accessTokenRequestTimeElapsedEstimated = ((new Date().getTime()) - this.accessTokenRequestStartTime) / 1000;
				
				startCountdownTimer(response.getInt("expires_in") - ((int) accessTokenRequestTimeElapsedEstimated));
				
				logger.info("accessTokenRequestTimeElapsedEstimated: " + accessTokenRequestTimeElapsedEstimated);
			} else {
				logger.error("Error implementing authorizeTokens(), response is null");
			}
			
			this.setCursor(Cursor.getDefaultCursor());
			refreshViews();
			this.taClientId.setEnabled(true);
			this.taClientSecret.setEnabled(true);
			this.taAuthorizationCode.setEnabled(true);
			this.taScope.setEnabled(true);
		} else if (source instanceof CountdownTimerTask) {
			Integer timeRemaining = (Integer) output;
			
			this.lAccessTokenExpirationSecondsValue.setText(Integer.toString(timeRemaining));
			
			if(timeRemaining <= 0) {
				this.timer.cancel();
			}
		}
	}
	
	private void refreshViews() {
		if (this.taClientId != null && this.taClientSecret != null && this.taAuthorizationCode != null) {
			String clientId = this.taClientId.getText();
			String clientSecret = this.taClientSecret.getText();
			String authToken = this.taAuthorizationCode.getText();
			
			if (clientId != null) {
				clientId = clientId.trim();
			}
			
			if (clientSecret != null) {
				clientSecret = clientSecret.trim();
			}
			
			if (authToken != null) {
				authToken = authToken.trim();
			}

			//logger.debug("clientId: " + clientId);
			//logger.debug("clientSecret: " + clientSecret);
			//logger.debug("authToken: " + authToken);

			if (clientId != null && !clientId.isEmpty()) {
				if (clientSecret != null && !clientSecret.isEmpty() && authToken != null && !authToken.isEmpty()) {
					this.btnContinue1.setEnabled(false);
					this.btnContinue2.setEnabled(true);					
				} else {
					this.btnContinue1.setEnabled(true);
					this.btnContinue2.setEnabled(false);					
				}
			} else {
				this.btnContinue1.setEnabled(false);
				this.btnContinue2.setEnabled(false);
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

	public String getAuthorizationCode() {
		return taAuthorizationCode.getText().trim();
	}

	public void setAuthorizationCode(String authorizationCode) {
		this.taAuthorizationCode.setText(authorizationCode);
	}

	public String getScope() {
		return taScope.getText().trim();
	}

	public void setScope(String scope) {
		this.taScope.setText(scope);
	}

	public String getRefreshToken() {
		String refreshToken = lRefreshTokenValue.getText();

		if (refreshToken != null) {
			refreshToken = refreshToken.trim();
		} else {
			refreshToken = "";
		}
		
		logger.debug("getRefreshToken(): " + refreshToken);
		
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.lRefreshTokenValue.setText(refreshToken);
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
		
		long timeRemaining = Long.parseLong(accessTokenExpirationSeconds) - ( now - this.lastExitTime) / 1000 - this.timerOffset;

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
		if(this.timer != null) {
			this.timer.cancel();
		}
		
		this.timer = new Timer();
		
		CountdownTimerTask countdownTimer = new CountdownTimerTask(this, secondsRemaining);
		
		this.timer.schedule(countdownTimer, 0, 1000);
	}
}
