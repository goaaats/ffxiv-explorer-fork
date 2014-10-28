package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.gui.SearchWindow.ISearchComplete;
import ca.fraggergames.ffxivextract.gui.components.EXDF_View;
import ca.fraggergames.ffxivextract.gui.components.ExplorerPanel_View;
import ca.fraggergames.ffxivextract.gui.components.Hex_View;
import ca.fraggergames.ffxivextract.gui.components.Image_View;
import ca.fraggergames.ffxivextract.gui.components.Loading_Dialog;
import ca.fraggergames.ffxivextract.gui.components.Lua_View;
import ca.fraggergames.ffxivextract.gui.components.Sound_View;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.helpers.LuaDec;
import ca.fraggergames.ffxivextract.helpers.OggVorbisPlayer;
import ca.fraggergames.ffxivextract.models.EXDF_File;
import ca.fraggergames.ffxivextract.models.EXHF_File;
import ca.fraggergames.ffxivextract.models.SCD_File;
import ca.fraggergames.ffxivextract.models.SCD_File.SCD_Sound_Info;
import ca.fraggergames.ffxivextract.models.SqPack_DatFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.Texture_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.storage.CompareFile;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

@SuppressWarnings("serial")
public class FileManagerWindow extends JFrame implements TreeSelectionListener, ISearchComplete, WindowListener {

	//DLLs
	LuaDec luadec;
	
	JMenuBar menu = new JMenuBar();
	
	//FILE IO
	File lastOpenedFile = null;
	SqPack_IndexFile currentIndexFile;
	SqPack_DatFile currentDatFile;
	CompareFile currentCompareFile;
	
	//UI
	SearchWindow searchWindow;
	ExplorerPanel_View fileTree = new ExplorerPanel_View();	
	JSplitPane splitPane;
	JLabel lblOffsetValue;
	JLabel lblHashValue ;
	JLabel lblContentTypeValue;
	Hex_View hexView = new Hex_View(16);
	EXDF_View exhfComponent;
	JProgressBar prgLoadingBar;
	
	//MENU
	JMenuItem file_Extract;
	JMenuItem file_ExtractRaw;
	JMenuItem file_Close;
	JMenuItem search_search;
	JMenuItem search_searchAgain;	
	
	OggVorbisPlayer player;
	
