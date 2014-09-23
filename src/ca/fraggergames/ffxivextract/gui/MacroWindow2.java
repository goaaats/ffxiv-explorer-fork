package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;

public class MacroWindow2 extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Create the frame.
	 */
	public MacroWindow2() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel pnlFileSelect = new JPanel();
		pnlFileSelect.setBorder(BorderFactory
						.createTitledBorder("Macro File"));
		contentPane.add(pnlFileSelect, BorderLayout.NORTH);
		
		JLabel label = new JLabel("Path to macro file: ");
		pnlFileSelect.add(label);
		
		textField = new JTextField();
		textField.setText("Point to your MACRO.DAT file");
		textField.setPreferredSize(new Dimension(200, 20));
		textField.setEditable(false);
		pnlFileSelect.add(textField);
		
		JButton button = new JButton("Browse");
		pnlFileSelect.add(button);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel label_1 = new JLabel("Macro: ");
		panel.add(label_1);
		label_1.setEnabled(false);
		
		JComboBox comboBox = new JComboBox();
		panel.add(comboBox);
		comboBox.setEnabled(false);
		
		JLabel label_2 = new JLabel("Icon: ");
		panel.add(label_2);
		label_2.setEnabled(false);
		
		JComboBox comboBox_1 = new JComboBox();
		panel.add(comboBox_1);
		comboBox_1.setEnabled(false);
		
		textField_1 = new JTextField();
		contentPane.add(textField_1, BorderLayout.SOUTH);
		textField_1.setEnabled(false);
	}

}
