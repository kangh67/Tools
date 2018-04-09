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

public class RandomPickForReview {
	public static int per = 10; //Extract 10%, for review	
	
	/**
	 * 2016 MAUDE
	 
	public static String input_Generic = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Result_Generic_filter.csv";
	public static String input_Manu = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Result_Manu_filter.csv";
	public static String input_exclude = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\exclude_100.txt";
	public static String output_summary = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\summary.txt";
	public static String output_extract_generic = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\Extract_generic.csv";
	public static String output_extract_manu = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\Extract_manu.csv";
	*/
	
	/**
	 * 2008-10 MAUDE
	 */
	
	public static String input_filtered = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2011-13 MAUDE\\event\\Result_filter_2013.txt";	
	public static String output_summary = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2011-13 MAUDE\\event\\summary.txt";
	public static String output_extract = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2011-13 MAUDE\\event\\Extracted.txt";	
	
	public static void main(String[] args) throws IOException {
		/**
		 * 2016 MAUDE
		 
		//<generic, all_case_info>
		Map<String, ArrayList<String[]>> info_generic = readInfo("Generic");
		
		//<manu, all_case_info>
		Map<String, ArrayList<String[]>> info_manu = readInfo("Manu");
		
		writeSummary(info_generic, info_manu);
		ExtractAndWrite_2016(info_generic, info_manu);
		**/
		
		
		/**
		 * 2008-2010 MAUDE
		 */
		//<event, all_case_info>
		Map<String, HashSet<String[]>> info = readInfo();
		
		writeSummary(info);
		
		ExtractAndWrite(info);
	}
	
	public static Map<String, HashSet<String[]>> readInfo() throws IOException {
		Map<String, HashSet<String[]>> info = new HashMap<String, HashSet<String[]>>();
		BufferedReader br = new BufferedReader(new FileReader(input_filtered));;
		String line = "";
			
		br.readLine();	//TITLE	
		
		while((line = br.readLine()) != null) {
			String[] col = line.split("\t");			
			
			String[] thisinfo = new String[10];
			thisinfo[0] = col[0];
			thisinfo[1] = col[1];
			thisinfo[2] = col[2];
			thisinfo[3] = col[3];
			thisinfo[4] = col[4];
			thisinfo[5] = col[5];
			thisinfo[6] = col[6];
			thisinfo[7] = col[7];
			thisinfo[8] = col[8];
			thisinfo[9] = col[9];
			
			HashSet<String[]> newlist = new HashSet<String[]>();
			
			if(info.containsKey(thisinfo[1]))
				newlist = info.get(thisinfo[1]);
								
			newlist.add(thisinfo);
			info.put(thisinfo[1], newlist);
		}
		
		br.close();
		
		return info;
	}
	
	public static void writeSummary(Map<String, HashSet<String[]>> info) throws IOException {
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(output_summary)));
		
		int sum_case = 0;
		
		for(String s : info.keySet()) {
			sum_case += info.get(s).size();
			System.out.println(s + "\t" + info.get(s).size());
			dos.writeBytes(s + "\t" + info.get(s).size() + "\r\n");
		}
		
		System.out.println("=== HCFA OR MANUFACTURER OR DISTRIBUTOR number: " + info.size());
		dos.writeBytes("=== HCFA OR MANUFACTURER OR DISTRIBUTOR number: " + info.size() + "\r\n");
		
		System.out.println("=== Case number: " + sum_case);
		dos.writeBytes("=== Case number: " + sum_case + "\r\n");
		
		dos.close();
	}
	
	/**
	 * 
	 * @param gene
	 * @param manu
	 * @throws IOException
	 
	public static void ExtractAndWrite_2016(Map<String, ArrayList<String[]>> gene, Map<String, ArrayList<String[]>> manu) throws IOException {
		Set<String> exclude = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(input_exclude));
		String line = "";
		
		//exclude the 100 cases which have been reviewed
		while((line = br.readLine()) != null) 
			exclude.add(line.trim());
		
		br.close();
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(output_extract_generic)));
		
		dos.writeBytes("MDR_REPORT_KEY,Generic_Name,Manufacturer_Name,FOI_TEXT" + "\r\n");
		
		for(String s : gene.keySet()) {
			for(int i=0; i<getSampleNum(gene.get(s).size()); i++) {				
				if(exclude.contains(gene.get(s).get(i)[0]))
					continue;
				else
					dos.writeBytes(gene.get(s).get(i)[0] + ",\"" + s + "\",\"" + gene.get(s).get(i)[2] + "\",\"" + gene.get(s).get(i)[3] + "\r\n");
			}				
		}
		
		dos = new DataOutputStream(new FileOutputStream(new File(output_extract_manu)));
		
		dos.writeBytes("MDR_REPORT_KEY,Generic_Name,Manufacturer_Name,FOI_TEXT" + "\r\n");
		
		for(String s : manu.keySet()) {
			for(int i=0; i<getSampleNum(manu.get(s).size()); i++) {				
				if(exclude.contains(manu.get(s).get(i)[0]))
					continue;
				else
					dos.writeBytes(manu.get(s).get(i)[0] + ",\"" + manu.get(s).get(i)[1] + "\",\"" + s + "\",\"" + manu.get(s).get(i)[3] + "\r\n");
			}				
		}
		
		dos.close();
	}
	**/
	
	public static void ExtractAndWrite(Map<String, HashSet<String[]>> info) throws IOException {			
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(output_extract)));
		
		dos.writeBytes("MDR_REPORT_KEY" + "\t" + "HCFA_OR_MANUFACTURER_OR_DISTRIBUTOR_ID" + "\t" + "REPORT_NUMBER" + "\t" + "GENERIC_NAME" + "\t" + "MANUFACTURER_NAME" + "\t" + "BRAND_NAME" + "\t" + "TEXT_EVENT_DESCRIPTION" + "\t" + "TEXT_ADDITIONAL_MFR_NARRATIVE" + "\t" + "TEXT_DEVICE_EVALUATED_BY_MANUFACTURER"  + "\t" + "TEXT_OTHER" + "\r\n");
		
		for(String s : info.keySet()) {
			int count = 0;
			int stop = getSampleNum(info.get(s).size());
			for(String[] data : info.get(s)) {
				dos.writeBytes(data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3] + "\t" + data[4] + "\t" + data[5] + "\t" + data[6] + "\t" + data[7] + "\t" + data[8] + "\t" + data[9] + "\r\n");
				count ++;
				if(count >= stop)
					break;
			}										
		}
			
		dos.close();
	}
	
	public static int getSampleNum(int num) {		
		return num * per / 100 + 1;
	}
}
