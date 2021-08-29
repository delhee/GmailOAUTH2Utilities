package org.albedu.oauth2.ui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.albedu.oauth2.OAuth2;
import org.albedu.oauth2.Util;
import org.apache.log4j.Logger;

public class GenerateOAUTH2StringPanel extends JPanel implements DocumentListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(GenerateOAUTH2StringPanel.class);
	
	private JTextArea taUser;
	private JTextArea taAccessToken;
	private JLabel lOAUTH2ArgumentValue;
	private JButton btnCopyOAUTH2Argument;
	private JButton btnContinue;
	
	public GenerateOAUTH2StringPanel() {
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
		
		JLabel lOAUTH2Argument = new JLabel("OAUTH2 Argument:");
		lOAUTH2Argument.setBounds(50, 140, 130, 40);

		this.lOAUTH2ArgumentValue = new JLabel();

		JScrollPane scrollOAUTH2Argument = new JScrollPane(this.lOAUTH2ArgumentValue);
		scrollOAUTH2Argument.setBounds(175, 140, 250, 40);
		
		this.btnCopyOAUTH2Argument = new JButton("Copy");
		this.btnCopyOAUTH2Argument.setBounds(445, 140, 80, 40);
		this.btnCopyOAUTH2Argument.addActionListener(this);
		this.btnCopyOAUTH2Argument.setEnabled(true);
		Util.registerEnterKeyAction(this.btnCopyOAUTH2Argument);

		this.btnContinue = new JButton("Generate OAUTH2 Argument");
		this.btnContinue.setBounds(50, 220, 195, 40);
		this.btnContinue.addActionListener(this);
		this.btnContinue.setEnabled(false);
		Util.registerEnterKeyAction(this.btnContinue);
		
		add(lUser);
		add(scrollUser);
		add(lAccessToken);
		add(scrollAccessToken);
		add(lOAUTH2Argument);
		add(scrollOAUTH2Argument);
		add(this.btnCopyOAUTH2Argument);
		add(this.btnContinue);
	}
	
	@Override
	public void changedUpdate(DocumentEvent e) {}

	@Override
	public void insertUpdate(DocumentEvent e) {
		refreshViews();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
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
			
			OAuth2 objOAuth2 = new OAuth2();
			String oauth2String = objOAuth2.generateOAuth2String(user, accessToken);
			
			logger.debug("oauth2String: " + oauth2String);
			
			this.lOAUTH2ArgumentValue.setText(oauth2String);
		} else if (arg0.getSource().equals(this.btnCopyOAUTH2Argument)) {
			logger.info("btnCopyOAUTH2Argument clicked. ");
			
			String oauth2Argument = this.lOAUTH2ArgumentValue.getText();
			
			if (oauth2Argument != null) {
				oauth2Argument = oauth2Argument.trim();
			}
			
			if(oauth2Argument != null && !oauth2Argument.isEmpty()) {
				Util.copyToClipboard(oauth2Argument);
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
