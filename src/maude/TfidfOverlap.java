package maude;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TfidfOverlap {
	public static String tfidf2015 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\TFIDF features_1541.csv";
	public static String tfidf2016 = "C:\\Users\\hkang1.UTHSAHS\\Google Drive\\MAUDE\\filter_classifier\\2016 MAUDE\\Review\\generic90_manu10_tfidf.csv";

	public static void main(String[] args) throws IOException {
		overlap();
	}
	
	public static void overlap() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(tfidf2015));
		String[] title = br.readLine().split(",");
		System.out.println(title.length);
		br.close();
	}
}