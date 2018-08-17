/*
 * Author: Sam Zhu
 * Date: 11/23/2016
 * Description: support of generating animation #s if not already generated and Animation XML file as well
 *
 */
package com.example.administrator.speech.java;

import com.example.administrator.speech.java.Reader;

public class MyAnimationRow {
    private static int aniCnt = 1;

    private int rowNum;
    private String serialNum;
    private String role;
    private String animationStr;
    private String defStr;

    private String xmlAniStr = "";

    MyAnimationRow(int row, String sn, String role, String aStr, String def, String motion) {
        rowNum = row;
        serialNum = sn;
        this.role = role;
        animationStr = aStr;
        defStr = def;

        if (animationStr == null || animationStr.equals("")) {
            if (defStr.equals("") || defStr == null) {
                animationStr = getCameraStr(motion, "" + aniCnt);

                xmlAniStr = "" + aniCnt;
            } else {
                String[] mos = motion.split("&");

                int num1 = aniCnt;
                aniCnt++;
                int num2 = aniCnt;

                xmlAniStr = "" + num1 + " & " + num2;

                if (mos.length == 1)
                    animationStr = getCameraStr(mos[0].trim(), "" + num1) + ":" + num2;
                else if (mos.length == 2)
                    animationStr = getCameraStr(mos[0].trim(), "" + num1) + ":" + getCameraStr(mos[1].trim(), "" + num2);
                else
                    animationStr = "??? motions string has more than 3 tokens";
            }
            aniCnt++;

            //System.out.println(toString());
        } else {
            String[] tokens = animationStr.split("&");

            xmlAniStr = animationStr;

            if (tokens.length == 1)
                animationStr = getCameraStr(motion, tokens[0].trim());
            else if (tokens.length == 2) {
                if (tokens[1].trim().equals("")) {
                    xmlAniStr = tokens[0].trim();
                    animationStr = getCameraStr(motion, tokens[0].trim());
                } else {
                    xmlAniStr = tokens[0].trim() + " & " + tokens[1].trim();
                    String[] mos = motion.split("&");

                    if (mos.length == 1)
                        animationStr = getCameraStr(mos[0].trim(), tokens[0].trim()) + ":" + tokens[1].trim();
                    else if (mos.length == 2)
                        animationStr = getCameraStr(mos[0].trim(), tokens[0].trim()) + ":" + getCameraStr(mos[1].trim(), tokens[1].trim());
                    else
                        animationStr = "!!! motions string has more than 3 tokens";
                }
            }
        }
        if (Reader.Log)
            System.out.println("con: xml=" + xmlAniStr);
    }

    private static String getMotionStr(String inStr, String aniNum) {
        String result = "";
        String[] tokens = inStr.split(",", 3);

        if (tokens.length != 3) {
            return "not 3 tokens";
        }

        if (tokens[2].equals(""))
            return aniNum + "," + tokens[0] + "," + tokens[1] + ",,,,,";
        else if (tokens[2].charAt(0) == 'r') {
            return aniNum + "," + tokens[0] + "," + tokens[1] + ",,,," + tokens[2].substring(1) + ",";
        }

        String[] tokens1 = tokens[2].split("=");

        if (tokens1.length != 2)
            return "tokens1 not 2 tokens";

        String[] tokens2 = tokens1[0].split("-");
        if (tokens2.length != 2)
            return "tokens2 not 2 tokens";

        if (tokens2[0].equals("X"))
            tokens2[0] = "";

        if (tokens2[1].equals("X"))
            tokens2[1] = "";

        String[] tokens3 = tokens1[1].split("-");
        if (tokens3.length != 2)
            return aniNum + "," + tokens[0] + "," + tokens[1] + "," + tokens2[0] + "," + tokens2[1] + "," + tokens3[0] + ",,";

        if (tokens3[1].equals("X"))
            tokens3[1] = "";

        return aniNum + "," + tokens[0] + "," + tokens[1] + "," + tokens2[0] + "," + tokens2[1] + "," + tokens3[0] + "," + tokens3[1] + ",";
    }

