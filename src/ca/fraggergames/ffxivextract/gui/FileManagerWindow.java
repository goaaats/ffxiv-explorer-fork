package ca.fraggergames.ffxivextract.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.gui.components.EXDF_View;
import ca.fraggergames.ffxivextract.gui.components.Hex_View;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.models.EXDF_File;
import ca.fraggergames.ffxivextract.models.SCD_File;
import ca.fraggergames.ffxivextract.models.SqPack_DatFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;

@SuppressWarnings("serial")
public class FileManagerWindow extends JFrame implements TreeSelectionListener {
	
	JMenuBar menu = new JMenuBar();
	
	//FILE IO
	File lastOpenedFile = null;
	SqPack_IndexFile currentIndexFile;
	SqPack_DatFile currentDatFile;
	
	//UI
	ExplorerPanel fileTree = new ExplorerPanel();	
	JSplitPane splitPane;
	Hex_View hexView = new Hex_View(32);

	//MENU
	JMenuItem file_Extract;
	JMenuItem file_ExtractRaw;
	JMenuItem file_Close;
	
	public FileManagerWindow(String title)
	{		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			                           fileTree, hexView);
		
		splitPane.setDividerLocation(150);

		//Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(100, 50);
		fileTree.setMinimumSize(minimumSize);
		//pictureScrollPane.setMinimumSize(minimumSize);
		
		fileTree.addTreeSelectionListener(this);
		
		setupMenu();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(900, 600);
		this.setTitle(title);
		ClassLoader cldr = this.getClass().getClassLoader();
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		this.getContentPane().add(splitPane);	
		setLocationRelativeTo(null);	
		
