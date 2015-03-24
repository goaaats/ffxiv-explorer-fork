package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
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
import ca.fraggergames.ffxivextract.gui.components.OpenGL_View;
import ca.fraggergames.ffxivextract.gui.components.Path_to_Hash_Window;
import ca.fraggergames.ffxivextract.gui.components.Sound_View;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.helpers.LuaDec;
import ca.fraggergames.ffxivextract.helpers.WavefrontObjectWriter;
import ca.fraggergames.ffxivextract.models.EXDF_File;
import ca.fraggergames.ffxivextract.models.EXHF_File;
import ca.fraggergames.ffxivextract.models.Model;
import ca.fraggergames.ffxivextract.models.SCD_File;
import ca.fraggergames.ffxivextract.models.SCD_File.SCD_Sound_Info;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.Texture_File;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

@SuppressWarnings("serial")
public class FileManagerWindow extends JFrame implements TreeSelectionListener, ISearchComplete, WindowListener {

	//DLLs
	LuaDec luadec;
	
	JMenuBar menu = new JMenuBar();
	
	//FILE IO
	File lastOpenedIndexFile = null;
	File lastSaveLocation = null;
	SqPack_IndexFile currentIndexFile;
	
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
	JLabel lblLoadingBarString;
	TexturePaint paint;
	JScrollPane defaultScrollPane;
	JViewport defaultViewPort;
	
	//MENU
	JMenuItem file_Extract;
	JMenuItem file_ExtractRaw;
	JMenuItem file_Close;
	JMenuItem search_search;
	JMenuItem search_searchAgain;	
	JCheckBoxMenuItem options_enableUpdate;
	
