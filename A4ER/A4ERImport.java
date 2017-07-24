import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERImport {

	public static boolean importA5ER(String path, A4ERCanvas canvas) {
		File file = new File(path);
		canvas.parent.setTitle(file.getName());
		try {
			canvas.lEREntities = new ArrayList<Entity>();
			canvas.lERFields = new HashMap<String, ArrayList<Field>>();
			canvas.lERReleations = new ArrayList<Relation>();
			canvas.lPages = new ArrayList<String>();
			canvas.lComments = new ArrayList<Comment>();

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
			Comment comment = new Comment();
			int level = 0;

			Collections.reverse(list);
			for (String str : list) {
				Matcher matcher;

				final Pattern p1 = Pattern.compile("^Position=\\\"(.*)\\\",(\\d+),(\\d+)");
				matcher = p1.matcher(str);
				if (matcher.find()) {
					String page = matcher.group(1).toUpperCase();

					Position position = new Position();
					position.x = Integer.parseInt(matcher.group(2));
					position.y = Integer.parseInt(matcher.group(3));
					position.page = page;
					entity.positions.add(position);

					continue;
				}

				final Pattern p2 = Pattern.compile("^Field=\\\"(.*?)\\\",\\\"(.*?)\\\",\\\"(.*?)\\\",([^,]*?),(\\d*?),");
				matcher = p2.matcher(str);
				if (matcher.find()) {
					field.logicalName = matcher.group(1);
					field.physicalName = matcher.group(2);
					field.type = matcher.group(3);
					field.notNull = matcher.group(4);
					field.key = matcher.group(5);
					fields.add(0, field);
					field = new Field();
					continue;
				}

				final Pattern p3 = Pattern.compile("^PName=(.+)");
				matcher = p3.matcher(str);
				if (matcher.find()) {
					entity.physicalName = matcher.group(1);
					continue;
				}

				final Pattern p4 = Pattern.compile("^LName=(.+)");
				matcher = p4.matcher(str);
				if (matcher.find()) {
					entity.logicalName = matcher.group(1);
					continue;
				}

				final Pattern p5 = Pattern.compile("^\\[Entity\\]");
				matcher = p5.matcher(str);
				if (matcher.find()) {
					canvas.lEREntities.add(0, entity);
					canvas.lERFields.put(entity.physicalName ,fields);
					entity = new Entity();
					fields = new ArrayList<Field>();
					field = new Field();
					relation = new Relation();
					comment = new Comment();
					continue;
				}

				final Pattern p6 = Pattern.compile("^Entity1=(.+)");
				matcher = p6.matcher(str);
				if (matcher.find()) {
					relation.entity1 = matcher.group(1);
					continue;
				}

				final Pattern p7 = Pattern.compile("^Entity2=(.+)");
				matcher = p7.matcher(str);
				if (matcher.find()) {
					relation.entity2 = matcher.group(1);
					continue;
				}

				final Pattern p8 = Pattern.compile("^\\[Relation\\]");
				matcher = p8.matcher(str);
				if (matcher.find()) {
					canvas.lERReleations.add(0, relation);
					entity = new Entity();
					fields = new ArrayList<Field>();
					field = new Field();
					relation = new Relation();
					comment = new Comment();
					continue;
				}

				final Pattern p9 = Pattern.compile("^ViewMode=(\\d+)");
				matcher = p9.matcher(str);
				if (matcher.find()) {
					int tmpLevel = Integer.parseInt(matcher.group(1));
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
				matcher = p10.matcher(str);
				if (matcher.find()) {
					canvas.lPages.add(0, matcher.group(1).toUpperCase());
					continue;
				}

				final Pattern p11 = Pattern.compile("^Bar1=(\\d+)");
				matcher = p11.matcher(str);
				if (matcher.find()) {
					relation.bar1 = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p12 = Pattern.compile("^Bar2=(\\d+)");
				matcher = p12.matcher(str);
				if (matcher.find()) {
					relation.bar2 = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p13 = Pattern.compile("^Bar3=(\\d+)");
				matcher = p13.matcher(str);
				if (matcher.find()) {
					relation.bar3 = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p14 = Pattern.compile("^RelationType1=(\\d+)");
				matcher = p14.matcher(str);
				if (matcher.find()) {
					relation.relationType1 = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p15 = Pattern.compile("^RelationType2=(\\d+)");
				matcher = p15.matcher(str);
				if (matcher.find()) {
					relation.relationType2 = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p16 = Pattern.compile("^Left=(\\d+)");
				matcher = p16.matcher(str);
				if (matcher.find()) {
					comment.left = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p17 = Pattern.compile("^Top=(\\d+)");
				matcher = p17.matcher(str);
				if (matcher.find()) {
					comment.top = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p18 = Pattern.compile("^Width=(\\d+)");
				matcher = p18.matcher(str);
				if (matcher.find()) {
					comment.width = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p19 = Pattern.compile("^Height=(\\d+)");
				matcher = p19.matcher(str);
				if (matcher.find()) {
					comment.height = Integer.parseInt(matcher.group(1));
					continue;
				}

				final Pattern p20 = Pattern.compile("^Page=(.+)");
				matcher = p20.matcher(str);
				if (matcher.find()) {
					comment.page = matcher.group(1);
					continue;
				}

				final Pattern p21 = Pattern.compile("^Comment=(.+)");
				matcher = p21.matcher(str);
				if (matcher.find()) {
					comment.comment = matcher.group(1);
					continue;
				}

				final Pattern p22 = Pattern.compile("^\\[Comment\\]");
				matcher = p22.matcher(str);
				if (matcher.find()) {
					canvas.lComments.add(0, comment);
					entity = new Entity();
					fields = new ArrayList<Field>();
					field = new Field();
					relation = new Relation();
					comment = new Comment();
					continue;
				}
				
			}

			canvas.parent.cbPage.removeAllItems();
			for (String page : canvas.lPages) {
				canvas.parent.cbPage.addItem(page);
			}
			canvas.parent.cbLevel.setSelectedIndex(level);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}