package maude;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Radiologic {
	public static String mdr = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\radio_liang\\mdrfoi.txt";
	public static String dev = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\radio_liang\\foidev.txt";
	public static String text = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\radio_liang\\foitext.txt";
	
	public static String output = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\radio_liang\\radiologic technologist_2017.csv";

	//<ID, [brand name, Generic name, manu name, narrative]>
	public static HashMap<String, String[]> res = new HashMap<String, String[]>();
	
	public static void main(String[] args) throws IOException {
		getID();		
		getDev();
		getText();
		writeResult_CSV();
	}
	
	public static void getID() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(mdr));
		String line = br.readLine();
		//System.out.println(line.split("\\|")[20]);
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			
			if(thisline[13].trim().equals("113")) {
				String[] s = new String[4];
				res.put(thisline[0], s);
			}
			
			//System.out.println(thisline[13]);
			//System.out.println(thisline[20]);
		}
		
		br.close();
	}
	
	public static void getDev() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(dev));
		String line = br.readLine();
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			if(res.containsKey(thisline[0].trim())) {
				res.get(thisline[0].trim())[0] = nameFilter(thisline[6]);
				res.get(thisline[0].trim())[1] = nameFilter(thisline[7]);
				res.get(thisline[0].trim())[2] = nameFilter(thisline[8]);
			}
		}
		
		br.close();
	}
	
	public static void getText() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(text));
		String line = br.readLine();
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			if(res.containsKey(thisline[0].trim())) {
				if(thisline.length < 6) 
					continue;
			
				if(thisline.length > 6) {
					for(int i=5; i<thisline.length-1; i++) {
						thisline[5] = thisline[5] + "|" + thisline[i+1];
					}
				}
				
				if(res.get(thisline[0].trim())[3] == null)
					res.get(thisline[0].trim())[3] = thisline[5];
				else
					res.get(thisline[0].trim())[3] += "><" + thisline[5];
			}
		}
		
		br.close();
	}
	
	public static void writeResult_CSV() throws IOException {		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(output)));
		
		dos.writeBytes("MDR_REPORT_KEY,BRAND_NAME,GENERIC_NAME,MANUFACTURER_NAME,FOI_TEXT" + "\r\n");
		
		for (String ID : res.keySet())			
			dos.writeBytes(ID + ",\"" + fixStr(res.get(ID)[0]) + "\",\"" + fixStr(res.get(ID)[1]) + "\",\"" + fixStr(res.get(ID)[2]) + "\",\"" + fixStr(res.get(ID)[3]) + "\"" + "\r\n");
			
		dos.close();
	}
	
	public static String nameFilter(String s) {
		if(s.length() < 1)
			return s;
		else if(s.charAt(s.length()-1) == ',')
			return s.substring(0, s.length()-1);
		else
			return s;
	}
	
	public static String fixStr(String s) {
		return s == null ? s : s.replaceAll("\"", "'");
	}
}
