package org.albedu.oauth2.ui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.albedu.oauth2.Util;
import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;

public class Base64ToTextPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(GenerateOAUTH2StringPanel.class);
	
	private JTextArea taBase64String;
	private JTextArea taTextValue;
	private JButton btnCopyText;
	private JButton btnContinue;
	
	public Base64ToTextPanel() {
		setLayout(null);
		
		JLabel lBase64String = new JLabel("Base64 String:");
		lBase64String.setBounds(50, 20, 130, 40);

		this.taBase64String = new JTextArea();
		this.taBase64String.addMouseListener(new ContextMenuMouseListener(this.taBase64String));
		Util.setFocusTraversalKeys(this.taBase64String);

		JScrollPane scrollBase64String = new JScrollPane(this.taBase64String);
		scrollBase64String.setBounds(145, 20, 280, 40);
		
		JLabel lText = new JLabel("Text:");
		lText.setBounds(50, 80, 130, 40);

		this.taTextValue = new JTextArea();
		this.taTextValue.setLineWrap(true);
		this.taTextValue.setWrapStyleWord(true);
		this.taTextValue.setOpaque(false);
		this.taTextValue.setEditable(false);

		JScrollPane scrollTextValue = new JScrollPane(this.taTextValue);
		scrollTextValue.setBounds(145, 80, 330, 240);
		
		this.btnCopyText = new JButton("Copy");
		this.btnCopyText.setBounds(495, 80, 80, 40);
		this.btnCopyText.addActionListener(this);
		this.btnCopyText.setEnabled(true);
		Util.registerEnterKeyAction(this.btnCopyText);

		this.btnContinue = new JButton("Convert Base64 String to Text");
		this.btnContinue.setBounds(50, 360, 215, 40);
		this.btnContinue.addActionListener(this);
		Util.registerEnterKeyAction(this.btnContinue);
		
		add(lBase64String);
		add(scrollBase64String);
		add(lText);
		add(scrollTextValue);
		add(this.btnCopyText);
		add(this.btnContinue);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Util.buttonClickDelay(logger);
		
		if (arg0.getSource().equals(this.btnContinue)) {
			logger.info("btnContinue clicked. ");
			
			this.taTextValue.setText(null);

			String base64String = this.taBase64String.getText();

			logger.debug("base64String: " + base64String);
			
			byte[] textByte = Base64.decodeBase64(base64String);
			String text = new String(textByte);
			
			logger.debug("text: " + text);
			
			this.taTextValue.setText(text);
		} else if (arg0.getSource().equals(this.btnCopyText)) {
			logger.info("btnCopyText clicked. ");
			
			String textValue = this.taTextValue.getText();
			
			if (textValue != null) {
				textValue = textValue.trim();
			}
			
			if(textValue != null && !textValue.isEmpty()) {
				Util.copyToClipboard(textValue);
			}
		}
	}

	public String getBase64String() {
		return taBase64String.getText().trim();
	}

	public void setTaBase64String(String base64String) {
		this.taBase64String.setText(base64String);
	}
}