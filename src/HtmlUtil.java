import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HtmlUtil {
	public static String getHTML(String urlToRead) throws InterruptedException, IOException {
		try {
			StringBuilder result = new StringBuilder();
			URL url = new URL(urlToRead);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/vnd.github.mercy-preview+json");
			BufferedReader rd;
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
			return result.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw e;
		} catch (ProtocolException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			if(e.getMessage().startsWith("Server returned HTTP response code: 403")) {
				System.out.println("Github API's request limit reached. Wainting for 600 seconds before next request.");
				Thread.sleep(600000);
				return getHTML(urlToRead);
			}
			e.printStackTrace();
			throw e;
		}
	}
}
