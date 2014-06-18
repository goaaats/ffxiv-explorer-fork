package ca.fraggergames.ffxivextract;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;

import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.models.SqPack_DatFile;
import ca.fraggergames.ffxivextract.models.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.views.HexView;

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
	HexView hexView = new HexView(16);
	
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
		this.getContentPane().add(splitPane);		
		
		//lastOpenedFile = new File("G:\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\000000.win32.index");
		//openFile(lastOpenedFile);
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
		
		currentIndexFile.displayIndexInfo();
		fileTree.fileOpened(currentIndexFile);
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
	}
	
	ActionListener menuHandler = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getActionCommand().equals("open"))
			{
				JFileChooser fileChooser = new JFileChooser(lastOpenedFile);				
				
				fileChooser.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return null;
					}
					
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".index") || f.isDirectory();
					}				
				});
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
			else if (event.getActionCommand().equals("extract"))
			{
				extractSelected();
			}
			else if (event.getActionCommand().equals("quit"))
			{
				System.exit(0);
			}
		}		
	};

	private void setupMenu(){		
		
		//File Menu
		JMenu file = new JMenu("File");				
		JMenuItem file_Open = new JMenuItem("Open");
		file_Open.setActionCommand("open");
		JMenuItem file_Close = new JMenuItem("Close");
		file_Close.setActionCommand("close");
		JMenuItem file_Extract = new JMenuItem("Extract");
		file_Extract.setActionCommand("extract");
		JMenuItem file_Quit = new JMenuItem("Quit");
		file_Quit.setActionCommand("quit");
		file_Open.addActionListener(menuHandler);
		file_Close.addActionListener(menuHandler);
		file_Extract.addActionListener(menuHandler);
		file_Quit.addActionListener(menuHandler);
		
		file.add(file_Open);
		file.add(file_Close);
		file.add(file_Extract);		
		file.add(file_Quit);	
		
		//Super Menus
		menu.add(file);		
		
		this.setJMenuBar(menu);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		if (fileTree.getSelectedFiles().size() == 0)
			return;
		
		try {
			byte[] data = currentDatFile.extractFile(fileTree.getSelectedFiles().get(0).getOffset());
			
			hexView.setBytes(data);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}	
	
	private void extractSelected() {
		JFileChooser fileChooser = new JFileChooser(lastOpenedFile);
		
		ArrayList<SqPack_File> files = fileTree.getSelectedFiles();
		
		if (files.size() > 1)
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return null;
			}
			
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".index") || f.isDirectory();
			}				
		});
		int retunval = fileChooser.showSaveDialog(FileManagerWindow.this);
		if (retunval == JFileChooser.APPROVE_OPTION)
		{
			lastOpenedFile = fileChooser.getSelectedFile();
			
			if (files.size() == 1)
			{
				try {
					byte[] data = currentDatFile.extractFile(files.get(0).getOffset());
					LERandomAccessFile out = new LERandomAccessFile(lastOpenedFile, "rw");
					out.write(data, 0, data.length);
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
			{
				for (int i = 0; i < files.size(); i++){
					try {
						byte[] data = currentDatFile.extractFile(files.get(i).getOffset());
						LERandomAccessFile out = new LERandomAccessFile(lastOpenedFile.getAbsolutePath() + "\\" + String.format("%X", files.get(i).getId() & 0xFFFFFFFF) + ".dat", "rw");
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
	}

}
