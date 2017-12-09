package io.github.ayakkus.xmld;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * The window that appears when the program is loaded.
 * Possibly merge into Launch.java?
 * @author Jack Stevenson (A-Yakkus)
 *
 */
public class OptionWindow {

	public Image logo = new ImageIcon(getClass().getResource("res/logo.png")).getImage();
	public String file = "";
	public static List<String> list = new ArrayList<String>();

	/**
	 * This is a Constructor
	 */
	public OptionWindow(){
		JFrame frm=new JFrame("XML Documenter");
		frm.setSize(640, 200);
		frm.setLocationRelativeTo(null);
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frm.setIconImage(logo);
		frm.setResizable(false);
		JPanel pnl = new JPanel();
		pnl.setBackground(new Color(0x795548));
		JButton btn1 = new JButton("How to use");
		btn1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				JOptionPane.showMessageDialog(null, "Select a folder using the choose folder button.\nThen select a language from the dropdown to get the right parser.\nType a custom name for the output file\n(N.B. This is also the name used for the root element)\nThen press the create button to create the documentation.");
			}
		});
		JTextField tf1 = new JTextField(55);
		tf1.setEditable(false);
		JButton btn = new JButton("Choose Folder....");
		btn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fc=new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setCurrentDirectory(new File("."));
				fc.setDialogTitle("Select Folder");
				fc.setAcceptAllFileFilterUsed(false);
				if(fc.showOpenDialog(frm)==JFileChooser.APPROVE_OPTION){
					tf1.setText(fc.getSelectedFile().getAbsolutePath()+"\\");
				}
			}
		});
		JComboBox<String> lang = new JComboBox<String>();
		lang.addItem("Java");
		//lang.addItem("C#");
		//lang.addItem("C/C++");		
		JTextField out=new JTextField(10);
		JLabel lbl1 = new JLabel("Output File");
		JButton okBtn = new JButton("Create Documentation");
		okBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed (ActionEvent arg0){
				list=new ArrayList<String>();
				XMLLogic.create(searchForFiles(new File(tf1.getText()),getTypesForLang(lang.getSelectedItem().toString().toLowerCase())), lang.getSelectedItem().toString().toLowerCase(), out.getText(), tf1.getText());
			}
		});
		
		pnl.add(btn1);
		pnl.add(btn);
		pnl.add(tf1);
		pnl.add(lang);
		pnl.add(lbl1);
		pnl.add(out);
		pnl.add(okBtn);
		frm.add(pnl);
		frm.setVisible(true);
	}
	
	public static List<String> searchForFiles (File f, String... types) {
		for(String type:types)
		if(f.isDirectory()){
			for(int i=0; i<f.listFiles().length;i++)
			{
				searchForFiles(f.listFiles()[i], type);
			}
		}
		else if(f.isFile()){
			if(f.getName().contains(type)&&!list.contains(f.getName())){
				list.add(f.getPath());
			}
		}
		return list;	
	}
	
	public static String[] getTypesForLang (String lang) {
		switch(lang){
		case "java":return new String[]{".java"};
		case "c#":return new String[]{".cs"};
		case "c/c++":return new String[]{".c",".h",".cpp"};
		default:return new String[]{""};
		}
	}
	
}