	public FileManagerWindow(String title)
	{	
		addWindowListener(this);

		//Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(100, 50);
		
		setupMenu();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1200, 800);
		this.setTitle(title);
		ClassLoader cldr = this.getClass().getClassLoader();
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		
		JPanel pnlContent = new JPanel();
		getContentPane().add(pnlContent, BorderLayout.CENTER);
		pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.X_AXIS));
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			                           fileTree, hexView);
		pnlContent.add(splitPane);
		
		splitPane.setDividerLocation(150);
		fileTree.setMinimumSize(minimumSize);
		//pictureScrollPane.setMinimumSize(minimumSize);
		
		fileTree.addTreeSelectionListener(this);
		
		JPanel pnlStatusBar = new JPanel();
		getContentPane().add(pnlStatusBar, BorderLayout.SOUTH);
		pnlStatusBar.setLayout(new BorderLayout(0, 0));
		
		JSeparator separator = new JSeparator();
		pnlStatusBar.add(separator, BorderLayout.NORTH);
		
		JPanel pnlInfo = new JPanel();
		FlowLayout fl_pnlInfo = (FlowLayout) pnlInfo.getLayout();
		fl_pnlInfo.setVgap(4);
		pnlInfo.setBorder(null);
		pnlStatusBar.add(pnlInfo, BorderLayout.WEST);
		
		JLabel lblOffset = new JLabel("Offset: ");
		pnlInfo.add(lblOffset);
		lblOffset.setHorizontalAlignment(SwingConstants.LEFT);
		
		lblOffsetValue = new JLabel("*");
		pnlInfo.add(lblOffsetValue);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setPreferredSize(new Dimension(1, 16));
		separator_1.setOrientation(SwingConstants.VERTICAL);
		pnlInfo.add(separator_1);
		
		JLabel lblHash = new JLabel("Hash: ");
		pnlInfo.add(lblHash);
		
		lblHashValue = new JLabel("*");
		pnlInfo.add(lblHashValue);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setPreferredSize(new Dimension(1, 16));
		separator_2.setOrientation(SwingConstants.VERTICAL);
		pnlInfo.add(separator_2);
		
		JLabel lblContentType = new JLabel("Content Type: ");
		pnlInfo.add(lblContentType);
		
		lblContentTypeValue = new JLabel("*");
		pnlInfo.add(lblContentTypeValue);
		
		JPanel pnlProgBar = new JPanel();
		pnlStatusBar.add(pnlProgBar, BorderLayout.EAST);
		
		prgLoadingBar = new JProgressBar();
		prgLoadingBar.setVisible(false);
		pnlProgBar.add(prgLoadingBar);
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
			//lastOpenedFile = new File("E:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\0a0000.win32.index");
			//openFile(lastOpenedFile);
		}
		
		//Init Luadec
		luadec = LuaDec.initLuaDec();		
	}	

	protected void openFile(File selectedFile) {
		
		if (currentIndexFile != null || currentDatFile != null)
			closeFile();
		
		OpenIndexTask openTask = new OpenIndexTask(selectedFile);
		openTask.execute();
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
		try {
			if (currentCompareFile != null)
				currentCompareFile.save();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		currentCompareFile = null;
		
		setTitle(Constants.APPNAME);
		hexView.setBytes(null);
		splitPane.setRightComponent(hexView);
		file_Close.setEnabled(false);
		search_search.setEnabled(false);
		search_searchAgain.setEnabled(false);
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
						return Strings.FILETYPE_FFXIV_INDEX;
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
				extract(true);
			}
			else if (event.getActionCommand().equals("extractr"))
			{
				extract(false);
			}
			else if (event.getActionCommand().equals("search"))
			{
				searchWindow = new SearchWindow(currentIndexFile, currentDatFile, FileManagerWindow.this);
				searchWindow.setLocationRelativeTo(FileManagerWindow.this);
				searchWindow.setVisible(true);
			}
			else if (event.getActionCommand().equals("searchagain"))
			{
				searchWindow.searchAgain();
			}
			else if (event.getActionCommand().equals("musicswapper"))
			{
				MusicSwapperWindow swapper = new MusicSwapperWindow();
				swapper.setLocationRelativeTo(FileManagerWindow.this);
				swapper.setVisible(true);
			}
			else if (event.getActionCommand().equals("macroeditor"))
			{
				MacroEditorWindow macroEditor = new MacroEditorWindow();
				macroEditor.setLocationRelativeTo(FileManagerWindow.this);
				macroEditor.setVisible(true);
			}
			else if (event.getActionCommand().equals("logviewer"))
			{
				LogViewerWindow logViewer = new LogViewerWindow();
				logViewer.setLocationRelativeTo(FileManagerWindow.this);
				logViewer.setVisible(true);
			}
			else if (event.getActionCommand().equals("options_sort"))
			{
				 
			}
			else if (event.getActionCommand().equals("quit"))
			{
				System.exit(0);
			}
			else if (event.getActionCommand().equals("about"))
			{
				AboutWindow aboutWindow = new AboutWindow(FileManagerWindow.this);
				aboutWindow.setLocationRelativeTo(FileManagerWindow.this);
				aboutWindow.setVisible(true);
			}
		}		
	};

	private void setupMenu(){		
		
		//File Menu
		JMenu file = new JMenu(Strings.MENU_FILE);
		JMenu search = new JMenu(Strings.MENU_SEARCH);
		JMenu tools = new JMenu(Strings.MENU_TOOLS);
		JMenu options = new JMenu(Strings.MENU_OPTIONS);
		JMenu help = new JMenu(Strings.MENU_HELP);
		JMenuItem file_Open = new JMenuItem(Strings.MENUITEM_OPEN);
		file_Open.setActionCommand("open");
		file_Close = new JMenuItem(Strings.MENUITEM_CLOSE);
		file_Close.setEnabled(false);
		file_Close.setActionCommand("close");
		file_Extract = new JMenuItem(Strings.MENUITEM_EXTRACT);
		file_Extract.setEnabled(false);
		file_ExtractRaw = new JMenuItem(Strings.MENUITEM_EXTRACTRAW);
		file_ExtractRaw.setEnabled(false);
		file_Extract.setActionCommand("extractc");
		file_ExtractRaw.setActionCommand("extractr");
		JMenuItem file_Quit = new JMenuItem(Strings.MENUITEM_QUIT);
		file_Quit.setActionCommand("quit");
		file_Open.addActionListener(menuHandler);
		file_Close.addActionListener(menuHandler);
		file_Extract.addActionListener(menuHandler);
		file_ExtractRaw.addActionListener(menuHandler);
		file_Quit.addActionListener(menuHandler);
		
		search_search = new JMenuItem(Strings.MENUITEM_SEARCH);
		search_search.setEnabled(false);
		search_search.setActionCommand("search");
		search_search.addActionListener(menuHandler);

		search_searchAgain = new JMenuItem(Strings.MENUITEM_SEARCHAGAIN);
		search_searchAgain.setEnabled(false);
		search_searchAgain.setActionCommand("searchagain");
		search_searchAgain.addActionListener(menuHandler);		
		
		JMenuItem tools_musicswapper = new JMenuItem(Strings.MENUITEM_MUSICSWAPPER);
		tools_musicswapper.setActionCommand("musicswapper");
		tools_musicswapper.addActionListener(menuHandler);
		
		JMenuItem tools_macroEditor = new JMenuItem(Strings.MENUITEM_MACROEDITOR);
		tools_macroEditor.setActionCommand("macroeditor");
		tools_macroEditor.addActionListener(menuHandler);
		
		JMenuItem tools_logViewer = new JMenuItem(Strings.MENUITEM_LOGVIEWER);
		tools_logViewer.setActionCommand("logviewer");
		tools_logViewer.addActionListener(menuHandler);
		
		JMenuItem options_enableSort = new JMenuItem(Strings.MENUITEM_ENABLESORT);
		options_enableSort.setActionCommand("options_sort");
		options_enableSort.addActionListener(menuHandler);
		
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
		
		search.add(search_search);
		search.add(search_searchAgain);
		
		tools.add(tools_musicswapper);
		tools.add(tools_macroEditor);
		tools.add(tools_logViewer);
		
		options.add(options_enableSort);
		
		help.add(help_About);
		
		//Super Menus
		menu.add(file);
		menu.add(search);
		menu.add(tools);
		//menu.add(options);
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

		if (fileTree.getSelectedFiles().size() > 1)
		{
			lblOffsetValue.setText("*");
			lblHashValue.setText("*");
			lblContentTypeValue.setText("*");
		}
		else
		{
			lblOffsetValue.setText(String.format("0x%08X",fileTree.getSelectedFiles().get(0).getOffset()));
			lblHashValue.setText(String.format("0x%08X",fileTree.getSelectedFiles().get(0).getId()));
			try{
				lblContentTypeValue.setText(""+currentDatFile.getContentType(fileTree.getSelectedFiles().get(0).getOffset()));
			}
			catch (IOException ioe)
			{
				lblContentTypeValue.setText("Content Type Error");
			}
		}
												
		openData(fileTree.getSelectedFiles().get(0));
	}	
	
	private void openData(SqPack_File file) {
		JTabbedPane tabs = new JTabbedPane();
				
		byte data[] = null;		
		int contentType = -1;
		try {
			 contentType = currentDatFile.getContentType(file.getOffset()); 
			data = currentDatFile.extractFile(file.dataoffset, null);
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(FileManagerWindow.this,
					"There was an error unpacking " + file.getName() + ".",					
				    "File Unpack Error",
				    JOptionPane.ERROR_MESSAGE);
		}
		
		if (data == null)
		{				
			hexView.setBytes(null);			
			tabs.addTab("Raw Hex", hexView);			
			splitPane.setRightComponent(tabs);
			return;
		}
					
		if (data.length >= 3 && data[0] == 'E' && data[1] == 'X' && data[2] == 'H' && data[3] == 'F')
		{							
			try {
				if (exhfComponent == null || !exhfComponent.isSame(file.getName()))				
					exhfComponent = new EXDF_View(currentIndexFile, currentDatFile, HashDatabase.getFolder(file.getId2()) + "/" + file.getName(), new EXHF_File(data));				
				tabs.addTab("EXDF File", exhfComponent);			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		else if (data.length >= 3 && data[0] == 'E' && data[1] == 'X' && data[2] == 'D' && data[3] == 'F')
		{								
			try {
				if (exhfComponent == null || !exhfComponent.isSame(file.getName()))				
					exhfComponent = new EXDF_View(currentIndexFile, currentDatFile, HashDatabase.getFolder(file.getId2()) + "/" + file.getName(), new EXDF_File(data));
				tabs.addTab("EXHF File", exhfComponent);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		else if (data.length >= 8 && data[0] == 'S' && data[1] == 'E' && data[2] == 'D' && data[3] == 'B' && data[4] == 'S' && data[5] == 'S' && data[6] == 'C' && data[7] == 'F')
		{								
			Sound_View scdComponent;
			try {
				scdComponent = new Sound_View(new SCD_File(data));
				tabs.addTab("SCD File", scdComponent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (contentType == 4 || file.getName().endsWith("atex"))
		{
			Image_View imageComponent = new Image_View(new Texture_File(data));
			tabs.addTab("TEX File", imageComponent);
		}
		else if (data.length >= 3 && data[1] == 'L' && data[2] == 'u'){
			
			if (luadec != null)			
			{
				Lua_View luaComponent = new Lua_View(("-- Decompiled using luadec 2.0.1 by sztupy (http://winmo.sztupy.hu)\n"+luadec.decompile(data)).split("\n"));
				tabs.addTab("Decompiled Lua", luaComponent);
			}
			else
			{
				
			}
		}	
		
		hexView.setBytes(data);			
		tabs.addTab("Raw Hex", hexView);			
		splitPane.setRightComponent(tabs);
	}

	private void extract(boolean doConvert) {				
		JFileChooser fileChooser = new JFileChooser(lastOpenedFile);
		
		ArrayList<SqPack_File> files = fileTree.getSelectedFiles();		
		
		if (files.size() > 1)
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		else
		{			
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);		

			String fileName = HashDatabase.getFileName(files.get(0).getId());
			
			if (fileName == null)
				fileName = String.format("%08X", files.get(0).getId() & 0xFFFFFFFF);
			
			fileChooser.setSelectedFile(new File(fileName));			
			FileFilter filter = new FileFilter() {
				
				@Override
				public String getDescription() {
					return "FFXIV Converted";
				}
				
				@Override
				public boolean accept(File f) {
					return f.getName().endsWith(".csv") || f.getName().endsWith(".ogg") || f.getName().endsWith(".wav") ||f.getName().endsWith(".png") || f.isDirectory();
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
	
			Loading_Dialog loadingDialog = new Loading_Dialog(FileManagerWindow.this, files.size());
			loadingDialog.setTitle("Extracting...");
			ExtractTask task = new ExtractTask(files, loadingDialog, doConvert);
			task.execute();
			loadingDialog.setLocationRelativeTo(this);
			loadingDialog.setVisible(true);		
		}
		
	}

	private String getExtension(int contentType, byte[] data) {
		if (data.length >= 4 && data[0] == 'E' && data[1] == 'X' && data[2] == 'D' && data[3] == 'F')
			return ".exd";
		else if (data.length >= 4 && data[0] == 'E' && data[1] == 'X' && data[2] == 'H' && data[3] == 'F')
			return ".exh";
		else if (data.length >= 5 && data[1] == 'L' && data[2] == 'u' && data[3] == 'a' && data[4] == 'Q' )
			return ".luab";
		else if (data.length >= 4 && data[0] == 'S' && data[1] == 'E' && data[2] == 'D' && data[3] == 'B' )
			return ".scd";
		else if (contentType == 4)
		{
			return ".png";
		}
		else
			return ".dat";
	}

	class OpenIndexTask extends SwingWorker<Void, Void>{
		
		File selectedFile;
		
		public OpenIndexTask(File selectedFile) {
			this.selectedFile = selectedFile;
			FileManagerWindow.this.setEnabled(false);
			prgLoadingBar.setVisible(true);
			prgLoadingBar.setValue(0);
		}

		@Override
		protected Void doInBackground() throws Exception {
			try {				
				currentIndexFile = new SqPack_IndexFile(selectedFile.getAbsolutePath(), prgLoadingBar);			
				currentDatFile = new SqPack_DatFile(selectedFile.getAbsolutePath().replace(".index", ".dat0"));
				currentCompareFile = CompareFile.getCompareFile(selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.')));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(FileManagerWindow.this,
						"There was an error opening this index file.",
					    "File Open Error",
					    JOptionPane.ERROR_MESSAGE);		
				return null;
			}			
			return null;		
		}
		
		@Override
		protected void done() {
			if (Constants.DEBUG)			
				currentIndexFile.displayIndexInfo();
			
			setTitle(Constants.APPNAME + " [" + selectedFile.getName() + "]");
			fileTree.fileOpened(currentIndexFile, currentCompareFile);
			file_Close.setEnabled(true);
			search_search.setEnabled(true);
			FileManagerWindow.this.setEnabled(true);
			prgLoadingBar.setValue(0);
			prgLoadingBar.setVisible(false);
		}
	}

	class ExtractTask extends SwingWorker<Void, Void>{

		ArrayList<SqPack_File> files;
		Loading_Dialog loadingDialog;
		boolean doConvert;
		
		public ExtractTask(ArrayList<SqPack_File> files, Loading_Dialog loadingDialog, boolean doConvert) {
			this.files = files;
			this.loadingDialog = loadingDialog;
			this.doConvert = doConvert;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			EXDF_View tempView = null;
			
			for (int i = 0; i < files.size(); i++){
				try {
					byte[] data = currentDatFile.extractFile(files.get(i).getOffset(), loadingDialog);
					byte[] dataToSave = null;
					String extension = getExtension(currentDatFile.getContentType(files.get(i).getOffset()), data);
					
					if (extension.equals(".exh") && doConvert)
					{
						if (tempView != null && tempView.isSame(files.get(i).getName()))
							continue;
						EXHF_File file = new EXHF_File(data);
						
						tempView = new EXDF_View(currentIndexFile, currentDatFile,  HashDatabase.getFolder(fileTree.getSelectedFiles().get(0).getId2()) + "/" + fileTree.getSelectedFiles().get(0).getName(), file);						
						dataToSave = tempView.getCSV().getBytes();
						extension = ".csv";
					}
					else if (tempView != null && extension.equals(".exd") && doConvert)
					{
						if (tempView.isSame(files.get(i).getName()))
							continue;
						EXDF_File file = new EXDF_File(data);
						
						tempView = new EXDF_View(currentIndexFile, currentDatFile,  HashDatabase.getFolder(fileTree.getSelectedFiles().get(0).getId2()) + "/" + fileTree.getSelectedFiles().get(0).getName(), file);						
						dataToSave = tempView.getCSV().getBytes();
						extension = ".csv";
					}
					else if (extension.equals(".png") && doConvert)
					{
						Texture_File tex = new Texture_File(data);
						dataToSave = tex.getImage("png");
						extension = ".png";
					}
					else if (extension.equals(".scd") && doConvert)
					{
						SCD_File file = new SCD_File(data);
						
						for (int s = 0; s < file.getNumEntries(); s++)
						{
							SCD_Sound_Info info = file.getSoundInfo(s);
							if (info == null)
								continue;
							if (info.dataType == 0x06)
							{
								dataToSave = file.getConverted(s);
								extension = ".ogg";
							}
							else if (info.dataType == 0x0C)
							{
								dataToSave = file.getConverted(s);
								extension = ".wav";
							}
							else							
								continue;
														
							String path = lastOpenedFile.getCanonicalPath();
							String fileName = files.get(i).getName();
							
							if (fileName == null)						
								fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
							
							if (files.size() > 1)
								path = lastOpenedFile.getCanonicalPath() + "\\" + fileName;														
							
							LERandomAccessFile out = new LERandomAccessFile(path + (file.getNumEntries()==1 ? "" : "_" + s) + extension, "rw");
							out.write(dataToSave, 0, dataToSave.length);
							out.close();
						}
												
					}
					else
					{
						dataToSave = data;
					}
					
					if (dataToSave == null)
					{
						JOptionPane.showMessageDialog(FileManagerWindow.this,
								String.format("%X", files.get(i).getId() & 0xFFFFFFFF) + " could not be converted to " + extension.substring(1).toUpperCase() + ".",
							    "Export Error",
							    JOptionPane.ERROR_MESSAGE);
						continue;
					}
					
					String path = lastOpenedFile.getCanonicalPath();
					String fileName = HashDatabase.getFileName(files.get(i).getId());
					
					if (fileName == null)						
						fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
					
					if (files.size() > 1)
						path = lastOpenedFile.getCanonicalPath() + "\\" + fileName;
					
					LERandomAccessFile out = new LERandomAccessFile(path + extension, "rw");
					out.write(dataToSave, 0, dataToSave.length);
					out.close();
					
					loadingDialog.nextFile(i+1, path + extension);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		protected void done() {
			loadingDialog.setVisible(false);
			loadingDialog.dispose();
		}
		
	}


	@Override
	public void onSearchChosen(SqPack_File file) {
		
		if (file == null)
		{
			search_searchAgain.setEnabled(false);
			return;
		}
		
		openData(file);
		fileTree.select(file.getOffset());
		search_searchAgain.setEnabled(true);

	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		closeFile();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
}
