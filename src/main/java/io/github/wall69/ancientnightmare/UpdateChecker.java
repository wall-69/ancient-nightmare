package io.github.wall69.ancientnightmare;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

// By: KillerQueen
public class UpdateChecker {

    private int resourceId;
    private URL resourceURL;
    private String currentVersionString;
    private String latestVersionString;
    private UpdateCheckResult updateCheckResult;

    public UpdateChecker(Main main, int resourceId) {
        try {
            this.resourceId = resourceId;
            this.resourceURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        } catch (Exception exception) {
            return;
        }

        currentVersionString = main.getDescription().getVersion();
        latestVersionString = getLatestVersion();

        if (latestVersionString == null) {
            updateCheckResult = UpdateCheckResult.NO_RESULT;
            return;
        }

        int currentVersion = Integer.parseInt(currentVersionString.replace("v", "")
                .replace(".", ""));
        int latestVersion = Integer.parseInt(getLatestVersion().replace("v", "")
                .replace(".", ""));

        if (currentVersion < latestVersion) updateCheckResult = UpdateCheckResult.OUT_DATED;
        else if (currentVersion == latestVersion) updateCheckResult = UpdateCheckResult.UP_TO_DATE;
        else updateCheckResult = UpdateCheckResult.UNRELEASED;
    }

    public enum UpdateCheckResult {
        NO_RESULT, OUT_DATED, UP_TO_DATE, UNRELEASED,
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/" + resourceId;
    }

    public String getCurrentVersionString() {
        return currentVersionString;
    }

    public String getLatestVersionString() {
        return latestVersionString;
    }

    public UpdateCheckResult getUpdateCheckResult() {
        return updateCheckResult;
    }

    public String getLatestVersion() {
        try {
            URLConnection urlConnection = resourceURL.openConnection();
            return new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
        } catch (Exception exception) {
            return null;
        }
    }

}
