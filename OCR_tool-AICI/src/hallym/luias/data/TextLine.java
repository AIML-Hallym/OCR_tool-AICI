package hallym.luias.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TextLine {

	private String text;
	private double sX, eX;
	
	public TextLine(JSONObject obj) {
		JSONArray vertices = (JSONArray)obj.get("location");
		
		sX = (double)(((JSONObject)vertices.get(0)).get("x"));
		eX = (double)(((JSONObject)vertices.get(1)).get("x"));
		
		text = (String)obj.get("text");
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
	
}
