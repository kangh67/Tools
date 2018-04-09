package maude;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class Manu_stat {
	public static String dev_2015 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\database\\device\\foidev2015.txt";
	
	public static String targetFile = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\database\\device\\resultOneYear.txt";
	
	public static void main(String[] args) throws IOException {
		statOneYear(dev_2015);
	}
	
	public static void statOneYear(String fileName) throws IOException {
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		
		String line = br.readLine(); //the title line;
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			String manu = removeRedundancy(thisline[8]);
			
			if(hm.containsKey(manu)) {
				hm.put(manu, hm.get(manu) + 1);
			}else
				hm.put(manu, 1);			
		}		
		
		br.close();
		
		Iterator<String> it = hm.keySet().iterator();		
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(targetFile)));
		
		while(it.hasNext()) {
			String key = it.next();
			int val = hm.get(key);
			dos.writeBytes(key + "\t" + val + "\r\n");			
		}
		
		dos.close();
	}
	
	public static String removeRedundancy(String manu) {
		if(manu.endsWith(" CO.") || manu.endsWith(" CO")) {
			manu = manu.replaceAll(" CO.", "");
			manu = manu.replaceAll(" CO", "");
		}
		
		if(manu.endsWith(" LLC.") || manu.endsWith(" LLC")) {
			manu = manu.replaceAll(" LLC.", "");
			manu = manu.replaceAll(" LLC", "");
		}
		
		if(manu.endsWith(" CORP.") || manu.endsWith(" CORP") || manu.endsWith("CORPORATION")) {
			manu = manu.replaceAll(" CORP.", "");
			manu = manu.replaceAll(" CORP", "");
			manu = manu.replaceAll("CORPORATION", "");
		}
		
		manu.trim();
		
		return manu;
	}
		
}
