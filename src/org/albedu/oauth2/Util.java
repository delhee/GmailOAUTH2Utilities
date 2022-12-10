package org.albedu.oauth2;

import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class Util {
	public static final String stackTraceToString(Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);

		return stringWriter.toString();
	}

	public static final JSONObject urlOpen(String requestUrl, HashMap<String, String> params) throws Exception {
		URL url = null;
		JSONObject response = null;

		try {
			url = new URL(requestUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

			writer.write(getPostDataString(params));

			writer.flush();

			writer.close();
			os.close();

			int responseCode = conn.getResponseCode();
			String line = null;
			BufferedReader br = null;
			String strResponse = null;

			if (responseCode == HttpsURLConnection.HTTP_OK) {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				while ((line = br.readLine()) != null) {
					strResponse += line;
				}

				response = new JSONObject(strResponse.substring(strResponse.indexOf("{")));
			} else {
				br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

				while ((line = br.readLine()) != null) {
					strResponse += line;
				}

				throw new Exception("There is error in response. Response Code: " + responseCode + ", Response Message: " + strResponse);
			}

			br.close();
		} catch (Exception e) {
			throw e;
		}

		return response;
	}

	public static Map<String, String> queryToMap(String query) {
		if (query == null) {
			return new HashMap<String, String>();
		}
				
		Map<String, String> result = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			} else {
				result.put(entry[0], "");
			}
		}
		
		return result;
	}
	
	private static final String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	public static void copyToClipboard(String copiedText) {
		StringSelection selection = new StringSelection(copiedText);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}

	public static void setFocusTraversalKeys(JTextArea textArea) {
		Set<KeyStroke> strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB")));
		textArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);

		strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB")));
		textArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, strokes);

	}

	public static void registerEnterKeyAction(JButton button) {
		button.registerKeyboardAction(button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false)), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);

		button.registerKeyboardAction(button.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true)), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);
	}

	public static void buttonClickDelay(Logger logger) {
		try {
			Thread.sleep(120);
		} catch (InterruptedException ex) {
			logger.error(Util.stackTraceToString(ex));
			ex.printStackTrace();
		}
	}

	public static Properties loadProperties(String configFile) throws IOException {
		Properties properties = null;
		File file = new File(configFile);

		if (file.exists()) {
			InputStream inputStream = new FileInputStream(file);
			properties = new Properties();
			properties.load(inputStream);
			inputStream.close();
		}

		return properties;
	}
}
