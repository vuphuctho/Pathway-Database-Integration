import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;


public class PathInt {
	
	public static TreeMap <String, Integer> allGenes;
	public static Integer geneCount;
	public static TreeMap <String, Vector<Integer>> glist1,glist2;
	public static Vector  <String> plist1,plist2;
	
	public static void main(String[] args) {
		// Usage
		if (args.length != 4) { 
			System.out.println("Usage : <pathway list 1> <pathway gene list 1>"
							 		+ " <pathway list 2> <pathway gene list 2>");
		} else try {
			allGenes 	= new TreeMap <String, Integer>();
			geneCount 	= 0;
			new PathInt(args[0],args[1],args[2],args[3]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Integer getGeneIndex(String geneCode) {
		Integer index = allGenes.get(geneCode);
		if (index != null) {
			return index;
		} else {
			allGenes.put(geneCode,geneCount++);
			return geneCount - 1;
		}
	}
	
	public static Vector <String> getPathwayList(String inputFile) 
	throws FileNotFoundException,IOException {
		BufferedReader fin = new BufferedReader(new FileReader(inputFile));
		String line;
		Vector <String> plist = new Vector <String>();
		while ((line = fin.readLine()) != null) {
			plist.add(line);
		}
		fin.close();
		return plist;
	}
	
	public static TreeMap < String, Vector<Integer> > getGeneList(String inputFile)
	throws FileNotFoundException,IOException {
		BufferedReader fin = new BufferedReader(new FileReader(inputFile));
		TreeMap < String, Vector <Integer> > glist = new TreeMap < String, Vector <Integer> >();
		
		// Initialize last pathways read 
		String lastpw = null, line, words[];
		Vector <Integer> genes = new Vector <Integer> ();
		
		while ((line = fin.readLine())!= null) {
			words = line.split(" ");
			Integer index = getGeneIndex(words[1]);
			if (lastpw == null) {
				// first time
				lastpw = words[0];
				genes.add(index);
			} else if (lastpw.equals(words[0])) {
				// same pathway
				genes.add(index);
			} else {
				// new pathway
				glist.put(lastpw, genes);
				lastpw = words[0];
				genes.clear(); genes.add(index);
			}
		}
		
		// Put last pathway
		glist.put(lastpw,genes);
		fin.close();
		
		return glist;
	}
	
	public static Double geneAgreementScore(String pathway1, String pathway2) {
		// Acquire 2 gene lists
		Vector <Integer> genes1 = glist1.get(pathway1),
						 genes2 = glist2.get(pathway2);
		// Intersection size
		int n_intersect = 0;
		
		// Get intersection size of 2 arrays (O(max(n1,n2)))
		HashSet<Integer> hash 	= new HashSet<Integer>();
		for (Integer i : genes1) hash.add(i);
		for (Integer j : genes2) if (hash.contains(j)) n_intersect++; 
		
		// Crude score (n_intersect / n_union), should be modified to hypergeometric test
		Double score =  (double) n_intersect / (double)(genes1.size() + genes2.size() - n_intersect);
		
		return score;
	}
	
	public PathInt(String pw1, String ge1, String pw2, String ge2) 
	throws FileNotFoundException, IOException {
		plist1 = getPathwayList(pw1); 
		plist2 = getPathwayList(pw2);
		glist1 = getGeneList(ge1);
		glist2 = getGeneList(ge2);
		
		Double scoreArr[][] = new Double[(int)plist1.size()][(int)plist2.size()];
		for (int i = 0; i < plist1.size(); i++) {
			for (int j = 0; j < plist2.size(); j++) {
				scoreArr[i][j] = geneAgreementScore(plist1.get(i),plist2.get(j));
			}
		}
	}
}
