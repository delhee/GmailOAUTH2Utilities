package org.albedu.oauth2;

import java.io.IOException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.net.imap.IMAPCommand;
import org.apache.commons.net.imap.IMAPSClient;
import org.apache.commons.net.smtp.SMTPCommand;
import org.apache.commons.net.smtp.SMTPSClient;
import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

public class OAuth2 {
	private static final String HELP_TEXT = "Performs client tasks for testing IMAP OAuth2 authentication." + "\n" 
			+ "To use this script, you'll need to have registered with Google as an OAuth" + "\n" 
			+ "application and obtained an OAuth client ID and client secret." + "\n" 
			+ "See https://developers.google.com/identity/protocols/OAuth2 for instructions on" + "\n" 
			+ "registering and for documentation of the APIs invoked by this code." + "\n" 
			+ "This script has 3 modes of operation." + "\n"  + "\n"
			+ "1. The first mode is used to generate and authorize an OAuth2 token, the" + "\n" 
			+ "first step in logging in via OAuth2." + "\n" + "\n"
			+ "  "  + "oauth2 --user=xxx@gmail.com"
			+ "      "  + "--client_id=1038[...].apps.googleusercontent.com"
			+ "      "  + "--client_secret=VWFn8LIKAMC-MsjBMhJeOplZ"
			+ "      "  + "--generate_oauth2_token" + "\n" + "\n"
			+ "The script will converse with Google and generate an oauth request" + "\n"
			+ "token, then present you with a URL you should visit in your browser to" + "\n"
			+ "authorize the token. Once you get the verification code from the Google" + "\n"
			+ "website, enter it into the script to get your OAuth access token. The output" + "\n"
			+ "from this command will contain the access token, a refresh token, and some" + "\n"
			+ "metadata about the tokens. The access token can be used until it expires, and" + "\n"
			+ "the refresh token lasts indefinitely, so you should record these values for" + "\n"
			+ "reuse." + "\n" + "\n"
			+ "2. The script will generate new access tokens using a refresh token." + "\n" + "\n"
			+ "  "  + "oauth2 --user=xxx@gmail.com"
			+ "      "  + "--client_id=1038[...].apps.googleusercontent.com"
			+ "      "  + "--client_secret=VWFn8LIKAMC-MsjBMhJeOplZ" 
			+ "      "  + "--refresh_token=1/Yzm6MRy4q1xi7Dx2DuWXNgT6s37OrP_DW_IoyTum4YA" + "\n" + "\n"
			+ "3. The script will generate an OAuth2 string that can be fed" + "\n"
			+ "directly to IMAP or SMTP. This is triggered with the --generate_oauth2_string" + "\n"  
			+ "option." + "\n" + "\n"
			+ "  "  + "oauth2 --generate_oauth2_string --user=xxx@gmail.com"
			+ "      "  + "--access_token=ya29.AGy[...]ezLg" + "\n" + "\n"
			+ "The output of this mode will be a base64-encoded string. To use it, connect to a" + "\n"   
			+ "IMAPFE and pass it as the second argument to the AUTHENTICATE command." + "\n" + "\n"   
			+ "  "  + "a AUTHENTICATE XOAUTH2 a9sha9sfs[...]9dfja929dk==" + "\n" + "\n" + "\n"
			;
	private static final String HELP_HEADER = null;
	private static final String HELP_FOOTER = null;
	private static final int HELP_TEXT_MAX_CHAR_PER_LINE = 200;
	
	private static final Logger logger = Logger.getLogger(OAuth2.class);

	// The URL root for accessing Google Accounts.
	private static final String GOOGLE_ACCOUNTS_BASE_URL = "https://accounts.google.com";

	// Hard-coded dummy redirect URI for non-web apps.
	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	private static final String OTP_KEY_GENERATE_OAUTH2_TOKEN = "generate_oauth2_token";
	private static final String OTP_KEY_GENERATE_OAUTH2_STRING = "generate_oauth2_string";
	private static final String OTP_KEY_CLIENT_ID = "client_id";
	private static final String OTP_KEY_CLIENT_SECRET = "client_secret";
	private static final String OTP_KEY_ACCESS_TOKEN = "access_token";
	private static final String OTP_KEY_REFRESH_TOKEN = "refresh_token";
	private static final String OTP_KEY_SCOPE = "scope";
	private static final String OTP_KEY_TEST_IMAP_AUTH = "test_imap_authentication";
	private static final String OTP_KEY_TEST_SMTP_AUTH = "test_smtp_authentication";
	private static final String OTP_KEY_USER = "user";
	private static final String OTP_KEY_QUIET = "quiet";

