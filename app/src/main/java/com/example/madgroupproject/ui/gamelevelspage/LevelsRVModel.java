package com.example.madgroupproject.ui.gamelevelspage;

public class LevelsRVModel {

    private String levelName;
    private int levelImg;
    private String levelDetail;
    private String levelNum;

    public LevelsRVModel(String levelName, String levelNum, String levelDetail, int levelImg) {
        this.levelDetail = levelDetail;
        this.levelImg = levelImg;
        this.levelName = levelName;
        this.levelNum = levelNum;
    }

    public String getLevelDetail() {
        return levelDetail;
    }

    public void setLevelDetail(String levelDetail) {
        this.levelDetail = levelDetail;
    }

    public int getLevelImg() {
        return levelImg;
    }

    public void setLevelImg(int levelImg) {
        this.levelImg = levelImg;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getLevelNum() {
        return levelNum;
    }

    public void setLevelNum(String levelNum) {
        this.levelNum = levelNum;
    }
}
