package xyz.bolitao.ttrsstowallabag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import xyz.bolitao.ttrsstowallabag.model.AddWallabag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppTest {
    String ttrssKey = "~";
    String wbUser = "~";
    String wbPass = "~";
    String wbKey = "~";
    String wbSecret = "~";

    @Test
    public void getStarredArticles() {
        HttpResponse<String> response = Unirest.get("https://rss.bolitao.xyz/plugins/fever/?api_key=" + ttrssKey + "&saved_item_ids=").asString();
        List<String> savedItemIds = Arrays.stream(new JSONObject(response.getBody()).get("saved_item_ids").toString().split(",")).collect(Collectors.toList());
        List<List<String>> partition = Lists.partition(savedItemIds, 50);

        String wbBaseWbUrl = "https://wallabag.bolitao.xyz";
        String wbOauthUrl = wbBaseWbUrl + "/oauth/v2/token";
        JSONObject wallabagToken = App.getWallabagToken(wbOauthUrl, wbUser, wbPass, wbKey, wbSecret);
        long startTs = System.currentTimeMillis();

        for (int i = 0; i < partition.size(); i++) {
            List<String> ids = partition.get(i);
            System.out.printf("all pages: %s, current page: %s, content size: %s%n", partition.size(), i + 1, ids.size());
            String s = String.join(",", ids);
            String feverInnerUrl = "https://rss.bolitao.xyz/plugins/fever/?api_key=" + ttrssKey + "&items=&with_ids=" + s;
            System.out.println(feverInnerUrl);
            boolean useProxy = true;
            if (useProxy) {
                Unirest.config()
                        .reset()
                        .socketTimeout(1000 * 60 * 10)
                        .connectTimeout(1000 * 60 * 10)
                        .proxy("192.168.31.5", 10809);
            } else {
                Unirest.config()
                        .reset()
                        .socketTimeout(1000 * 60 * 10)
                        .connectTimeout(1000 * 60 * 10);
            }

            HttpResponse<String> resp = Unirest.get(feverInnerUrl).asString();
            JSONArray items = new JSONObject(resp.getBody()).getJSONArray("items");

            try {
                for (Object next : items) {
                    JSONObject articleJson = (JSONObject) next;
                    String title = articleJson.getString("title");
                    String html = articleJson.getString("html");
                    String url = articleJson.getString("url");
                    String author = articleJson.getString("author");
                    int feedId = articleJson.getInt("feed_id");
                    int id = articleJson.getInt("id");
                    System.out.printf("id: %s, title: %s\n", id, title);

                    AddWallabag addWallabag = new AddWallabag(url, html, title);

                    try {
                        if (((System.currentTimeMillis() - startTs) / 1000) > (3600 - 600)) {
                            System.out.println("refresh wallabag token");
                            wallabagToken = App.getWallabagToken(wbOauthUrl, wbUser, wbPass, wbKey, wbSecret);
                            startTs = System.currentTimeMillis();
                        }
                        String wbAddEntityUrl = wbBaseWbUrl + "/api/entries.json";
                        String accessToken = wallabagToken.getString("access_token");
                        HttpResponse<String> sendWallabagResult = Unirest.post(wbAddEntityUrl)
                                .queryString("access_token", accessToken)
                                .body(addWallabag)
                                .asString();
                        String sendWallabagResultBody = sendWallabagResult.getBody();
                        System.out.println("sendWallabagResultBody: " + sendWallabagResultBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void loadYml() throws IOException {
        Yaml yaml = new Yaml();
        InputStream fileInputStream = Files.newInputStream(new File("settings.yml").toPath());
        Map<String, Object> yml = yaml.load(fileInputStream);
        System.out.println(yml);
        System.out.println(yml.get("use_proxy"));
    }

    @Test
    public void wallabag() throws JsonProcessingException {
        String wbAddEntityUrl = "https://wallabag.bolitao.xyz/api/entries.json";
        String accessToken = "haha";
        AddWallabag addWallabag = new AddWallabag("https://baidu.com", "<h1>hey</h1>", "just a test");
        String s = new ObjectMapper().writeValueAsString(addWallabag);
        System.out.println(s);
        HttpResponse<String> response = Unirest.post(wbAddEntityUrl)
                .header("Content-Type", "application/json")
                .queryString("access_token", accessToken)
                .body(s)
                .asString();
        System.out.println(response.getBody());
    }
}