	private Options setOptions() {
		Options options = new Options();

		Option option1 = new Option(null, OTP_KEY_GENERATE_OAUTH2_TOKEN, false, "generates an OAuth2 token for testing");
		Option option2 = new Option(null, OTP_KEY_GENERATE_OAUTH2_STRING, false, "generates an initial client response string for OAuth2");
		Option option3 = new Option(null, OTP_KEY_CLIENT_ID, true, "Client ID of the application that is authenticating. See OAuth2 documentation for details.");
		Option option4 = new Option(null, OTP_KEY_CLIENT_SECRET, true, "Client secret of the application that is authenticating. See OAuth2 documentation for details.");
		Option option5 = new Option(null, OTP_KEY_ACCESS_TOKEN, true, "OAuth2 access token");
		Option option6 = new Option(null, OTP_KEY_REFRESH_TOKEN, true, "OAuth2 refresh token");
		Option option7 = new Option(null, OTP_KEY_SCOPE, true, "scope for the access token. Multiple scopes can be listed separated by spaces with the whole argument quoted.");
		Option option8 = new Option(null, OTP_KEY_TEST_IMAP_AUTH, false, "attempts to authenticate to IMAP");
		Option option9 = new Option(null, OTP_KEY_TEST_SMTP_AUTH, false, "attempts to authenticate to SMTP");
		Option option10 = new Option(null, OTP_KEY_USER, true, "email address of user whose account is being accessed");
		Option option11 = new Option(null, OTP_KEY_QUIET, false, "Omit verbose descriptions and only print machine-readable outputs.");

		options.addOption(option1);
		options.addOption(option2);
		options.addOption(option3);
		options.addOption(option4);
		options.addOption(option5);
		options.addOption(option6);
		options.addOption(option7);
		options.addOption(option8);
		options.addOption(option9);
		options.addOption(option10);
		options.addOption(option11);

		return options;
	}

	private String accountsUrl(String command) {
		/**
		 * Generates the Google Accounts URL. 
		 * 
		 * Args: 
		 *   command: The command to execute. 
		 *   
		 * Returns: 
		 *   A URL for the given command.
		 */

		return String.format("%s/%s", GOOGLE_ACCOUNTS_BASE_URL, command);
	}

	private String formatUrlParams(Properties params) {
		/**
		 * Formats parameters into a URL query string. 
		 * 
		 * Args: 
		 *   params: A key-value map. 
		 *   
		 * Returns: 
		 *   A URL query string version of the given parameters.
		 * 
		 */

		String formattedParamString = "";

		try {
			ArrayList<String> param_fragments = new ArrayList<String>();
			Enumeration<?> param = params.propertyNames();

			while (param.hasMoreElements()) {
				// in sorted(params.iteritems(), key=lambda x: x[0]):
				String key = (String) param.nextElement();
				String value = (String) params.getProperty(key);

				param_fragments.add(String.format("%s=%s", key, URLEncoder.encode(value, "UTF-8")));
			}

			for (int i = 0; i < param_fragments.size(); i++) {
				formattedParamString = formattedParamString + "&" + param_fragments.get(i);
			}
		} catch (Exception e) {
			logger.error(Util.stackTraceToString(e));
		}

		return formattedParamString;
	}

	public String generatePermissionUrl(String clientId, String scope) {
		/**
		 * Generates the URL for authorizing access. 
		 * 
		 * This uses the "OAuth2 for Installed Applications" flow 
		 * described at
		 * 
		 * https://developers.google.com/accounts/docs/OAuth2InstalledApp
		 *  
		 * Args:
		 *   client_id: Client ID obtained by registering your app. 
		 *   scope: scope for access token, e.g. 'https://mail.google.com' 
		 * 
		 * Returns: 
		 *   A URL that the user should visit in their browser.
		 *
		 */

		if (scope == null || scope.isEmpty()) {
			scope = "https://mail.google.com/";
		}

		Properties params = new Properties();
		params.setProperty("client_id", clientId);
		params.setProperty("redirect_uri", REDIRECT_URI);
		params.setProperty("scope", scope);
		params.setProperty("response_type", "code");

		return String.format("%s?%s", accountsUrl("o/oauth2/auth"), formatUrlParams(params));
	}
	
