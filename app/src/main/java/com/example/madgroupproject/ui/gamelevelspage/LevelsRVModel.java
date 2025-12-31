package com.example.madgroupproject.ui.gamelevelspage;

public class LevelsRVModel {

    private String levelName,levelDetail;
    private int levelFrame, levelNum, bgColor;

    public LevelsRVModel(String levelName, int levelNum, String levelDetail, int levelFrame, int bgColor) {
        this.levelDetail = levelDetail;
        this.levelFrame = levelFrame;
        this.levelName = levelName;
        this.levelNum = levelNum;
        this.bgColor = bgColor;
    }

    public String getLevelDetail() {
        return levelDetail;
    }

    public void setLevelDetail(String levelDetail) {
        this.levelDetail = levelDetail;
    }

    public int getLevelFrame() {
        return levelFrame;
    }

    public void setLevelFrame(int levelFrame) {
        this.levelFrame = levelFrame;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public int getLevelNum() {
        return levelNum;
    }

    public void setLevelNum(int levelNum) {
        this.levelNum = levelNum;
    }
    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }
}
