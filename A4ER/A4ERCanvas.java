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

			int situation = Point.SITUATION_NONE;

			if (entity1.physicalName.equals(entity2.physicalName)) {
				grp.drawLine(right1.x + scrollX, (top1.y * bar1 + bottom1.y * rab1) / 1000 + scrollY, 
					right1.x + 16 + scrollX, (top1.y * bar1 + bottom1.y * rab1) / 1000 + scrollY);
				grp.drawLine(right1.x + 16 + scrollX, (top1.y * bar1 + bottom1.y * rab1) / 1000 + scrollY, 
					right1.x + 16 + scrollX, (top1.y * bar3 + bottom1.y * rab3) / 1000 + scrollY);
				grp.drawLine(right2.x + 16 + scrollX, (top2.y * bar3 + bottom2.y * rab3) / 1000 + scrollY, 
					right2.x + scrollX, (top2.y * bar3 + bottom2.y * rab3) / 1000 + scrollY);
				situation = Point.SITUATION_SELF_TO_SELF;
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
				situation = Point.SITUATION_LEFT_TO_RIGHT;
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
				situation = Point.SITUATION_TOP_TO_BOTTOM;
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
				situation = Point.SITUATION_RIGHT_TO_LEFT;
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
				situation = Point.SITUATION_BOTTOM_TO_TOP;
			}

			String relationType1 = "";
			if (relation.relationType1 == 1) {
				relationType1 = "0...1";
			} else if (relation.relationType1 == 2) {
				relationType1 = "1";
			} else if (relation.relationType1 == 3) {
				relationType1 = "0...N";
			} else if (relation.relationType1 == 4) {
				relationType1 = "1...N";
			} else if (relation.relationType1 == 5) {
				relationType1 = "N";
			}

			if (situation == Point.SITUATION_LEFT_TO_RIGHT) {
				grp.drawString(relationType1, left1.x + scrollX - 4 - metrics.getStringBounds(relationType1, grp).getBounds().width, 
					left1.y + scrollY - 4);
			} else if (situation == Point.SITUATION_TOP_TO_BOTTOM) {
				grp.drawString(relationType1, top1.x + scrollX + 4, 
					top1.y + scrollY - 16);
			} else if (situation == Point.SITUATION_RIGHT_TO_LEFT) {
				grp.drawString(relationType1, right1.x + scrollX + 4, 
					right1.y + scrollY - 4);
			} else if (situation == Point.SITUATION_BOTTOM_TO_TOP) {
				grp.drawString(relationType1, bottom1.x + scrollX + 4, 
					bottom1.y + scrollY + 16);
			} else {
				grp.drawString(relationType1, right1.x + scrollX + 4, 
					(top1.y * bar1 + bottom1.y * rab1) / 1000 + scrollY - 4);
			}

			String relationType2 = "";
			if (relation.relationType2 == 1) {
				relationType2 = "0...1";
			} else if (relation.relationType2 == 2) {
				relationType2 = "1";
			} else if (relation.relationType2 == 3) {
				relationType2 = "0...N";
			} else if (relation.relationType2 == 4) {
				relationType2 = "1...N";
			} else if (relation.relationType2 == 5) {
				relationType2 = "N";
			}

			if (situation == Point.SITUATION_LEFT_TO_RIGHT) {
				grp.drawString(relationType2, right2.x + scrollX + 4, 
					right2.y + scrollY - 4);
			} else if (situation == Point.SITUATION_TOP_TO_BOTTOM) {
				grp.drawString(relationType2, bottom2.x + scrollX + 4, 
					bottom2.y + scrollY + 16);
			} else if (situation == Point.SITUATION_RIGHT_TO_LEFT) {
				grp.drawString(relationType2, left2.x + scrollX - 4 - metrics.getStringBounds(relationType2, grp).getBounds().width, 
					left2.y + scrollY - 4);
			} else if (situation == Point.SITUATION_BOTTOM_TO_TOP) {
				grp.drawString(relationType2, top2.x + scrollX + 4, 
					top2.y + scrollY - 16);
			} else {
				grp.drawString(relationType2, right2.x + scrollX + 4, 
					(top2.y * bar3 + bottom2.y * rab3) / 1000 + scrollY - 4);
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
		A4ERImport.importA5ER(path, this);
		repaint();
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
			if (scrollX < -(this.getWidth() + maxWidth / xRate)) {
				scrollX = -(int)(this.getWidth() + maxWidth / xRate);
			}
			if (scrollY < -(this.getHeight() + maxHeight / yRate)) {
				scrollY = -(int)(this.getHeight() + maxHeight / yRate);
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
			if (arg0.getButton() == MouseEvent.BUTTON2 || arg0.isAltDown()) {
				scrollX = originalX + (arg0.getX() - clickedX) * 2;
				scrollY = originalY + (arg0.getY() - clickedY) * 2;
			} else {
				scrollX = originalX + (arg0.getX() - clickedX);
				scrollY = originalY + (arg0.getY() - clickedY);
			}
			if (arg0.isShiftDown()) {
				if (Math.abs(arg0.getX() - clickedX) > Math.abs(arg0.getY() - clickedY)) {
					scrollY = originalY;
				} else {
					scrollX = originalX;
				}
			}
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
			if (arg0.isAltDown()) {
				scrollY += 32;
			} else {
				scrollY += 8;
			}
			repaint();
			break;
			case KeyEvent.VK_DOWN:
			if (arg0.isAltDown()) {
				scrollY -= 32;
			} else {
				scrollY -= 8;
			}
			repaint();
			break;
			case KeyEvent.VK_LEFT:
			if (arg0.isAltDown()) {
				scrollX += 32;
			} else {
				scrollX += 8;
			}
			repaint();
			break;
			case KeyEvent.VK_RIGHT:
			if (arg0.isAltDown()) {
				scrollX -= 32;
			} else {
				scrollX -= 8;
			}
			repaint();
			break;
			case KeyEvent.VK_BACK_SPACE :
			case KeyEvent.VK_HOME :
			scrollX = 0;
			scrollY = 0;
			repaint();
			break;
			case KeyEvent.VK_END :
			scrollX = -(int)(this.getWidth() + maxWidth / xRate);
			scrollY = -(int)(this.getHeight() + maxHeight / yRate);
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