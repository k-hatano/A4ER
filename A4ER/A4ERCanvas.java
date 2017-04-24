import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERCanvas extends Canvas {

	A4ER parent;
	ArrayList<String> lList = new ArrayList<String>();

	public A4ERCanvas(A4ER a4er) {
		parent = a4er;
	}

	public void paint(final Graphics g){
		int w = this.getWidth();
		int h = this.getHeight();

		Image img = createImage(w,h);
		Graphics2D grp = (Graphics2D)(img.getGraphics());

		grp.setColor(Color.white);
		grp.fillRect(0, 0, w, h);

		grp.setColor(Color.black);
		int y = 0;
		for (String str : lList) {
			grp.drawString(str, 0, y);
			y += 16;
		}

		g.drawImage(img, 0, 0, this);
	}

	public void showImportFileDialog() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setFileFilter(new FileNameExtensionFilter("ERダイアグラム", "a5er"));
		int res = chooser.showOpenDialog(this);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file=chooser.getSelectedFile();
			importA5ER(file.getAbsolutePath());
		}
	}

	public void importA5ER(String path) {
		File file = new File(path);
		parent.setTitle(file.getName());
		try {
			lList = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String str = reader.readLine();
			while (str != null) {
				lList.add(str);
				str = reader.readLine();
			}
			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}