	public JSONObject authorizeTokens(String clientId, String clientSecret, String authorizationCode) {
        HashMap<String, String> postDataParams = new HashMap<String, String>();
        postDataParams.put("client_id", clientId);
        postDataParams.put("client_secret", clientSecret);
        postDataParams.put("code", authorizationCode);
        postDataParams.put("redirect_uri", REDIRECT_URI);
        postDataParams.put("grant_type", "authorization_code");
        	
        JSONObject response = null;
        
        try { 
        	response = Util.urlOpen(accountsUrl("o/oauth2/token"), postDataParams);
		} catch (Exception e) {
			logger.error(Util.stackTraceToString(e));
		}
        
		return response;
	}
	
	public JSONObject refreshToken(String clientId, String clientSecret, String refreshToken) {
		/**
		 * Obtains a new token given a refresh token.
		 * 
		 * See https://developers.google.com/accounts/docs/OAuth2InstalledApp#refresh
		 * 
		 * Args:
		 *   client_id: Client ID obtained by registering your app.
		 *   client_secret: Client secret obtained by registering your app.
		 *   refresh_token: A previously-obtained refresh token.
		 *   
		 * Returns:
		 *   The decoded response from the Google Accounts server, as a dict. Expected
		 *   fields include 'access_token', 'expires_in', and 
		 *   'refresh_token'.
		 *   
		 */
		HashMap<String, String> postDataParams = new HashMap<String, String>();
        postDataParams.put("client_id", clientId);
        postDataParams.put("client_secret", clientSecret);
        postDataParams.put("refresh_token", refreshToken);
        postDataParams.put("grant_type", "refresh_token");
        
        String requestUrl = accountsUrl("o/oauth2/token");
        	
        JSONObject response = null;
        
        try { 
        	response = Util.urlOpen(requestUrl, postDataParams);
		} catch (Exception e) {
			logger.error(Util.stackTraceToString(e));
		}
        
		return response;		
	}

	public String generateOAuth2String(String username, String accessToken) {
		return generateOAuth2String(username, accessToken, true);
	}
	
	public String generateOAuth2String(String username, String accessToken, boolean base64Encode) {
		/**
		 * Generates an IMAP OAuth2 authentication string.
		 * 
		 * See https://developers.google.com/google-apps/gmail/oauth2_overview
		 * 
		 * Args:
		 *   username: the username (email address) of the account to 
		 *   authenticate
		 *   accessToken: An OAuth2 access token.
		 *   base64Encode: Whether to base64-encode the output.
		 * 
		 * Returns:
		 *   The SASL argument for the OAuth2 mechanism.
		 *   
		 */
		
		  String authString = "user=" + username + "\1auth=Bearer " + accessToken + "\1\1";
	
		  if (base64Encode) {
			  authString = new String(Base64.encodeBase64(authString.getBytes()));
		  }
		  
		  return authString;
	}
	
	public void testImapAuthentication(String authString) throws SocketException, IOException {
		/**
		 * Authenticates to IMAP with the given auth_string.
		 * 
		 * Prints a debug trace of the attempted IMAP connection.
		 * 
		 * Args:
		 *   user: The Gmail username (full email address)
		 *   auth_string: A valid OAuth2 string, as returned by 
		 *   GenerateOAuth2String
		 *       Must not be base64-encoded, since imaplib does its 
		 *       own base64-encoding.
		 *     
		 */
		
		IMAPSClient imapClient = new IMAPSClient(true);
		imapClient.connect("imap.gmail.com", 993);
		
		String resp = imapClient.getReplyString();
		logger.debug(resp);
		System.out.println(resp);
		
		imapClient.sendCommand(IMAPCommand.AUTHENTICATE, "XOAUTH2 " + authString);

		resp = imapClient.getReplyString();
		logger.debug(resp);
		System.out.println(resp);
		
		imapClient.sendCommand(IMAPCommand.SELECT, "INBOX");
		
		resp = imapClient.getReplyString();
		logger.debug(resp);
		System.out.println(resp);
		
		imapClient.sendCommand(IMAPCommand.LOGOUT);

		resp = imapClient.getReplyString();
		logger.debug(resp);
		System.out.println(resp);
		
		imapClient.disconnect();
		
		resp = imapClient.getReplyString();
		logger.debug(resp);
		//System.out.println(resp);
	}
	
