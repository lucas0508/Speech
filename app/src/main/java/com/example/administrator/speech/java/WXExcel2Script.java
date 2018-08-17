/*
 * Author: Sam Zhu
 * Date: 07/16/2018
 * Description:
 *   An excel to txt script converter for new formatted xls file for WX
 */

package com.example.administrator.speech.java;

import com.example.administrator.speech.java.MyLine;
import com.example.administrator.speech.java.Reader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

public class WXExcel2Script extends Reader {
    public static final int ROW_NODE_NUM = 2;
    public static final int NODE_W = 152;
    public static final int NODE_H = (int) (152 * .68);
    public static final int STARTING_X = 192 + NODE_W;
    public static final int STARTING_Y = 60;

    public static int NODE_CNT = 0;

    private static final String STAR_SUFFIX = "-*";

    private static final int COL_SN = 0;                                  // 场次
    private static int COL_ANIMATION = COL_SN + 4;                        // 角色动画
    private static int COL_DIALOGUE = COL_ANIMATION + 1;                  // 对话
    private static int COL_INPUT = COL_DIALOGUE + 2;                      // 监听句之前
    private static int COL_KEY = COL_INPUT + 1;                           // 监听句
    private static int COL_PREV_SN = COL_KEY + 1;                         // 提示内容
    private static int COL_NOTION = COL_PREV_SN + 1;                      // 事件描述
    private static int COL_NEXT_SN_FOR_RIGHT = COL_NOTION + 1;            // 答对跳转场次
    private static int COL_NEXT_SN_FOR_WRONG = COL_NEXT_SN_FOR_RIGHT + 1; // 答错跳转场次
    private static int COL_NEXT_SN = COL_NEXT_SN_FOR_WRONG + 1;           // 连接场次

    private static LinkedHashMap<String, MyLine> lineList = new LinkedHashMap<String, MyLine>();

