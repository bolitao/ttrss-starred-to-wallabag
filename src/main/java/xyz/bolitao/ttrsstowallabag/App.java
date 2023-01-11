package xyz.bolitao.ttrsstowallabag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.yaml.snakeyaml.Yaml;
import xyz.bolitao.ttrsstowallabag.model.AddWallabag;
import xyz.bolitao.ttrsstowallabag.model.InputInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) throws IOException {
        // InputInfo inputInfo = getInputInfo();
        InputInfo inputInfo = App.getConfigFromYml();
        String feverApiKey = DigestUtils.md5Hex(inputInfo.getTtrssUsername() + ":" + inputInfo.getTtrssPassword());
        System.out.println("fever api key: " + feverApiKey);
        HttpResponse<String> response = Unirest.get(inputInfo.getTtrssUrl() + "/?api_key=" + feverApiKey + "&saved_item_ids=").asString();
        List<String> savedItemIds = Arrays.stream(new JSONObject(response.getBody()).get("saved_item_ids").toString().split(",")).collect(Collectors.toList());
        List<List<String>> partition = Lists.partition(savedItemIds, 50);

        String wbBaseWbUrl = inputInfo.getWallabagUrl();
        String wbOauthUrl = wbBaseWbUrl + "/oauth/v2/token";
        JSONObject wallabagToken = App.getWallabagToken(wbOauthUrl, inputInfo.getWallabagUsername(), inputInfo.getWallabagPassword(),
                inputInfo.getWallabagApiKey(), inputInfo.getWallabagApiSecret());
        long startTs = System.currentTimeMillis();

        for (int i = 0; i < partition.size(); i++) {
            List<String> ids = partition.get(i);
            System.out.printf("all pages: %s, current page: %s, content size: %s%n", partition.size(), i + 1, ids.size());
            String s = String.join(",", ids);
            String feverInnerUrl = inputInfo.getTtrssUrl() + "/?api_key=" + feverApiKey + "&items=&with_ids=" + s;
            System.out.println(feverInnerUrl);
            boolean useProxy = true;
            if (inputInfo.getUseProxy()) {
                Unirest.config()
                        .reset()
                        .socketTimeout(1000 * 60 * 10)
                        .connectTimeout(1000 * 60 * 10)
                        .proxy(inputInfo.getProxyHost(), inputInfo.getProxyPort());
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
                    Document document = Jsoup.parse(html);
                    // System.out.println(id + " " + document.body());

                    AddWallabag addWallabag = new AddWallabag(url, html, title);

                    try {
                        if (((System.currentTimeMillis() - startTs) / 1000) > (3600 - 600)) {
                            System.out.println("refresh wallabag token before expire...");
                            wallabagToken = App.getWallabagToken(wbOauthUrl, inputInfo.getWallabagUsername(), inputInfo.getWallabagPassword(),
                                    inputInfo.getWallabagApiKey(), inputInfo.getWallabagApiSecret());
                            startTs = System.currentTimeMillis();
                        }
                        String wbAddEntityUrl = wbBaseWbUrl + "/api/entries.json";
                        String accessToken = wallabagToken.getString("access_token");
                        HttpResponse<String> sendWallabagResult = Unirest.post(wbAddEntityUrl)
                                .header("Content-Type", "application/json")
                                .queryString("access_token", accessToken)
                                .body(new ObjectMapper().writeValueAsString(addWallabag))
                                .asString();
                        String sendWallabagResultBody = sendWallabagResult.getBody();
                        System.out.println("send to wallabag result: " + sendWallabagResultBody);
                    } catch (Exception e) {
                        System.out.printf("ERROR occurred when exporting to wallabag，ttrss id: %s, article title: %s\n", id, title);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get token
     *
     * @param url      url
     * @param username username
     * @param password pw
     * @param key      key
     * @param secret   secret
     * @return auth result. e.g.<br>
     * {
     * "access_token": "aaa",
     * "expires_in": 3600,
     * "token_type": "bearer",
     * "scope": null,
     * "refresh_token": "bbb"
     * }
     */
    public static JSONObject getWallabagToken(String url, String username, String password, String key, String secret) {
        String body = Unirest.post(url)
                .field("grant_type", "password")
                .field("client_id", key)
                .field("client_secret", secret)
                .field("username", username)
                .field("password", password)
                .asString().getBody();
        JSONObject jsonObject = new JSONObject(body);
        System.out.println("wallabag token: " + jsonObject.get("access_token"));
        return jsonObject;
    }

    public static InputInfo getInputInfo() {
        Scanner scanner = new Scanner(System.in);
        InputInfo inputInfo = new InputInfo();
        System.out.println("ttrss url: ");
        inputInfo.setTtrssUrl(scanner.nextLine() + "/plugins/fever");
        System.out.println("ttrss fever username: ");
        inputInfo.setTtrssUsername(scanner.nextLine());
        System.out.println("ttrss fever password: ");
        inputInfo.setTtrssPassword(scanner.nextLine());
        System.out.println("wallabag url (no /api): ");
        inputInfo.setWallabagUrl(scanner.nextLine());
        System.out.println("wallabag username: ");
        inputInfo.setWallabagUsername(scanner.nextLine());
        System.out.println("wallabag password: ");
        inputInfo.setWallabagPassword(scanner.nextLine());
        System.out.println("wallabag api key: ");
        inputInfo.setWallabagApiKey(scanner.nextLine());
        System.out.println("wallabag apiPassword: ");
        inputInfo.setWallabagApiSecret(scanner.nextLine());
        System.out.println("use proxy, 1 / 0?");
        inputInfo.setUseProxy(1 == Integer.parseInt(scanner.nextLine()));
        if (inputInfo.getUseProxy()) {
            System.out.println("proxy host: ");
            inputInfo.setProxyHost(scanner.nextLine());
            System.out.println("proxy port: ");
            inputInfo.setProxyPort(Integer.parseInt(scanner.nextLine()));
        }
        System.out.println(inputInfo);
        return inputInfo;
    }

    public static InputInfo getConfigFromYml() throws IOException {
        Yaml yaml = new Yaml();
        InputStream fileInputStream = Files.newInputStream(new File("settings.yml").toPath());
        Map<String, Object> yml = yaml.load(fileInputStream);
        System.out.println("config:" + yml);
        InputInfo inputInfo = new InputInfo();
        inputInfo.setTtrssUrl((String) yml.get("ttrss_url"));
        inputInfo.setTtrssUsername((String) yml.get("fever_username"));
        inputInfo.setTtrssPassword((String) yml.get("fever_password"));
        inputInfo.setWallabagUrl((String) yml.get("wallabag_url"));
        inputInfo.setWallabagUsername((String) yml.get("wallabag_username"));
        inputInfo.setWallabagPassword((String) yml.get("wallabag_password"));
        inputInfo.setWallabagApiKey((String) yml.get("wallabag_api_key"));
        inputInfo.setWallabagApiSecret((String) yml.get("wallabag_api_secret"));
        if (yml.get("use_proxy") == null) {
            inputInfo.setUseProxy(false);
        } else {
            boolean useProxy = (boolean) yml.get("use_proxy");
            inputInfo.setUseProxy(useProxy);
            if (useProxy) {
                inputInfo.setUseProxy(true);
                inputInfo.setProxyHost((String) yml.get("proxy_host"));
                inputInfo.setProxyPort((Integer) yml.get("proxy_port"));
            }
        }
        return inputInfo;
    }
}
