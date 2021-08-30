import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import hallym.luias.data.TextLine;

public class test2 {

	public static void main(String... strings) {

		String jug_name = "광주지법_2013고합85_판결서";
		
		ArrayList<TextLine> texts = new ArrayList<TextLine>();

		try {

			File dir = new File("./OCR_temp/"+jug_name);
			File[] files = dir.listFiles();
			
			JSONArray lines = new JSONArray();

			for (int i = 0; i < files.length; i++) {
				if(files[i].isDirectory())
					continue;
				
				if(files[i].getName().contains("png"))
					continue;
				
				if(files[i].getName().contains("주석"))
					continue;
				
				BufferedReader br = new BufferedReader(new InputStreamReader(

						new FileInputStream(files[i])));

				StringBuffer sb = new StringBuffer();

				String s;
				while ((s = br.readLine()) != null) {
					sb.append(s);
				}

				JSONParser parser = new JSONParser();
				JSONObject obj = (JSONObject) parser.parse(sb.toString());

				JSONArray images = (JSONArray) obj.get("images");
				JSONArray fields = (JSONArray) ((JSONObject) images.get(0)).get("fields");

				ConcatArray(lines, SentenceParser(fields));
			}

			JSONObject newObject = new JSONObject();
			newObject.put("textLines", lines);

			for (int i = 0; i < lines.size(); i++) {
				texts.add(new TextLine((JSONObject) lines.get(i)));
				System.out.println((texts.get(i).getText()));
			}

			//prettyPrint(newObject);

			//checkSentence(texts);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void checkSentence(ArrayList<TextLine> texts) {

		TextLine prev = null;
		ArrayList<String> temp = new ArrayList<String>();

		StringBuffer sb = new StringBuffer();

		System.out.println(texts.size());
		for (int i = 0; i < texts.size(); i++) {
			TextLine cur = texts.get(i);

			if (prev == null) {
				prev = cur;
				sb.append(prev.getText());
				continue;
			}

			// 같은 위치에서 시작하는가?
			if (isStartSamePos(prev, cur)) {
				// 이전 문장이 끝까지 다 쓰여졌는가?
				if (isPrevSentenceFullLength(prev)) {
					sb.append(" " + cur.getText());
				} else {
					// 이전 문장이 끝까지 다 쓰여지지 않았다면,
					temp.add(sb.toString());
					sb = new StringBuffer();

					sb.append(cur.getText());
					if (i >= texts.size() - 1) {
						temp.add(sb.toString());
					}
				}
			} else {
				if (isPrevSentenceFullLength(prev)) {
					sb.append(" " + cur.getText());
				} else {
					temp.add(sb.toString());
					sb = new StringBuffer();

					sb.append(cur.getText());

					if (i >= texts.size() - 1) {
						temp.add(sb.toString());
					}
				}
			}

			prev = cur;
		}

		for (int i = 0; i < temp.size(); i++) {

			System.out.println(temp.get(i));
			System.out.println();

		}

	}

	public static boolean isPrevSentenceFullLength(TextLine t1) {
		double gap = Math.abs(t1.geteX() - 2650);
		return (gap / 100d) <= 0.2d ? true : false;
	}

	public static boolean isStartSamePos(TextLine t1, TextLine t2) {
		double gap = Math.abs(t1.getsX() - t2.getsX());
		return (gap / 100d) <= 0.4d ? true : false;
	}

	public static void ConcatArray(JSONArray arr1, JSONArray arr2) {

		for (int i = 0; i < arr2.size(); i++) {
			arr1.add(arr2.get(i));
		}

	}

	public static JSONArray SentenceParser(JSONArray texts) {
		JSONObject prev = null;
		JSONObject pprev = null;

		StringBuffer sb = new StringBuffer();
		String single_word;
		boolean single_line_sep = false;

		JSONObject nType = new JSONObject();
		JSONArray lines = new JSONArray();
		JSONObject start_v = null, end_v = null;

		for (int i = 0; i < texts.size(); i++) {
			JSONObject obj = (JSONObject) texts.get(i);
			JSONObject temp = new JSONObject();

			if (prev == null) {
				prev = obj;
				start_v = getStartVertice((JSONObject) obj.get("boundingPoly"));
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
							end_v = getEndVertice((JSONObject) pprev.get("boundingPoly"));
							temp.put("text", sb.toString());
							temp.put("location", objsTOvertices(start_v, end_v));
							lines.add(temp);

							start_v = null;
							end_v = null;
							sb = new StringBuffer();
							single_line_sep = false;
						}

						if (start_v == null)
							start_v = getStartVertice((JSONObject) prev.get("boundingPoly"));

						single_word = ((String) prev.get("inferText") + ((String) obj.get("inferText")));
						sb.append(single_word);
					} else {

					}
				} else {

					if (single_line_sep) {
						end_v = getEndVertice((JSONObject) pprev.get("boundingPoly"));
						temp.put("text", sb.toString());
						temp.put("location", objsTOvertices(start_v, end_v));
						lines.add(temp);

						start_v = null;
						end_v = null;
						sb = new StringBuffer();

						if (start_v == null)
							start_v = getStartVertice((JSONObject) prev.get("boundingPoly"));

						sb.append((String) prev.get("inferText"));
						single_line_sep = false;
					}

					sb.append(" " + (String) obj.get("inferText"));
				}

			} else {
				if (!isSingle(obj)) {
					end_v = getEndVertice((JSONObject) prev.get("boundingPoly"));
					temp.put("text", sb.toString());
					temp.put("location", objsTOvertices(start_v, end_v));
					lines.add(temp);

					start_v = null;
					end_v = null;
					sb = new StringBuffer();

					start_v = getStartVertice((JSONObject) obj.get("boundingPoly"));
					sb.append((String) obj.get("inferText"));
				} else {
					single_line_sep = true;
				}
			}

			pprev = prev;
			prev = obj;

			if (i >= texts.size() - 1) {
				if (sb.length() > 0 && start_v != null) {
					end_v = getEndVertice((JSONObject) prev.get("boundingPoly"));
					temp.put("text", sb.toString());
					temp.put("location", objsTOvertices(start_v, end_v));
					lines.add(temp);
				}
			}

		}

		return lines;
	}

	public static void prettyPrint(JSONObject obj) {

		System.out.println("{");
		JSONArray lines = (JSONArray) obj.get("textLines");
		System.out.println("  \"textLines\":[");
		for (int i = 0; i < lines.size(); i++) {
			JSONObject text = (JSONObject) lines.get(i);
			System.out.println("    {");
			System.out.println("      \"text\":" + "\"" + (String) text.get("text") + "\",");
			JSONArray locs = (JSONArray) text.get("location");
			System.out.println("      \"location\":[");
			for (int k = 0; k < locs.size(); k++)
				System.out.println("          " + ((JSONObject) locs.get(k)).toString() + ",");
			System.out.println("      ]");
			System.out.println("    },");
		}
		System.out.println("  ],");
		System.out.println("  \"page_num\": \"\"");
		System.out.println("}");

	}

	public static JSONArray objsTOvertices(JSONObject obj1, JSONObject obj2) {
		JSONArray jarray = new JSONArray();
		jarray.add(obj1);
		jarray.add(obj2);

		return jarray;
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
