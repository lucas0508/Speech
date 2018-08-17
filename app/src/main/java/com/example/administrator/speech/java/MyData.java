package com.example.administrator.speech.java;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyData {

    private boolean start0 = false;//贴图索引是否从0开始

    private String xmls;//其他

    private String cellStr;//动画号
    private String cellStr2;//镜头
    private String cellStr3;//npc
    private String cellStr4;//贴图

    private Pattern pat;

    public MyData() {

    }

    public void SetAll(String cellStr, String cellStr2, String cellStr3, String cellStr4, boolean start0) {

        this.cellStr = isContainChinese(cellStr);
        this.cellStr2 = isContainChinese(cellStr2);
        this.cellStr3 = isContainChinese(cellStr3);
        this.cellStr4 = isContainChinese(cellStr4);
        this.start0 = start0;

        init();

    }

    public void clear() {
        this.cellStr = null;
        this.cellStr2 = null;
        this.cellStr3 = null;
        this.cellStr4 = null;
        this.xmls = null;

    }

    public String getXmls() {
        return xmls;
    }

    private void init() {
        String act[] = cellStr.split("&");    //动画号

        StringBuilder xml = new StringBuilder();

        //start other
        String ttNum = "";
        String tts = "";
        String ttType = "0";
        String ttTime = "0";
        if (cellStr4 != null && !"".equals(cellStr4)) {
            String[] other = cellStr4.replace("，", ",").split(",", 2);

            String regEx = "(?=[\\s\\S]*?)([0-9]+\\.[0-9]*[1-9][0-9]*|[0-9]*[1-9][0-9]*\\.[0-9]+|[0-9]*[1-9][0-9]*)(?=[\\s\\S]*?)";
            String str = other[0];
            pat = Pattern.compile(regEx);
            Matcher mat = pat.matcher(str);
            while (mat.find()) {
                if (mat.group(1) != null) {
                    float ma = Float.parseFloat(mat.group(1));
                    if (ma > 0) {
                        ttType = "1";
                        ttTime = ma + "";
                    }

                }
                break;
            }

            if (cellStr4.indexOf("P") != -1) {
                str = other[1];
                String regex = "([^\u4e00-\u9fa5]+)";
                Pattern pat = Pattern.compile(regex);
                mat = pat.matcher(str);
                while (mat.find()) {
                    str = mat.group(1).trim();
                    break;
                }

                String[] ttU = str.split("=");

                ttNum = ttU[0].substring(ttU[0].indexOf("P") + 1, ttU[0].length());
                String regExz = "(?=[\\s\\S]*?)([0-9]+\\.[0-9]*[1-9][0-9]*|[0-9]*[1-9][0-9]*\\.[0-9]+|[0-9]*[1-9][0-9]*)(?=[\\s\\S]*?)";
                pat = Pattern.compile(regExz);
                mat = pat.matcher(ttU[0]);
                while (mat.find()) {
                    ttNum = mat.group(1);
                    break;
                }

                if (ttU.length > 1) {
                    pat = Pattern.compile(regEx);
                    mat = pat.matcher(ttU[1]);
                    while (mat.find()) {
                        tts = mat.group(1);
                    }
                }

            }
        }
        //stop other

        for (int i = 0; i < act.length; i++) {
            String ac = act[i].trim();

            if (ac.equals(""))
                continue;

            xml.append("<id act=\"" + ac + "\">\n");
            String testStr = "";

            String cell[] = cellStr2.split(",");
            if (cell.length != 3) {
                xml.append("<name name=\"cam\">,,0,0,0</name>\n");
            } else {
//        if (i < 1) {
                if (cell[2] != null || !cell[2].trim().equals("")) {
                    pat = Pattern.compile("-(.*)=");
                    Matcher m = pat.matcher(cell[2]);
                    while (m.find()) {
                        testStr = m.group(1);
                    }
                }
//        }
                xml.append("<name name=\"cam\">," + testStr + ",0," + cell[1] + "</name>\n");
            }

            //npc  (存在多个动画号下的不同npc)
//			xml.append(castNpcs(cellStr3,i));//一行存在多个动画号（1 & 2）
            if (i < 1) {
                castNpcs(xml, cellStr3);
            } else {
                castDefaultNpcs(xml, cellStr3);
            }

            //other append
            String ttNumS = ttNum;
            if (!start0 && !ttNum.equals("")) {
                ttNumS = (Integer.parseInt(ttNum) - 1) + "";
            }

            xml.append("<name name=\"other\">" + ttNumS + "," + ttType + "," + cell[0] + "," + tts + "," + ttTime + "</name>\n");
            xml.append("</id>\n");
        }
        xmls = xml.toString();
        if (Reader.Log)
            System.out.println(xml);
    }


    private void castDefaultNpcs(StringBuilder npcStr, String npcs0) {
        String npcs[] = npcs0.split(";");

        for (String npc : npcs) {

            pat = Pattern.compile("[()]+");
            String[] npc0 = pat.split(npc.trim());

            if (npc0.length < 1)
                continue;

            String name = npc0[0].split(":")[0].trim();
            if ("".equals(name))
                continue;

            String startD = null;
            if (npc0.length > 1) {
                startD = npc0[1];
            } else {
                npcStr.append("<name name=\"" + name + "\">,,,</name>\n");
            }
            if (startD != null && !startD.trim().equals("")) {
                String[] npcLine = castNpcDOrK(npcStr, startD);
                if (npcLine != null)
                    castNpcLine(npcStr, npcLine, name, true);
            }

        }
    }

    private void castNpcs(StringBuilder npcStr, String npcs0) {
        String npcs[] = npcs0.split(";");

        for (String npc : npcs) {

            pat = Pattern.compile("[()]+");
            String[] npc0 = pat.split(npc.trim());

            if (npc0.length < 1)
                continue;

            String name = npc0[0].split(":")[0].trim();
            if ("".equals(name))
                continue;

            if (npc0[0].split(":").length < 2)
                continue;
            String startK = npc0[0].split(":")[1];
            if (startK != null && !startK.trim().equals("")) {
                String[] npcLine = castNpcDOrK(npcStr, startK);
                if (npcLine != null)
                    castNpcLine(npcStr, npcLine, name, false);
            }
        }
    }

    private String[] castNpcDOrK(StringBuilder npcStr, String start) {
        String[] npsLine = new String[4];
        npsLine[1] = "";
        npsLine[3] = "";


        String zw[] = start.trim().split(",");

        if (zw == null)
            return null;

        if (zw != null) {
            String z = zw[0];
            String[] strs1 = null;

            pat = Pattern.compile("[s\\[ \\]]+");

            if (zw.length > 1) {
                strs1 = pat.split(zw[1]);
            }

            String[] strs = pat.split(z);


            String strs01 = "";
            String strs11 = "";
            if (strs.length > 1) {
                strs01 = strs[1];
            }
            if (strs1 != null && strs1.length > 1) {
//					strs11 = strs1[1];
                pat = Pattern.compile("-(.*)=");
                Matcher m = pat.matcher(strs1[1]);
                while (m.find()) {
                    strs11 = m.group(1);
                }
                npsLine[1] = strs11;
                npsLine[3] = strs1[0];
            }

            if (strs != null || strs.length > 1) {
                pat = Pattern.compile("[^0-9]");
                Matcher matcher = pat.matcher(strs01);
                strs01 = matcher.replaceAll("");
                npsLine[0] = strs01;
                npsLine[2] = strs[0];
            }
        }
        return npsLine;
    }

    protected void castNpcLine(StringBuilder npcStr, String[] npcLine, String name, boolean type) {
        npcStr.append("<name name=\"" + name + "\">" + npcLine[0] + "," + npcLine[1] + "," + npcLine[2] + "," + npcLine[3] + "</name>\n");
    }


    public static String isContainChinese(String str) {
        str = str.replace("+", "");

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);

        while (m.find()) {
            str = str.replace(m.group(0), "");
        }
        return str;
    }


    public static void main(String[] args) {

//		Pattern pattern = Pattern.compile("[, |]+");
//		String[] strs = pattern.split("Java Hello World  Java,Hello,,World|Sun");
//		for (int i=0;i<strs.length;i++) {
//		    System.out.println(strs[i]);
//		} 

//		Pattern pattern = Pattern.compile("[()]+");
//		String[] strs = pattern.split("NPC2: ss 0s[idel],0s[1-1=0s]  (0s[idel],0s[1-1=0s])");
//		for (int i=0;i<strs.length;i++) {
//		    System.out.println(strs[i]);
//		}

//		String[] other = "0,P,4".split("," , 2);
//		for (String a : other) {
//			System.out.println(a);
//		}

////		String regEx = "(?=[\\s\\S]*?)([0-9]+\\.[0-9]*[1-9][0-9]*|[0-9]*[1-9][0-9]*\\.[0-9]+|[0-9]*[1-9][0-9]*)(?=[\\s\\S]*?)";
//		String regEx = "(?=[\\s\\S]*?)(\\[smiley=[0-9]+\\])(?=[\\s\\S]*?)";
////		String regEx ="^(([0-9]+\\.[0-9]*[1-9][0-9]*) ?([0-9]*[1-9][0-9]*\\.[0-9]+) ?([0-9]*[1-9][0-9]*))$";
//		String str = "adfasdf[smiley=0.4]kk [smiley=1.0]   mko[smiley=2],sdfaasdfa fd";
//		Pattern pat = Pattern.compile(regEx);
//		Matcher mat = pat.matcher(str);
//		while(mat.find()){
//			System.out.println(mat.group(1));
//		}

//	        String phoneString = "哈哈,13888889999P1";
//	        // 提取数字
//	        // 1
//	        Pattern pattern = Pattern.compile("[^0-9]");
//	        Matcher matcher = pattern.matcher(phoneString);
//	        String all = matcher.replaceAll("");
//	        System.out.println("phone:" + all);
//	        // 2
//	        Pattern.compile("[^0-9]").matcher(phoneString).replaceAll("");

//		String regEx = "(?=[\\s\\S]*?)([0-9]+\\.[0-9]*[1-9][0-9]*|[0-9]*[1-9][0-9]*\\.[0-9]+|[0-9]*[1-9][0-9]*)(?=[\\s\\S]*?)";
//		String str = "p1p2";
//		Pattern pat = Pattern.compile(regEx);
//		Matcher mat = pat.matcher(str);
//		while(mat.find()){
//			System.out.println(mat.group(1));
//		}

//		Pattern pattern = Pattern.compile("[\\[ \\]]+");
//		String[] strs = pattern.split(" [镜阿沙市超] Hi, everybody! I'm Anna. Here are my family photos. [镜头移动到老爷爷特写]");
//		for (int i=0;i<strs.length;i++) {
//		    System.out.println(strs[i]);
//		} 

//		Pattern pattern = Pattern.compile("[: M:]+");
//		String[] strs = pattern.split("...:saasd:123M:erwrM");
//		for (int i=0;i<strs.length;i++) {
//		    System.out.println(strs[i]);
//		} 

//		String[] pt ="Wowyou did a great job! Bravo!:scripts/kid_family/11-0-P-K-B-0.ogg:21:50M:".split("[: M:]");
//		
//		System.out.println(Arrays.asList(pt).toString());

//		String testStr = " [镜阿沙市超] Hi, everybody! I'm Anna. Here are my family photos. [镜头移动到老爷爷特写] ";
//		testStr.replace("Hi", "");
//		System.out.println(testStr+"\n"+testStr.replace("Hi", ""));
//		Pattern pattern = Pattern.compile("(\\[)([^<]+?)(\\])");
//		 Matcher matcher = pattern.matcher(testStr);

//		 while (matcher.find()) {
//		 System.out.println(matcher.group(2));
//		 }

//		String s="s: : ";
//		System.out.println(s.split(":").length);
//		for(String a : s.split(":")){
//			System.out.println(a);
//		}


    }


}
