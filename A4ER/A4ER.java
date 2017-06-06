import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;

import javax.swing.*;

public class A4ER extends JFrame implements ActionListener {
	JPanel pHeaderPanel;
	JMenuBar mbMenuBar;
	JMenu mFile;
	JMenuItem miOpen,miQuit;
	JComboBox cbPage, cbMode;
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
		setJMenuBar(mbMenuBar);

		a4erCanvas = new A4ERCanvas(this);
		add("Center", a4erCanvas);

		pHeaderPanel = new JPanel();
		pHeaderPanel.setLayout(new BorderLayout());

		cbPage = new JComboBox();
		cbPage.addActionListener(e -> a4erCanvas.repaint());
		pHeaderPanel.add("Center", cbPage);

		cbMode = new JComboBox();
		cbPage.addActionListener(e -> a4erCanvas.repaint());
		pHeaderPanel.add("East", cbMode);
		cbMode.addItem("Attributes");

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