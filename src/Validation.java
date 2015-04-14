import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

import com.opencsv.*;
/*
 * This class provide validation for Db Integration Methods
 */
public class Validation {
	private static String input_dir = "data\\preprocessed\\";
	private static String[] pathways = {	input_dir + "intpathway\\pathway.txt",
											input_dir + "KEGG\\pathway.txt",
											input_dir + "wikipathways\\pathway.txt"};
	private static String[] genes    = {	input_dir + "intpathway\\pathway_gene.txt",
											input_dir + "KEGG\\pathway_gene.txt",
											input_dir + "wikipathways\\pathway_gene.txt"};
	private static String[] genepairs    = {	input_dir + "intpathway\\pathway_genepair.txt",
												input_dir + "KEGG\\pathway_genepair.txt",
												input_dir + "wikipathways\\pathway_genepair.txt"};
	private static List<Double> LCSWeightSet = Arrays.asList(0.0, 0.0, 0.0, 1.0);
	private static List<Double> GAWeightSet = Arrays.asList(0.0, 0.1, 0.0, 0.0);
	private static List<Double> GPAWeightSet = Arrays.asList(0.0, 0.0, 1.0, 0.0);
	private PathInt pathint;
	private TextMatch textmatch;
	private Double[][] scores;
	
	public Validation(String database1, String database2) {
		int index1 = getDbIndex(database1);
		int index2 = getDbIndex(database2);
		setIntPath(index1, index2);
		setTextMatch(index1, index2);
	}
	
