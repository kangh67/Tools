package maude;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class CombineTfidfBtm {
	public static String btm_file = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\btm\\data_Ts\\data_with_numT_80.csv";
	public static String tfidf_file = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\TFIDF features_1541.csv";
	
	public static String combined_file = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\btm\\btm_tfidf\\80topics_tfidf_1541.csv";
	
	public static void main(String[] args) throws IOException {
		BufferedReader br1 = new BufferedReader(new FileReader(btm_file));
		BufferedReader br2 = new BufferedReader(new FileReader(tfidf_file));
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(combined_file)));
		
		String line1 = "";
		String line2 = "";
		
		while((line1 = br1.readLine()) != null) {
			line2 = br2.readLine();			
			String[] tfidf = line2.split(",");
			
			dos.writeBytes(line1);
			
			for(int i=1; i<tfidf.length; i++)
				dos.writeBytes("," + tfidf[i]);
			
			dos.writeBytes("\r\n");	
		}
		
		dos.close();
		br1.close();
		br2.close();
	}
}