	public void testSmtpAuthentication(String authString) throws Exception {
		/**
		 * Authenticates to SMTP with the given auth_string.
		 * 
		 * Args:
		 *   user: The Gmail username (full email address)
		 *   auth_string: A valid OAuth2 string, not base64-encoded, as returned by 
		 *       GenerateOAuth2String.
		 *   
		 */		
		SMTPSClient smtpClient = new SMTPSClient(false);
		smtpClient.connect("smtp.gmail.com", 587);
		
		String resp = smtpClient.getReplyString();
		logger.debug(resp);
		System.out.println(resp);
		
		//smtpClient.helo("test");
		smtpClient.sendCommand("ehlo test");
		
		resp = smtpClient.getReplyString();
		logger.debug("Sent: ehlo test\nReply: " + resp);
		System.out.println("Sent: ehlo test\nReply: " + resp);

		if (smtpClient.execTLS()) {
			resp = smtpClient.getReplyString();
			logger.debug("Sent: STARTTLS\nReply: " + resp);
			System.out.println("Sent: STARTTLS\nReply: " + resp);
			
			smtpClient.sendCommand("AUTH XOAUTH2 " + authString);
			
			resp = smtpClient.getReplyString();
			logger.debug("Sent: AUTH XOAUTH2  " + authString + "\nReply: " + resp);
			System.out.println("Sent: AUTH XOAUTH2 " + authString + "\nReply: " + resp);
			
			smtpClient.sendCommand(SMTPCommand.QUIT);
			
			resp = smtpClient.getReplyString();
			logger.debug("Sent: QUIT\nReply: " + resp);
			System.out.println("Sent: QUIT\nReply: " + resp);
			
			smtpClient.disconnect();
			
			resp = smtpClient.getReplyString();
			logger.debug("Request: smtpClient.disconnect()\nReply: " + resp);
			//System.out.println("Request: smtpClient.disconnect()\nReply: " + resp);
		} else {
			throw new Exception("smtpClient.execTLS() failed. ");
		}
	}
	
	private void requireOptions(Options options, String[] args) {
		for (int i = 0; i < args.length; i++) {
			Option option = options.getOption(args[i]);
			option.setRequired(true);
			options.addOption(option);
		}
	}
	
	private List<String> getArgumentList(Options availableOptions, String[] args) throws ParseException {
		List<String> argsList = new ArrayList<String>();
		CommandLineParser parser = new DefaultParser();
		CommandLine command = parser.parse(availableOptions, args, false);
		
		Option[] options = command.getOptions();
		
		for(int i = 0; i < options.length; i++) {
			argsList.add(options[i].getLongOpt());
		}
		
		return argsList;
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("config/log4j-command-line-tool.properties");

		OAuth2 objOAuth2 = new OAuth2();
		Options options = objOAuth2.setOptions();

		CommandLineParser parser = null;
		HelpFormatter formatter = null;

		if (args != null && args.length > 0) {

			try {
				List<String> argsList = objOAuth2.getArgumentList(options, args);

				parser = new DefaultParser();

				CommandLine command = null;
				
				if (argsList.contains(OTP_KEY_REFRESH_TOKEN)) {
					String[] requiredOptions = new String[2];
					requiredOptions[0] = OTP_KEY_CLIENT_ID;
					requiredOptions[1] = OTP_KEY_CLIENT_SECRET;
					//requiredOptions[2] = OTP_KEY_REFRESH_TOKEN;

					logger.debug("Is " + OTP_KEY_CLIENT_ID + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_CLIENT_ID).isRequired());
					logger.debug("Is " + OTP_KEY_CLIENT_SECRET + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_CLIENT_SECRET).isRequired());
					//logger.debug("Is " + OTP_KEY_REFRESH_TOKEN + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_REFRESH_TOKEN).isRequired());

					objOAuth2.requireOptions(options, requiredOptions);

					logger.debug("Is " + OTP_KEY_CLIENT_ID + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_CLIENT_ID).isRequired());
					logger.debug("Is " + OTP_KEY_CLIENT_SECRET + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_CLIENT_SECRET).isRequired());
					//logger.debug("Is " + OTP_KEY_REFRESH_TOKEN + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_REFRESH_TOKEN).isRequired());

					command = parser.parse(options, args, false);
					
					String clientIdValue = command.getOptionValue(OTP_KEY_CLIENT_ID);
					String clientSecretValue = command.getOptionValue(OTP_KEY_CLIENT_SECRET);
					String refreshTokenValue = command.getOptionValue(OTP_KEY_REFRESH_TOKEN);
					
