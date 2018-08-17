package com.example.administrator.speech.java;



public class KidMyData extends MyData {

    public KidMyData() {
        super();
    }

    protected void castNpcLine(StringBuilder npcStr, String[] npcLine, String name, boolean type) {

        if (type)
            npcStr.append("<name name=\"" + isContainChinese(name) + "\">" + isContainChinese(npcLine[0]) + ",," + isContainChinese(npcLine[2]) + "," + isContainChinese(npcLine[3]) + "</name>\n");
        else
            npcStr.append("<name name=\"" + isContainChinese(name) + "\">" + isContainChinese(npcLine[0]) + "," + isContainChinese(npcLine[1]) + "," + isContainChinese(npcLine[2]) + "," + isContainChinese(npcLine[3]) + "</name>\n");
    }

}
