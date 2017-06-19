class Point {
	public int x;
	public int y;

	Point(int aX, int aY) {
		this.x = aX;
		this.y = aY;
	}

	public int distanceTo(Point aPoint){
		double dist = Math.sqrt( (this.x - aPoint.x) * (this.x - aPoint.x) + (this.y - aPoint.y) * (this.y - aPoint.y) );
		return (int)dist;
	}

	public int manhattanDistanceTo(Point aPoint){
		double dist = Math.abs(this.x - aPoint.x) + Math.abs(this.y - aPoint.y);
		return (int)dist;
	}
}