import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class GitLabStaleBranchCleaner {

    private static final String GITLAB_API_URL = "https://gitlab.com/api/v4";
    private static final String PRIVATE_TOKEN = "YOUR_GITLAB_ACCESS_TOKEN"; // Replace with your token
    private static final String PROJECT_ID = "YOUR_PROJECT_ID"; // Replace with your project ID
    private static final int STALE_DAYS = 30; // Branches older than 30 days will be deleted

    public static void main(String[] args) throws IOException {
        System.out.println("Starting stale branch detection...");
        listAndDeleteStaleBranches();
        System.out.println("Stale branch detection complete.");
    }

    private static void listAndDeleteStaleBranches() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(GITLAB_API_URL + "/projects/" + PROJECT_ID + "/repository/branches");
        request.setHeader("PRIVATE-TOKEN", PRIVATE_TOKEN);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONArray branches = new JSONArray(responseBody);

            for (int i = 0; i < branches.length(); i++) {
                JSONObject branch = branches.getJSONObject(i);
                String branchName = branch.getString("name");
                String commitDate = branch.getJSONObject("commit").getString("committed_date");

                if (isBranchStale(commitDate)) {
                    System.out.println("Stale branch detected: " + branchName);
                    deleteBranch(httpClient, branchName);
                }
            }
        }
    }

    private static boolean isBranchStale(String commitDate) {
        OffsetDateTime lastCommitDate = OffsetDateTime.parse(commitDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime now = OffsetDateTime.now();

        long daysDifference = java.time.Duration.between(lastCommitDate, now).toDays();
        return daysDifference > STALE_DAYS;
    }

    private static void deleteBranch(CloseableHttpClient httpClient, String branchName) throws IOException {
        HttpDelete deleteRequest = new HttpDelete(GITLAB_API_URL + "/projects/" + PROJECT_ID + "/repository/branches/" + branchName);
        deleteRequest.setHeader("PRIVATE-TOKEN", PRIVATE_TOKEN);

        try (HttpResponse response = httpClient.execute(deleteRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                System.out.println("Deleted branch: " + branchName);
            } else {
                System.out.println("Failed to delete branch: " + branchName + " - Status code: " + statusCode);
            }
        }
    }
}