					logger.debug("clientIdValue: " + clientIdValue);
					logger.debug("clientSecretValue: " + clientSecretValue);
					logger.debug("refreshTokenValue: " + refreshTokenValue);
					
					JSONObject response = objOAuth2.refreshToken(clientIdValue, clientSecretValue, refreshTokenValue);

					if (response != null) {
						if (argsList.contains("--" + OTP_KEY_QUIET)) {
							logger.debug(response.getString("access_token"));
							System.out.println(response.getString("access_token"));
						} else {
							logger.debug("Access Token: " + response.getString("access_token"));
							logger.debug("Access Token Expiration Seconds: " + response.getInt("expires_in"));
							
							System.out.println("Access Token: " + response.getString("access_token"));
							System.out.println("Access Token Expiration Seconds: " + response.getInt("expires_in"));
						}
					} else {
						logger.error("Error implementing authorizeTokens(), response is null");
					}
				} else if (argsList.contains(OTP_KEY_GENERATE_OAUTH2_STRING)) {
					String[] requiredOptions = new String[2];
					requiredOptions[0] = OTP_KEY_USER;
					requiredOptions[1] = OTP_KEY_ACCESS_TOKEN;
					
					logger.debug("Is " + OTP_KEY_USER + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_USER).isRequired());
					logger.debug("Is " + OTP_KEY_ACCESS_TOKEN + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_ACCESS_TOKEN).isRequired());
					
					objOAuth2.requireOptions(options, requiredOptions);
					
					logger.debug("Is " + OTP_KEY_USER + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_USER).isRequired());
					logger.debug("Is " + OTP_KEY_ACCESS_TOKEN + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_ACCESS_TOKEN).isRequired());
					
					command = parser.parse(options, args, false);
					
					String userValue = command.getOptionValue(OTP_KEY_USER);
					String accessTokenValue = command.getOptionValue(OTP_KEY_ACCESS_TOKEN);

					logger.debug("userValue: " + userValue);
					logger.debug("accessTokenValue: " + accessTokenValue);
					
					String oauth2String = objOAuth2.generateOAuth2String(userValue, accessTokenValue);
					
					if (argsList.contains("--" + OTP_KEY_QUIET)) {
						logger.debug(oauth2String);
						System.out.println(oauth2String);
					} else {
						logger.debug("OAuth2 argument:\n" + oauth2String);
						System.out.println("OAuth2 argument:\n" + oauth2String);
					}
				} else if (argsList.contains(OTP_KEY_GENERATE_OAUTH2_TOKEN)) {
					String[] requiredOptions = new String[2];
					requiredOptions[0] = OTP_KEY_CLIENT_ID;
					requiredOptions[1] = OTP_KEY_CLIENT_SECRET;

					logger.debug("Is " + OTP_KEY_CLIENT_ID + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_CLIENT_ID).isRequired());
					logger.debug("Is " + OTP_KEY_CLIENT_SECRET + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_CLIENT_SECRET).isRequired());

					objOAuth2.requireOptions(options, requiredOptions);

					logger.debug("Is " + OTP_KEY_CLIENT_ID + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_CLIENT_ID).isRequired());
					logger.debug("Is " + OTP_KEY_CLIENT_SECRET + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_CLIENT_SECRET).isRequired());

					command = parser.parse(options, args, false);
					
					String clientIdValue = command.getOptionValue(OTP_KEY_CLIENT_ID);
					String clientSecretValue = command.getOptionValue(OTP_KEY_CLIENT_SECRET);
					String scopeValue = command.getOptionValue(OTP_KEY_SCOPE);

					logger.debug("clientIdValue: " + clientIdValue);
					logger.debug("clientSecretValue: " + clientSecretValue);
					logger.debug("scopeValue: " + scopeValue);
					
					String permissionUrl = objOAuth2.generatePermissionUrl(clientIdValue, scopeValue);

					logger.debug("To authorize token, visit this url and follow the directions: ");
					logger.debug(permissionUrl);
					logger.debug("Enter verification code: ");
					
					System.out.println("To authorize token, visit this url and follow the directions: ");
					System.out.println(permissionUrl);
					System.out.println("Enter verification code: ");

					Scanner verificationCodeScanner = new Scanner(System.in);
					String verificationCode = verificationCodeScanner.nextLine();

					logger.debug("verificationCode input: " + verificationCode);
					
					JSONObject response = objOAuth2.authorizeTokens(clientIdValue, clientSecretValue, verificationCode);
					
					if (response != null) {
						logger.debug("Refresh Token: " + response.getString("refresh_token"));
						logger.debug("Access Token: " + response.getString("access_token"));
						logger.debug("Access Token Expiration Seconds: " + response.getInt("expires_in"));
						
						System.out.println("Refresh Token: " + response.getString("refresh_token"));
						System.out.println("Access Token: " + response.getString("access_token"));
						System.out.println("Access Token Expiration Seconds: " + response.getInt("expires_in"));
					} else {
						logger.error("Error implementing authorizeTokens(), response is null");
					}

					verificationCodeScanner.close();
				} else if (argsList.contains(OTP_KEY_TEST_IMAP_AUTH)) {
					String[] requiredOptions = new String[2];
					requiredOptions[0] = OTP_KEY_USER;
					requiredOptions[1] = OTP_KEY_ACCESS_TOKEN;

					logger.debug("Is " + OTP_KEY_USER + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_USER).isRequired());
					logger.debug("Is " + OTP_KEY_ACCESS_TOKEN + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_ACCESS_TOKEN).isRequired());

					objOAuth2.requireOptions(options, requiredOptions);

					logger.debug("Is " + OTP_KEY_USER + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_USER).isRequired());
					logger.debug("Is " + OTP_KEY_ACCESS_TOKEN + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_ACCESS_TOKEN).isRequired());

					command = parser.parse(options, args, false);
					
					String user = command.getOptionValue(OTP_KEY_USER);
					String accessToken = command.getOptionValue(OTP_KEY_ACCESS_TOKEN);

					logger.debug("user: " + user);
					logger.debug("accessToken: " + accessToken);
					
					objOAuth2.testImapAuthentication(objOAuth2.generateOAuth2String(user, accessToken, true));
				} else if (argsList.contains(OTP_KEY_TEST_SMTP_AUTH)) {
					String[] requiredOptions = new String[2];
					requiredOptions[0] = OTP_KEY_USER;
					requiredOptions[1] = OTP_KEY_ACCESS_TOKEN;

					logger.debug("Is " + OTP_KEY_USER + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_USER).isRequired());
					logger.debug("Is " + OTP_KEY_ACCESS_TOKEN + " required before executing RequireOptions(...) method: " + options.getOption(OTP_KEY_ACCESS_TOKEN).isRequired());

					objOAuth2.requireOptions(options, requiredOptions);

					logger.debug("Is " + OTP_KEY_USER + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_USER).isRequired());
					logger.debug("Is " + OTP_KEY_ACCESS_TOKEN + " required after executing RequireOptions(...) method: " + options.getOption(OTP_KEY_ACCESS_TOKEN).isRequired());

					command = parser.parse(options, args, false);
					
					String user = command.getOptionValue(OTP_KEY_USER);
					String accessToken = command.getOptionValue(OTP_KEY_ACCESS_TOKEN);

					logger.debug("user: " + user);
					logger.debug("accessToken: " + accessToken);
					
					objOAuth2.testSmtpAuthentication(objOAuth2.generateOAuth2String(user, accessToken, true));
				} else {
					if (formatter == null) {
						formatter = new HelpFormatter();
					}

					formatter.printHelp(HELP_TEXT_MAX_CHAR_PER_LINE, HELP_TEXT, HELP_HEADER, options, HELP_FOOTER);

					logger.debug("\n\nNothing to do, exiting.");
					
					System.out.println("\n\nNothing to do, exiting.");
				}
			} catch (ParseException e) {
				logger.error(Util.stackTraceToString(e));
				e.printStackTrace();
			} catch (SocketException e) {
				logger.error(Util.stackTraceToString(e));
				e.printStackTrace();
			} catch (IOException e) {
				logger.error(Util.stackTraceToString(e));
				e.printStackTrace();
			} catch (Exception e) {
				logger.error(Util.stackTraceToString(e));
				e.printStackTrace();
			}
		} else {
			System.out.println("Missing options\n");
			logger.error("Missing options\n");

			if (formatter == null) {
				formatter = new HelpFormatter();
			}

			formatter.printHelp(HELP_TEXT_MAX_CHAR_PER_LINE, HELP_TEXT, HELP_HEADER, options, HELP_FOOTER);
		}
	}

}
