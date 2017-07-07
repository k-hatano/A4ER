class Point {
	public static final int SITUATION_NONE = 0;
	public static final int SITUATION_LEFT_TO_RIGHT = 1;
	public static final int SITUATION_TOP_TO_BOTTOM = 2;
	public static final int SITUATION_RIGHT_TO_LEFT = 3;
	public static final int SITUATION_BOTTOM_TO_TOP = 4;
	public static final int FAR_AWAY = 99999;

	public int x;
	public int y;

	Point(int aX, int aY) {
		this.x = aX;
		this.y = aY;
	}

	public int distanceTo(Point aPoint, int situation){
		double dist = Math.sqrt( (this.x - aPoint.x) * (this.x - aPoint.x) + (this.y - aPoint.y) * (this.y - aPoint.y) );
		
		if (situation == Point.SITUATION_LEFT_TO_RIGHT && this.x < aPoint.x) {
			return FAR_AWAY;
		} else if (situation == Point.SITUATION_TOP_TO_BOTTOM && this.y < aPoint.y) {
			return FAR_AWAY;
		} else if (situation == Point.SITUATION_RIGHT_TO_LEFT && this.x > aPoint.x) {
			return FAR_AWAY;
		} else if (situation == Point.SITUATION_BOTTOM_TO_TOP && this.y > aPoint.y) {
			return FAR_AWAY;
		}

		return (int)dist;
	}

	public int manhattanDistanceTo(Point aPoint, int situation){
		double dist = Math.abs(this.x - aPoint.x) + Math.abs(this.y - aPoint.y);

		if (situation == Point.SITUATION_LEFT_TO_RIGHT && this.x < aPoint.x) {
			return FAR_AWAY;
		} else if (situation == Point.SITUATION_TOP_TO_BOTTOM && this.y < aPoint.y) {
			return FAR_AWAY;
		} else if (situation == Point.SITUATION_RIGHT_TO_LEFT && this.x > aPoint.x) {
			return FAR_AWAY;
		} else if (situation == Point.SITUATION_BOTTOM_TO_TOP && this.y > aPoint.y) {
			return FAR_AWAY;
		}

		return (int)dist;
	}
}