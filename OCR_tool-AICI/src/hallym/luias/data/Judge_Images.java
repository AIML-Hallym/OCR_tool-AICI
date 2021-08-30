package hallym.luias.data;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Judge_Images {

	private ArrayList <BufferedImage> images = new ArrayList<BufferedImage>();
	private String file_name;
	private int current_page = 0;
	
	
	public Judge_Images(String file_name) {
		this.file_name = file_name;
	}
	
	public String getName() {
		return file_name;
	}
	
	public BufferedImage getCurrentPage() {
		return images.get(current_page);
	}
	
	public void getNextPage() {
		current_page++;
		if(current_page >= images.size()) current_page = (images.size()-1);

	}
	
	public void getPrevPage() {
		current_page--;
		if(current_page <= 0) current_page = 0;
	}
	
	public int getCurrentImageIndex() {
		return current_page;
	}
	
	public void firstPage() {
		current_page = 0;
	}
	
	
	public void addPage(BufferedImage image) {
		images.add(image);
	}
	
	public int getTotalPage() {
		return images.size();
	}
	
	
}
