import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class A4ER extends JFrame implements ActionListener {
	JMenuBar mbMenuBar;
	JMenu mFile;
	JMenuItem miOpen,miQuit;
	JComboBox cbPage;
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

		cbPage = new JComboBox();
		add("North", cbPage);

		a4erCanvas = new A4ERCanvas(this);
		add("Center", a4erCanvas);

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				closeWindow();
			}
		});
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
}

