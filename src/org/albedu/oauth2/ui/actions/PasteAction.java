package org.albedu.oauth2.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

public class PasteAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private JTextComponent textComponent;

	public PasteAction(JTextComponent textComponent) {
		super("Paste");
    	this.textComponent = textComponent;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		this.textComponent.paste();
	}

	public JTextComponent getTextComponent() {
		return this.textComponent;
	}

	public void setTextComponent(JTextComponent textComponent) {
		this.textComponent = textComponent;
	}
	
	
}
