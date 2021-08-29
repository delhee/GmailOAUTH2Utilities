package org.albedu.oauth2.ui.views;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.albedu.oauth2.ui.actions.CopyAction;
import org.albedu.oauth2.ui.actions.CutAction;
import org.albedu.oauth2.ui.actions.PasteAction;
import org.albedu.oauth2.ui.actions.RedoAction;
import org.albedu.oauth2.ui.actions.SelectAllAction;
import org.albedu.oauth2.ui.actions.UndoAction;

public class ContextMenuMouseListener extends MouseAdapter implements UndoableEditListener {
    private JPopupMenu contextMenu = new JPopupMenu();

    private CutAction cutAction;
    private CopyAction copyAction;
    private PasteAction pasteAction;
    private UndoAction undoAction;
    private RedoAction redoAction;
    private SelectAllAction selectAllAction;

    private JTextComponent textComponent;
    private UndoManager undoManager;

    public ContextMenuMouseListener(JTextComponent textComponent) {
    	this.textComponent = textComponent;
    	this.undoManager = new UndoManager();
    	
    	this.textComponent.getDocument().addUndoableEditListener(this);
    	
    	this.undoAction = new UndoAction(this.undoManager);
    	this.textComponent.getActionMap().put("Undo", this.undoAction);
    	this.textComponent.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
    	this.contextMenu.add(this.undoAction);
    	
    	this.redoAction = new RedoAction(this.undoManager);
    	this.textComponent.getActionMap().put("Redo", this.redoAction);
    	this.textComponent.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    	this.contextMenu.add(this.redoAction);
    	
    	this.contextMenu.addSeparator();

        this.cutAction = new CutAction(this.textComponent);
        this.contextMenu.add(this.cutAction);

        this.copyAction = new CopyAction(this.textComponent);
        this.contextMenu.add(this.copyAction);

        this.pasteAction = new PasteAction(this.textComponent);
        this.contextMenu.add(this.pasteAction);
        
        this.contextMenu.addSeparator();

        this.selectAllAction = new SelectAllAction(this.textComponent);
        this.contextMenu.add(selectAllAction);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            if (!(e.getSource() instanceof JTextComponent)) {
                return;
            }
            
            if(!this.textComponent.isFocusOwner()) {
            	this.textComponent.setCaretPosition(this.textComponent.getText().length());
            	this.textComponent.requestFocus();
            }

            boolean enabled = this.textComponent.isEnabled();
            boolean editable = this.textComponent.isEditable();
            boolean notEmpty = (this.textComponent.getText() != null && !this.textComponent.getText().equals(""));
            boolean textSelected = this.textComponent.getSelectedText() != null;

            boolean pasteAvailable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);

            this.undoAction.setEnabled(enabled && editable && this.undoManager.canUndo());
            this.redoAction.setEnabled(enabled && editable && this.undoManager.canRedo());
            this.cutAction.setEnabled(enabled && editable && textSelected);
            this.copyAction.setEnabled(enabled && textSelected);
            this.pasteAction.setEnabled(enabled && editable && pasteAvailable);
            this.selectAllAction.setEnabled(enabled && notEmpty);

            int contextMenuDisplayPosition = e.getX();

            if (contextMenuDisplayPosition > 500) {
            	contextMenuDisplayPosition = contextMenuDisplayPosition - this.contextMenu.getSize().width;
            }

            this.contextMenu.show(e.getComponent(), contextMenuDisplayPosition, e.getY() - this.contextMenu.getSize().height);
        }
    }

	@Override
	public void undoableEditHappened(UndoableEditEvent arg0) {
		this.undoManager.addEdit(arg0.getEdit());
	}
}
