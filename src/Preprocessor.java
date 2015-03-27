import java.util.*;
import java.io.*;

public class Preprocessor {
	private HashMap<String, String> sgd_gene;
	
	public Preprocessor() {
		sgd_gene = new HashMap<String, String>();
	}
	
	public void loadGSDID() {
		String dir = "data\\raw\\SGD_CDS_xref.txt";
		File file = new File(dir);
		try {
			FileReader fw = new FileReader(file.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fw);
			String line;
			while ((line=br.readLine())!=null) {
				String[] elem = line.split("\\s+");
				// get SGDID and gene systematic names
				sgd_gene.put(elem[2], elem[3]);
			}
			br.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void loadWikiPathway() {
		String dir = "data\\raw\\wikipathways";
		String outp = "data\\preprocessed\\wikipathways\\";
		HashSet<String> pathway = new HashSet<String>();
		HashMap<String, List<String>> pathway_gene = new HashMap<String,
																List<String>>();
		File folder = new File(dir);
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				// get pathway name from file name
				String name = file.getName().substring(0, file.getName().length()-4);
				pathway.add(name);
				// read content of file
				try {
					FileReader fr = new FileReader(file.getAbsoluteFile());
					BufferedReader br = new BufferedReader(fr);
					String line;
					List<String> genes = new ArrayList<String>();
					while ((line=br.readLine())!=null) {
						String[] elem = line.split("\\s+");
						if (elem.length==2 && elem[1].equals("SGD")) {
							// get systematic name from SGDID
							// System.out.println(elem[0] + " " + sgd_gene.get(elem[0]));
							if (sgd_gene.containsKey(elem[0])) {
								genes.add(sgd_gene.get(elem[0]));
							}
						}
					}
					pathway_gene.put(name, genes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		writePathway(outp, pathway);
		writePathwayGene(outp, pathway_gene);
	}
	
	public void loadKEGG() {
		String inp = "data\\raw\\KEGG";
		String outp = "data\\preprocessed\\KEGG\\";
		HashSet<String> pathway = new HashSet<String>();
		HashMap<String, List<String>> pathway_gene = new HashMap<String,
																List<String>>();
		File folder = new File(inp);
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				// read content of file
				try {
					FileReader fr = new FileReader(file.getAbsoluteFile());
					BufferedReader br = new BufferedReader(fr);
					String line; int count = 0;
					String name = "";
					List<String> genes = new ArrayList<String>();
					while ((line=br.readLine())!=null) {
						count++;
						if (count==1) {
							name = line;
						} else {
							genes.add(line);
						}
					}
					br.close(); fr.close();
					pathway_gene.put(name, genes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		writePathway(outp, pathway);
		writePathwayGene(outp, pathway_gene);
	}
	
	public void loadIntPathway() {
		HashSet<String> pathway = new HashSet<String>();
		HashMap<String, List<String>> pathway_gene = new HashMap<String,
																List<String>>();
		String inp = "data\\raw\\intpathway\\cerevisiaeIntPathGenes";
		String outp = "data\\preprocessed\\intpathway\\";
		File file = new File(inp);
		try {
			FileReader fr = new FileReader(file.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line=br.readLine())!=null) {
				List<String> elem = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
				// remove unneccessary info
				elem.remove("KEGG"); elem.remove("WikiPathways"); elem.remove("BioCyc");
				// get name of pathway
				String name = getPathwayName(elem.subList(0, elem.size()-1));
				pathway.add(name);
				if (!pathway_gene.containsKey(name)) {
					pathway_gene.put(name, new ArrayList<String>());
				} else {
					List<String> genes = pathway_gene.get(name);
					genes.add(elem.get(elem.size()-1));
					pathway_gene.put(name, genes);
				}
			}
			br.close(); fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		writePathway(outp, pathway);
		writePathwayGene(outp, pathway_gene);
	}
	
	private String getPathwayName(List<String> list) {
		String result = "";
		for (String str : list) {
			result += str + " ";
		}
		result = result.substring(0, result.length()-1);
		return result;
	}
	
	private void writePathway(String outp, HashSet<String> pathway) {
		File file = new File(outp + "pathway.txt");
		try {
			if (!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (String name : pathway) {
				// replace space with underscore
				// and turn to lower case
				name = name.replaceAll(" ", "_").toLowerCase();
				bw.write(name + "\n");
			}
			bw.close(); fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void writePathwayGene(String outp, HashMap<String, List<String>> p_g) {
		File file = new File(outp + "pathway_gene.txt");
		try {
			if (!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (String name : p_g.keySet()) {
				// replace space with underscore
				// and turn to lower case
				String shortname = name.replaceAll(" ", "_").toLowerCase();
				for (String gene : p_g.get(name)) {
					bw.write(shortname + " " + gene + "\n"); 
				}
			}
			bw.close(); fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Preprocessor prep = new Preprocessor();
		// pre-process GSDID - Gene systematic names
		prep.loadGSDID();
		// pre-process Wikipathway database
		prep.loadWikiPathway();
		// pre-process KEGG database
		prep.loadKEGG();
		// preprocess intPathway database
		prep.loadIntPathway();
	}
}