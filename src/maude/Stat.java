package maude;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Stat {
	public static String inputFolder = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\stat\\";
	public static String text_2016 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\foitext2016.txt";
	
	public static Set<String> noRedundancy = new HashSet<String>();
	
	public static void main(String[] args) throws IOException {
		summary("key_medication.txt");
		summary("key_type_Billing.txt");
		summary("key_automated dispensing.txt");
		summary("key_EHR.txt");
		summary("key_user interface.txt");
		summary("key_lab information.txt");
		summary("key_PACS.txt");
		summary("key_incompatibility.txt");
		summary("key_equipment.txt");
		summary("key_equipment maintenace.txt");
		summary("key_hardware failure.txt");
		summary("key_network failure.txt");
		summary("key_ergonomics.txt");
		summary("key_security.txt");
		summary("key_software design.txt");
		System.out.println("All cases (no redundancy): " + noRedundancy.size());
	}
	
	public static void summary(String keywords) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inputFolder + keywords));
		//get keywords
		List<String> key = new ArrayList<String>();
		String line = "";
		
		while((line = br.readLine()) != null)
			key.add(line.trim());
		
		br.close();
		
		//get cases
		List<Set<String>> info = new ArrayList<Set<String>>();
		br = new BufferedReader(new FileReader(text_2016));
		for(int i=0; i<key.size(); i++) {
			Set<String> set = new HashSet<String>();
			info.add(set);
		}
		line = br.readLine(); //title
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			if(thisline.length < 6) 
				continue;		
			if(thisline.length > 6) {
				for(int i=5; i<thisline.length-1; i++) {
					thisline[5] = thisline[5] + "|" + thisline[i+1];
				}
			}
			for(int i=0; i<key.size(); i++) {
				String[] thiskey = key.get(i).split("&");
				String[] thiskey1 = key.get(i).split("\\|");				
				
				for(int j=0; j<thiskey.length; j++) {
					if(thisline[5].contains(thiskey[j].toUpperCase())) {
						if(j == thiskey.length-1) {
							info.get(i).add(thisline[0]);
							noRedundancy.add(thisline[0]);
						}
					}else
						break;
				}				
				
				for(int j=0; j<thiskey1.length; j++) {
					if(thisline[5].contains(thiskey1[j].toUpperCase()))	{		
						info.get(i).add(thisline[0]);
						noRedundancy.add(thisline[0]);
					}
				}
			}
		}
		
		br.close();
		
		Set<String> unique = new HashSet<String>();
		
		for(int i=0; i<info.size(); i++) {
			for(String s : info.get(i))
				unique.add(s);
		}
		
		for(int i=0; i<key.size(); i++) 
			System.out.println(key.get(i) + "\t" + info.get(i).size());
		System.out.println("Total (no redundancy): " + unique.size());
	}
}
