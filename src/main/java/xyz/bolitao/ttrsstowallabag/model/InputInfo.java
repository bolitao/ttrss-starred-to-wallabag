package xyz.bolitao.ttrsstowallabag.model;

import java.io.Serializable;

public class InputInfo implements Serializable {
    private static final long serialVersionUID = -5661982512040942494L;
    private String ttrssUrl;
    private String ttrssUsername;
    private String ttrssPassword;
    private String wallabagUrl;
    private String wallabagUsername;
    private String wallabagPassword;
    private String wallabagApiKey;
    private String wallabagApiSecret;
    private Boolean useProxy;
    private String proxyHost;
    private Integer proxyPort;

    public InputInfo(String ttrssUrl, String ttrssUsername, String ttrssPassword, String wallabagUrl, String wallabagUsername, String wallabagPassword, String wallabagApiKey, String wallabagApiSecret, Boolean useProxy, String proxyHost, Integer proxyPort) {
        this.ttrssUrl = ttrssUrl;
        this.ttrssUsername = ttrssUsername;
        this.ttrssPassword = ttrssPassword;
        this.wallabagUrl = wallabagUrl;
        this.wallabagUsername = wallabagUsername;
        this.wallabagPassword = wallabagPassword;
        this.wallabagApiKey = wallabagApiKey;
        this.wallabagApiSecret = wallabagApiSecret;
        this.useProxy = useProxy;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Override
    public String toString() {
        return "InputInfo{" +
                "ttrssUrl='" + ttrssUrl + '\'' +
                ", ttrssUsername='" + ttrssUsername + '\'' +
                ", ttrssPassword='" + ttrssPassword + '\'' +
                ", wallabagUrl='" + wallabagUrl + '\'' +
                ", wallabagUsername='" + wallabagUsername + '\'' +
                ", wallabagPassword='" + wallabagPassword + '\'' +
                ", wallabagApiKey='" + wallabagApiKey + '\'' +
                ", wallabagApiSecret='" + wallabagApiSecret + '\'' +
                ", useProxy=" + useProxy +
                ", proxyHost='" + proxyHost + '\'' +
                ", proxyPort='" + proxyPort + '\'' +
                '}';
    }

    public Boolean getUseProxy() {
        return useProxy;
    }

    public void setUseProxy(Boolean useProxy) {
        this.useProxy = useProxy;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public InputInfo() {
    }

    public String getTtrssUrl() {
        return ttrssUrl;
    }

    public void setTtrssUrl(String ttrssUrl) {
        this.ttrssUrl = ttrssUrl;
    }

    public String getTtrssUsername() {
        return ttrssUsername;
    }

    public void setTtrssUsername(String ttrssUsername) {
        this.ttrssUsername = ttrssUsername;
    }

    public String getTtrssPassword() {
        return ttrssPassword;
    }

    public void setTtrssPassword(String ttrssPassword) {
        this.ttrssPassword = ttrssPassword;
    }

    public String getWallabagUrl() {
        return wallabagUrl;
    }

    public void setWallabagUrl(String wallabagUrl) {
        this.wallabagUrl = wallabagUrl;
    }

    public String getWallabagUsername() {
        return wallabagUsername;
    }

    public void setWallabagUsername(String wallabagUsername) {
        this.wallabagUsername = wallabagUsername;
    }

    public String getWallabagPassword() {
        return wallabagPassword;
    }

    public void setWallabagPassword(String wallabagPassword) {
        this.wallabagPassword = wallabagPassword;
    }

    public String getWallabagApiKey() {
        return wallabagApiKey;
    }

    public void setWallabagApiKey(String wallabagApiKey) {
        this.wallabagApiKey = wallabagApiKey;
    }

    public String getWallabagApiSecret() {
        return wallabagApiSecret;
    }

    public void setWallabagApiSecret(String wallabagApiSecret) {
        this.wallabagApiSecret = wallabagApiSecret;
    }
}
