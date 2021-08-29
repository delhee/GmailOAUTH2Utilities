package org.albedu.oauth2.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

public class SelectAllAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    private JTextComponent textComponent;
    
    public SelectAllAction(JTextComponent textComponent) {
    	super("Select All");
    	this.textComponent = textComponent;
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		this.textComponent.selectAll();
	}

	public JTextComponent getTextComponent() {
		return textComponent;
	}

	public void setTextComponent(JTextComponent textComponent) {
		this.textComponent = textComponent;
	}
}
