import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GithubRepo {
	public GithubRepo(JSONObject repositoryJSON)
			throws JSONException, ParseException, InterruptedException, IOException {
		this.id = repositoryJSON.getInt("id");
		this.name = repositoryJSON.getString("name");
		this.fullName = repositoryJSON.getString("full_name");
		this.language = repositoryJSON.getString("language");

		this.htmlUrl = repositoryJSON.getString("html_url");
		this.url = repositoryJSON.getString("url");
		this.tagslUrl = url + "/tags";
		this.contributorslUrl = url + "/contributors";
		this.commitslUrl = url + "/commits";
		this.topicsUrl = url + "/topics";

		this.creationDate = new SimpleDateFormat(Values.DateTimeFormat).parse(repositoryJSON.getString("created_at"));
		this.lastPushDate = new SimpleDateFormat(Values.DateTimeFormat).parse(repositoryJSON.getString("pushed_at"));

		this.starGazerCount = repositoryJSON.getInt("stargazers_count");
		this.watcherCount = repositoryJSON.getInt("watchers_count");
		this.forkCount = repositoryJSON.getInt("forks_count");
		this.size = repositoryJSON.getInt("size");

		this.releaseCount = countItems(tagslUrl);
		this.contributorCount = countItems(contributorslUrl);
		this.commitCount = countCommits(commitslUrl);
		this.topics = fetchTopics(topicsUrl);
		
		this.timestamp =  new Date();
	}

	private ArrayList<String> fetchTopics(String url) throws InterruptedException, IOException {
		ArrayList<String> topics = new ArrayList<String>();
		try {
			String response = HtmlUtil.getHTML(url + "?access_token=" + Values.AccessToken);
			JSONObject object = new JSONObject(response);
			JSONArray names = object.getJSONArray("names");
			for (Object item : names) {
				topics.add(item.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return topics;
	}

	private int countCommits(String url) throws InterruptedException, IOException {
		try {
			JSONObject firstCommit, lastCommit;
			String base, head;
			DateFormat dateFormat = new SimpleDateFormat(Values.DateTimeFormatShort);
			Date firstCommitApproaxDate = new Date(this.creationDate.getTime());
			Calendar c = Calendar.getInstance();
			c.setTime(firstCommitApproaxDate);
			JSONArray list;
			String response;
			do{
				c.add(Calendar.DATE, 5); 
				firstCommitApproaxDate = c.getTime();
				response = HtmlUtil.getHTML(url + "?" + "until=" + dateFormat.format(firstCommitApproaxDate) + "T23:59:59Z"
						+ "&per_page=" + 100 + "&access_token=" + Values.AccessToken);
				list = new JSONArray(response);
			} while (list.length() == 0);

			firstCommit = (JSONObject) list.get(list.length() - 1);
			base = firstCommit.getString("sha");
			response = HtmlUtil.getHTML(url + "?" + "per_page=" + 1 + "&access_token=" + Values.AccessToken);
			list = new JSONArray(response);
			lastCommit = (JSONObject) list.get(0);
			head = lastCommit.getString("sha");
			response = HtmlUtil
					.getHTML(this.url + "/compare/" + base + "..." + head + "?" + "access_token=" + Values.AccessToken);
			JSONObject diff = new JSONObject(response);
			return diff.getInt("ahead_by") + 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	private int countItems(String url) throws InterruptedException, IOException {
		int count;
		try {
			count = 0;
			int page = 0;
			JSONArray list;
			do {
				page++;
				String response = HtmlUtil
						.getHTML(url + "?per_page=" + 100 + "&page=" + page + "&access_token=" + Values.AccessToken);
				list = new JSONArray(response);
				count += list.length();
			} while (list.length() != 0);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return count;
	}

	public String toString() {
		String topicsString = "";
		for (String string : topics) {
			topicsString += string + " | ";
		}
		String description = name + ",\t" + starGazerCount + ",\t" + commitCount + ",\t" + releaseCount
				+ ",\t" + contributorCount 
				+ ",\t" + size 
				+ ",\t" + new SimpleDateFormat(Values.DateTimeFormatShort).format(creationDate)
				+ ",\t" + new SimpleDateFormat(Values.DateTimeFormatShort).format(lastPushDate) 
				+ ",\t" + topicsString
				+ ",\t" + htmlUrl
				+ ",\t" + new SimpleDateFormat(Values.DateTimeFormat).format(timestamp);
		return description;
	}

	private String name;
	private String fullName;
	private String language;
	private int id;

	public String htmlUrl;
	private String url;
	private String tagslUrl;
	private String contributorslUrl;
	private String commitslUrl;
	private String topicsUrl;

	private int releaseCount;
	private int contributorCount;
	private int commitCount;
	private ArrayList<String> topics = new ArrayList<>();

	private Date creationDate;
	private Date lastPushDate;

	public int starGazerCount;
	private int watcherCount;
	private int forkCount;
	private int size;
	
	private Date timestamp;
}
