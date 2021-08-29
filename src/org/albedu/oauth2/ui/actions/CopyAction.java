package org.albedu.oauth2.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

public class CopyAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextComponent textComponent;

    public CopyAction(JTextComponent textComponent) {
		super("Copy");
    	this.textComponent = textComponent;
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
        this.textComponent.copy();
	}

	public void setTextComponent(JTextComponent textComponent) {
		this.textComponent = textComponent;
	}
}
