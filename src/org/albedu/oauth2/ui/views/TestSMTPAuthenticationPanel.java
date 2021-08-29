package org.albedu.oauth2.ui.views;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.albedu.oauth2.Util;
import org.albedu.oauth2.ui.tasks.BackgroundTaskListener;
import org.albedu.oauth2.ui.tasks.TestSMTPAuthenticationTask;
import org.apache.log4j.Logger;

public class TestSMTPAuthenticationPanel extends JPanel implements DocumentListener, ActionListener, BackgroundTaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(TestIMAPAuthenticationPanel.class);
	
	private JTextArea taUser;
	private JTextArea taAccessToken;
	private JTextArea taTestResultValue;
	private JButton btnContinue;

	public TestSMTPAuthenticationPanel() {
		setLayout(null);
		
		JLabel lUser = new JLabel("User:");
		lUser.setBounds(50, 20, 130, 40);

		this.taUser = new JTextArea();
		this.taUser.addMouseListener(new ContextMenuMouseListener(this.taUser));
		this.taUser.getDocument().addDocumentListener(this);
		Util.setFocusTraversalKeys(this.taUser);

		JScrollPane scrollUser = new JScrollPane(this.taUser);
		scrollUser.setBounds(175, 20, 250, 40);

		JLabel lAccessToken = new JLabel("Access Token:");
		lAccessToken.setBounds(50, 80, 130, 40);

		this.taAccessToken = new JTextArea();
		this.taAccessToken.addMouseListener(new ContextMenuMouseListener(this.taAccessToken));
		this.taAccessToken.getDocument().addDocumentListener(this);
		Util.setFocusTraversalKeys(this.taAccessToken);

		JScrollPane scrollAccessToken = new JScrollPane(this.taAccessToken);
		scrollAccessToken.setBounds(175, 80, 250, 40);
		
		JLabel lTestResult = new JLabel("Test Result:");
		lTestResult.setBounds(50, 140, 130, 40);

		this.taTestResultValue = new JTextArea();
		this.taTestResultValue.addMouseListener(new ContextMenuMouseListener(this.taTestResultValue));
		this.taTestResultValue.setOpaque(false);
		this.taTestResultValue.setEditable(false);
		//this.taTestResultValue.setWrapStyleWord(true);
		//this.taTestResultValue.setLineWrap(true);

		JScrollPane scrollTestResult = new JScrollPane(this.taTestResultValue);
		scrollTestResult.setBounds(175, 140, 400, 240);

		this.btnContinue = new JButton("Test SMTP Authentication");
		this.btnContinue.setBounds(50, 420, 195, 40);
		this.btnContinue.addActionListener(this);
		this.btnContinue.setEnabled(false);
		Util.registerEnterKeyAction(this.btnContinue);
		
		add(lUser);
		add(scrollUser);
		add(lAccessToken);
		add(scrollAccessToken);
		add(lTestResult);
		add(scrollTestResult);
		add(this.btnContinue);
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
		
		if (arg0.getSource().equals(this.btnContinue)) {
			logger.info("btnContinue clicked. ");

			String user = this.taUser.getText();
			String accessToken = this.taAccessToken.getText();

			logger.debug("user: " + user);
			logger.debug("accessToken: " + accessToken);
			
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.taUser.setEnabled(false);
			this.taAccessToken.setEnabled(false);
			this.btnContinue.setEnabled(false);
			this.taTestResultValue.setText(null);
						
			TestSMTPAuthenticationTask testSMTPAuthenticationTask = new TestSMTPAuthenticationTask(this, user, accessToken);
			testSMTPAuthenticationTask.execute();
		}
	}

	@Override
	public void updateUI(Object source, Object output) {
		if (source instanceof TestSMTPAuthenticationTask) {
			if(output instanceof String) {
				String testResult = (String) output;
				this.taTestResultValue.append(testResult);
			} else if (output instanceof Boolean) {
				this.setCursor(Cursor.getDefaultCursor());				
				refreshViews();				
				this.taUser.setEnabled(true);
				this.taAccessToken.setEnabled(true);
			}
		}
	}
	
	private void refreshViews() {
		if (this.taUser != null && this.taAccessToken != null) {
			String user = this.taUser.getText();
			String accessToken = this.taAccessToken.getText();
			
			if (user != null) {
				user = user.trim();
			}
			
			if (accessToken != null) {
				accessToken = accessToken.trim();
			}
			
			//logger.debug("user: " + user);
			//logger.debug("accessToken: " + accessToken);

			if (user != null && !user.isEmpty() && accessToken != null && !accessToken.isEmpty()) {
				this.btnContinue.setEnabled(true);
			} else {
				this.btnContinue.setEnabled(false);
			}
		}
	}

	public String getUser() {
		return this.taUser.getText().trim();
	}

	public void setUser(String user) {
		this.taUser.setText(user);
	}

	public String getAccessToken() {
		return this.taAccessToken.getText().trim();
	}

	public void setAccessToken(String accessToken) {
		this.taAccessToken.setText(accessToken);
	}
}