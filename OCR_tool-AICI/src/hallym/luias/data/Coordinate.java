package hallym.luias.data;

public class Coordinate <type>{

	private type x, y;
	
	public Coordinate(type x, type y){
		this.x = x;
		this.y = y;
	}
	
	public Integer[] toInt() {
		return new Integer[] {(Integer) x, (Integer) y};
	}
	
	public Double[] toDouble() {
		return new Double[] {(Double) x, (Double)y};
	}
	
	public type getX() {
		return x;
	}
	
	public type getY() {
		return y;
	}
	
}
