package com.example.ggoggodong.story;

public class StoryPage {
    private String text;
    private String drawingPath; // 내부 저장소 경로

    public StoryPage() {}

    public StoryPage(String text, String drawingPath) {
        this.text = text;
        this.drawingPath = drawingPath;
    }

    public String getText() {
        return text;
    }

    public String getDrawingPath() {
        return drawingPath;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDrawingPath(String drawingPath) {
        this.drawingPath = drawingPath;
    }
}
