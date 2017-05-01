import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERCanvas extends Canvas {

	A4ER parent;
	ArrayList<Entity> lEREntities = new ArrayList<Entity>();
	HashMap<String, ArrayList<HashMap<String, String>>> lERFields = new HashMap<String, ArrayList<HashMap<String, String>>>();
	ArrayList<String> lPages = new ArrayList<String>();

	public A4ERCanvas(A4ER a4er) {
		parent = a4er;
	}

	public void paint(final Graphics g){
		int w = this.getWidth();
		int h = this.getHeight();

		Image img = createImage(w,h);
		Graphics2D grp = (Graphics2D)(img.getGraphics());

		String currentPage = (String) parent.cbPage.getSelectedItem();

		grp.setColor(Color.white);
		grp.fillRect(0, 0, w, h);

		grp.setColor(Color.black);
		for (Entity item : lEREntities) {
			if (!item.page.equals(currentPage)) {
				continue;
			}

			grp.drawString(item.logicalName, 
				item.left,
				item.top);

			int left = item.left;
			int right = item.top;
			int width = item.logicalName.length() * 16;
			int height = lERFields.get(item.physicalName).size() * 16;

			grp.setColor(Color.white);
			grp.fillRect(left, right, width, height);
			grp.setColor(Color.black);
			grp.drawRect(left, right, width, height);
		}

		for (String key : lERFields.keySet()) {
			Entity entity = null;
			for (Entity anEntity : lEREntities) {
				if (!anEntity.page.equals(currentPage)) {
					continue;
				}

				if (anEntity.physicalName.equals(key)) {
					entity = anEntity;
					break;
				}
			}
			if (entity == null) {
				continue;
			}

			ArrayList<HashMap<String, String>> fields = lERFields.get(key);
			int x = entity.left;
			int y = entity.top;
			for (HashMap<String, String> item : fields) {
				y+=16;
				grp.drawString(item.get("name"),x,y);
			}
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
			lEREntities = new ArrayList<Entity>();
			lERFields = new HashMap<String, ArrayList<HashMap<String, String>>>();
			lPages = new ArrayList<String>();

			ArrayList<String> list = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				list.add(line);
				line = reader.readLine();
			}


			Entity entity = new Entity();
			ArrayList<HashMap<String, String>> fields = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> field = new HashMap<String, String>();
			entity.logicalName = "";
			entity.physicalName = "";

			Collections.reverse(list);
			for (String str : list) {
				final Pattern p1 = Pattern.compile("^Position=\\\"(.*)\\\",(\\d+),(\\d+)");
				Matcher m1 = p1.matcher(str);
				if (m1.find()) {
					entity.left = Integer.parseInt(m1.group(2));
					entity.top = Integer.parseInt(m1.group(3));
					entity.page = m1.group(1);
					if (lPages.indexOf(entity.page) < 0) {
						lPages.add(entity.page);
					}

					continue;
				}

				final Pattern p2 = Pattern.compile("^Field=\\\"(.*?)\\\",\\\"(.*?)\\\",\\\"(.*?)\\\"");
				Matcher m2 = p2.matcher(str);
				if (m2.find()) {
					field.put("name", m2.group(1));
					field.put("key", m2.group(2));
					field.put("type", m2.group(3));
					fields.add(field);
					field = new HashMap<String, String>();
					continue;
				}

				final Pattern p3 = Pattern.compile("^PName=(.+)");
				Matcher m3 = p3.matcher(str);
				if (m3.find()) {
					entity.physicalName = m3.group(1);
					continue;
				}

				final Pattern p4 = Pattern.compile("^LName=(.+)");
				Matcher m4 = p4.matcher(str);
				if (m4.find()) {
					entity.logicalName = m4.group(1);
					continue;
				}

				final Pattern p5 = Pattern.compile("^\\[Entity\\]");
				Matcher m5 = p5.matcher(str);
				if (m5.find()) {
					lEREntities.add(entity);
					lERFields.put(entity.physicalName ,fields);
					entity = new Entity();
					fields = new ArrayList<HashMap<String, String>>();
					field = new HashMap<String, String>();
					entity.logicalName = "";
					entity.physicalName = "";
					continue;
				}
			}

			parent.cbPage.removeAllItems();
			for (String page : lPages) {
				parent.cbPage.addItem(page);
			}
			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}