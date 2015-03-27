
// providing methods for text-matching
// currently only provide LCS
public class TextMatcher {
	
	/*
	 * Input: two strings str1 and str2
	 * Output: their longest common subsequence
	 */
	public static String LCS(String str1, String str2) {
		String result = "";
		int n = str1.length(); 
		int m = str2.length();
		
		int[][] opt = new int[n+1][m+1];
		
		for (int i=n-1;i>=0; i--) {
			for (int j=m-1; j>=0; j--) {
				// score for match = 1; insert/delete = 0
				if (str1.charAt(i)==str2.charAt(j)) {
					opt[i][j] = opt[i+1][j+1]+1;
				} else {
					opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
				}
			}
		}
		
		int i=0; int j=0;
		while (i<n && j<m) {
			if (str1.charAt(i)==str2.charAt(j)) {
				result += str1.charAt(i);
				i++; j++;
			} else if (opt[i+1][j]>=opt[i][j+1]) {
				i++;
			} else {
				j++;
			}
		}
		 
		return result;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String str1 = "nematode_knowledge";
		String str2 = "empty_bottle";
		System.out.println(TextMatcher.LCS(str1, str2));
	}

}
