import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;

import javax.swing.*;

public class A4ER extends JFrame implements ActionListener {
	JPanel pHeaderPanel;
	JMenuBar mbMenuBar;
	JMenu mFile,mEdit,mERDiagram;
	JMenuItem miOpen,miQuit;
	JMenuItem miEntitiesList,miSearchString,miCancelSearching,miCopyPage;
	JCheckBoxMenuItem miAntialiasing,miGrid;
	JComboBox cbPage, cbLevel;
	A4ERCanvas a4erCanvas;
	
	A4ER() {
		super();

		setSize(720,732);
		setLayout(new BorderLayout());
		setTitle("A4ER");

		mbMenuBar = new JMenuBar();

		mFile = new JMenu("File");
		miOpen = new JMenuItem("Open...");
		miOpen.addActionListener(this);
		miOpen.setAccelerator(KeyStroke.getKeyStroke('O',KeyEvent.CTRL_MASK));
		mFile.add(miOpen);
		mFile.addSeparator();
		miQuit = new JMenuItem("Quit");
		miQuit.addActionListener(this);
		miQuit.setAccelerator(KeyStroke.getKeyStroke('Q',KeyEvent.CTRL_MASK));
		mFile.add(miQuit);
		mbMenuBar.add(mFile);


		mEdit = new JMenu("Edit");
		miCopyPage = new JMenuItem("Copy Page as Image");
		miCopyPage.setAccelerator(KeyStroke.getKeyStroke('C',KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
		miCopyPage.addActionListener(this);
		mEdit.add(miCopyPage);
		miEntitiesList = new JMenuItem("Entities List...");
		miEntitiesList.addActionListener(this);
		mEdit.add(miEntitiesList);
		mEdit.addSeparator();
		miSearchString = new JMenuItem("Search String and Show It In Red...");
		miSearchString.setAccelerator(KeyStroke.getKeyStroke('F',KeyEvent.CTRL_MASK));
		miSearchString.addActionListener(this);
		mEdit.add(miSearchString);
		miCancelSearching = new JMenuItem("Cancel Searching");
		miCancelSearching.addActionListener(this);
		mEdit.add(miCancelSearching);
		mbMenuBar.add(mEdit);

		mERDiagram = new JMenu("ER Diagram");
		miAntialiasing = new JCheckBoxMenuItem("Antialiasing");
		miAntialiasing.addActionListener(this);
		mERDiagram.add(miAntialiasing);
		miGrid = new JCheckBoxMenuItem("Grid");
		miGrid.addActionListener(this);
		mERDiagram.add(miGrid);
		mbMenuBar.add(mERDiagram);

		setJMenuBar(mbMenuBar);

		a4erCanvas = new A4ERCanvas(this);
		add("Center", a4erCanvas);

		pHeaderPanel = new JPanel();
		pHeaderPanel.setLayout(new BorderLayout());

		cbPage = new JComboBox();
		cbPage.addActionListener(e -> a4erCanvas.repaint());
		pHeaderPanel.add("Center", cbPage);

		cbLevel = new JComboBox();
		cbLevel.addActionListener(e -> a4erCanvas.repaint());
		pHeaderPanel.add("East", cbLevel);
		cbLevel.addItem("Keys");
		cbLevel.addItem("Attributes");
		cbLevel.addItem("Attributes & Types");
		cbLevel.addItem("Attributes (Physical & Logical Names)");
		cbLevel.addItem("Attributes (Physical & Logical Names) & Types");

		add("North", pHeaderPanel);

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				closeWindow();
			}
		});

		new DropTarget(this,new Dropper(this));
	}

	public static void main(String[] argv){
		A4ER a4er = new A4ER();
		a4er.show();
	}

	public void closeWindow(){
		System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == miQuit) {
			closeWindow();
		} else if (arg0.getSource() == miOpen) {
			a4erCanvas.showImportFileDialog();
		} else if (arg0.getSource() == miEntitiesList) {
			a4erCanvas.showEntitiesList();
		} else if (arg0.getSource() == miSearchString) {
			a4erCanvas.searchStringAndShowItInRed();
		} else if (arg0.getSource() == miCancelSearching) {
			a4erCanvas.cancelSearching();
		} else if (arg0.getSource() == miAntialiasing || arg0.getSource() == miGrid) {
			a4erCanvas.repaint();
		} else if (arg0.getSource() == miCopyPage) {
			a4erCanvas.copyPageAsImage();
		}
	}

	class Dropper extends DropTargetAdapter{

		A4ER superview;

		Dropper(A4ER a4er){
			super();
			superview = a4er;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void drop(DropTargetDropEvent arg0) {
			try {
				Transferable t = arg0.getTransferable();
				if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					arg0.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					File file = ((java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor)).get(0);
					superview.a4erCanvas.importA5ER(file.getAbsolutePath());
				}
			}
			catch (Exception ex){
				ex.printStackTrace(System.err);
			}
		}
	}
}