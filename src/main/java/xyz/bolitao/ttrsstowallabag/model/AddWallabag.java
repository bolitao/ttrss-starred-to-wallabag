package xyz.bolitao.ttrsstowallabag.model;

import java.io.Serializable;

public class AddWallabag implements Serializable {
    private static final long serialVersionUID = -3217846117103070185L;

    private String url;
    private String content;
    private String title;

    @Override
    public String toString() {
        return "AddWallabag{" +
                "url='" + url + '\'' +
                ", content='" + content + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AddWallabag() {
    }

    public AddWallabag(String url, String content, String title) {
        this.url = url;
        this.content = content;
        this.title = title;
    }
}
