import java.util.*;
import java.util.Map.Entry;
import java.io.*;

import com.opencsv.*;

public class OverlapChecker {
	private String file1;
	private String file2;
	private HashMap<String, String> pair1;
	private HashMap<String, String> pair2;
	private HashMap<String, String> pair1_G;
	private HashMap<String, String> pair2_G;
	private HashMap<String, String> pair1_GP;
	private HashMap<String, String> pair2_GP;
	
	public OverlapChecker(String _file1, String _file2) {
		file1 = _file1;
		file2 = _file2;
		pair1 = new HashMap<String, String>();
		pair2 = new HashMap<String, String>();
		pair1_G = new HashMap<String, String>();
		pair1_GP = new HashMap<String, String>();
		pair2_G = new HashMap<String, String>();
		pair2_GP = new HashMap<String, String>();
	}
	
	public void checkOverlap() {
		loadData(file1, 1); loadData(file2, 2);
		int similar = 0; int mis = 0;
		Vector<String> mispair = new Vector<String>();
		for (Entry<String, String> entry1 : pair1.entrySet()) {
			String pathway1_1 = entry1.getKey();
			String pathway1_2 = entry1.getValue();
			if (pair2.containsKey(pathway1_1) &&
				(pair2.get(pathway1_1).equals(pathway1_2))) { 
				similar++;
			} else {
				mis++;
				mispair.add(pathway1_1);
			}
		}
		System.out.println(file1 + " " + file2);
		System.out.println("Total pair of pathways: " + (similar + mis));
		System.out.println("Similar result: " + similar);
		System.out.println("Rate: " + ((double)similar/(similar + mis)));
		writeMis(mispair);
	}
	
	private void loadData(String file, int index) {
		try {
			CSVReader reader = new CSVReader(new FileReader(file));
			String[] nextLine;
			int count = 0;
			while((nextLine=reader.readNext())!=null) {
				count++;
				if (count>=2) {
					if (index ==1) {
						pair1.put(nextLine[0], nextLine[1]);
						pair1_G.put(nextLine[0], nextLine[4]);
						pair1_GP.put(nextLine[0], nextLine[5]);
					} else {
						pair2.put(nextLine[0], nextLine[1]);
						pair2_G.put(nextLine[0], nextLine[4]);
						pair2_GP.put(nextLine[0], nextLine[5]);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void writeMis(Vector<String> mispair) {
		String file = "data\\result\\mismatch.csv";
		try {
			File f = new File(file);
			if (!f.exists()) f.createNewFile();
			FileWriter fw = new FileWriter(f.getAbsolutePath());
			CSVWriter writer = new CSVWriter(fw);
			String[] attrs = {"", file1, "", file2, ""};
			writer.writeNext(attrs);
			String[] attrs2 = {"Pathway p", "GA score 1", "GPA score 1", "GA score 2", "GPA score 2"};
			writer.writeNext(attrs2);
			for (String str: mispair) {
				String[] entry = {str, pair1_G.get(str), pair1_GP.get(str), pair2_G.get(str), pair2_GP.get(str)};
				writer.writeNext(entry);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		try {
			OverlapChecker oc;
			if (args.length==2) {
				oc= new OverlapChecker(args[0], args[1]);
				oc.checkOverlap();
			} else {
				String file1 = "data\\resultGA\\wikipathways_kegg_topMatch_.csv";
				String file2 = "data\\result\\wikipathways_kegg_topMatch_LCS.csv";
				String file3 = "data\\result\\wikipathways_kegg_topMatch_Gene.csv";
				String file4 = "data\\result\\wikipathways_kegg_topMatch_GenePair.csv";
				oc= new OverlapChecker(file1, file2);
				oc.checkOverlap();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
