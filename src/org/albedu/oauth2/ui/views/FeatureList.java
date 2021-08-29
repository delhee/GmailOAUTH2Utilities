package org.albedu.oauth2.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

public class FeatureList extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JList<String> jList;

	public FeatureList(String[] featureArray) {
		jList = new JList<String>(featureArray);
		
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setDragEnabled(true);
		jList.setSelectedIndex(0);
		
		setLayout(new BorderLayout());
		setBackground(Color.WHITE);
		add(jList, BorderLayout.NORTH);
	}
	
	public JList<String> getJList() {
		return jList;
	}
}
