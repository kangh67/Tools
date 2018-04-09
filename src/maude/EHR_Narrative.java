package maude;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EHR_Narrative {
	public static String dev_2016 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\foidev2016.txt";
	public static String text_2016 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\foitext2016.txt";
	
	public static String GenericFilter = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Result_Generic_filter.csv";
	public static String ManuFilter = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Result_Manu_filter.csv";

	public static String EHRKeywords = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\EHR keywords.txt";
	
	public static String output = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\Result_EHR_Keywords_On_Narrative.txt";
	public static String outputFolder = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\";
	
	public static void main(String[] args) throws IOException {
		/**
		//<EHR_Keyword, <ID, narrative>>, may have redundancy among EHR_Keywords
		Map<String, Map<String, String>> key_IDs = getIDsForEachKeyword();
		
		//<EHR_Keyword, <ID, narrative>>, may have redundancy among EHR_Keywords, no redundancy with the filtered reports
		key_IDs = removeRecordsFromFilter(key_IDs);
		
		//write ID,generic name,manufacturer name,narrative keyword,narrative content. HAVE REDUNDANCY
		writeInfo(key_IDs);
		**/
		
		//analysis();
		
		extractCertainKeywordResult("Picture Archiving");
	}
	
	//<EHR_Keyword, <ID, narrative>>
	public static Map<String, Map<String, String>> getIDsForEachKeyword() throws IOException {		
		Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();		
		BufferedReader br = new BufferedReader(new FileReader(EHRKeywords));
		String line = "";
		
		//get keywords
		while((line = br.readLine()) != null) {
			Map<String, String> map = new HashMap<String, String>();
			res.put(line.trim(), map);
		}
		
		br.close();
		
		br = new BufferedReader(new FileReader(text_2016));
		
		//remove redundancy
		Set<String> sum = new HashSet<String>();
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			
			if(thisline.length < 6) 
				continue;
		
			if(thisline.length > 6) {
				for(int i=5; i<thisline.length-1; i++) {
					thisline[5] = thisline[5] + "|" + thisline[i+1];
				}
			}			
			
			for(String s : res.keySet()) {
				if(thisline[5].contains(s.toUpperCase())) {
					if(res.get(s).containsKey(thisline[0]))
						res.get(s).put(thisline[0], res.get(s).get(thisline[0]) + "><" + thisline[5]);
					else
						res.get(s).put(thisline[0], thisline[5]);
					sum.add(thisline[0]);					
				}
			}			
		}
		
		br.close();
		
		for(String s : res.keySet()) 
			System.out.println(s + "\t" + res.get(s).size());
		System.out.println("=== Total (redundancy removed): " + sum.size());
		
		return res;
	}
	
	public static Map<String, Map<String, String>> removeRecordsFromFilter(Map<String, Map<String, String>> key_IDs) throws IOException {
		Set<String> filterRecords = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(GenericFilter));
		
		//title
		String line = br.readLine();
		while((line = br.readLine()) != null) {
			String[] thisline = line.split(",");
			filterRecords.add(thisline[0]);
		}
		
		br.close();
		
		br = new BufferedReader(new FileReader(ManuFilter));
		
		//title
		line = br.readLine();
		while((line = br.readLine()) != null) {
			String[] thisline = line.split(",");
			filterRecords.add(thisline[0]);
		}
				
		br.close();
		
		System.out.println("=== Records by filter: " + filterRecords.size());
		
		//remove redundancy from key_IDs
		Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
		Set<String> sum = new HashSet<String>();
					
		for(String s : key_IDs.keySet()) {
			Map<String, String> map = new HashMap<String, String>();
			for(String ID : key_IDs.get(s).keySet()) {
				if(!filterRecords.contains(ID)) {
					map.put(ID, key_IDs.get(s).get(ID));
					sum.add(ID);
				}					
			}
			res.put(s, map);
		}
		
		System.out.println("=== Remove Redundancy from the Filter Results ===");
		
		for(String s : res.keySet()) 
			System.out.println(s + "\t" + res.get(s).size());
		System.out.println("=== Total (redundancy removed by Filtered Results): " + sum.size());
		
		return res;
	}
	
	public static void writeInfo(Map<String, Map<String, String>> info) throws IOException {
		Map<String, String[]> ID_names = new HashMap<String, String[]>();
		BufferedReader br = new BufferedReader(new FileReader(dev_2016));
		String line = br.readLine(); //the title line;
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\\|");
			String[] g_m = {Narrative2016.nameFilter(thisline[7]), Narrative2016.nameFilter(thisline[8])} ;
			ID_names.put(thisline[0], g_m);
		}
		
		br.close();
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(output)));
		
		dos.writeBytes("ID	Generic Name	Manufacturer Name	Narrative Keyword	Narrative" + "\r\n");
		
		for(String s : info.keySet())
			for(String ID : info.get(s).keySet())
				dos.writeBytes(ID + "\t\"" + Narrative2016.fixStr(ID_names.get(ID)[0]) + "\"\t\"" + Narrative2016.fixStr(ID_names.get(ID)[1]) + "\"\t" + s + "\t\"" + Narrative2016.fixStr(info.get(s).get(ID)) + "\"" + "\r\n");
		
		dos.close();
	}
	
	public static void analysis() throws IOException {
		//imaging System
		Map<String, Integer> gene1 = new HashMap<String, Integer>();
		Map<String, Integer> manu1 = new HashMap<String, Integer>();
		//others
		Map<String, Integer> gene2 = new HashMap<String, Integer>();
		Map<String, Integer> manu2 = new HashMap<String, Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(output));
		String line = br.readLine(); //the title line;
		
		while((line = br.readLine()) != null) {
			String[] thisline = line.split("\t");
			if(thisline[3].equals("Imaging System")) {
				if(thisline[1].length() > 2) {
					String gene = thisline[1].substring(1, thisline[1].length()-1);
					gene1.put(gene, gene1.getOrDefault(gene, 0) + 1);
				}
				if(thisline[2].length() > 2) {
					String manu = thisline[2].substring(1, thisline[2].length()-1);
					manu1.put(manu, manu1.getOrDefault(manu, 0) + 1);
				}
			}else {
				if(thisline[1].length() > 2) {
					String gene = thisline[1].substring(1, thisline[1].length()-1);
					gene2.put(gene, gene2.getOrDefault(gene, 0) + 1);
				}
				if(thisline[2].length() > 2) {
					String manu = thisline[2].substring(1, thisline[2].length()-1);
					manu2.put(manu, manu2.getOrDefault(manu, 0) + 1);
				}
			}				
		}
		
		br.close();
		
		System.out.println("=== Imaging System - generic names ===");
		
		for(String s: gene1.keySet())
			System.out.println(s + "\t" + gene1.get(s));
		
		System.out.println("=== Imaging System - manu names ===");
		
		for(String s: manu1.keySet())
			System.out.println(s + "\t" + manu1.get(s));
		
		System.out.println("=== Other keywords - generic names ===");
		
		for(String s: gene2.keySet())
			System.out.println(s + "\t" + gene2.get(s));
		
		System.out.println("=== Other keywords - manu names ===");
		
		for(String s: manu2.keySet())
			System.out.println(s + "\t" + manu2.get(s));
	}
	
	public static void extractCertainKeywordResult(String keyword) throws IOException {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(outputFolder + "EHR_keywords_on_narrative_" + keyword + ".csv")));
		BufferedReader br = new BufferedReader(new FileReader(output));
		String line = br.readLine(); //the title line;
		dos.writeBytes(line.replaceAll("\t", ",") + "\r\n");
		
		while((line = br.readLine()) != null) {
			if(line.split("\t")[3].equals(keyword))
				dos.writeBytes(line.replaceAll("\t", ",") + "\r\n");
		}
		
		br.close();
		dos.close();
	}
	
}
