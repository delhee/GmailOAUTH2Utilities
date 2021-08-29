package org.albedu.oauth2.ui.views;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.albedu.oauth2.Util;
import org.apache.log4j.Logger;

public class AuthTokenDialog extends JDialog implements ActionListener {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AuthTokenDialog.class);
	private JButton btnOpenWithBrowser;
	private JButton btnCopyURL;
	private String permissionUrl;

	public AuthTokenDialog(JFrame rootFrame) {
	    super(rootFrame, null, true);
	    
		String dialogMessage = "Permission URL is generated successfully. You may open it with your browser now or copy it to your clipboard to further generate Access Token and Refresh Token. Would you like to proceed?";
		JTextArea taDialogMessage = new JTextArea(dialogMessage);
		taDialogMessage.setBounds(20, 20, 385, 80);
		taDialogMessage.setFont(new Font("Default", Font.PLAIN, 13));
		taDialogMessage.setLineWrap(true);
		taDialogMessage.setWrapStyleWord(true);
		taDialogMessage.setOpaque(false);
		taDialogMessage.setEditable(false);
		
		this.btnOpenWithBrowser = new JButton("Open with Browser");
		btnOpenWithBrowser.setBounds(20, 120, 145, 30);
		btnOpenWithBrowser.addActionListener(this);
		Util.registerEnterKeyAction(this.btnOpenWithBrowser);
		
		this.btnCopyURL = new JButton("Copy URL");
		btnCopyURL.setBounds(185, 120, 100, 30);
		btnCopyURL.addActionListener(this);
		Util.registerEnterKeyAction(this.btnCopyURL);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(305, 120, 100, 30);
		btnCancel.addActionListener(this);
		Util.registerEnterKeyAction(btnCancel);
		
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.add(taDialogMessage);
		panel.add(this.btnOpenWithBrowser);
		panel.add(this.btnCopyURL);
		panel.add(btnCancel);

		add(panel);
		
		try {
			setIconImage(ImageIO.read(new File("resources/images/dev_oauth_badge_20.png")));
		} catch (IOException e) {
			logger.error(Util.stackTraceToString(e));
			e.printStackTrace();
		}
		
		setPreferredSize(new Dimension(445, 210));
		pack();
		setLocationRelativeTo(rootFrame);
	}
		
	public void setPermissionUrl(String permissionUrl) {
		this.permissionUrl = permissionUrl;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Util.buttonClickDelay(logger);
		
		this.setVisible(false);
		
		if (e.getSource().equals(this.btnOpenWithBrowser)) {
			logger.info("btnOpenWithBrowser clicked");
			
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
		        try {
		            desktop.browse(new URL(this.permissionUrl).toURI());
		        } catch (Exception ex) {
					logger.error(Util.stackTraceToString(ex));
		            ex.printStackTrace();
		        }
		    }
		} else if (e.getSource().equals(this.btnCopyURL)) {
			logger.info("btnCopyURL clicked");
			
			Util.copyToClipboard(this.permissionUrl);
		}
	}
}