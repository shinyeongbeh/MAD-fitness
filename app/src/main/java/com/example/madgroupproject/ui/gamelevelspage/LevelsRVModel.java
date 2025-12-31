package com.example.madgroupproject.ui.gamelevelspage;

public class LevelsRVModel {

    private String levelName,levelDetail;
    private int levelImg, levelNum;

    public LevelsRVModel(String levelName, int levelNum, String levelDetail, int levelImg) {
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

    public int getLevelNum() {
        return levelNum;
    }

    public void setLevelNum(int levelNum) {
        this.levelNum = levelNum;
    }
}