    public static String generateTxtFromExcel(String dir, String xlsFile) {
        String status = "";

        lineList.clear();

        boolean isFirstSN = false;
        int index = xlsFile.lastIndexOf(".");

        String fileMainName = xlsFile.substring(0, index);

        try {
            Workbook wb = getWorkbook(dir + xlsFile);

            if (wb == null) {
                System.out.println("不是excel格式文件");
                return "不是excel格式文件";
            }

            int sheetNum = wb.getNumberOfSheets();

            if (Log)
                myPrintln("sheetNum=" + sheetNum);

            for (int i = 0; i < sheetNum; i++) {
                Sheet sheet = wb.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                myPrintln("sheetName=" + sheetName);

                int rowNum = sheet.getLastRowNum();
                myPrintln("lastRow=" + rowNum);

                NODE_CNT = 0;

                for (int j = 0; j <= rowNum; j++) {
                    String sn = "";          // 场次
                    String animation = "";   // 角色动画
                    String dialogue = "";    // 对话
                    String input = "";       // 监听句之前
                    String keywords = "";    // 监听句
                    String prevSN = "";      // 提示内容
                    String notion = "";      // 事件描述
                    String nextSNRight = ""; // 答对跳转场次
                    String nextSNWrong = ""; // 答错跳转场次
                    String nextSN = "";
                    ;     // 连接场次

                    Row row = sheet.getRow(j);

                    sn = getCellStr(row.getCell(COL_SN));

                    if (sn.equals(""))
                        continue;

                    if (sn.equals("场次")) {
                        int lastCellNum = row.getLastCellNum();
                        myPrintln("lastCellNum=" + lastCellNum);

                        for (int k = 1; k < lastCellNum; k++) {
                            String val = getCellStr(row.getCell(k));

                            if (val.equals("角色动画"))
                                COL_ANIMATION = k;
                            else if (val.equals("对话"))
                                COL_DIALOGUE = k;
                            else if (val.equals("监听句之前"))
                                COL_INPUT = k;
                            else if (val.equals("监听句"))
                                COL_KEY = k;
                            else if (val.equals("5秒无操作自动提示和帮助内容"))
                                COL_PREV_SN = k;
                            else if (val.equals("事件描述"))
                                COL_NOTION = k;
                            else if (val.equals("答对跳转场次"))
                                COL_NEXT_SN_FOR_RIGHT = k;
                            else if (val.equals("答错跳转场次"))
                                COL_NEXT_SN_FOR_WRONG = k;
                            else if (val.equals("连接场次"))
                                COL_NEXT_SN = k;
                        }

                        isFirstSN = true;

                        continue;
                    }

                    if (isFirstSN) {
                        // XY{256.0,147.0} N{} S{START} R{B} K{} C{2-0} O{0} T{} I{0} A{ACTSTART} D{} HT{} HP{};
                        MyLine starLine = new MyLine("START", "B", getEditorSN(sn), "", "", "0", "0", "", "", "", "");
                        starLine.setNotion("");
                        lineList.put("START", starLine);
                        isFirstSN = false;
                    }

                    animation = replaceExcelLineBreak(getCellStr(row.getCell(COL_ANIMATION)));
                    dialogue = replaceExcelLineBreak(replaceDialogueComma(getCellStr(row.getCell(COL_DIALOGUE))));

                    input = getCellStr(row.getCell(COL_INPUT));
                    keywords = getCellStr(row.getCell(COL_KEY));
                    prevSN = getCellStr(row.getCell(COL_PREV_SN));
                    notion = getCellStr(row.getCell(COL_NOTION));
                    nextSNRight = getCellStr(row.getCell(COL_NEXT_SN_FOR_RIGHT));
                    nextSNWrong = getCellStr(row.getCell(COL_NEXT_SN_FOR_WRONG));
                    nextSN = getCellStr(row.getCell(COL_NEXT_SN));

                    myPrintln("#" + j + ",sn=" + sn + ",ani=" + animation + ",dialogue=" + dialogue + ",key=" + keywords + ",prevSN=" + prevSN + ",notion=" + notion + ",rightSN=" + nextSNRight + ",wrongSN=" + nextSNWrong + ",nextSN=" + nextSN);

                    String role = "B";
                    String sentence = dialogue;

                    if (!input.equals("")) {
                        input = "1";
                    } else {
                        input = "0";
                    }

                    String choices = "";
                    String serialNo = getEditorSN(sn);

                    MyLine line = null;

                    if (!nextSN.equals("")) {
                        String[] tokens = nextSN.split("/");

                        for (int ii = 0; ii < tokens.length; ii++) {
                            choices += getEditorSN(tokens[ii]);

                            if (ii != tokens.length - 1)
                                choices += "/";
                        }

                        line = new MyLine(serialNo, role, choices, sentence, "", "0", input, animation, "", "", "");
                        line.setNotion(notion);
                        lineList.put(serialNo, line);
                    } else if (!keywords.equals("")) {
                        role = "A";
                        sentence = keywords;
                        choices = getEditorSN(nextSNRight);

                        MyLine setChoiceLine = lineList.get(getEditorSN(prevSN));

                        String oldChoices = setChoiceLine.getChoices();
                        myPrintln("oldChoices=" + oldChoices);

                        if (!nextSNWrong.equals("")) {
                            line = new MyLine(serialNo, role, choices, sentence, "", "0", input, animation, "", "", "");
                            line.setNotion("");
                            lineList.put(serialNo, line);

                            if (oldChoices.contains(STAR_SUFFIX) && (oldChoices.contains(serialNo + "/") || oldChoices.contains("/" + serialNo))) {
                                String[] tokens = oldChoices.split("/");
                                String starSN = "";

                                for (int ii = tokens.length - 1; ii >= 0; ii--) {
                                    if (tokens[ii].endsWith(STAR_SUFFIX)) {
                                        starSN = tokens[ii];
                                        break;
                                    }
                                }

                                // always put * at the end
                                //
                                MyLine starLine = lineList.get(starSN);
                                lineList.remove(starSN);
                                lineList.put(starSN, starLine);

                                continue;
                            }

                            String starSN = sn + STAR_SUFFIX;

                            if (oldChoices.equals(""))
                                setChoiceLine.setChoices(getEditorSN(sn) + "/" + starSN);
                            else {
                                if (oldChoices.contains(STAR_SUFFIX)) {
                                    String[] tokens = oldChoices.split("/");

                                    String starNo = "";
                                    String newChoices = "";
                                    for (int ii = 0; ii < tokens.length; ii++) {
                                        if (!tokens[ii].endsWith(STAR_SUFFIX))
                                            newChoices += tokens[ii] + "/";
                                        else
                                            starNo = tokens[ii];
                                    }
                                    myPrintln("oldC=" + oldChoices + ",newC=" + newChoices + ",starNo=" + starNo);

                                    // remove the old *
                                    //
                                    lineList.remove(starNo);

                                    oldChoices = newChoices + serialNo;
                                }

                                setChoiceLine.setChoices(oldChoices + "/" + starSN);
                            }

                            // TODO: get choice from 答错跳转场次, nextSNWrong (第一遍答错播放长梗 6 第二遍短梗 8 第三遍重复短梗，并弹出“跳过”按钮。)
                            //
                            int index1 = nextSNWrong.indexOf("长梗");
                            int index2 = nextSNWrong.indexOf("第二遍");

                            int firstSN = Integer.parseInt(nextSNWrong.substring(index1 + 2, index2).trim());

                            index1 = nextSNWrong.indexOf("短梗");
                            index2 = nextSNWrong.indexOf("第三遍");

                            int secondSN = Integer.parseInt(nextSNWrong.substring(index1 + 2, index2).trim());

                            myPrintln(firstSN + "," + secondSN);

                            String wrongChoices = getEditorSN("" + firstSN) + "/" + getEditorSN("" + secondSN);

                            MyLine starLine = new MyLine(starSN, role, wrongChoices, "*", "", "0", "0", "", "", "", "");
                            starLine.setNotion("");
                            lineList.put(starSN, starLine);
                        } else {
                            myPrintln("oldChoices2=" + oldChoices + ",prevSN" + prevSN);

                            if (oldChoices.contains(serialNo + "/") || oldChoices.contains("/" + serialNo)) {
                                line = new MyLine(serialNo, role, choices, sentence, "", "0", input, animation, "", "", "");
                                line.setNotion("");
                                lineList.put(serialNo, line);

                                continue;
                            }

                            String[] tokens = oldChoices.split("/");
                            String newChoices = "";

                            String starSN = ""; // assume * choice is always the last one

                            for (int ii = 0; ii < tokens.length; ii++) {
                                if (ii != tokens.length - 1) {
                                    newChoices += tokens[ii] + "/";
                                } else {
                                    starSN = tokens[ii];

                                    newChoices += serialNo + "/" + starSN;

                                    line = new MyLine(serialNo, role, choices, sentence, "", "0", input, animation, "", "", "");
                                    line.setNotion("");
                                    lineList.put(serialNo, line);

                                    MyLine starLine = lineList.get(starSN);
                                    lineList.remove(starSN);
                                    lineList.put(starSN, starLine);
                                }
                            }
                            setChoiceLine.setChoices(newChoices);
                        }
                    } else {
                        if (input.equals("1"))
                            choices = "";
                        else
                            choices = getEditorSN("" + (Integer.parseInt(sn) + 1));

                        if (!prevSN.equals("")) {
                            myPrintln("prevSN=" + prevSN);
                        }

                        line = new MyLine(serialNo, role, choices, sentence, "", "0", input, animation, "", "", "");
                        line.setNotion(notion);
                        lineList.put(serialNo, line);
                    }
                }

                String txtFile = dir + fileMainName + ".txt";

                FileOutputStream out = new FileOutputStream(txtFile);

                String base = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DIR_DATA + "base.txt")));

                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    base += line + System.lineSeparator();
                }
                br.close();
                base += "# WX_SCRIPT=true" + System.lineSeparator();

                out.write(base.getBytes("utf-8"));

                String lastKey = null;
                for (String key : lineList.keySet()) {
                    lastKey = key;
                }

                for (String key : lineList.keySet()) {
                    MyLine line = lineList.get(key);

                    if (key.equals(lastKey))
                        lineList.get(key).setChoices("END");

                    out.write((line.getLine() + ";" + System.lineSeparator()).getBytes("utf-8"));
                }

                MyLine endLine = new MyLine("END", "B", "0", "", "", "0", "0", "", "", "", "");
                endLine.setNotion("");
                out.write((endLine.getLine() + ";" + System.lineSeparator()).getBytes("utf-8"));

                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

            StackTraceElement[] traces = e.getStackTrace();

            for (int i = 0; i < traces.length; i++) {
                status += traces[i].toString() + System.lineSeparator();
            }
        }

        return status;
    }

    protected static String getEditorSN(String sn) {
        return sn + "-0";
    }

    protected static String replaceExcelLineBreak(String input) {
        int index = input.indexOf("\n");

        while (index != -1) {
            input = input.substring(0, index) + "$$" + input.substring(index + 1);

            index = input.indexOf("\n");
        }

        return input;
    }

    protected static String replaceDialogueComma(String input) {
        int index = input.indexOf(":");

        while (index != -1) {
            input = input.replace(":", "：");

            index = input.indexOf(":");
        }

        return input;
    }

    public static void main(String args[]) {
        //generateTxtFromExcel("ConvenientStoreCheckout.xlsx");
        generateTxtFromExcel("./", "TTT.xlsx");
        System.out.println("Done!!!");
    }
}