	public FileManagerWindow(String title)
	{	
		addWindowListener(this);
		
		//Load generic bg img
		BufferedImage bg;
		try {
			bg = ImageIO.read(getClass().getResource("/res/triangular.png"));
			paint = new TexturePaint(bg, new Rectangle(120, 120));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setupMenu();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1200, 800);
		this.setTitle(title);
		
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
		
		JPanel pnlContent = new JPanel();
		getContentPane().add(pnlContent, BorderLayout.CENTER);
		pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.X_AXIS));
		
		defaultScrollPane = new JScrollPane();
		defaultViewPort = new JViewport(){
			@Override
			public boolean isOpaque() {
				return false;
			}
			
			@Override
			protected void paintComponent(Graphics g) {			
		        Graphics2D g2d = (Graphics2D) g;
		        g2d.setPaint(paint);
		        g2d.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		defaultScrollPane.setViewport(defaultViewPort);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			                           fileTree, defaultScrollPane);
		pnlContent.add(splitPane);
		
		splitPane.setDividerLocation(150);
		
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
		
		lblLoadingBarString = new JLabel("0%");
		lblLoadingBarString.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLoadingBarString.setVisible(false);
		pnlProgBar.add(lblLoadingBarString);
		pnlProgBar.add(prgLoadingBar);
		
		setLocationRelativeTo(null);	
		
		//Check Windows registry for a FFXIV folder
		//String value = null;
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
		
		Preferences prefs = Preferences.userNodeForPackage(ca.fraggergames.ffxivextract.Main.class);
		
		if (prefs.get(Constants.PREF_LASTOPENED, null) != null)
			lastOpenedIndexFile = new File(prefs.get(Constants.PREF_LASTOPENED, null));
		
		//Init Luadec
		luadec = LuaDec.initLuaDec();		
	}	

	protected void openFile(File selectedFile) {
		
		if (currentIndexFile != null)
			closeFile();
		
		OpenIndexTask openTask = new OpenIndexTask(selectedFile);
		openTask.execute();		
	}

	protected void closeFile() {
		
		if (currentIndexFile == null)
			return;
		
		fileTree.fileClosed();	
		currentIndexFile = null;		
		
		setTitle(Constants.APPNAME);
		hexView.setBytes(null);
		splitPane.setRightComponent(defaultScrollPane);
		file_Close.setEnabled(false);
		search_search.setEnabled(false);
		search_searchAgain.setEnabled(false);
		
		lblOffsetValue.setText("*");
		lblHashValue.setText("*");
		lblContentTypeValue.setText("*");
	}
	
	ActionListener menuHandler = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getActionCommand().equals("open"))
			{
				JFileChooser fileChooser = new JFileChooser(lastOpenedIndexFile);
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
					lastOpenedIndexFile = fileChooser.getSelectedFile();
					openFile(fileChooser.getSelectedFile());
					
					Preferences prefs = Preferences.userNodeForPackage(ca.fraggergames.ffxivextract.Main.class);
					prefs.put(Constants.PREF_LASTOPENED, lastOpenedIndexFile.getAbsolutePath());					
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
				searchWindow.setLocationRelativeTo(FileManagerWindow.this);
				searchWindow.setVisible(true);
				searchWindow.reset();
			}
			else if (event.getActionCommand().equals("searchagain"))
			{
				searchWindow.searchAgain();
			}
			else if (event.getActionCommand().equals("hashcalc"))
			{
				Path_to_Hash_Window hasher = new Path_to_Hash_Window();
				hasher.setLocationRelativeTo(FileManagerWindow.this);
				hasher.setVisible(true);
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
			else if (event.getActionCommand().equals("options_update"))
			{
				Preferences prefs = Preferences.userNodeForPackage(ca.fraggergames.ffxivextract.Main.class);				
				prefs.putBoolean(Constants.PREF_DO_DB_UPDATE, options_enableUpdate.isSelected());
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
		file_Extract.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
		file_ExtractRaw = new JMenuItem(Strings.MENUITEM_EXTRACTRAW);
		file_ExtractRaw.setEnabled(false);
		file_ExtractRaw.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.SHIFT_MASK|KeyEvent.CTRL_MASK));
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
		search_search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));
		search_search.addActionListener(menuHandler);

		search_searchAgain = new JMenuItem(Strings.MENUITEM_SEARCHAGAIN);
		search_searchAgain.setEnabled(false);
		search_searchAgain.setActionCommand("searchagain");
		search_searchAgain.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
		search_searchAgain.addActionListener(menuHandler);		
		
		JMenuItem tools_musicswapper = new JMenuItem(Strings.MENUITEM_MUSICSWAPPER);
		tools_musicswapper.setActionCommand("musicswapper");
		tools_musicswapper.addActionListener(menuHandler);
		
		JMenuItem tools_hashcalculator = new JMenuItem(Strings.MENUITEM_HASHCALC);
		tools_hashcalculator.setActionCommand("hashcalc");
		tools_hashcalculator.addActionListener(menuHandler);
		
		JMenuItem tools_macroEditor = new JMenuItem(Strings.MENUITEM_MACROEDITOR);
		tools_macroEditor.setActionCommand("macroeditor");
		tools_macroEditor.addActionListener(menuHandler);
		
		JMenuItem tools_logViewer = new JMenuItem(Strings.MENUITEM_LOGVIEWER);		
		tools_logViewer.setActionCommand("logviewer");
		tools_logViewer.addActionListener(menuHandler);

		//tools_musicswapper.setEnabled(false);
		tools_macroEditor.setEnabled(false);
		tools_logViewer.setEnabled(false);
		
		Preferences prefs = Preferences.userNodeForPackage(ca.fraggergames.ffxivextract.Main.class);
		options_enableUpdate = new JCheckBoxMenuItem(Strings.MENUITEM_ENABLEUPDATE, prefs.getBoolean(Constants.PREF_DO_DB_UPDATE, false));
		options_enableUpdate.setActionCommand("options_update");
		options_enableUpdate.addActionListener(menuHandler);
		
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
		tools.add(tools_hashcalculator);
		tools.add(tools_macroEditor);
		tools.add(tools_logViewer);
		
		options.add(options_enableUpdate);
		
		help.add(help_About);
		
		//Super Menus
		menu.add(file);
		menu.add(search);
		menu.add(tools);
		menu.add(options);
		menu.add(help);
		
		this.setJMenuBar(menu);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		if (fileTree.isOnlyFolder())
		{
			splitPane.setRightComponent(defaultScrollPane);
			lblOffsetValue.setText("*");
			lblHashValue.setText("*");			
			lblContentTypeValue.setText("*");
			file_Extract.setEnabled(true);
			file_ExtractRaw.setEnabled(true);	
			return;
		}
		
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
			int datNum = (int) ((fileTree.getSelectedFiles().get(0).getOffset() & 0x000F) / 2);
			
			lblOffsetValue.setText(String.format("0x%08X",fileTree.getSelectedFiles().get(0).getOffset()*0x8) + " (Dat: " + datNum +")");
			lblHashValue.setText(String.format("0x%08X",fileTree.getSelectedFiles().get(0).getId()));
			try{
				lblContentTypeValue.setText(""+currentIndexFile.getContentType(fileTree.getSelectedFiles().get(0).getOffset()));
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
			 contentType = currentIndexFile.getContentType(file.getOffset()); 
			 
			 //If it's a placeholder, don't bother			 
			 if (contentType == 0x01)
			 {
				 JLabel lblFNFError = new JLabel("This is currently a placeholder, there is no data to here.");
				 tabs.addTab("No Data", lblFNFError);
				 hexView.setBytes(null);							
				 splitPane.setRightComponent(tabs);
				 return;
			 }
			 
			 data = currentIndexFile.extractFile(file.dataoffset, null);
		}
		catch (FileNotFoundException eFNF) {
			if (Constants.DEBUG)
				eFNF.printStackTrace();
			JLabel lblFNFError = new JLabel("The dat for this file is missing!");
			tabs.addTab("Extract Error", lblFNFError);
			hexView.setBytes(null);							
			splitPane.setRightComponent(tabs);
			return;
		}
		catch (IOException e) {
			if (Constants.DEBUG)
				e.printStackTrace();
			JLabel lblLoadError = new JLabel("Something went terribly wrong extracting this file.");
			tabs.addTab("Extract Error", lblLoadError);
			hexView.setBytes(null);							
			splitPane.setRightComponent(tabs);
			return;
		}				
		
		if (contentType == 3)
		{		
			OpenGL_View view = null;
			
			Model model = null;
			/*
			try {
				model = new Model("chara/human/c1101/obj/hair/h0002/model/c1101h0002_hir.mdl", currentIndexFile, currentIndexFile.extractFile(0x3543000, null));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			try{
				if (HashDatabase.getFolder(file.getId2()) == null)
					view = new OpenGL_View(new Model(null, currentIndexFile, data), model);
				else
					view = new OpenGL_View(new Model(HashDatabase.getFolder(file.getId2()) + "/" + file.getName(), currentIndexFile, data), model);				
			}catch(Exception modelException)
			{
				modelException.printStackTrace();	
				JLabel lblLoadError = new JLabel("Error loading Model.");
				tabs.addTab("Error", lblLoadError);
				hexView.setBytes(data);							
				tabs.addTab("Raw Hex", hexView);			
				splitPane.setRightComponent(tabs);
				return;
			}
			
			tabs.addTab("3D Model", view);			
		}
		
		if (data == null)
		{
			JLabel lblLoadError = new JLabel("Something went terribly wrong extracting this file.");
			tabs.addTab("Extract Error", lblLoadError);
			hexView.setBytes(null);							
			splitPane.setRightComponent(tabs);
			return;
		}
					
		if (data.length >= 3 && data[0] == 'E' && data[1] == 'X' && data[2] == 'H' && data[3] == 'F')
		{							
			try {
				if (exhfComponent == null || !exhfComponent.isSame(file.getName()))				
					exhfComponent = new EXDF_View(currentIndexFile, HashDatabase.getFolder(file.getId2()) + "/" + file.getName(), new EXHF_File(data));				
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
					exhfComponent = new EXDF_View(currentIndexFile, HashDatabase.getFolder(file.getId2()) + "/" + file.getName(), new EXDF_File(data));
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
		/*else if (data.length >= 4 && data[0] == 'X' && data[1] == 'F' && data[2] == 'V' && data[3] == 'A')
		{
			AVFX_File avfxFile = new AVFX_File(data);
			avfxFile.printOut();
		}*/
		else if (contentType == 4 || file.getName().endsWith("atex"))
		{
			Image_View imageComponent = new Image_View(new Texture_File(data));
			tabs.addTab("TEX File", imageComponent);
		}
		else if (data.length >= 5 && data[0] == 0x1B && data[1] == 'L' && data[2] == 'u' && data[3] == 'a' && data[4] == 'Q'){
			
			if (luadec != null)			
			{
				try{
				Lua_View luaComponent = new Lua_View(("-- Decompiled using luadec 2.0.1 by sztupy (http://winmo.sztupy.hu)\n"+luadec.decompile(data)).split("\n"));
				tabs.addTab("Decompiled Lua", luaComponent);
				}
				catch (Exception e){}
			}
			else
			{
				JLabel lbl3DModelError = new JLabel("Cannot show decompiled lua because luadec.dll has not been found or was not loaded correctly.");
				tabs.addTab("Decompiled Lua", lbl3DModelError);						
				splitPane.setRightComponent(tabs);				
			}
		}	
		
		hexView.setBytes(data);	
		//if (contentType != 3)
			tabs.addTab("Raw Hex", hexView);			
		splitPane.setRightComponent(tabs);
	}

	private void extract(boolean doConvert) {				
		JFileChooser fileChooser = new JFileChooser(lastSaveLocation);
		
		ArrayList<SqPack_File> files = fileTree.getSelectedFiles();		
		
	
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
				
		FileFilter filter = new FileFilter() {
			
			@Override
			public String getDescription() {
				return "FFXIV Converted";
			}
			
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".csv") || f.getName().endsWith(".ogg") || f.getName().endsWith(".wav") ||f.getName().endsWith(".png") ||f.getName().endsWith(".obj") || f.isDirectory();
			}				
		};
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
	
	
		int retunval = fileChooser.showSaveDialog(FileManagerWindow.this);
		
		if (retunval == JFileChooser.APPROVE_OPTION)
		{
			lastSaveLocation = fileChooser.getSelectedFile();
			lastSaveLocation.getParentFile().mkdirs();
	
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
		else if (contentType == 3)
		{
			return ".obj";
		}
		else if (contentType == 4)
		{
			return ".png";
		}
		else
			return "";
	}

	class OpenIndexTask extends SwingWorker<Void, Void>{
		
		File selectedFile;
		
		public OpenIndexTask(File selectedFile) {
			this.selectedFile = selectedFile;
			menu.setEnabled(false);
			prgLoadingBar.setVisible(true);
			prgLoadingBar.setValue(0);
			lblLoadingBarString.setVisible(true);
			for (int i = 0; i < menu.getMenuCount(); i++)
				menu.getMenu(i).setEnabled(false);
		}

		@Override
		protected Void doInBackground() throws Exception {
			try {				
				HashDatabase.beginConnection();
				currentIndexFile = new SqPack_IndexFile(selectedFile.getAbsolutePath(), prgLoadingBar, lblLoadingBarString);
				HashDatabase.closeConnection();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(FileManagerWindow.this,
						"There was an error opening this index file.",
					    "File Open Error",
					    JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return null;
			}			
			return null;		
		}
		
		@Override
		protected void done() {
			if (Constants.DEBUG)			
				currentIndexFile.displayIndexInfo();
			
			setTitle(Constants.APPNAME + " [" + selectedFile.getName() + "]");
			file_Close.setEnabled(true);
			search_search.setEnabled(true);
			prgLoadingBar.setValue(0);
			prgLoadingBar.setVisible(false);
			lblLoadingBarString.setVisible(false);
			fileTree.fileOpened(currentIndexFile);
			searchWindow = new SearchWindow(FileManagerWindow.this, currentIndexFile, FileManagerWindow.this);
			for (int i = 0; i < menu.getMenuCount(); i++)
				menu.getMenu(i).setEnabled(true);
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
					String folderName = HashDatabase.getFolder(files.get(i).getId2());					
					String fileName = files.get(i).getName();					
					if (fileName == null)						
						fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);												
					if (folderName == null)
						folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);	
					
					loadingDialog.nextFile(i, folderName + "/" + fileName);
					
					byte[] data = currentIndexFile.extractFile(files.get(i).getOffset(), loadingDialog);
					byte[] dataToSave = null;
					
					String extension = getExtension(currentIndexFile.getContentType(files.get(i).getOffset()), data);
					
					if (extension.equals(".exh") && doConvert)
					{
						if (tempView != null && tempView.isSame(files.get(i).getName()))
							continue;
						EXHF_File file = new EXHF_File(data);
						
						tempView = new EXDF_View(currentIndexFile, HashDatabase.getFolder(fileTree.getSelectedFiles().get(i).getId2()) + "/" + fileTree.getSelectedFiles().get(i).getName(), file);						
						
						for (int l = 0; l < (tempView.getNumLangs() == 1 ? 1 : 4); l++)
						{
											
							String path = lastSaveLocation.getCanonicalPath();
							
							if (fileName == null)						
								fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
															
							if (folderName == null)
								folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
							
							path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
							
							File mkDirPath = new File(path);
							mkDirPath.getParentFile().mkdirs();						
													
							tempView.saveCSV(path + (tempView.getNumLangs()==1 ? "" : "_" + EXDF_View.langs[l]) +  ".csv", l);
														
						}
						
						continue;
					}
					else if (extension.equals(".exd") && doConvert)
					{
						if (tempView != null && tempView.isSame(files.get(i).getName()))
							continue;
						EXDF_File file = new EXDF_File(data);
						
						tempView = new EXDF_View(currentIndexFile,  HashDatabase.getFolder(fileTree.getSelectedFiles().get(i).getId2()) + "/" + fileTree.getSelectedFiles().get(i).getName(), file);						
						
						//Remove the thing
						String exhName = files.get(i).getName(); 
						System.out.println(exhName);
						exhName = exhName.replace("_en.exd", "");
						exhName = exhName.replace("_ja.exd", "");
						exhName = exhName.replace("_de.exd", "");
						exhName = exhName.replace("_fr.exd", "");
						exhName = exhName.replace("_chs.exd", "");
						exhName = exhName.substring(0, exhName.lastIndexOf("_")) +".exh";
						
						for (int l = 0; l < (tempView.getNumLangs() == 1 ? 1 : 4); l++)
						{	
							String path = lastSaveLocation.getCanonicalPath();
							
							fileName = exhName;
															
							if (folderName == null)
								folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
							
							path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
							
							File mkDirPath = new File(path);
							mkDirPath.getParentFile().mkdirs();																																	
							
							path += "\\" + fileName;	
							tempView.saveCSV(path + (tempView.getNumLangs()==1 ? "" : "_" + EXDF_View.langs[l]) + ".csv", l);
						}
						
						continue;
					}
					else if (extension.equals(".obj") && doConvert)
					{						
						Model model = new Model(folderName + "/" + fileName, currentIndexFile, data);
						
						String path = lastSaveLocation.getCanonicalPath();
						
						if (fileName == null)				
							fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
														
						if (folderName == null)
							folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
						
						path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
						
						File mkDirPath = new File(path);
						mkDirPath.getParentFile().mkdirs();																															
									
						WavefrontObjectWriter.writeObj(path, model);
						
						continue;
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
											
							String path = lastSaveLocation.getCanonicalPath();
							
							if (fileName == null)				
								fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
															
							if (folderName == null)
								folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
							
							path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
							
							File mkDirPath = new File(path);
							mkDirPath.getParentFile().mkdirs();																															
							
							LERandomAccessFile out = new LERandomAccessFile(path + (file.getNumEntries()==1 ? "" : "_" + s) + extension, "rw");
							out.write(dataToSave, 0, dataToSave.length);
							out.close();
						}
						continue;		
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
					
					String path = lastSaveLocation.getCanonicalPath();
					
					if (fileName == null){						
						fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
						if (!doConvert)
							extension = "";
					}
					else if (!doConvert)
						extension = "";
						
					if (folderName == null)
						folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
					
					path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
					
					File mkDirPath = new File(path);
					mkDirPath.getParentFile().mkdirs();
					
					LERandomAccessFile out = new LERandomAccessFile(path + extension, "rw");
					out.write(dataToSave, 0, dataToSave.length);
					out.close();					
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
			searchWindow.reset();
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
