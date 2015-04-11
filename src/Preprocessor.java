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
		HashMap<String, List<String>> pathway_genepair = new HashMap<String,
																List<String>>();
		File folder = new File(dir);
		HashSet<String> types = new HashSet<String>();
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
						if (elem.length>=2) {
							types.add(elem[1]);
							if (elem[1].equals("SGD")) {
								// get systematic name from SGDID
								// System.out.println(elem[0] + " " + sgd_gene.get(elem[0]));
								if (sgd_gene.containsKey(elem[0])) {
									genes.add(sgd_gene.get(elem[0]));
								}
							} else if (elem[1].equals("Ensembl")) {
								genes.add(elem[0]);
							} else if (elem[1].equals("Entrez")) {
								// scrap gene name from NBCI
								if (elem[0].length()!=0)
									genes.add(WebScraper.scrapNCBI(elem[0]));
							} else if (elem[1].equals("Uniprot-TrEMBL")) {
								// scrap gene name from Uniprot
							}
						}
					}
					pathway_gene.put(name, genes);
					br.close(); fr.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		for (String p : pathway) {
			List<String> genes = pathway_gene.get(p);
			if (genes.isEmpty()) {
				System.out.println("		" + p);
			}
			for (String gene_1 : genes) {
				for (String gene_2 : genes) {
					if (!gene_1.equals(gene_2)) {
						if (pathway_genepair.containsKey(p)) {
							List<String> gp = pathway_genepair.get(p);
							gp.add(gene_1 + " " + gene_2);
							pathway_genepair.put(p, gp);
						} else {
							List<String> gp = new ArrayList<String>();
							gp.add(gene_1 + " " + gene_2);  
							pathway_genepair.put(p, gp);
						}
					}
				}
			}
		}
		
		writePathway(outp, pathway);
		writePathwayGene(outp, pathway_gene);
		writePathwayGenePair(outp, pathway_genepair);
	}
	
	public void loadKEGG() {
		String inp = "data\\raw\\KEGG";
		String outp = "data\\preprocessed\\KEGG\\";
		HashSet<String> pathway = new HashSet<String>();
		HashMap<String, List<String>> pathway_gene = new HashMap<String,
																List<String>>();
		HashMap<String, List<String>> pathway_genepair = new HashMap<String, 
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
		for (String p : pathway) {
			List<String> genes = pathway_gene.get(p);
			for (String gene_1 : genes) {
				for (String gene_2 : genes) {
					if (!gene_1.equals(gene_2)) {
						if (pathway_genepair.containsKey(p)) {
							List<String> gp = pathway_genepair.get(p);
							gp.add(gene_1 + " " + gene_2);
							pathway_genepair.put(p, gp);
						} else {
							List<String> gp = new ArrayList<String>();
							gp.add(gene_1 + " " + gene_2);  
							pathway_genepair.put(p, gp);
						}
					}
				}
			}
		}
		
		writePathway(outp, pathway);
		writePathwayGene(outp, pathway_gene);
		writePathwayGenePair(outp, pathway_genepair);
	}
	
	public void loadIntPathway() {
		HashSet<String> pathway = new HashSet<String>();
		HashMap<String, List<String>> pathway_gene = new HashMap<String,
																List<String>>();
		HashMap<String, List<String>> pathway_genepair = new HashMap<String, 
																List<String>>();
		String inp1 = "data\\raw\\intpathway\\cerevisiaeIntPathGenes";
		String inp2 = "data\\raw\\intpathway\\cerevisiaeIntPathGenePairs";
		String outp = "data\\preprocessed\\intpathway\\";
		// load pathway gene
		File file1 = new File(inp1);
		try {
			FileReader fr = new FileReader(file1.getAbsoluteFile());
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
					List<String> genes = new ArrayList<String>();
					genes.add(elem.get(elem.size()-1));
					pathway_gene.put(name, genes);
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
		
		//load pathway gene pairs
		File file2 = new File(inp2);
		try {
			FileReader fr = new FileReader(file2.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line=br.readLine())!=null) {
				List<String> elem = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
				// remove unneccessary info
				elem.remove("KEGG"); elem.remove("WikiPathways"); elem.remove("BioCyc");
				// get name of pathway
				String name = getPathwayName(elem.subList(3, elem.size()));
				// get gene pair
				String pair = elem.get(0) + " " + elem.get(1);
				if (!pathway_genepair.containsKey(name)) {
					List<String> pairs = new ArrayList<String>();
					pairs.add(pair);
					pathway_genepair.put(name, pairs);
				} else {
					List<String> pairs = pathway_genepair.get(name);
					pairs.add(pair);
					pathway_genepair.put(name, pairs);
				}
			}
			br.close(); fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		writePathway(outp, pathway);
		writePathwayGene(outp, pathway_gene);
		writePathwayGenePair(outp, pathway_genepair);
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
	
	private void writePathwayGenePair(String outp, HashMap<String, List<String>> p_gp) {
		File file = new File(outp + "pathway_genepair.txt");
		try {
			if (!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (String name : p_gp.keySet()) {
				// replace space with underscore
				// and turn to lower case
				String shortname = name.replaceAll(" ", "_").toLowerCase();
				for (String gene : p_gp.get(name)) {
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
		//prep.loadKEGG();
		// preprocess intPathway database
		//prep.loadIntPathway();
	}
}