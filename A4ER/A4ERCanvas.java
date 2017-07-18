import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERCanvas extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	A4ER parent;
	ArrayList<Entity> lEREntities = new ArrayList<Entity>();
	HashMap<String, ArrayList<Field>> lERFields = new HashMap<String, ArrayList<Field>>();
	ArrayList<Relation> lERReleations = new ArrayList<Relation>();
	ArrayList<String> lPages = new ArrayList<String>();

	int originalX, originalY;
	int scrollX, scrollY;
	int clickedX, clickedY;
	boolean dragging = false;

	public int maxWidth = 0;
	public int maxHeight = 0;

	float xRate = 1.4f;
	float yRate = 1.4f;

	long lastClickedTime = 0;
	int selectedIndex = 0;

	File lastFile = null;

	public A4ERCanvas(A4ER a4er) {
		parent = a4er;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
	}

	public void paint(final Graphics g){
		int w = this.getWidth();
		int h = this.getHeight();
		maxWidth = 0;
		maxHeight = 0;

		int level = parent.cbLevel.getSelectedIndex();

		Image img = createImage(w,h);
		Graphics2D grp = (Graphics2D)(img.getGraphics());

		if (parent.miAntialiasing.getState()) {
			grp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            	            RenderingHints.VALUE_ANTIALIAS_ON);
		}

		String currentPage = (String) parent.cbPage.getSelectedItem();
		if (currentPage != null) {
			currentPage = currentPage.toUpperCase();
		}
		FontMetrics metrics = grp.getFontMetrics();

		grp.setColor(Color.white);
		grp.fillRect(0, 0, w, h);

		grp.setColor(Color.black);
		for (Entity item : lEREntities) {
			Position position = item.positionInPage(currentPage);
			if (position == null) {
				continue;
			}

			grp.drawString(item.logicalName, 
				(int)(position.x / xRate + scrollX),
				(int)(position.y / yRate - 2 + scrollY));

			if (level == 1) {
				int tableLogicalNameWidth = metrics.getStringBounds(item.logicalName, grp).getBounds().width;

				grp.drawString(item.physicalName, 
					(int)(position.x / xRate + tableLogicalNameWidth + 8 + scrollX),
					(int)(position.y / yRate - 2 + scrollY));
			}

			if (item.physicalNameWidth == 0) {
				int physicalNameWidth = 0;
				for (Field field : lERFields.get(item.physicalName)) {
					physicalNameWidth = metrics.getStringBounds(field.key, grp).getBounds().width;
					if (item.physicalNameWidth < physicalNameWidth) {
						item.physicalNameWidth = physicalNameWidth;
					}
				}
			}

			if (item.logicalNameWidth == 0) {
				int logicalNameWidth = 0;
				for (Field field : lERFields.get(item.physicalName)) {
					logicalNameWidth = metrics.getStringBounds(field.name, grp).getBounds().width;
					if (item.logicalNameWidth < logicalNameWidth) {
						item.logicalNameWidth = logicalNameWidth;
					}
				}
			}

			if (item.typeWidth == 0) {
				int typeWidth = 0;
				for (Field field : lERFields.get(item.physicalName)) {
					typeWidth = metrics.getStringBounds(field.type, grp).getBounds().width;
					if (item.typeWidth < typeWidth) {
						item.typeWidth = typeWidth;
					}
				}
			}

			int left = (int)(position.x / xRate + scrollX - 2);
			int top = (int)(position.y / yRate + scrollY);
			int width = item.logicalNameWidth + 8;
			int height = lERFields.get(item.physicalName).size() * 16 + 2;
			if (level == 1) {
				width += item.physicalNameWidth;
			} else if (level == 2) {
				width += item.typeWidth;
			}
			item.tmpWidth = width;
			item.tmpHeight = height;

			grp.setColor(Color.white);
			grp.fillRect(left, top, width, height);
			grp.setColor(Color.black);
			grp.drawRect(left, top, width, height);

			if (maxWidth < left + width) {
				maxWidth = left + width;
			}

			if (maxHeight < top + height) {
				maxHeight = top + height;
			}
		}

		for (String key : lERFields.keySet()) {
			Entity entity = Entity.entityNamed(lEREntities, key);
			if (entity == null) {
				continue;
			}
			Position position = entity.positionInPage(currentPage);
			if (position == null) {
				continue;
			}

			ArrayList<Field> fields = lERFields.get(key);
			int x = (int)(position.x / xRate + scrollX);
			int y = (int)(position.y / yRate + scrollY);
			for (Field item : fields) {
				y += 16;
				grp.drawString(item.name, x, y);
				if (level == 1) {
					grp.drawString(item.key, x + entity.logicalNameWidth + 4, y);
				} else if (level == 2) {
					grp.drawString(item.type, x + entity.logicalNameWidth + 4, y);
				}
			}
		}

		for (Relation relation : lERReleations) {
			Entity entity1 = Entity.entityNamed(lEREntities, relation.entity1);
			Entity entity2 = Entity.entityNamed(lEREntities, relation.entity2);
			if (entity1 == null || entity2 == null) {
				continue;
			}
			Position position1 = entity1.positionInPage(currentPage);
			Position position2 = entity2.positionInPage(currentPage);

			if (position1 == null || position2 == null) {
				continue;
			}

			Position tmpPosition1 = new Position(position1);
			Position tmpPosition2 = new Position(position2);

			tmpPosition1.x = (int)(tmpPosition1.x / xRate);
			tmpPosition1.y = (int)(tmpPosition1.y / yRate);
			tmpPosition2.x = (int)(tmpPosition2.x / xRate);
			tmpPosition2.y = (int)(tmpPosition2.y / yRate);

			Point left1 = new Point(tmpPosition1.x , 
				(tmpPosition1.y + tmpPosition1.y + entity1.tmpHeight) / 2);
			Point top1 = new Point((tmpPosition1.x + tmpPosition1.x + entity1.tmpWidth) / 2 ,
				tmpPosition1.y);
			Point right1 = new Point(tmpPosition1.x + entity1.tmpWidth ,
				(tmpPosition1.y + tmpPosition1.y + entity1.tmpHeight) / 2);
			Point bottom1 = new Point((tmpPosition1.x + tmpPosition1.x + entity1.tmpWidth) / 2 , 
				tmpPosition1.y + entity1.tmpHeight);

			Point left2 = new Point(tmpPosition2.x ,
				(tmpPosition2.y + tmpPosition2.y + entity2.tmpHeight) / 2);
			Point top2 = new Point((tmpPosition2.x + tmpPosition2.x + entity2.tmpWidth) / 2 ,
				tmpPosition2.y);
			Point right2 = new Point(tmpPosition2.x + entity2.tmpWidth ,
				(tmpPosition2.y + tmpPosition2.y + entity2.tmpHeight) / 2);
			Point bottom2 = new Point((tmpPosition2.x + tmpPosition2.x + entity2.tmpWidth) / 2 ,
				 tmpPosition2.y + entity2.tmpHeight);

			int minDist = (int)Math.min(left1.manhattanDistanceTo(right2, Point.SITUATION_LEFT_TO_RIGHT),
				Math.min(top1.manhattanDistanceTo(bottom2, Point.SITUATION_TOP_TO_BOTTOM),
					Math.min(right1.manhattanDistanceTo(left2, Point.SITUATION_RIGHT_TO_LEFT),
						bottom1.manhattanDistanceTo(top2, Point.SITUATION_BOTTOM_TO_TOP))));

			int rab1 = relation.bar1;
			int rab2 = relation.bar2;
			int rab3 = relation.bar3;

			int bar1 = 1000 - rab1;
			int bar2 = 1000 - rab2;
			int bar3 = 1000 - rab3;

			if (entity1.physicalName.equals(entity2.physicalName)) {
				grp.drawLine(right1.x + scrollX, (top1.y * bar1 + bottom1.y * rab1) / 1000 + scrollY, 
					right1.x + 16 + scrollX, (top1.y * bar1 + bottom1.y * rab1) / 1000 + scrollY);
				grp.drawLine(right1.x + 16 + scrollX, (top1.y * bar1 + bottom1.y * rab1) / 1000 + scrollY, 
					right1.x + 16 + scrollX, (top1.y * bar3 + bottom1.y * rab3) / 1000 + scrollY);
				grp.drawLine(right2.x + 16 + scrollX, (top2.y * bar3 + bottom2.y * rab3) / 1000 + scrollY, 
					right2.x + scrollX, (top2.y * bar3 + bottom2.y * rab3) / 1000 + scrollY);
			} else if (minDist == left1.manhattanDistanceTo(right2, Point.SITUATION_LEFT_TO_RIGHT)) {
				left1 = new Point(tmpPosition1.x , 
					(tmpPosition1.y * bar1 + (tmpPosition1.y + entity1.tmpHeight) * rab1) / 1000);
				right2 = new Point(tmpPosition2.x + entity2.tmpWidth ,
					(tmpPosition2.y * bar3 + (tmpPosition2.y + entity2.tmpHeight) * rab3) / 1000);

				grp.drawLine(left1.x + scrollX, left1.y + scrollY, 
					(left1.x * bar2 + right2.x * rab2) / 1000 + scrollX, left1.y + scrollY);
				grp.drawLine((left1.x * bar2 + right2.x * rab2) / 1000 + scrollX, left1.y + scrollY, 
					(left1.x * bar2 + right2.x * rab2) / 1000 + scrollX, right2.y + scrollY);
				grp.drawLine((left1.x * bar2 + right2.x * rab2) / 1000 + scrollX, right2.y + scrollY, 
					right2.x + scrollX, right2.y + scrollY);
			} else if (minDist == top1.manhattanDistanceTo(bottom2, Point.SITUATION_TOP_TO_BOTTOM))  {
				top1 = new Point((tmpPosition1.x * bar1 + (tmpPosition1.x + entity1.tmpWidth) * rab1) / 1000 ,
					tmpPosition1.y);
				bottom2 = new Point((tmpPosition2.x * bar3 + (tmpPosition2.x + entity2.tmpWidth) * rab3) / 1000 ,
					tmpPosition2.y + entity2.tmpHeight);

				grp.drawLine(top1.x + scrollX, top1.y + scrollY, 
					top1.x + scrollX, (top1.y * bar2 + bottom2.y * rab2) / 1000 + scrollY);
				grp.drawLine(top1.x + scrollX, (top1.y * bar2 + bottom2.y * rab2) / 1000 + scrollY, 
					bottom2.x + scrollX, (top1.y * bar2 + bottom2.y * rab2) / 1000 + scrollY);
				grp.drawLine(bottom2.x + scrollX, (top1.y * bar2 + bottom2.y * rab2) / 1000 + scrollY, 
					bottom2.x + scrollX, bottom2.y + scrollY);
			} else if (minDist == right1.manhattanDistanceTo(left2, Point.SITUATION_RIGHT_TO_LEFT))  {
				right1 = new Point(tmpPosition1.x + entity1.tmpWidth ,
					(tmpPosition1.y * bar1 + (tmpPosition1.y + entity1.tmpHeight) * rab1) / 1000);
				left2 = new Point(tmpPosition2.x ,
					(tmpPosition2.y * bar3 + (tmpPosition2.y + entity2.tmpHeight) * rab3) / 1000);

				grp.drawLine(right1.x + scrollX, right1.y + scrollY, 
					(right1.x * bar2 + left2.x * rab2) / 1000 + scrollX, right1.y + scrollY);
				grp.drawLine((right1.x * bar2 + left2.x * rab2) / 1000 + scrollX, right1.y + scrollY, 
					(right1.x * bar2 +left2.x * rab2) / 1000 + scrollX, left2.y + scrollY);
				grp.drawLine((right1.x * bar2 + left2.x * rab2) / 1000 + scrollX, left2.y + scrollY,
					left2.x + scrollX, left2.y + scrollY);
			} else {
				bottom1 = new Point((tmpPosition1.x * bar1 + (tmpPosition1.x + entity1.tmpWidth) * rab1) / 1000 , 
					tmpPosition1.y + entity1.tmpHeight);
				top2 = new Point((tmpPosition2.x * bar3 + (tmpPosition2.x + entity2.tmpWidth) * rab3) / 1000 ,
					tmpPosition2.y);

				grp.drawLine(bottom1.x + scrollX, bottom1.y + scrollY, 
					bottom1.x + scrollX, (bottom1.y * bar2 + top2.y * rab2) / 1000 + scrollY);
				grp.drawLine(bottom1.x + scrollX, (bottom1.y * bar2 + top2.y * rab2) / 1000 + scrollY, 
					top2.x + scrollX, (bottom1.y * bar2 + top2.y * rab2) / 1000 + scrollY);
				grp.drawLine(top2.x + scrollX, (bottom1.y * bar2 + top2.y * rab2) / 1000 + scrollY, 
					top2.x + scrollX, top2.y + scrollY);
			}
		}

		if (dragging) {
			grp.setColor(Color.red);
			grp.drawRect(scrollX, scrollY, maxWidth - scrollX, maxHeight - scrollY);

			grp.drawString("" + (-scrollX) + "," + (-scrollY), 2, 16);
		}

		g.drawImage(img, 0, 0, this);
	}

	public void showImportFileDialog() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setFileFilter(new FileNameExtensionFilter("ER Diagram", "a5er"));
		if (lastFile != null) {
			chooser.setSelectedFile(lastFile);
		}
		int res = chooser.showOpenDialog(this);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			lastFile = file;
			importA5ER(file.getAbsolutePath());
		}
	}

	public void importA5ER(String path) {
		File file = new File(path);
		parent.setTitle(file.getName());
		try {
			lEREntities = new ArrayList<Entity>();
			lERFields = new HashMap<String, ArrayList<Field>>();
			lERReleations = new ArrayList<Relation>();
			lPages = new ArrayList<String>();

			ArrayList<String> list = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
			String line = reader.readLine();
			while (line != null) {
				list.add(line);
				line = reader.readLine();
			}


			Entity entity = new Entity();
			ArrayList<Field> fields = new ArrayList<Field>();
			Field field = new Field();
			Relation relation = new Relation();
			int level = 0;

			Collections.reverse(list);
			for (String str : list) {
				final Pattern p1 = Pattern.compile("^Position=\\\"(.*)\\\",(\\d+),(\\d+)");
				Matcher m1 = p1.matcher(str);
				if (m1.find()) {
					String page = m1.group(1).toUpperCase();

					Position position = new Position();
					position.x = Integer.parseInt(m1.group(2));
					position.y = Integer.parseInt(m1.group(3));
					position.page = page;
					entity.positions.add(position);

					continue;
				}

				final Pattern p2 = Pattern.compile("^Field=\\\"(.*?)\\\",\\\"(.*?)\\\",\\\"(.*?)\\\"");
				Matcher m2 = p2.matcher(str);
				if (m2.find()) {
					field.name = m2.group(1);
					field.key = m2.group(2);
					field.type = m2.group(3);
					fields.add(0, field);
					field = new Field();
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
					lEREntities.add(0, entity);
					lERFields.put(entity.physicalName ,fields);
					entity = new Entity();
					fields = new ArrayList<Field>();
					field = new Field();
					relation = new Relation();
					continue;
				}

				final Pattern p6 = Pattern.compile("^Entity1=(.+)");
				Matcher m6 = p6.matcher(str);
				if (m6.find()) {
					relation.entity1 = m6.group(1);
					continue;
				}

				final Pattern p7 = Pattern.compile("^Entity2=(.+)");
				Matcher m7 = p7.matcher(str);
				if (m7.find()) {
					relation.entity2 = m7.group(1);
					continue;
				}

				final Pattern p8 = Pattern.compile("^\\[Relation\\]");
				Matcher m8 = p8.matcher(str);
				if (m8.find()) {
					lERReleations.add(0, relation);
					entity = new Entity();
					fields = new ArrayList<Field>();
					field = new Field();
					relation = new Relation();
					continue;
				}

				final Pattern p9 = Pattern.compile("^ViewMode=(\\d+)");
				Matcher m9 = p9.matcher(str);
				if (m9.find()) {
					int tmpLevel = Integer.parseInt(m9.group(1));
					switch (tmpLevel) {
						case 0:
						case 1:
						case 2:
						level = 0;
						break;
						case 3:
						case 4:
						case 6:
						level = 2;
						break;
						case 5:
						case 7:
						level = 1;
						break;
						default:
						level = 0;
						break;
					}
					continue;
				}

				final Pattern p10 = Pattern.compile("^PageInfo=\"([^\"]*)\"");
				Matcher m10 = p10.matcher(str);
				if (m10.find()) {
					lPages.add(0, m10.group(1).toUpperCase());
					continue;
				}

				final Pattern p11 = Pattern.compile("^Bar1=(\\d+)");
				Matcher m11 = p11.matcher(str);
				if (m11.find()) {
					relation.bar1 = Integer.parseInt(m11.group(1));
					continue;
				}

				final Pattern p12 = Pattern.compile("^Bar2=(\\d+)");
				Matcher m12 = p12.matcher(str);
				if (m12.find()) {
					relation.bar2 = Integer.parseInt(m12.group(1));
					continue;
				}

				final Pattern p13 = Pattern.compile("^Bar3=(\\d+)");
				Matcher m13 = p13.matcher(str);
				if (m13.find()) {
					relation.bar3 = Integer.parseInt(m13.group(1));
					continue;
				}
				
			}

			parent.cbPage.removeAllItems();
			for (String page : lPages) {
				parent.cbPage.addItem(page);
			}
			parent.cbLevel.setSelectedIndex(level);
			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (System.currentTimeMillis() - lastClickedTime < 1000) {
			if (scrollX > 0) {
				scrollX = 0;
			}
			if (scrollY > 0) {
				scrollY = 0;
			}
			if (scrollX < -(this.getWidth() + maxWidth)) {
				scrollX = -(this.getWidth() + maxWidth);
			}
			if (scrollY < -(this.getHeight() + maxHeight)) {
				scrollY = -(this.getHeight() + maxHeight);
			}
			repaint();
		}
		lastClickedTime = System.currentTimeMillis();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		originalX = scrollX;
		originalY = scrollY;
		clickedX = arg0.getX();
		clickedY = arg0.getY();
		dragging = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		dragging = false;
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if (dragging) {
			if (arg0.getButton() == MouseEvent.BUTTON2) {
				scrollX = originalX + (arg0.getX() - clickedX) * 2;
				scrollY = originalY + (arg0.getY() - clickedY) * 2;
			} else {
				scrollX = originalX + (arg0.getX() - clickedX);
				scrollY = originalY + (arg0.getY() - clickedY);
			}
			selectedIndex = 0;
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (arg0.isShiftDown()) {
			scrollX += arg0.getWheelRotation();
		} else {
			scrollY += arg0.getWheelRotation();
		}
		selectedIndex = 0;
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent arg0){
		int keyCode = arg0.getKeyCode();
		switch (keyCode) {
			case KeyEvent.VK_UP:
			scrollY -= 8;
			repaint();
			break;
			case KeyEvent.VK_DOWN:
			scrollY += 8;
			repaint();
			break;
			case KeyEvent.VK_LEFT:
			scrollX -= 8;
			repaint();
			break;
			case KeyEvent.VK_RIGHT:
			scrollX += 8;
			repaint();
			break;
			case KeyEvent.VK_BACK_SPACE :
			scrollX = 0;
			scrollY = 0;
			repaint();
			break;
			default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0){

	}

	@Override
	public void keyTyped(KeyEvent arg0){
		
	}

	public int showEntitiesList() {
		ArrayList<String> entitiesList = new ArrayList<String>();

		for (Entity item : lEREntities) {
			entitiesList.add(item.logicalName);
		}

		Collections.sort(entitiesList);
		String[] entities = (String[])(entitiesList.toArray(new String[0]));

		if (entities.length <= 0) {
			JOptionPane.showMessageDialog(parent,"No entities loaded.", 
				"Entities List", JOptionPane.INFORMATION_MESSAGE);
		}

		String result = (String)JOptionPane.showInputDialog(parent,
			null,
			"Entities List",
			JOptionPane.INFORMATION_MESSAGE,
			null,
			entities,
			entities[selectedIndex]);
		
		if (result != null && result.length() > 0) {
			for (Entity item : lEREntities) {
				if (item.logicalName.equals(result)) {
					Position position = item.positions.get(0);
					parent.cbPage.setSelectedItem(position.page);
					scrollX = (int)(-position.x / xRate + 64);
					scrollY = (int)(-position.y / yRate + 64);
					this.selectedIndex = entitiesList.indexOf(result);
					repaint();
					break;
				}
			}
		}

		return 0;
	}

}