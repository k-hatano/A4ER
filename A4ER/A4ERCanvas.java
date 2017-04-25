import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERCanvas extends Canvas {

	A4ER parent;
	ArrayList<HashMap<String, String>> lERList = new ArrayList<HashMap<String, String>>();

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
		for (HashMap<String, String> item : lERList) {
			grp.drawString(item.get("name"), 0, y);
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
			ArrayList<String> list = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				list.add(line);
				line = reader.readLine();
			}

			Collections.reverse(list);
			for (String str : list) {
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("name", str);
				lERList.add(item);
			}

			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}