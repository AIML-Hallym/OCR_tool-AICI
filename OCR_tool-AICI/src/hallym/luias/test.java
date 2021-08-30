package hallym.luias;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class test {

	public static void main(String... strings) {

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File("./OCR_temp/수원지법_2020고합701_판결서/수원지법_2020고합701_판결서_page3"
							+ ".json"))));

			StringBuffer sb = new StringBuffer();

			String s;
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}

			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(sb.toString());

			JSONArray images = (JSONArray) obj.get("images");
			JSONArray fields = (JSONArray) ((JSONObject) images.get(0)).get("fields");

			templatePage1(fields);
			//System.out.println();
			
			//System.out.println(Page1Template(fields));
			//allWordPrint(fields);
			JSONObject newObject = new JSONObject();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void allWordPrint(JSONArray texts) {
		
		for(int i = 0; i < texts.size(); i++) {
			JSONObject obj = (JSONObject) texts.get(i);
			System.out.print((String)obj.get("inferText")+" ");
			
			if( (i + 1) % 10 == 0)
				System.out.println();
		}
		
	}

	public static String Page1Template(JSONArray texts) {
		JSONObject prev = null;

		StringBuffer sb = new StringBuffer();
		String single_word;
		boolean single_line_sep = false;
		for (int i = 0; i < texts.size(); i++) {
			JSONObject obj = (JSONObject) texts.get(i);
			if (prev == null) {
				prev = obj;
				sb.append((String) prev.get("inferText"));
				continue;
			}

			if (isSameLine((JSONObject) prev.get("boundingPoly"), (JSONObject) obj.get("boundingPoly"))) {
				// 같은 라인일 경우
				if (isBothSingle(prev, obj)) {
					// 이전 글자 현재 글자 둘다 외자 일경우,
					if (!isAlphabet(prev) && !isAlphabet(obj)) {
						// 둘다 알파벳이 아닐경우
						if (single_line_sep) {
							sb.append("\n");
							single_line_sep = false;
						}

						single_word = ((String) prev.get("inferText") + ((String) obj.get("inferText")));
						sb.append(single_word);
						//sb.append(" ");
					} else {

					}
				} else {

					if (single_line_sep) {
						sb.append("\n");
						sb.append((String) prev.get("inferText"));
						single_line_sep = false;
					}

					sb.append(" " + (String) obj.get("inferText"));
				}

			} else {
				if (!isSingle(obj))
					sb.append("\n" + (String) obj.get("inferText"));
				else
					single_line_sep = true;
			}

			prev = obj;
		}

		return sb.toString();
	}

	public static JSONObject getEndVertice(JSONObject obj) {
		return (JSONObject) ((JSONArray) obj.get("vertices")).get(1);
	}

	public static JSONObject getStartVertice(JSONObject obj) {
		return (JSONObject) ((JSONArray) obj.get("vertices")).get(0);
	}

	public static JSONObject[] getStartEndVertices(JSONObject obj) {
		JSONArray vertices = (JSONArray) obj.get("vertices");

		JSONObject[] vtics = new JSONObject[2];
		for (int i = 0; i < vtics.length; i++)
			vtics[i] = (JSONObject) vertices.get(i);

		return vtics;
	}

	public static boolean isSingle(JSONObject o) {
		String s = (String) o.get("inferText");
		return s.length() <= 1 ? true : false;
	}

	public static boolean isAlphabet(JSONObject o) {
		String s = (String) o.get("inferText");
		char c = s.charAt(0);
		if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122))
			return true;

		return false;
	}

	public static boolean isBothSingle(JSONObject o1, JSONObject o2) {
		String s1 = (String) o1.get("inferText");
		String s2 = (String) o2.get("inferText");

		return s1.length() <= 1 && s2.length() <= 1 ? true : false;
	}

	public static String templatePage1(JSONArray texts) {

		JSONObject prev = null;

		for (int i = 0; i < texts.size() - 2; i++) {
			JSONObject object = (JSONObject) texts.get(i);

			if (prev == null) {
				prev = object;

				System.out.print(object.get("inferText"));
			} else {
				if (isSameLine((JSONObject) prev.get("boundingPoly"), (JSONObject) object.get("boundingPoly"))) {
					System.out.print("\t");
					System.out.print(object.get("inferText"));
				} else {
					System.out.print("\n");
					System.out.print(object.get("inferText"));
				}

				prev = object;
			}

		}

		return null;
	}

	public static boolean isSameLine(JSONObject o1, JSONObject o2) {
		JSONArray o1_array = (JSONArray) o1.get("vertices");
		JSONArray o2_array = (JSONArray) o2.get("vertices");

		JSONObject o1_xy = (JSONObject) o1_array.get(0);
		JSONObject o2_xy = (JSONObject) o2_array.get(0);

		double o1_y = ((Double) o1_xy.get("y"));
		double o2_y = ((Double) o2_xy.get("y"));

		double gap = Math.abs(o1_y - o2_y);
		gap /= 100;

		return gap <= 0.4 ? true : false;
	}

}
