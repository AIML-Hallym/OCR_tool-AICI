package hallym.luias.funtions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.json.simple.JSONObject;

import hallym.luias.Main;

public class Network {

	public static File ClovaOCR(File image) {
		
		try {
			
			URL url = new URL(Main.CLOVA_OCR_INVOKE);
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setUseCaches(false);
			http.setDoInput(true);
			http.setDoOutput(true);
			
			http.setReadTimeout(3000000);
			http.setRequestMethod("POST");
			
			String boundary = "----" + UUID.randomUUID().toString().replace("-", "");
			http.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
			http.setRequestProperty("X-OCR-SECRET", Main.CLOVA_OCR_SECRET);
			
			http.setConnectTimeout(1000000);
			
			
			JSONObject obj = utils.getJSonObject();
			
			http.connect();
			DataOutputStream wr = new DataOutputStream(http.getOutputStream());
			writeMultiPart(wr, obj.toString(), image, boundary);
			wr.close();
			
			int reponseCode = http.getResponseCode();
			BufferedReader br;
			
			if(reponseCode == 200) {
				br = new BufferedReader(new InputStreamReader((InputStream)http.getContent(), "UTF-8"));
			}else {
				br = new BufferedReader(new InputStreamReader((InputStream)http.getContent(), "UTF-8"));
			}
			
			String inputLine;
			StringBuffer response = new StringBuffer();
			while((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			
			br.close();
			
			String name = image.getName().substring(0, image.getName().indexOf("."));
			File save = new File(image.getParent()+"/"+name+".json");
			
			PrintWriter pw = new PrintWriter(new FileOutputStream(save));
			pw.print(response.toString());
			pw.flush();
			
			pw.close();
			
			return save;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static void writeMultiPart(DataOutputStream wr, String json, File image, String boundary) {
		
		try {
			
			StringBuilder sb = new StringBuilder();
			sb.append("--").append(boundary).append("\r\n");
			sb.append("Content-Disposition:form-data; name=\"message\"\r\n\r\n");
			sb.append(json);
			sb.append("\r\n");
			
			wr.write(sb.toString().getBytes("UTF-8"));
			wr.flush();
			
			if(image != null && image.isFile()) {
				wr.write(("--"+boundary+"\r\n").getBytes("UTF-8"));
				StringBuilder fileString = new StringBuilder();
				fileString.append("Content-Disposition:form-data; name=\"file\"; filename=");
				fileString.append("\""+image.getName()+"\"\r\n");
				fileString.append("Content-Type: application/octet-stream\r\n\r\n");
				wr.write(fileString.toString().getBytes("UTF-8"));
				wr.flush();
				
				try (FileInputStream fis = new FileInputStream(image)){
					byte[] buffer = new byte[8192];
					int count;
					while((count = fis.read(buffer)) != -1) {
						wr.write(buffer, 0, count);
					}
					wr.write("\r\n".getBytes());
				}
				
				wr.write(("--" + boundary + "--\r\n").getBytes("UTF-8"));
			}
			wr.flush();
		}catch(Exception e) {}
		
	}
	
}
