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
		List<List<Double>> scores = new ArrayList<List<Double>>();
		//brute force: text match db1 and db2
		for (String name1 : db1) {
			List<Double> row_score = new ArrayList<Double>();
			for (String name2 : db2) {
				row_score.add((double)TextMatcher.LCS(name1, name2).length());
			}
			scores.add(row_score);
		}
		// normalize scores for further validation/comparison
		scores = normalize(scores);
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
	
	// NOTE: always normalize scores after computing
	// normalize scores using feature scaling
	private List<List<Double>> normalize(List<List<Double>> scores) {
		List<List<Double>> normalized = new ArrayList<List<Double>>();
		double min = scores.get(0).get(0); double max = scores.get(0).get(0);
		for (List<Double> row_score : scores) {
			for (double score : row_score) {
				if (score>max) max=score;
				if (score<min) min=score;
			}
		}
		if (max!=min) {
			for (List<Double> row_score : scores) {
				List<Double> row = new ArrayList<Double>();
				for (double score : row_score) {
					row.add((score-min)/(max-min));
				}
				normalized.add(row);
			}
		} else {
			normalized = scores;
		}
		return normalized;
	}
	
	public static void main(String[] args) {}
}