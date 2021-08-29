package org.albedu.oauth2.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;



public class CutAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextComponent textComponent;

    public CutAction(JTextComponent textComponent) {
		super("Cut");
    	this.textComponent = textComponent;
    }
    
	@Override
	public void actionPerformed(ActionEvent arg0) {
        this.textComponent.cut();
	}

	public void setTextComponent(JTextComponent textComponent) {
		this.textComponent = textComponent;
	}
}
