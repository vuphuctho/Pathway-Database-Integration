import java.io.*;
import java.util.*;

public class DatabaseIntegration {
	private String dir1;
	private String dir2;
	
	public DatabaseIntegration(String _dir1, String _dir2) {
		this.dir1 = _dir1;
		this.dir2 = _dir2;
	}
	
	// matching 2 databases using text matching (LCS)
	public void matchByName() {
		List<String> db1 = loadPathway(dir1);
		List<String> db2 = loadPathway(dir2);
		//brute force: match db1 and db2
		for (String name1 : db1) {
			for (String name2 : db2) {
				
			}
		}
	}
	
	// load pathway.txt files inside the 2 input directories
	private ArrayList<String> loadPathway(String dir) {
		ArrayList<String> result = new ArrayList<String>();
		File file = new File(dir+"\\pathway.txt");
		try {
			FileReader fr = new FileReader(file.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while((line=br.readLine())!=null) {
				result.add(line);
			}
			br.close(); fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// load pathway_gene.txt files
	public void loadPathwayGene() {
		
	}
	
	public static void main(String[] args) {
		
	}
}
