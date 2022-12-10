package org.albedu.oauth2.ui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.albedu.oauth2.Util;
import org.albedu.oauth2.ui.tasks.RedirectURIChangeListener;
import org.apache.log4j.Logger;

public class SettingsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(SettingsPanel.class);

	private JTextArea taVerificationCodeRedirectURI;
	private JButton btnApplyRedireceURI;

	private RedirectURIChangeListener redirectURIChangeListener;

	public SettingsPanel(RedirectURIChangeListener redirectURIChangeListener) {
		this.redirectURIChangeListener = redirectURIChangeListener;

		setLayout(null);

		JLabel lVerificationCodeRedirectURI = new JLabel("<html>Verification Code<br>Redirect URI</html>:");
		lVerificationCodeRedirectURI.setBounds(50, 20, 130, 40);

		this.taVerificationCodeRedirectURI = new JTextArea();
		this.taVerificationCodeRedirectURI.addMouseListener(new ContextMenuMouseListener(this.taVerificationCodeRedirectURI));
		// this.taUser.getDocument().addDocumentListener(this);
		Util.setFocusTraversalKeys(this.taVerificationCodeRedirectURI);

		JScrollPane scrollVerificationCodeRedirectURI = new JScrollPane(this.taVerificationCodeRedirectURI);
		scrollVerificationCodeRedirectURI.setBounds(175, 20, 250, 40);

		this.btnApplyRedireceURI = new JButton("Apply");
		this.btnApplyRedireceURI.setBounds(445, 20, 80, 40);
		this.btnApplyRedireceURI.addActionListener(this);
		this.btnApplyRedireceURI.setEnabled(true);
		Util.registerEnterKeyAction(this.btnApplyRedireceURI);

		add(lVerificationCodeRedirectURI);
		add(scrollVerificationCodeRedirectURI);
		add(this.btnApplyRedireceURI);
	}

	public String getVerificationCodeRedirectURI() {
		return taVerificationCodeRedirectURI.getText().trim();
	}

	public void setVerificationCodeRedirectURI(String verificationCodeRedirectURI) {
		this.taVerificationCodeRedirectURI.setText(verificationCodeRedirectURI);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(this.btnApplyRedireceURI)) {
			String newRedirectURI = getVerificationCodeRedirectURI();

			logger.debug("newRedirectURI: " + newRedirectURI);

			this.redirectURIChangeListener.onRedirectURIChange(newRedirectURI);
		}
	}
}
