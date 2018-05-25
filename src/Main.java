import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		Values.AccessToken = "f882ac6a24f6725037ce27da557d31e9966f7a7b";

		int minimumStars = 500;
		int maximumStars = 99999999;
		String activeAfter = "2018-01-01";
		String language = "java";

		int pageSize = 100;
		boolean printMatchCount = true;

		int matchCount = 0;
		boolean isIncomplete = true;

		int processedMatchCount = 0;
		int processedMatchCountForOneQuery = 0;
		int currentPage = 0;
		int lowestStar = 99999999;
		int searchLimit = 1000;
		ArrayList<GithubRepo> repositories = new ArrayList<GithubRepo>();

		DateFormat dateFormat = new SimpleDateFormat(Values.DateTimeFormatForNaming);
		Date date = new Date();
		String fileName = dateFormat.format(date) + ".csv";
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.println(
				"Name,\tStars,\tCommits,\tReleases,\tContributors,\tSize (kb),\tStart Date,\tLastest Commit Date,\tTopics,\tLink,\tTimestamp");
		writer.close();

		do {
			try {
				currentPage++;
				String requestUrl = "https://api.github.com/search/repositories?q=language:" + language + "+stars:"
						+ minimumStars + ".." + maximumStars + "+created:>=1991-01-01+pushed:>=" + activeAfter
						+ "&sort=stars&order=desc&per_page=" + pageSize + "&page=" + currentPage + "&access_token="
						+ Values.AccessToken;
				String response = "";
				response = HtmlUtil.getHTML(requestUrl);
				JSONObject object = new JSONObject(response);
				isIncomplete = object.getBoolean("incomplete_results");
				if (isIncomplete) {
					System.out.println("Incomplete response. Trying again.");
					currentPage--;
					continue;
				}

				matchCount = object.getInt("total_count");
				JSONArray matches = object.getJSONArray("items");
				processedMatchCountForOneQuery += matches.length();
				processedMatchCount += matches.length();

				if (printMatchCount) {
					System.out.println(matchCount + " Github repositories match the query.");
					printMatchCount = false;
				}

				for (int i = 0; i < matches.length(); i++) {
					String repoLink = "'Unknown JSON object'";
					try {
						JSONObject repositoryJSON = (JSONObject) matches.get(i);
						repoLink = repositoryJSON.getString("html_url");
						GithubRepo githubRepo = new GithubRepo(repositoryJSON);
						if (githubRepo.starGazerCount == maximumStars)
							if (repositories.stream().anyMatch(repo -> repo.starGazerCount == githubRepo.starGazerCount
									&& repo.htmlUrl.equals(githubRepo.htmlUrl))) {
								processedMatchCount--;
								continue;
							}
						lowestStar = githubRepo.starGazerCount;
						repositories.add(githubRepo);
						try (FileWriter fw = new FileWriter(fileName, true);
								BufferedWriter bw = new BufferedWriter(fw);
								PrintWriter out = new PrintWriter(bw)) {
							out.println(githubRepo);
						} catch (IOException e) {
							e.printStackTrace();
							System.out.println("Couldn't gather info for " + repoLink);
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Couldn't gather info for " + repoLink);
					}
				}

				System.out.println("Analyzed " + processedMatchCount + " matches.");
				;

				if (matchCount > searchLimit) {
					if (processedMatchCountForOneQuery >= searchLimit) {
						maximumStars = lowestStar;
						processedMatchCountForOneQuery = 0;
						currentPage = 0;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (processedMatchCountForOneQuery < matchCount);
	}

}
