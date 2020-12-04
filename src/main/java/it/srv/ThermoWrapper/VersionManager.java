package it.srv.ThermoWrapper;

import it.srv.ThermoWrapper.dao.InfoDAO;
import it.srv.ThermoWrapper.model.Info;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;

public class VersionManager {
    public Info searchNewVersion(){
        Info info = new Info();
        try{
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://serva.altervista.org/prove/thermo.php"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            info.setLastsearch(LocalDateTime.now());
            info.setWebversion(json.getString("webversion"));
            info.setToolsversion(json.getString("toolsversion"));
            info.setWebeurl(json.getString("weburl"));
            info.setToolsurl(json.getString("toolsurl"));
            info.setWebextra(json.optString("webextra").equalsIgnoreCase("")? null : json.optString("webextra"));
            info.setToolsextra(json.optString("toolsextra").equalsIgnoreCase("")? null : json.optString("toolsextra"));
        } catch (IOException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        return info;
    }

    public void download(String url, String fileName){
        try{
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(fileName);
            //134MB max download
            fos.getChannel().transferFrom(rbc, 0, 1<<27);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public boolean checkVersions(Info newVersion, Info actualVersion, InfoDAO infoDAO) {
        boolean newVersionInstalled = false;
        if (!newVersion.getWebversion().equalsIgnoreCase(actualVersion.getWebversion())
                || !newVersion.getToolsversion().equalsIgnoreCase(actualVersion.getToolsversion())) {
            if (!newVersion.getWebversion().equalsIgnoreCase(actualVersion.getWebversion()))
                download(newVersion.getWebeurl(), "ThermoSmartSpring".concat(newVersion.getWebversion()).concat(".jar"));
            if (!newVersion.getToolsversion().equalsIgnoreCase(actualVersion.getToolsversion()))
                download(newVersion.getToolsurl(), "ThermoTools".concat(newVersion.getToolsversion()).concat(".jar"));
            newVersion.setLastupdate(LocalDateTime.now());
            infoDAO.save(newVersion);
            newVersionInstalled = true;
        } else {
            actualVersion.setLastsearch(LocalDateTime.now());
            infoDAO.save(actualVersion);
        }
        return newVersionInstalled;
    }
}
