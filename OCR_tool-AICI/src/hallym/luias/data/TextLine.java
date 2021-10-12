package hallym.luias.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TextLine {

	private String text;
	private double sX, eX;
	private double sY, eY;
	
	public TextLine(JSONObject obj) {
		JSONArray vertices = (JSONArray)obj.get("location");
		
		sX = (double)(((JSONObject)vertices.get(0)).get("x"));
		sY = (double)(((JSONObject)vertices.get(0)).get("y"));
		eX = (double)(((JSONObject)vertices.get(1)).get("x"));
		eY = (double)(((JSONObject)vertices.get(1)).get("y"));
		
		text = (String)obj.get("text");
	}
	
	public void appendText(TextLine line) {
		this.text += " " + line.getText();
		this.eX = line.geteX();
		this.eY = line.geteY();
	}
	
	public String getText() {
		return text;
	}
	
	public double getsX() {
		return sX;
	}
	
	public double geteX() {
		return eX;
	}
	
	public double getsY() {
		return sY;
	}
	
	public double geteY() {
		return eY;
	}
	
	@Override
	public String toString() {
		
		return "Start with :"+ sX+ ", " + sY + " End with :" + eX + ", " + eY +" Text :" + text;
		
	}
	
}
