import java.util.*;

public class Entity {
	public String physicalName;
	public String logicalName;
	public ArrayList<Position> positions;

	public int physicalNameWidth;
	public int logicalNameWidth;
	public int typeWidth;
	public int keys;

	public int tmpLeft;
	public int tmpTop;
	public int tmpWidth;
	public int tmpHeight;

	public Entity() {
		super();
		positions = new ArrayList<Position>();
		physicalName = "";
		logicalName = "";
		keys = 0;
	}

	public static Entity entityNamed(ArrayList<Entity> entities,String entityName) {
		for (Entity entity : entities) {
			if (entity.physicalName.equals(entityName)) {
				return entity;
			}
		}
		return null;
	}

	public Position positionInPage(String page){
		for (Position position : positions) {
			if (position.page.equals(page)) {
				return position;
			}
		}
		return null;
	}
}