	private void setIntPath(int index1, int index2) {
		try {
			pathint = new PathInt(	pathways[index1], genes[index1], genepairs[index1],
									pathways[index2], genes[index2], genepairs[index2]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setTextMatch(int index1, int index2) {
		try {
			textmatch = new TextMatch(pathways[index1], pathways[index2]);
			textmatch.matchByName();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int getDbIndex(String database) {
		if (database.toLowerCase().endsWith("intpathway")) {
			return 0;
		} else if (database.toLowerCase().endsWith("kegg")) {
			return 1;
		} else if (database.toLowerCase().endsWith("wikipathways")) {
			return 2;
		} 
		return -1;
	}
	
	private void calculateScore(List<Double> weightSet) {
		scores = new Double[PathInt.plist1.size()][PathInt.plist2.size()];
		for (int i=0; i<PathInt.plist1.size(); i++) {
			for (int j=0; j<PathInt.plist2.size(); j++) {
				String pathway1 = PathInt.plist1.get(i);
				String pathway2 = PathInt.plist2.get(j);
				double normalizedLCS = textmatch.getNormalizedScore(pathway1, pathway2);
				double geneAgreement = PathInt.scoreArr[i][j][0];
				double genePairAgreement = PathInt.scoreArr[i][j][1];
				scores[i][j] = weightSet.get(0) + weightSet.get(1) * geneAgreement 
							+ weightSet.get(2) * genePairAgreement 
							+ weightSet.get(3) * normalizedLCS;
			}
		}
	}
	
	public List<Double> getTopHundred(List<Double> weightSet) {
		// calculate scores based on weight set
		calculateScore(weightSet);
		// get a sorted list
		List<Double> sortedScores = new ArrayList<Double>();
		for (int i=0; i<scores.length; i++) {
			for (int j=0; j<scores[0].length; j++) {
				sortedScores.add(scores[i][j]);
			}
		} 
		Collections.sort(sortedScores);
		List<Double> topHundred = sortedScores.subList(0, 100);
		
		return topHundred;
	}
	
	public TreeMap<String, String> getTopMatch(List<Double> weightSet) {
		TreeMap<String, String> topMatch = new TreeMap<String, String>();
		calculateScore(weightSet);
		// get best match for each pathway in plist1
		for (int i=0; i<PathInt.plist1.size(); i++) {
			int index=0; double max = scores[i][index];
			for (int j=1; j<PathInt.plist2.size(); j++) {
				if (max < scores[i][j]) {
					index = j; max = scores[i][j];
				}
			}
			topMatch.put(PathInt.plist1.get(i), PathInt.plist2.get(index));
		}
		
		return topMatch;
	}
	
	public void writeToCSV(String db1, String db2) {
		String out = "data\\result\\" + db1.toLowerCase() + "_" + db2.toLowerCase() + ".csv";
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(out));
			String[] attr = {db1, db2, "LCS", "Gene Agreement", "GenePair Agreement"};
			writer.writeNext(attr);
			
			for (int i=0; i<PathInt.plist1.size(); i++) {
				for (int j=0; j<PathInt.plist2.size(); j++) {
					String[] entry = new String[5];
					entry[0] = PathInt.plist1.get(i);
					entry[1] = PathInt.plist2.get(j);
					entry[2] = String.format("%.0f", textmatch.getLCSScore(entry[0], entry[1]));
					entry[3] = String.format("%.3f", PathInt.scoreArr[i][j][0]); 
					entry[4] = String.format("%.3f", PathInt.scoreArr[i][j][1]);
					writer.writeNext(entry);
				}
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void writeToCSV(String db1, String db2, List<Double> weightSet) {
		String out = "data\\resultGA\\" + db1.toLowerCase() + "_" + db2.toLowerCase() + ".csv";
		try {
			calculateScore(weightSet);
			CSVWriter writer = new CSVWriter(new FileWriter(out));
			String[] w_set = {"Weight set: ", weightSet.toString()};
			writer.writeNext(w_set);
			String[] attr = {db1, db2, "LCS", "Normalized LCS" ,"Gene Agreement", "GenePair Agreement", "Score"};
			writer.writeNext(attr);
			
			for (int i=0; i<PathInt.plist1.size(); i++) {
				for (int j=0; j<PathInt.plist2.size(); j++) {
					String[] entry = new String[7];
					entry[0] = PathInt.plist1.get(i);
					entry[1] = PathInt.plist2.get(j);
					entry[2] = String.format("%.0f", textmatch.getLCSScore(entry[0], entry[1]));
					entry[3] = String.format("%.3f", textmatch.getNormalizedScore(entry[0], entry[1]));
					entry[4] = String.format("%.3f", PathInt.scoreArr[i][j][0]); 
					entry[5] = String.format("%.3f", PathInt.scoreArr[i][j][1]);
					entry[6] = String.format("%.3f", scores[i][j]);
					writer.writeNext(entry);
				}
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void writeTopMatchToCSV(String db1, String db2, List<Double> weightSet) {
		String hash =  Double.toString(Math.random());
		TreeMap<String, String> topMatch = getTopMatch(weightSet);
		String out = "data\\result\\" + db1.toLowerCase() + "_" + db2.toLowerCase() 
															+ "_topMatch_" + hash + ".csv";
		try {
			calculateScore(weightSet);
			CSVWriter writer = new CSVWriter(new FileWriter(out));
			String[] w_set = {"Weight set: ", weightSet.toString()};
			writer.writeNext(w_set);
			String[] attr = {db1, db2, "LCS", "Normalized LCS" ,"Gene Agreement", "GenePair Agreement", "Score"};
			writer.writeNext(attr);
			
			for (Entry<String, String> pair : topMatch.entrySet()) {
				String pathway1 = pair.getKey();
				String pathway2 = pair.getValue();
				String[] entry = new String[7];
				entry[0] = pathway1;
				entry[1] = pathway2;
				entry[2] = String.format("%.0f", textmatch.getLCSScore(entry[0], entry[1]));
				entry[3] = String.format("%.3f", textmatch.getNormalizedScore(entry[0], entry[1]));
				int i = PathInt.plist1.indexOf(pathway1); int j = PathInt.plist2.indexOf(pathway2);
				entry[4] = String.format("%.3f", PathInt.scoreArr[i][j][0]); 
				entry[5] = String.format("%.3f", PathInt.scoreArr[i][j][1]);
				entry[6] = String.format("%.3f", scores[i][j]);
				writer.writeNext(entry);
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		try {
			if (args.length==1 && args[0].equals("--all")) {
				String [] options = {"wikipathways", "KEGG", "intpathway"};
				Validation val1 = new Validation(options[0], options[1]);
				val1.writeToCSV (options[0], options[1]);
				Validation val2 = new Validation(options[1], options[2]);
				val2.writeToCSV(options[1], options[2]);
				Validation val3 = new Validation(options[2], options[0]);
				val3.writeToCSV(options[2], options[0]);
			} else if (args.length==2) {
				Validation val = new Validation(args[0], args[1]);
				val.writeToCSV(args[0], args[1]);
				val.writeTopMatchToCSV(args[0], args[1], LCSWeightSet);
				val.writeTopMatchToCSV(args[0], args[1], GAWeightSet);
			} else {
				//print out usage guide
				System.out.println("Usage:	<database1> <database2> or \n" 
									+ "	--all for using all databases\n" 
									+ "Available options: wikipathways, KEGG, intpathway");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}