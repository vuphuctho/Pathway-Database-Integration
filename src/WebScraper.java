import java.io.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;


public class WebScraper {
	public static enum Db {KEGG, WikiPathway};
	// write pathway data of a specific database to a txt file
	public static void writeCSVData(Db db, String filename, String pathway, List<String> genes) {
		String directory = "data\\raw\\";
		switch (db) {
			case KEGG:
				directory += "KEGG\\" + filename + ".txt";
				break;
			default:
		}
		try {
			File file = new File(directory);
			if (!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(pathway + "\n");
			for (String gene : genes) {
				bw.write(gene + "\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void scrapKEGG() {
		Document doc;
		try {
			// http protocol
			doc = Jsoup.connect("http://www.genome.jp/dbget-bin/get_linkdb?-t+2+genome:T00005").get();
			
			// get all pathway links
			Element pre = doc.select("pre").first();
			Elements links = pre.select("a[href]");
			// Element link = links.get(0);
			for (Element link : links) {
				// System.out.println("\nlink : " + link.absUrl("href"));
				// System.out.println("text : " + link.text());
				Document pathway;
				// extend timeout for each page
				pathway = Jsoup.connect(link.absUrl("href")).timeout(10*1000).get();
				Element name = pathway.select("td.td31").first();
				// System.out.println(name.text());
				// get all genes involved in pathway
				Elements trs = pathway.select("tr");
				for (Element tr : trs) {
					// find row with header "Gene"
					Element th = tr.select("th").first();
					if (th!= null && th.text().compareTo("Gene")==0) {
						Elements nobrs = tr.select("td").first().select("nobr");
						List<String> genes = new ArrayList<String>();
						for (Element nobr : nobrs) {
							// System.out.println("	Gene : " + nobr.text());
							genes.add(nobr.text());
						}
						// now write all info of pathway to CSV file
						WebScraper.writeCSVData(WebScraper.Db.KEGG, link.text(), name.text(), genes);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static void scrapBioCyc() {
		Document doc;
		try {
			// http protocol
			doc = Jsoup.connect("http://biocyc.org/YEAST/pathway-genes?object=PWY-5143").get();
			System.out.println("Title : " + doc.title());
			
			/* get all pathway links */
			// get all div possibly containing pathways
			Elements navBoxes = doc.select("a[href]");
			for (Element e : navBoxes) {
				System.out.println(" Text : " + e.text());
			}
			/*Elements divs = doc.select("div.ygtvitem");
			for (Element div : divs) {
				// check if the div is leaf (no children divs)
				if (div.select("div.ygtvchildren").size()==0) {
					// grab href link 
					Element pathway = div.select("a[href]").first();
					System.out.println(" Text : " + pathway.text());
				}
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// scraping pathway data of S.cerevisiae from KEGG Database
		// WebScraper.scrapKEGG();
		// scraping pathway data of S.cerevisiae from BioCyc
		WebScraper.scrapBioCyc();
	}
}