    private static String getCameraStr(String inStr, String aniNum) {
        if (inStr.equals(Reader.NO_MOTION) || inStr.equals(""))
            return aniNum;

        String[] tokens = inStr.split(";");

        if (tokens.length == 1) {
            if (tokens[0].charAt(0) == 'P')
                return aniNum + ",,,,,,," + aniNum;
            else if (tokens[0].charAt(0) == 'M')
                return getMotionStr(tokens[0].substring(2), aniNum) + "";
            else
                return "wrong format 1";
        } else if (tokens.length == 2) {
            String result = "";
            if (tokens[0].charAt(0) == 'M')
                result = getMotionStr(tokens[0].substring(2), aniNum);
            else
                return "wrong format 2";

            if (tokens[1].charAt(0) == 'P')
                return result += aniNum;
            else
                return "wrong format 3";
        } else
            return "wrong format 1";
    }

    public String getAnimationStr() {
        return animationStr;
    }

    public int getRowNum() {
        return rowNum;
    }

    public String getXmlAniStr() {
        return xmlAniStr;
    }

    public String getXMLLine(String mantype) {
        String[] tokens = xmlAniStr.split("&");
        String result = "";
        String sTmp = "";

        System.out.println("xml=" + xmlAniStr);

        sTmp = serialNum.replace('-', '_');

        for (int i = 0; i < tokens.length; i++) {
            if (role.equals("H"))
                result += "  <animator id =\"" + tokens[i].trim() + "\">\n    <name></name>\n  </animator>\n";
            else if (role.equals("B"))
                result += "  <animator id =\"" + tokens[i].trim() + "\">\n    <name mantype=\"" + mantype + "\">Action_"
                        + sTmp + "_" + tokens[i].trim() + "</name>\n  </animator>\n";
            else if (role.equals("X")) {
                result += "  <animator id =\"" + tokens[i].trim() + "\">\n    <name mantype=\"NPC\">Action_"
                        + sTmp + "_" + tokens[i].trim() + "</name>\n  </animator>\n";
                result += "  <animator id =\"" + tokens[i].trim() + "-1\">\n    <name mantype=\"NPC\">Action_"
                        + sTmp + "_" + tokens[i].trim() + "</name>\n  </animator>\n";
            }
        }


//	System.out.println(result);

        return result;
    }

    public String getXMLLineJT(String mantype) {
        String[] tokens = xmlAniStr.split("&");
        String result = "";
        String sTmp = "";

        System.out.println("xml=" + xmlAniStr);

        sTmp = serialNum.replace('-', '_');

        for (int i = 0; i < tokens.length; i++) {
            if (role.equals("H"))
                result += "  <animator id =\"" + tokens[i].trim() + "\">\n    <name></name>\n  </animator>\n";
            else if (role.equals("B"))
                result += "  <animator id =\"" + tokens[i].trim() + "\">\n    <name mantype=\"" + mantype + "\">" + tokens[i].trim() + "</name>\n  </animator>\n";
            else if (role.equals("X")) {
                result += "  <animator id =\"" + tokens[i].trim() + "\">\n    <name mantype=\"NPC\">" + tokens[i].trim() + "</name>\n  </animator>\n";
//		result += "  <animator id =\"" + tokens[i].trim() + "-1\">\n    <name mantype=\"NPC\">" + tokens[i].trim() + "</name>\n  </animator>\n";
            }
        }


//	System.out.println(result);

        return result;
    }

    public static void reset() {
        aniCnt = 1;
    }

    public String toString() {
        return rowNum + ":" + serialNum + ":" + role + ":" + animationStr;
    }

    public static void main(String args[]) {
        String str1 = "M:1.5,2.2,1-2=2-3;P";
        String str2 = "M:1.5,2.2,X-2=2-3;P";
        String str3 = "M:1.5,0,1-2=2-3";
        String str4 = "M:1.5,0,;P";
        String str5 = "P";
        String str6 = "M:1.5,0,1-2=2";
        String str7 = "M:1.5,0,r5";
        String str8 = "M:0.5,0,9-9=0-3";

        System.out.println(getCameraStr(str1, "62"));
        System.out.println(getCameraStr(str2, "62"));
        System.out.println(getCameraStr(str3, "62"));
        System.out.println(getCameraStr(str4, "62"));
        System.out.println(getCameraStr(str5, "62"));
        System.out.println(getCameraStr(str6, "62"));
        System.out.println(getCameraStr(str7, "62"));
        System.out.println(getCameraStr(str8, "61"));
    }
}
