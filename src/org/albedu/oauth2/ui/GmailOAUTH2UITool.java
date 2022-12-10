package org.albedu.oauth2.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.albedu.oauth2.Constant;
import org.albedu.oauth2.RedirectResponseHandler;
import org.albedu.oauth2.Util;
import org.albedu.oauth2.ui.tasks.RedirectURIChangeListener;
import org.albedu.oauth2.ui.views.Base64ToTextPanel;
import org.albedu.oauth2.ui.views.FeatureList;
import org.albedu.oauth2.ui.views.GenerateOAUTH2StringPanel;
import org.albedu.oauth2.ui.views.GenerateOAUTH2TokenPanel;
import org.albedu.oauth2.ui.views.RefreshOAUTH2TokenPanel;
import org.albedu.oauth2.ui.views.SettingsPanel;
import org.albedu.oauth2.ui.views.TestIMAPAuthenticationPanel;
import org.albedu.oauth2.ui.views.TestSMTPAuthenticationPanel;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class GmailOAUTH2UITool extends JFrame implements ListSelectionListener, RedirectURIChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GmailOAUTH2UITool.class);

	private JScrollPane scrollPanelRight;
	private FeatureList featureList;
	private RefreshOAUTH2TokenPanel mRefreshOAUTH2TokenPanel;
	private GenerateOAUTH2StringPanel mGenerateOAUTH2StringPanel;
	private GenerateOAUTH2TokenPanel mGenerateOAUTH2TokenPanel;
	private TestIMAPAuthenticationPanel mTestIMAPAuthenticationPanel;
	private TestSMTPAuthenticationPanel mTestSMTPAuthenticationPanel;
	private Base64ToTextPanel mBase64ToTextPanel;
	private SettingsPanel mSettingsPanel;

	private String refreshTokenPanelClientId;
	private String refreshTokenPanelClientSecret;
	private String refreshTokenPanelRefreshToken;
	private String refreshTokenPanelAccessToken;
	private String refreshTokenPanelTimer;
	private String genOAUTH2StringPanelUser;
	private String genOAUTH2StringPanelAccessToken;
	private String genOAUTH2TokenPanelClientId;
	private String genOAUTH2TokenPanelClientSecret;
	private String genOAUTH2TokenPanelScope;
	// private String genOAUTH2TokenPanelVerificationCode;
	private String genOAUTH2TokenPanelAccessToken;
	private String genOAUTH2TokenPanelRefreshToken;
	private String genOAUTH2TokenPanelTimer;
	private String testIMAPAuthPanelUser;
	private String testIMAPAuthPanelAccessToken;
	private String testSMTPAuthPanelUser;
	private String testSMTPAuthPanelAccessToken;
	private String base64ToTextPanelBase64String;
	private String verificationCodeRedirectURI;
	private boolean isVerificationCodeRedirectURIValid;

	private final Properties appProperties;
	private final Properties savedInputData;

	private long lastExitTime;
	private long timerOffset;

	private RedirectResponseHandler mRedirectResponseHandler = null;

	public static void main(String[] args) {
		PropertyConfigurator.configure(Constant.LOG4J_UI_TOOL_CONFIG_FILE_PATH);

		try {
			new GmailOAUTH2UITool();
		} catch (IOException e) {
			logger.error(Util.stackTraceToString(e));
			e.printStackTrace();
		}
	}

	private GmailOAUTH2UITool() throws IOException {
		this.appProperties = Util.loadProperties(Constant.APP_PROPERTIES_FILE_PATH);
		this.savedInputData = Util.loadProperties(Constant.SAVED_INPUT_DATA_PATH);

		this.mRedirectResponseHandler = RedirectResponseHandler.getInstance();

		init();

		String features[] = { "Refresh Token", "Generate OAUTH2 String", "Generate OAUTH2 Token", "Test IMAP Auth", "Test SMTP Auth", "Convert Base64 String to Text", "Settings" };

		this.featureList = new FeatureList(features);
		this.featureList.getJList().addListSelectionListener(this);

		JScrollPane scrollPanelLeft = new JScrollPane(featureList);

		this.scrollPanelRight = new JScrollPane();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPanelLeft, scrollPanelRight);
		splitPane.setResizeWeight(Double.parseDouble(this.appProperties.getProperty(Constant.KEY_SPLIT_PANE_RESIZE_WEIGHT)));

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(10, 10));
		contentPane.add(splitPane, BorderLayout.CENTER);

		setTitle("Gmail OAUTH 2 UI Tool");
		setIconImage(ImageIO.read(new File(this.appProperties.getProperty(Constant.KEY_APP_ICON_PATH))));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 950, 680);
		setContentPane(contentPane);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		if (this.isVerificationCodeRedirectURIValid) {
			this.mRefreshOAUTH2TokenPanel = new RefreshOAUTH2TokenPanel(this.lastExitTime, this.timerOffset);
			this.mRefreshOAUTH2TokenPanel.setClientId(this.refreshTokenPanelClientId);
			this.mRefreshOAUTH2TokenPanel.setClientSecret(this.refreshTokenPanelClientSecret);
			this.mRefreshOAUTH2TokenPanel.setRefreshToken(this.refreshTokenPanelRefreshToken);
			this.mRefreshOAUTH2TokenPanel.setAccessToken(this.refreshTokenPanelAccessToken);
			this.mRefreshOAUTH2TokenPanel.setAccessTokenExpirationSeconds(this.refreshTokenPanelTimer);
			this.scrollPanelRight.setViewportView(this.mRefreshOAUTH2TokenPanel);
			// this.featureList.getJList().setSelectedIndex(0);
		} else {
			this.featureList.getJList().setSelectedIndex(6);
			this.mSettingsPanel = new SettingsPanel(this);
			this.mSettingsPanel.setVerificationCodeRedirectURI(this.verificationCodeRedirectURI);
			this.scrollPanelRight.setViewportView(this.mSettingsPanel);
		}
	}

	private void init() {
		int labelFontSize = Integer.parseInt(this.appProperties.getProperty(Constant.KEY_LABEL_FONT_SIZE));
		UIManager.put("Label.font", new Font("Default", Font.BOLD, labelFontSize));

		int buttonFontSize = Integer.parseInt(this.appProperties.getProperty(Constant.KEY_BUTTON_FONT_SIZE));
		UIManager.put("Button.font", new Font("Default", Font.BOLD, buttonFontSize));

		loadPreviouslyInputData();

		this.startWebServer(this.verificationCodeRedirectURI, false);
	}

	private void loadPreviouslyInputData() {
		this.timerOffset = Long.parseLong(this.appProperties.getProperty(Constant.KEY_TIMER_OFFSET, "0"));
		this.lastExitTime = Long.parseLong(this.savedInputData.getProperty(Constant.KEY_LAST_EXIT_TIME, "0"));
		this.refreshTokenPanelClientId = this.savedInputData.getProperty(Constant.KEY_REFRESH_TOKEN_PANEL_CLIENT_ID);
		this.refreshTokenPanelClientSecret = this.savedInputData.getProperty(Constant.KEY_REFRESH_TOKEN_PANEL_CLIENT_SECRET);
		this.refreshTokenPanelRefreshToken = this.savedInputData.getProperty(Constant.KEY_REFRESH_TOKEN_PANEL_REFRESH_TOKEN);
		this.refreshTokenPanelAccessToken = this.savedInputData.getProperty(Constant.KEY_REFRESH_TOKEN_PANEL_ACCESS_TOKEN);
		this.refreshTokenPanelTimer = this.savedInputData.getProperty(Constant.KEY_REFRESH_TOKEN_PANEL_TIMER);
		this.genOAUTH2StringPanelUser = this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_STR_PANEL_USER);
		this.genOAUTH2StringPanelAccessToken = this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_STR_PANEL_ACCESS_TOKEN);
		this.genOAUTH2TokenPanelClientId = this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_CLIENT_ID);
		this.genOAUTH2TokenPanelClientSecret = this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_CLIENT_SECRET);
		this.genOAUTH2TokenPanelScope = this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_SCOPE);
		// this.genOAUTH2TokenPanelVerificationCode =
		// this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_VERIFICATION_CODE);
		this.genOAUTH2TokenPanelAccessToken = this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_ACCESS_TOKEN);
		this.genOAUTH2TokenPanelRefreshToken = this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_REFRESH_TOKEN);
		this.genOAUTH2TokenPanelTimer = this.savedInputData.getProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_TIMER);
		this.testIMAPAuthPanelUser = this.savedInputData.getProperty(Constant.KEY_TEST_IMAP_AUTH_PANEL_USER);
		this.testIMAPAuthPanelAccessToken = this.savedInputData.getProperty(Constant.KEY_TEST_IMAP_AUTH_PANEL_ACCESS_TOKEN);
		this.testSMTPAuthPanelUser = this.savedInputData.getProperty(Constant.KEY_TEST_SMTP_AUTH_PANEL_USER);
		this.testSMTPAuthPanelAccessToken = this.savedInputData.getProperty(Constant.KEY_TEST_SMTP_AUTH_PANEL_ACCESS_TOKEN);
		this.base64ToTextPanelBase64String = this.savedInputData.getProperty(Constant.KEY_BASE64_TO_TEXT_PANEL_BASE64_STR);
		this.verificationCodeRedirectURI = this.savedInputData.getProperty(Constant.KEY_SETTINGS_PANEL_VERIFICATION_CODE_REDIRECT_URI);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		try {
			if (e.getID() == WindowEvent.WINDOW_CLOSING) {
				saveUserInput();
				this.mRedirectResponseHandler.stopServer();
			}

			super.processWindowEvent(e);
		} catch (Exception ex) {
			logger.error(Util.stackTraceToString(ex));
			ex.printStackTrace();

			System.exit(-1);
		}
	}

	private void saveUserInput() {
		this.savedInputData.setProperty(Constant.KEY_LAST_EXIT_TIME, Long.toString((new Date().getTime())));

		if (this.mRefreshOAUTH2TokenPanel != null) {
			this.savedInputData.setProperty(Constant.KEY_REFRESH_TOKEN_PANEL_CLIENT_ID, this.mRefreshOAUTH2TokenPanel.getClientId());
			this.savedInputData.setProperty(Constant.KEY_REFRESH_TOKEN_PANEL_CLIENT_SECRET, this.mRefreshOAUTH2TokenPanel.getClientSecret());
			this.savedInputData.setProperty(Constant.KEY_REFRESH_TOKEN_PANEL_REFRESH_TOKEN, this.mRefreshOAUTH2TokenPanel.getRefreshToken());
			this.savedInputData.setProperty(Constant.KEY_REFRESH_TOKEN_PANEL_ACCESS_TOKEN, this.mRefreshOAUTH2TokenPanel.getAccessToken());
			this.savedInputData.setProperty(Constant.KEY_REFRESH_TOKEN_PANEL_TIMER, this.mRefreshOAUTH2TokenPanel.getAccessTokenExpirationSeconds());
		}

		if (this.mGenerateOAUTH2StringPanel != null) {
			this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_STR_PANEL_USER, this.mGenerateOAUTH2StringPanel.getUser());
			this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_STR_PANEL_ACCESS_TOKEN, this.mGenerateOAUTH2StringPanel.getAccessToken());
		}

		if (this.mGenerateOAUTH2TokenPanel != null) {
			this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_CLIENT_ID, this.mGenerateOAUTH2TokenPanel.getClientId());
			this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_CLIENT_SECRET, this.mGenerateOAUTH2TokenPanel.getClientSecret());
			this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_SCOPE, this.mGenerateOAUTH2TokenPanel.getScope());
			// this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_VERIFICATION_CODE,
			// this.mGenerateOAUTH2TokenPanel.getAuthorizationCode());
			this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_ACCESS_TOKEN, this.mGenerateOAUTH2TokenPanel.getAccessToken());
			this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_REFRESH_TOKEN, this.mGenerateOAUTH2TokenPanel.getRefreshToken());
			this.savedInputData.setProperty(Constant.KEY_GEN_OAUTH2_TOKEN_PANEL_TIMER, this.mGenerateOAUTH2TokenPanel.getAccessTokenExpirationSeconds());
		}

		if (this.mTestIMAPAuthenticationPanel != null) {
			this.savedInputData.setProperty(Constant.KEY_TEST_IMAP_AUTH_PANEL_USER, this.mTestIMAPAuthenticationPanel.getUser());
			this.savedInputData.setProperty(Constant.KEY_TEST_IMAP_AUTH_PANEL_ACCESS_TOKEN, this.mTestIMAPAuthenticationPanel.getAccessToken());
		}

		if (this.mTestSMTPAuthenticationPanel != null) {
			this.savedInputData.setProperty(Constant.KEY_TEST_SMTP_AUTH_PANEL_USER, this.mTestSMTPAuthenticationPanel.getUser());
			this.savedInputData.setProperty(Constant.KEY_TEST_SMTP_AUTH_PANEL_ACCESS_TOKEN, this.mTestSMTPAuthenticationPanel.getAccessToken());
		}

		if (this.mBase64ToTextPanel != null) {
			this.savedInputData.setProperty(Constant.KEY_BASE64_TO_TEXT_PANEL_BASE64_STR, this.mBase64ToTextPanel.getBase64String());
		}

		if (this.mSettingsPanel != null) {
			this.savedInputData.setProperty(Constant.KEY_SETTINGS_PANEL_VERIFICATION_CODE_REDIRECT_URI, this.mSettingsPanel.getVerificationCodeRedirectURI());
		}

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(Constant.SAVED_INPUT_DATA_PATH);

			this.savedInputData.store(fileOutputStream, null);

			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			logger.error(Util.stackTraceToString(e));
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(Util.stackTraceToString(e));
			e.printStackTrace();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if (arg0.getValueIsAdjusting() == false) {
			if (arg0.getSource().equals(this.featureList.getJList())) {
				this.scrollPanelRight.setViewportView(null);

				logger.info("Feature selected: " + this.featureList.getJList().getSelectedIndex());

				switch (this.featureList.getJList().getSelectedIndex()) {
				case 0:
					if (this.mRefreshOAUTH2TokenPanel == null) {
						this.mRefreshOAUTH2TokenPanel = new RefreshOAUTH2TokenPanel(this.lastExitTime, this.timerOffset);
						this.mRefreshOAUTH2TokenPanel.setClientId(this.refreshTokenPanelClientId);
						this.mRefreshOAUTH2TokenPanel.setClientSecret(this.refreshTokenPanelClientSecret);
						this.mRefreshOAUTH2TokenPanel.setRefreshToken(this.refreshTokenPanelRefreshToken);
						this.mRefreshOAUTH2TokenPanel.setAccessToken(this.refreshTokenPanelAccessToken);
						this.mRefreshOAUTH2TokenPanel.setAccessTokenExpirationSeconds(this.refreshTokenPanelTimer);
					}

					this.scrollPanelRight.setViewportView(this.mRefreshOAUTH2TokenPanel);
					break;
				case 1:
					if (this.mGenerateOAUTH2StringPanel == null) {
						this.mGenerateOAUTH2StringPanel = new GenerateOAUTH2StringPanel();
						this.mGenerateOAUTH2StringPanel.setUser(this.genOAUTH2StringPanelUser);
						this.mGenerateOAUTH2StringPanel.setAccessToken(this.genOAUTH2StringPanelAccessToken);
					}

					this.scrollPanelRight.setViewportView(this.mGenerateOAUTH2StringPanel);
					break;
				case 2:
					if (this.mGenerateOAUTH2TokenPanel == null) {
						this.mGenerateOAUTH2TokenPanel = new GenerateOAUTH2TokenPanel(this.lastExitTime, this.timerOffset);
						this.mGenerateOAUTH2TokenPanel.setClientId(this.genOAUTH2TokenPanelClientId);
						this.mGenerateOAUTH2TokenPanel.setClientSecret(this.genOAUTH2TokenPanelClientSecret);
						this.mGenerateOAUTH2TokenPanel.setScope(this.genOAUTH2TokenPanelScope);
						// this.mGenerateOAUTH2TokenPanel.setAuthorizationCode(this.genOAUTH2TokenPanelVerificationCode);
						this.mGenerateOAUTH2TokenPanel.setAccessToken(this.genOAUTH2TokenPanelAccessToken);
						this.mGenerateOAUTH2TokenPanel.setRefreshToken(this.genOAUTH2TokenPanelRefreshToken);
						this.mGenerateOAUTH2TokenPanel.setAccessTokenExpirationSeconds(this.genOAUTH2TokenPanelTimer);
						this.mGenerateOAUTH2TokenPanel.setRedirectURI(this.verificationCodeRedirectURI);
					}

					this.scrollPanelRight.setViewportView(this.mGenerateOAUTH2TokenPanel);
					break;
				case 3:
					if (this.mTestIMAPAuthenticationPanel == null) {
						this.mTestIMAPAuthenticationPanel = new TestIMAPAuthenticationPanel();
						this.mTestIMAPAuthenticationPanel.setUser(this.testIMAPAuthPanelUser);
						this.mTestIMAPAuthenticationPanel.setAccessToken(this.testIMAPAuthPanelAccessToken);
					}

					this.scrollPanelRight.setViewportView(this.mTestIMAPAuthenticationPanel);
					break;
				case 4:
					if (this.mTestSMTPAuthenticationPanel == null) {
						this.mTestSMTPAuthenticationPanel = new TestSMTPAuthenticationPanel();
						this.mTestSMTPAuthenticationPanel.setUser(this.testSMTPAuthPanelUser);
						this.mTestSMTPAuthenticationPanel.setAccessToken(this.testSMTPAuthPanelAccessToken);
					}

					this.scrollPanelRight.setViewportView(this.mTestSMTPAuthenticationPanel);
					break;
				case 5:
					if (this.mBase64ToTextPanel == null) {
						this.mBase64ToTextPanel = new Base64ToTextPanel();
						this.mBase64ToTextPanel.setBase64String(this.base64ToTextPanelBase64String);
					}

					this.scrollPanelRight.setViewportView(this.mBase64ToTextPanel);
					break;
				case 6:
					if (this.mSettingsPanel == null) {
						this.mSettingsPanel = new SettingsPanel(this);
						this.mSettingsPanel.setVerificationCodeRedirectURI(this.verificationCodeRedirectURI);
					}

					this.scrollPanelRight.setViewportView(this.mSettingsPanel);
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public void onRedirectURIChange(String uri) {
		this.verificationCodeRedirectURI = uri;

		if (this.mSettingsPanel != null) {
			this.mSettingsPanel.setVerificationCodeRedirectURI(uri);
		}

		if (this.mGenerateOAUTH2TokenPanel != null) {
			this.mGenerateOAUTH2TokenPanel.setRedirectURI(uri);
		}

		this.mRedirectResponseHandler.stopServer();

		this.startWebServer(uri, true);
	}

	private void startWebServer(String uri, boolean notifySuccess) {
		try {
			mRedirectResponseHandler.startServer(uri);

			this.isVerificationCodeRedirectURIValid = true;

			if (notifySuccess) {
				JOptionPane.showMessageDialog(null, "New Redirect URI is applied successfully. ", "", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (IOException e) {
			logger.error("Web server URI: " + uri);
			logger.error(Util.stackTraceToString(e));

			// e.printStackTrace();

			this.isVerificationCodeRedirectURIValid = false;
			JOptionPane.showMessageDialog(null, "Invalid Redirect URI found. Please verify and try again. ", "", JOptionPane.ERROR_MESSAGE);
		} catch (URISyntaxException e) {
			logger.error("Web server URI: " + uri);
			logger.error(Util.stackTraceToString(e));

			// e.printStackTrace();

			this.isVerificationCodeRedirectURIValid = false;

			JOptionPane.showMessageDialog(null, "Invalid Redirect URI found. Please verify and try again. ", "", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			logger.error("Web server URI: " + uri);
			logger.error(Util.stackTraceToString(e));

			// e.printStackTrace();

			this.isVerificationCodeRedirectURIValid = false;

			JOptionPane.showMessageDialog(null, "Invalid Redirect URI found. Please verify and try again. ", "", JOptionPane.ERROR_MESSAGE);
		}
	}
}