		//Check Windows registry for a FFXIV folder
		String value = null;
		/*try {
			value = WinRegistry.readString (
				    WinRegistry.HKEY_LOCAL_MACHINE,                             
				   "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",           
				   "ProductName");
		} catch (Exception e){}
		*/
		if (Constants.DEBUG){
			lastOpenedFile = new File("F:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\0a0000.win32.index");
			openFile(lastOpenedFile);
		}
	}	

	protected void openFile(File selectedFile) {
		
		if (currentIndexFile != null || currentDatFile != null)
			closeFile();
		
		try {
			currentIndexFile = new SqPack_IndexFile(selectedFile.getAbsolutePath());			
			currentDatFile = new SqPack_DatFile(selectedFile.getAbsolutePath().replace(".index", ".dat0"));
		} catch (Exception e) {
			return;
		}
		
		if (Constants.DEBUG)			
			currentIndexFile.displayIndexInfo();
		
		fileTree.fileOpened(currentIndexFile);
		file_Close.setEnabled(true);
	}

	protected void closeFile() {
		
		if (currentDatFile == null || currentIndexFile == null)
			return;
		
		fileTree.fileClosed();
		try {
			currentDatFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		currentIndexFile = null;
		currentDatFile = null;
		hexView.setBytes(null);
		splitPane.setRightComponent(hexView);
		file_Close.setEnabled(false);
	}
	
	ActionListener menuHandler = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getActionCommand().equals("open"))
			{
				JFileChooser fileChooser = new JFileChooser(lastOpenedFile);
				FileFilter filter = new FileFilter() {
					
					@Override
					public String getDescription() {
						return "FFXIV Index File (.index)";
					}
					
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".index") || f.isDirectory();
					}				
				};
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.setFileFilter(filter);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int retunval = fileChooser.showOpenDialog(FileManagerWindow.this);
				if (retunval == JFileChooser.APPROVE_OPTION)
				{
					lastOpenedFile = fileChooser.getSelectedFile();
					openFile(fileChooser.getSelectedFile());
				}
			}
			else if (event.getActionCommand().equals("close"))
			{
				closeFile();				
			}
			else if (event.getActionCommand().equals("extractc"))
			{
				extractConverted();
			}
			else if (event.getActionCommand().equals("extractr"))
			{
				extractRAW();
			}
			else if (event.getActionCommand().equals("quit"))
			{
				System.exit(0);
			}
			else if (event.getActionCommand().equals("about"))
			{
				AboutWindow aboutWindow = new AboutWindow();
				aboutWindow.setLocationRelativeTo(FileManagerWindow.this);
				aboutWindow.setVisible(true);
			}
		}		
	};

	private void setupMenu(){		
		
		//File Menu
		JMenu file = new JMenu("File");
		JMenu help = new JMenu("Help");
		JMenuItem file_Open = new JMenuItem("Open");
		file_Open.setActionCommand("open");
		file_Close = new JMenuItem("Close");
		file_Close.setEnabled(false);
		file_Close.setActionCommand("close");
		file_Extract = new JMenuItem("Extract");
		file_Extract.setEnabled(false);
		file_ExtractRaw = new JMenuItem("Extract Raw Dat");
		file_ExtractRaw.setEnabled(false);
		file_Extract.setActionCommand("extractc");
		file_ExtractRaw.setActionCommand("extractr");
		JMenuItem file_Quit = new JMenuItem("Quit");
		file_Quit.setActionCommand("quit");
		file_Open.addActionListener(menuHandler);
		file_Close.addActionListener(menuHandler);
		file_Extract.addActionListener(menuHandler);
		file_ExtractRaw.addActionListener(menuHandler);
		file_Quit.addActionListener(menuHandler);
		
		JMenuItem help_About = new JMenuItem("About");
		help_About.setActionCommand("about");
		help_About.addActionListener(menuHandler);
		
		file.add(file_Open);
		file.add(file_Close);
		file.addSeparator();
		file.add(file_Extract);
		file.add(file_ExtractRaw);
		file.addSeparator();
		file.add(file_Quit);	
		
		help.add(help_About);
		
		//Super Menus
		menu.add(file);
		menu.add(help);
		
		this.setJMenuBar(menu);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		if (fileTree.getSelectedFiles().size() == 0)
		{
			file_Extract.setEnabled(false);
			file_ExtractRaw.setEnabled(false);
			return;
		}
		else
		{
			file_Extract.setEnabled(true);
			file_ExtractRaw.setEnabled(true);
		}
		
		try {
			byte[] data = currentDatFile.extractFile(fileTree.getSelectedFiles().get(0).getOffset());
			
			if (data[0] == 'E' && data[1] == 'X' && data[2] == 'D' && data[3] == 'F')
			{
				EXDF_View exdfComponent = new EXDF_View(new EXDF_File(data));
				splitPane.setRightComponent(exdfComponent);
			}
			else				
			{
				splitPane.setRightComponent(hexView);
				hexView.setBytes(data);				
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}	
	
	private void extractRAW() {
		JFileChooser fileChooser = new JFileChooser(lastOpenedFile);
		
		ArrayList<SqPack_File> files = fileTree.getSelectedFiles();
		
		if (files.size() > 1)
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		else
		{			
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);		
			fileChooser.setSelectedFile(new File(String.format("%X", files.get(0).getId() & 0xFFFFFFFF)));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("FFXIV Raw Dat (.dat, .exd, .exh, .scd)", new String[] {".dat",".exd",".exh",".scd"});
			fileChooser.addChoosableFileFilter(filter);
			fileChooser.setFileFilter(filter);
			fileChooser.setAcceptAllFileFilterUsed(false);
		}
	
		int retunval = fileChooser.showSaveDialog(FileManagerWindow.this);
		if (retunval == JFileChooser.APPROVE_OPTION)
		{
			lastOpenedFile = fileChooser.getSelectedFile();
			lastOpenedFile.getParentFile().mkdirs();
			
			
				for (int i = 0; i < files.size(); i++){
					try {
						byte[] data = currentDatFile.extractFile(files.get(i).getOffset());
						String extension = getExtension(data);
						
						String path = lastOpenedFile.getCanonicalPath();
						if (files.size() > 1)
							path = lastOpenedFile.getCanonicalPath() + "\\" + String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
						
						LERandomAccessFile out = new LERandomAccessFile(path + extension, "rw");
						out.write(data, 0, data.length);
						out.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		}
	}
	
	private void extractConverted() {
		JFileChooser fileChooser = new JFileChooser(lastOpenedFile);
		
		ArrayList<SqPack_File> files = fileTree.getSelectedFiles();
		
		if (files.size() > 1)
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		else
		{			
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);		
			fileChooser.setSelectedFile(new File(String.format("%X", files.get(0).getId() & 0xFFFFFFFF)));			
			FileFilter filter = new FileFilter() {
				
				@Override
				public String getDescription() {
					return "FFXIV Converted (.csv, .ogg)";
				}
				
				@Override
				public boolean accept(File f) {
					return f.getName().endsWith(".csv") || f.getName().endsWith(".ogg") || f.isDirectory();
				}				
			};
			fileChooser.addChoosableFileFilter(filter);
			fileChooser.setFileFilter(filter);
			fileChooser.setAcceptAllFileFilterUsed(false);
		}
	
		int retunval = fileChooser.showSaveDialog(FileManagerWindow.this);
		if (retunval == JFileChooser.APPROVE_OPTION)
		{
			lastOpenedFile = fileChooser.getSelectedFile();
			lastOpenedFile.getParentFile().mkdirs();
			
			
				for (int i = 0; i < files.size(); i++){
					try {
						byte[] data = currentDatFile.extractFile(files.get(i).getOffset());
						byte[] dataToSave;
						String extension = getExtension(data);
						
						if (extension.equals(".exd"))
						{
							EXDF_File file = new EXDF_File(data);
							dataToSave = file.getCSV().getBytes();
							extension = ".csv";
						}
						else if (extension.equals(".scd"))
						{
							SCD_File file = new SCD_File(data);
							dataToSave = file.getData();
							extension = ".ogg";
						}
						else
						{
							dataToSave = data;
						}
						
						if (dataToSave == null)
						{
							JOptionPane.showMessageDialog(this,
									String.format("%X", files.get(i).getId() & 0xFFFFFFFF) + " could not be converted to " + extension.substring(1).toUpperCase() + ".",
								    "Export Error",
								    JOptionPane.ERROR_MESSAGE);
							continue;
						}
						
						String path = lastOpenedFile.getCanonicalPath();
						if (files.size() > 1)
							path = lastOpenedFile.getCanonicalPath() + "\\" + String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
						
						LERandomAccessFile out = new LERandomAccessFile(path + extension, "rw");
						out.write(dataToSave, 0, dataToSave.length);
						out.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			
		}
	}

	private String getExtension(byte[] data) {
		if (data[0] == 'E' && data[1] == 'X' && data[2] == 'D' && data[3] == 'F')
			return ".exd";
		else if (data[0] == 'E' && data[1] == 'X' && data[2] == 'H' && data[3] == 'F')
			return ".exh";
		else if (data[1] == 'L' && data[2] == 'u' && data[3] == 'a' && data[4] == 'Q' )
			return ".luab";
		else if (data[0] == 'S' && data[1] == 'E' && data[2] == 'D' && data[3] == 'B' )
			return ".scd";
		else
			return ".dat";
	}

}
