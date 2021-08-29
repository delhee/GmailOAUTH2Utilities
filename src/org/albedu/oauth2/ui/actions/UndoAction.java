package org.albedu.oauth2.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.UndoManager;

public class UndoAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    private UndoManager undoManager;

    public UndoAction(UndoManager undoManager) {
    	super("Undo");
    	this.undoManager = undoManager;
    }
    
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (this.undoManager.canUndo()) {
			this.undoManager.undo();
        }
	}
}
