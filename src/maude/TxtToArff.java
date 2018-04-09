package maude;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TxtToArff {
	public static String txtFile = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\generic90_manu10.txt";
	public static String arffFile = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\generic90_manu10.arff";
	
	public static void main(String[] args) throws IOException {
		ArrayList<ArrayList<String>> info = readTxt();
		writeArff(info);
	}
	
	public static ArrayList<ArrayList<String>> readTxt() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(txtFile));
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		
		String line = br.readLine(); //the title line;
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\t");
			if(thisline.length != 3)
				System.out.println("Warning! following line cannot be split into 3 subString: " + line);
			ArrayList<String> subres = new ArrayList<String>();
			for(String s : thisline)
				subres.add(s);
			res.add(subres);
		}
		
		br.close();
		return res;
	}
	
	public static void writeArff(ArrayList<ArrayList<String>> info) throws IOException {		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(arffFile)));
		
		dos.writeBytes("@relation HIT" + "\r\n");
		dos.writeBytes("\r\n");
		dos.writeBytes("@attribute MDR_REPORT_KEY numeric" + "\r\n");
		dos.writeBytes("@attribute TEXT string" + "\r\n");
		dos.writeBytes("@attribute HIT.OR.NOT {YES,NO}" + "\r\n");
		dos.writeBytes("\r\n");
		dos.writeBytes("@data" + "\r\n");
		
		for (ArrayList<String> list : info)
			dos.writeBytes(list.get(0) + ",'" + list.get(1).replaceAll("\"", "").replaceAll("\\'", "\\\\'") + "'," + list.get(2).replaceAll("0", "NO").replaceAll("1", "YES") + "\r\n");					
				
		dos.close();
	}
}
