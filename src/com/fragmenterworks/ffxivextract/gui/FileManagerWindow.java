package com.fragmenterworks.ffxivextract.gui;

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
import javax.swing.JComponent;
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

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.Strings;
import com.fragmenterworks.ffxivextract.storage.HashDatabase;

import com.fragmenterworks.ffxivextract.gui.SearchWindow.ISearchComplete;
import com.fragmenterworks.ffxivextract.gui.components.CMP_View;
import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.gui.components.ExplorerPanel_View;
import com.fragmenterworks.ffxivextract.gui.components.Hex_View;
import com.fragmenterworks.ffxivextract.gui.components.Image_View;
import com.fragmenterworks.ffxivextract.gui.components.Loading_Dialog;
import com.fragmenterworks.ffxivextract.gui.components.Lua_View;
import com.fragmenterworks.ffxivextract.gui.components.OpenGL_View;
import com.fragmenterworks.ffxivextract.gui.components.PAP_View;
import com.fragmenterworks.ffxivextract.gui.components.Path_to_Hash_Window;
import com.fragmenterworks.ffxivextract.gui.components.Shader_View;
import com.fragmenterworks.ffxivextract.gui.components.Sound_View;
import com.fragmenterworks.ffxivextract.gui.modelviewer.ModelViewerWindow;
import com.fragmenterworks.ffxivextract.gui.outfitter.OutfitterWindow;
import com.fragmenterworks.ffxivextract.helpers.HashFinding_Utils;
import com.fragmenterworks.ffxivextract.helpers.HavokNative;
import com.fragmenterworks.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.helpers.LuaDec;
import com.fragmenterworks.ffxivextract.helpers.WavefrontObjectWriter;
import com.fragmenterworks.ffxivextract.models.AVFX_File;
import com.fragmenterworks.ffxivextract.models.CMP_File;
import com.fragmenterworks.ffxivextract.models.EXDF_File;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.Model;
import com.fragmenterworks.ffxivextract.models.PAP_File;
import com.fragmenterworks.ffxivextract.models.SCD_File;
import com.fragmenterworks.ffxivextract.models.SHCD_File;
import com.fragmenterworks.ffxivextract.models.SCD_File.SCD_Sound_Info;
import com.fragmenterworks.ffxivextract.models.SGB_File;
import com.fragmenterworks.ffxivextract.models.SHPK_File;
import com.fragmenterworks.ffxivextract.models.SKLB_File;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import com.fragmenterworks.ffxivextract.models.Texture_File;

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
		
		pnlContent.registerKeyboardAction(menuHandler, "search", KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		pnlContent.registerKeyboardAction(menuHandler, "extractc", KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		pnlContent.registerKeyboardAction(menuHandler, "extractr", KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.SHIFT_MASK|KeyEvent.CTRL_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		pnlContent.registerKeyboardAction(menuHandler, "searchagain", KeyStroke.getKeyStroke(KeyEvent.VK_F3,0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
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
		
		Preferences prefs = Preferences.userNodeForPackage(com.fragmenterworks.ffxivextract.Main.class);
		
		if (prefs.get(Constants.PREF_LASTOPENED, null) != null)
			lastOpenedIndexFile = new File(prefs.get(Constants.PREF_LASTOPENED, null));
		
		//Init Luadec
		luadec = LuaDec.initLuaDec();
		HavokNative.initHavokNativ();
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
				FileFilter filter2 = new FileFilter() {
					
					@Override
					public String getDescription() {
						return Strings.FILETYPE_FFXIV_INDEX2;
					}
					
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".index2") || f.isDirectory();
					}				
				};
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.addChoosableFileFilter(filter2);
				
				fileChooser.setFileFilter(filter);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int retunval = fileChooser.showOpenDialog(FileManagerWindow.this);
				if (retunval == JFileChooser.APPROVE_OPTION)
				{
					lastOpenedIndexFile = fileChooser.getSelectedFile();
					openFile(fileChooser.getSelectedFile());
					
					Preferences prefs = Preferences.userNodeForPackage(com.fragmenterworks.ffxivextract.Main.class);
					prefs.put(Constants.PREF_LASTOPENED, lastOpenedIndexFile.getAbsolutePath());					
				}
			}
			else if (event.getActionCommand().equals("close"))
			{
				closeFile();				
			}
			else if (event.getActionCommand().equals("extractc")&& file_Extract.isEnabled())
			{
				extract(true);
			}
			else if (event.getActionCommand().equals("extractr") && file_ExtractRaw.isEnabled())
			{
				extract(false);
			}
			else if (event.getActionCommand().equals("search") && search_search.isEnabled())
			{				
				searchWindow.setLocationRelativeTo(FileManagerWindow.this);
				searchWindow.setVisible(true);
				searchWindow.reset();
			}
			else if (event.getActionCommand().equals("searchagain") && search_searchAgain.isEnabled())
			{
				searchWindow.searchAgain();
			}
			else if (event.getActionCommand().equals("hashcalc"))
			{
				Path_to_Hash_Window hasher = new Path_to_Hash_Window();
				hasher.setLocationRelativeTo(FileManagerWindow.this);
				hasher.setVisible(true);
			}
			else if (event.getActionCommand().equals("modelviewer"))
			{				
				if (Constants.datPath == null || Constants.datPath.isEmpty() || !new File(Constants.datPath).exists())
				{
					JOptionPane.showMessageDialog(
			                FileManagerWindow.this,
			                "You have not set a valid FFXIV path. Please set it first in Settings under the Options menu.",
			                "FFXIV Path Not Set",
			                JOptionPane.ERROR_MESSAGE);					
					return;
				}
				
				ModelViewerWindow modelviewer = new ModelViewerWindow(FileManagerWindow.this, Constants.datPath);
				//modelviewer.setLocationRelativeTo(FileManagerWindow.this);
				modelviewer.beginLoad();
			}
			else if (event.getActionCommand().equals("outfitter"))
			{				
				if (Constants.datPath == null || Constants.datPath.isEmpty() || !new File(Constants.datPath).exists())
				{
					JOptionPane.showMessageDialog(
			                FileManagerWindow.this,
			                "You have not set a valid FFXIV path. Please set it first in Settings under the Options menu.",
			                "FFXIV Path Not Set",
			                JOptionPane.ERROR_MESSAGE);					
					return;
				}
				
				OutfitterWindow outfitter = new OutfitterWindow(FileManagerWindow.this, Constants.datPath);
				//modelviewer.setLocationRelativeTo(FileManagerWindow.this);
				outfitter.beginLoad();
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
			else if (event.getActionCommand().equals("find_exh"))
			{
				if (Constants.datPath == null || Constants.datPath.isEmpty() || !new File(Constants.datPath).exists())
				{
					JOptionPane.showMessageDialog(
			                FileManagerWindow.this,
			                "You have not set a valid FFXIV path. Please set it first in Settings under the Options menu.",
			                "FFXIV Path Not Set",
			                JOptionPane.ERROR_MESSAGE);					
					return;
				}
				HashFinding_Utils.findExhHashes();
				
				JOptionPane.showMessageDialog(
		                FileManagerWindow.this,
		                "Finished searching for hashes. Reopen the archive if you have it currently open.",
		                "Done",
		                JOptionPane.INFORMATION_MESSAGE);		
			}
			else if (event.getActionCommand().equals("find_music"))
			{
				if (Constants.datPath == null || Constants.datPath.isEmpty() || !new File(Constants.datPath).exists())
				{
					JOptionPane.showMessageDialog(
			                FileManagerWindow.this,
			                "You have not set a valid FFXIV path. Please set it first in Settings under the Options menu.",
			                "FFXIV Path Not Set",
			                JOptionPane.ERROR_MESSAGE);					
					return;
				}
				HashFinding_Utils.findMusicHashes();
				
				JOptionPane.showMessageDialog(
		                FileManagerWindow.this,
		                "Finished searching for hashes. Reopen the archive if you have it currently open.",
		                "Done",
		                JOptionPane.INFORMATION_MESSAGE);	
			}
			else if (event.getActionCommand().equals("find_maps"))
			{
				if (Constants.datPath == null || Constants.datPath.isEmpty() || !new File(Constants.datPath).exists())
				{
					JOptionPane.showMessageDialog(
			                FileManagerWindow.this,
			                "You have not set a valid FFXIV path. Please set it first in Settings under the Options menu.",
			                "FFXIV Path Not Set",
			                JOptionPane.ERROR_MESSAGE);					
					return;
				}
				HashFinding_Utils.findMapHashes();
				
				JOptionPane.showMessageDialog(
		                FileManagerWindow.this,
		                "Finished searching for hashes. Reopen the archive if you have it currently open.",
		                "Done",
		                JOptionPane.INFORMATION_MESSAGE);	
			}
			else if  (event.getActionCommand().equals("settings"))
			{
				SettingsWindow settings = new SettingsWindow(FileManagerWindow.this);
				settings.setLocationRelativeTo(FileManagerWindow.this);
				settings.setVisible(true);
			}
			else if (event.getActionCommand().equals("options_update"))
			{
				Preferences prefs = Preferences.userNodeForPackage(com.fragmenterworks.ffxivextract.Main.class);				
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
		JMenu dataviewers = new JMenu(Strings.MENU_DATAVIEWERS);
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
		search_search.addActionListener(menuHandler);
		search_search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));		
		
		search_searchAgain = new JMenuItem(Strings.MENUITEM_SEARCHAGAIN);
		search_searchAgain.setEnabled(false);
		search_searchAgain.setActionCommand("searchagain");		
		search_searchAgain.addActionListener(menuHandler);	
		search_searchAgain.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
		
		JMenuItem dataviewer_modelViewer = new JMenuItem(Strings.MENUITEM_MODELVIEWER);		
		dataviewer_modelViewer.setActionCommand("modelviewer");
		dataviewer_modelViewer.addActionListener(menuHandler);
		
		JMenuItem dataviewer_outfitter = new JMenuItem(Strings.MENUITEM_OUTFITTER);		
		dataviewer_outfitter.setActionCommand("outfitter");
		dataviewer_outfitter.addActionListener(menuHandler);
		
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

		JMenuItem tools_findexhs = new JMenuItem(Strings.MENUITEM_FIND_EXH);		
		tools_findexhs.setActionCommand("find_exh");
		tools_findexhs.addActionListener(menuHandler);
		
		JMenuItem tools_findmusic = new JMenuItem(Strings.MENUITEM_FIND_MUSIC);		
		tools_findmusic.setActionCommand("find_music");
		tools_findmusic.addActionListener(menuHandler);
		
		JMenuItem tools_findmaps = new JMenuItem(Strings.MENUITEM_FIND_MAPS);		
		tools_findmaps.setActionCommand("find_maps");
		tools_findmaps.addActionListener(menuHandler);
		
		JMenuItem options_settings =  new JMenuItem(Strings.MENUITEM_SETTINGS);		
		options_settings.setActionCommand("settings");
		options_settings.addActionListener(menuHandler);		
		
		Preferences prefs = Preferences.userNodeForPackage(com.fragmenterworks.ffxivextract.Main.class);
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
		
		dataviewers.add(dataviewer_modelViewer);
		dataviewers.add(dataviewer_outfitter);
		
		tools.add(tools_musicswapper);
		tools.add(tools_macroEditor);
		tools.add(tools_logViewer);
		tools.add(tools_hashcalculator);
		tools.addSeparator();
		tools.add(tools_findexhs);
		tools.add(tools_findmusic);
		tools.add(tools_findmaps);
		
		options.add(options_settings);
		options.add(options_enableUpdate);
		
		help.add(help_About);
		
		//Super Menus
		menu.add(file);
		menu.add(search);
		menu.add(dataviewers);
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
				 JLabel lblFNFError = new JLabel("This is currently a placeholder, there is no data here.");
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
					
			try{
				if (HashDatabase.getFolder(file.getId2()) == null)
					view = new OpenGL_View(new Model(null, currentIndexFile, data));
				else
					view = new OpenGL_View(new Model(HashDatabase.getFolder(file.getId2()) + "/" + file.getName(), currentIndexFile, data));				
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
		else if (data.length >= 4 && data[0] == 'X' && data[1] == 'F' && data[2] == 'V' && data[3] == 'A')
		{
			AVFX_File avfxFile = new AVFX_File(data);
			avfxFile.printOut();
		}
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
		else if (data.length >= 4 && data[0] == 'p' && data[1] == 'a' && data[2] == 'p' && data[3] == ' ')
		{
			try {
				PAP_View papView = new PAP_View(new PAP_File(data));
				tabs.addTab("Animation File", papView);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (data.length >= 4 && data[0] == 'S' && data[1] == 'h' && data[2] == 'C' && data[3] == 'd')
		{
			try {
				Shader_View shaderView = new Shader_View(new SHCD_File(data));
				tabs.addTab("Shader File", shaderView);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (data.length >= 4 && data[0] == 'S' && data[1] == 'h' && data[2] == 'P' && data[3] == 'k')
		{
			try {
				Shader_View shaderView = new Shader_View(new SHPK_File(data));
				tabs.addTab("Shader Pack", shaderView);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (data.length >= 4 && data[0] == 'S' && data[1] == 'G' && data[2] == 'B' && data[3] == '1')
		{
			try {
				SGB_File sgbFile = new SGB_File(data);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (file.getName().equals("human.cmp"))
		{
			try {
				CMP_File cmpFile = new CMP_File(data);
				CMP_View cmpView = new CMP_View(cmpFile);
				tabs.addTab("CMP Viewer", cmpView);
			} catch (IOException e) {
				e.printStackTrace();
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
				return f.getName().endsWith(".csv") || f.getName().endsWith(".ogg") || f.getName().endsWith(".wav") ||f.getName().endsWith(".png") || f.getName().endsWith(".hkx") || f.getName().endsWith(".obj") || f.isDirectory();
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
		else if (data.length >= 4 && data[0] == 'p' && data[1] == 'a' && data[2] == 'p' && data[3] == ' ' )		
			return ".hkx";
		else if (data.length >= 4 && data[0] == 'b' && data[1] == 'l' && data[2] == 'k' && data[3] == 's' )		
			return ".hkx";		
		else if (data.length >= 4 && data[0] == 'S' && data[1] == 'h' && data[2] == 'C' && data[3] == 'd' )		
			return ".cso";
		else if (data.length >= 4 && data[0] == 'S' && data[1] == 'h' && data[2] == 'P' && data[3] == 'k' )		
			return ".shpk";
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
					
					if (currentIndexFile.getContentType(files.get(i).getOffset()) == 1)
						continue;
					
					byte[] data = currentIndexFile.extractFile(files.get(i).getOffset(), loadingDialog);
					byte[] dataToSave = null;										
					
					String extension = getExtension(currentIndexFile.getContentType(files.get(i).getOffset()), data);
					
					if (extension.equals(".exh") && doConvert)
					{
						if (tempView != null && tempView.isSame(files.get(i).getName()))
							continue;
						EXHF_File file = new EXHF_File(data);
						
						tempView = new EXDF_View(currentIndexFile, HashDatabase.getFolder(fileTree.getSelectedFiles().get(i).getId2()) + "/" + fileTree.getSelectedFiles().get(i).getName(), file);						
						
						for (int l = 0; l < (tempView.getNumLangs()); l++)
						{
											
							String path = lastSaveLocation.getCanonicalPath();
							
							if (fileName == null)						
								fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
															
							if (folderName == null)
								folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
							
							path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
							
							File mkDirPath = new File(path);
							mkDirPath.getParentFile().mkdirs();						
													
							tempView.saveCSV(path + EXHF_File.languageCodes[tempView.getExhFile().getLanguageTable()[l]] +  ".csv", l);
														
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
						
						for (int l = 0; l < EXHF_File.languageCodes.length; l++)
							exhName = exhName.replace(String.format("%s.exd", EXHF_File.languageCodes[l]), "");
						
						exhName = exhName.substring(0, exhName.lastIndexOf("_")) +".exh";
						
						for (int l = 0; l < (tempView.getNumLangs()); l++)
						{	
							String path = lastSaveLocation.getCanonicalPath();
							
							fileName = exhName;
															
							if (folderName == null)
								folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
							
							path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
							
							File mkDirPath = new File(path);
							mkDirPath.getParentFile().mkdirs();																																	
							
							tempView.saveCSV(path + EXHF_File.languageCodes[tempView.getExhFile().getLanguageTable()[l]] + ".csv", l);
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
					else if (extension.equals(".hkx") && doConvert)
					{
						if (data.length >= 4 && data[0] == 'p' && data[1] == 'a' && data[2] == 'p' && data[3] == ' ' )		
						{
							PAP_File pap = new PAP_File(data);
							dataToSave = pap.getHavokData();
						}
						else if (data.length >= 4 && data[0] == 'b' && data[1] == 'l' && data[2] == 'k' && data[3] == 's' )
						{
							SKLB_File pap = new SKLB_File(data);
							dataToSave = pap.getHavokData();
						}
						
						extension = ".hkx";
					}
					else if (extension.equals(".cso") && doConvert)
					{						
						SHCD_File shader = new SHCD_File(data);
						dataToSave = shader.getShaderBytecode();
						extension = ".cso";
					}
					else if (extension.equals(".shpk") && doConvert)
					{
						try {
							SHPK_File shader = new SHPK_File(data);
							
							for (int j = 0; j < shader.getNumVertShaders(); j++)
							{
								dataToSave = shader.getShaderBytecode(j);
								extension = ".vs.cso";
								String path = lastSaveLocation.getCanonicalPath();
								
								if (fileName == null)				
									fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
																
								if (folderName == null)
									folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
								
								path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
								
								File mkDirPath = new File(path);
								mkDirPath.getParentFile().mkdirs();																															
								
								LERandomAccessFile out = new LERandomAccessFile(path + j + extension, "rw");
								out.write(dataToSave, 0, dataToSave.length);
								out.close();
							}
							for (int j = 0; j < shader.getNumPixelShaders(); j++)
							{
								dataToSave = shader.getShaderBytecode(shader.getNumVertShaders()+j);
								extension = ".ps.cso";
								String path = lastSaveLocation.getCanonicalPath();
								
								if (fileName == null)				
									fileName = String.format("%X", files.get(i).getId() & 0xFFFFFFFF);
																
								if (folderName == null)
									folderName = String.format("%X", files.get(i).getId2() & 0xFFFFFFFF);
								
								path = lastSaveLocation.getCanonicalPath() + "\\" + folderName + "\\" + fileName;
								
								File mkDirPath = new File(path);
								mkDirPath.getParentFile().mkdirs();																															
								
								LERandomAccessFile out = new LERandomAccessFile(path + j + extension, "rw");
								out.write(dataToSave, 0, dataToSave.length);
								out.close();
							}
							
						} catch (IOException e) {
							e.printStackTrace();
						}
						
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
