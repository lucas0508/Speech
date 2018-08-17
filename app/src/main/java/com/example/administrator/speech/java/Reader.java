/*
 * Author: Sam Zhu
 * Date: 11/03/2016
 * Description: A xls reader
 *
 */
package com.example.administrator.speech.java;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reader {
    public static final String DIR_SCRIPTS = "./scripts/";
    public static final String DIR_DATA = "./data/";

    public static final String NO_MOTION = "na";
    public static final String[] ROLES = {"Professor Ingles", "Professor", "Nut", "Coco", "NPC", "B", "Alicia", "Dylan", "Erica", "Toby", "Raco", "Alex", "Dr.X",
            "User", "Clerk", "Attendant", "Attendanet", "Broadcasting", "Officer", "Office", "Ground", "Ground service",
            "Guard", "Man", "X_Guard", "Grandma", "Betty", "Anna", "Sound", "Teacher", "Grandpa", "Matt", "Piggy"};
    public static final String[] ROLES_SUFFIXS = {":", "：", " :", " ："};

    public static final String[] FILETYPE = {"xls", "xlsx"};

    private static String prevFuncName = null;

    private static HashMap<String, MyRow> rowHash = new HashMap<String, MyRow>();
    private static ArrayList<MyRow> rowList = new ArrayList<MyRow>();
    private static ArrayList<MyAnimationRow> aniList = new ArrayList<MyAnimationRow>();

    private static ReentrantLock sessionLock = new ReentrantLock();

    private static final int COL_TWO = 2;

    static boolean Log = true;

    private static int col_sn = 0;
    private static int col_ani = 1;
    private static int col_cam = 2;
    private static int col_ht = 2;
    private static int col_key = 3;
    private static int col_hp = 4;

    private static int col_tt = 6;// chartlet
    private static int col_jt = 7;// Lens
    private static int col_npc1 = 8;// npc

    protected static String catalogue;

    private static MyData mydata = null;

    private static boolean[] start0s;

    protected static void myPrint(String line) {
        System.out.print(line);
    }

    protected static void myPrintln(String line) {
        System.out.println(line);
    }

    protected static String getCellStr(Cell cell) {
        String result = "";

        if (cell != null) {
            try {
                result = cell.getStringCellValue();
            } catch (Exception e) {
                result = "" + (int) (cell.getNumericCellValue());
            }
        }

        return result.trim();
    }

    public static String getFilename(String file) {
        File files = new File(file);
        catalogue = file.substring(0, file.indexOf(files.getName()));

        return files.getName();
    }

    private static boolean FileTypeIsXls(String file) {
        String type = file.substring(file.lastIndexOf(".") + 1, file.length());

        return FILETYPE[0].equals(type);
    }

    public static Workbook getWorkbook(String file) {
        try {
            if (!file.contains("xls"))
                return null;

            InputStream fs = new FileInputStream(new File(file));

            if (FileTypeIsXls(file))
                return new HSSFWorkbook(fs);
            else
                return new XSSFWorkbook(fs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getPrevFuncName() {
        return prevFuncName;
    }

    public static void setPrevFuncName(String funcName) {
        prevFuncName = funcName;
    }

    private static void init() {
        myPrintln("!!!!!!!!!!Reader::init()");
        prevFuncName = null;

        rowHash.clear();
        rowList.clear();
        aniList.clear();
    }

    public static String xlsReader(String file, String mantype, String type) {
        init();

        String errLog = "";// 异常日志
        String retStr = "";

        try {
            Workbook wb = getWorkbook(file);
            file = getFilename(file);

            if (wb == null) {
                System.out.println("不是excel格式文件");
                return "不是excel格式文件";
            }

            int sheetNum = wb.getNumberOfSheets();

            if (Log)
                myPrintln("sheetNum=" + sheetNum);

            start0s = new boolean[sheetNum];

            for (int i = 0; i < sheetNum; i++) {
                Map<String, String> maps = new HashMap<String, String>();

                Sheet sheet = wb.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                System.out.println("sheetName=" + sheetName);

                if (!sheetName.contains("_")) {
                    retStr += "Excel文件的工作单名" + sheetName + "格式不对。应与文件名一致（不含扩展名），且包含\"_\"字符\n";
                    continue;
                }

                myPrintln("sheet=" + sheetName);

                int rowNum = sheet.getLastRowNum();
                myPrintln("lastRow=" + rowNum);

                rowList.clear();
                aniList.clear();
                MyAnimationRow.reset();

                if (getCellStr(sheet.getRow(1).getCell(COL_TWO)).equals("镜头切换")) {
                    col_cam = COL_TWO;
                    col_ht = col_cam + 1;
                    col_key = col_ht + 1;
                    col_hp = col_key + 1;
                } else {
                    col_cam = -1;
                    col_ht = COL_TWO;
                    col_key = col_ht + 1;
                    col_hp = col_key + 1;
                }

                if (type.startsWith("kid"))
                    mydata = new KidMyData();
                else
                    mydata = new MyData();

                start0s[i] = false;

                for (int j = 2; j <= rowNum; j++) {
                    Row row = sheet.getRow(j);

                    // 查row
                    if (row == null)
                        continue;

                    String serialNo = getCellStr(row.getCell(col_sn));
                    String keys = getCellStr(row.getCell(col_key));

                    if (!start0s[i])
                        start0s[i] = isStart0(getCellStr(row.getCell(col_tt)));

                    if (Log)
                        myPrintln("rowNum=" + j + ":" + serialNo + ":" + keys);

                    if (serialNo.equals("") || keys.equals(""))
                        continue;

                    MyRow mRow = new MyRow(sheetName, serialNo, getCellStr(row.getCell(col_ani)),
                            getCellStr(row.getCell(col_ht)), keys, getCellStr(row.getCell(col_hp)));

                    if (serialNo.equals("FUNC_END"))
                        continue;

                    for (int n = 0; n < rowList.size(); n++) {
                        if (mRow.getSerial().equals(rowList.get(n).getSerial())) {
                            errLog += sheetName + "---->" + mRow.getSerial();
                            System.out.println("!!!!!!!!! Error3: duplicate serialNo =" + mRow.getSerial());
                            retStr += "!!!!!!!!! Error3: duplicate serialNo =" + mRow.getSerial() + System.lineSeparator();
                        }
                    }

                    System.out.println("added " + mRow.getSerial() + ",choice=" + mRow.getChoice() + ",act=" + mRow.getAni());
                    rowList.add(mRow);
                    rowHash.put(mRow.getSerial(), mRow);

                    // System.out.println("added sn=" + mRow.getSerial());

                    if (!mRow.getRole().equals("A")) {
                        String motionStr = NO_MOTION;

                        if (col_cam != -1)
                            motionStr = getCellStr(row.getCell(col_cam));

                        MyAnimationRow aRow = new MyAnimationRow(j, mRow.getSerial(), mRow.getRole(), mRow.getAni(),
                                mRow.getDefault(), motionStr);

                        mRow.setAnimations(aRow.getAnimationStr());
                        aniList.add(aRow);
                    }

                    MyRow lastRow;

                    if (rowList.size() > 1) {
                        lastRow = rowList.get(rowList.size() - 2);

                        if (lastRow.getChoice().equals("") && !lastRow.getSerial().contains("OUT-"))
                            lastRow.setChoice(mRow.getSerial());

                        if (sheetName.equals("0-任务下达") && mRow.getRole().equals("A")) {
                            if (!mRow.getHint().equals("")) {
                                lastRow.setHint(mRow.getHint());
                                mRow.setHint("");
                            }

                            if (!mRow.getHelp().equals("")) {
                                lastRow.setHelp(mRow.getHelp());
                                mRow.setHelp("");
                            }
                        }
                    }
                }

                String base = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DIR_DATA + "base.txt")));

                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    base += line + System.lineSeparator();
                }
                br.close();

                String sheetUrl = catalogue + sheetName;
                System.out.println("!!!!!cata=" + catalogue + ",sheetName=" + sheetName);

                File file1 = new File(sheetUrl);

                if (!file1.exists())
                    file1.mkdir();

                String txtUrl = sheetUrl + "\\" + sheetName + ".txt";
                file1 = new File(txtUrl);

                //file1 = new File(sheetUrl, sheetName + ".txt");

                if (file1.exists())
                    file1.delete();

                // ****.intro .extra

                File fileIntro = new File(sheetUrl + "\\" + sheetName + ".intro");
                //File fileIntro = new File(sheetUrl, sheetName + ".intro");

                if (fileIntro.exists())
                    fileIntro.delete();

                File fileExtra = new File(sheetUrl + "\\" + sheetName + ".extra");
                //File fileExtra = new File(sheetUrl, sheetName + ".extra");
                if (fileExtra.exists())
                    fileExtra.delete();

                fileIntro.createNewFile();
                OutputStreamWriter outIntro = new OutputStreamWriter(new FileOutputStream(fileIntro), "utf-8");
                outIntro.write("#" + System.lineSeparator() + sheetName + ":" + sheetName + ":" + sheetName + ":" + sheetName + ":" + sheetName
                        + ":" + 0 + "");

                fileExtra.createNewFile();
                OutputStreamWriter outExtra = new OutputStreamWriter(new FileOutputStream(fileExtra), "utf-8");
                outExtra.write("#");

                outIntro.close();
                outExtra.close();

                sessionLock.lock();

                file1.createNewFile();
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file1), "utf-8");

                Pattern p = Pattern.compile("S\\{0-1\\}(.*?);");
                Matcher mat = p.matcher(base);
                String s01 = null;

                while (mat.find()) {
                    s01 = "S{0-1}" + mat.group(1);
                }

                for (int l = 0; l < rowList.size(); l++) {
                    // System.out.println(rowList.get(l).getLine());
                    String[] choices = rowList.get(l).getChoice().split("/");
                    for (int k = 0; k < choices.length; k++) {
                        MyRow mRow = rowHash.get(choices[k]);

                        System.out.println("=======setInput: choice=" + choices[k] + ",mRow=" + mRow);

                        if (mRow == null)
                            continue;

                        System.out.println("=======setInput: role=" + mRow.getRole());

                        if (mRow.getRole().equals("A")) {
                            rowList.get(l).setInput("1");
                            break;
                        } else {
                            rowList.get(l).setInput("0");
                        }
                    }

                    String xyn;
                    String sn = rowList.get(l).getSerial();

                    if (sn.startsWith("FUNC_") && !sn.contains("@"))
                        xyn = "XY{-1, -1} N{} ";
                    else
                        xyn = "XY{1000, 800} N{} ";

                    if (rowList.get(l).getLine().contains("S{0-1}")) {
                        if (s01 != null) {
                            base = base.replace(s01, rowList.get(l).getLine());
                            out.write(base);
                        } else {
                            out.write(xyn + (rowList.get(l).getLine() + ";") + System.lineSeparator());
                        }
                    } else {
                        if (l == 0)
                            out.write(base);

                        if (rowList.get(l).getSerial().startsWith("FUNC_")
                                && rowList.get(l).getSerial().indexOf("@") == -1)
                            out.write((System.lineSeparator() + xyn + rowList.get(l).getLine() + ";") + System.lineSeparator());
                        else
                            out.write(xyn + (rowList.get(l).getLine() + ";") + System.lineSeparator());
                    }
                }
                out.close();

                sessionLock.unlock();

                // if(client != null){
                // sessionLock.lock();
                // try {
                // client.sendUrl(txtUrl);
                //
                // } catch (Exception e) {
                // System.out.println("消息发送失败！！！");
                // e.printStackTrace();
                // }
                // sessionLock.unlock();
                // }
                //

                // generate XML & write to xls if needed
                //
                String xmlContent = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<Root>\n";
                StringBuffer xmlContentJT = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<Root>\n");

                for (int m = 0; m < aniList.size(); m++) {
                    MyAnimationRow aRow = aniList.get(m);
                    xmlContent += aRow.getXMLLine(mantype);
                    xmlContentJT.append(aRow.getXMLLineJT(mantype));

                    // modify row->cell value
                    //
                    Row row = sheet.getRow(aRow.getRowNum());
                    Cell cell1 = row.getCell(col_ani);
                    String valStr1 = getCellStr(cell1);

                    // System.out.println("???: " + valStr1 + ", cell1=" +
                    // cell1);

                    if (valStr1.equals("") || valStr1 == null) {
                        if (cell1 == null) {
                            cell1 = row.createCell(col_ani);
                            cell1.setCellValue(aRow.getXmlAniStr());

                            if (Log)
                                System.out.println("check: " + getCellStr(cell1));
                        } else {
                            cell1.setCellValue(aRow.getXmlAniStr());

                            if (Log)
                                System.out.println("check111: " + getCellStr(cell1));
                        }
                    }
                }

                // generate xml file
                //
                xmlContent += "</Root>\n";
                xmlContentJT.append("</Root>\n");

                // File file2 = new File(catalogue+sheetName+".xml");
                // if (file2.exists())
                // file2.delete();
                //
                // file2.createNewFile();
                // FileOutputStream out2 = new FileOutputStream(file2);
                // out2.write(xmlContent.getBytes("utf-8"));
                // out2.close();
                //
                // file2 = new File(catalogue+sheetName+"Jt.xml");
                // if (file2.exists())
                // file2.delete();
                //
                // file2.createNewFile();
                // out2 = new FileOutputStream(file2);
                // out2.write(xmlContentJT.toString().getBytes("utf-8"));
                // out2.close();
            }

            // save the changes for xls file
            //
            OutputStream xlsFile = new FileOutputStream(new File(catalogue + "new_" + file));
            wb.write(xlsFile);
            xlsFile.close();

            castFindCat(catalogue + "new_" + file, mantype);// 生成xml

            // errLog
            if (!errLog.equals("")) {
                File file1 = new File(catalogue + "log.txt");

                if (file1.exists())
                    file1.delete();

                file1.createNewFile();

                FileOutputStream out2 = new FileOutputStream(file1);
                out2.write(errLog.toString().getBytes("utf-8"));
                out2.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // if(client != null)
            // client.closeClient();
        }

        return retStr;
    }

    private static void castFindCat(String file, String mantype) {
        try {
            Workbook wb = getWorkbook(file);

            if (wb == null) {
                System.out.println("不是excel格式文件");
                return;
            }

            int sheetNum = wb.getNumberOfSheets();

            if (!Log)
                System.out.println(sheetNum);

            for (int i = 0; i < sheetNum; i++) {
                Sheet sheet = wb.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                if (!sheetName.contains("-"))
                    continue;

                myPrintln("sheet=" + sheetName);

                if (sheetName.startsWith("5")) {
                    System.out.println(sheetName);
                }

                int rowNum = sheet.getLastRowNum();
                myPrintln("lastRow=" + rowNum);

                rowList.clear();
                aniList.clear();
                MyAnimationRow.reset();

                sessionLock.lock();

                StringBuffer datas = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<objects>\n");
                for (int j = 2; j <= rowNum; j++) {
                    Row row = sheet.getRow(j);
                    // 查row
                    if (row == null)
                        continue;

                    // start --build unit xml
                    if (Log)
                        System.out.println(row.getCell(col_npc1));

                    String cellStr = getCellStr(row.getCell(col_ani));

                    if ("".equals(cellStr)) {
                        continue;
                    }

                    mydata.SetAll(cellStr, getCellStr(row.getCell(col_jt)), getCellStr(row.getCell(col_npc1)),
                            getCellStr(row.getCell(col_tt)), start0s[i]);

                    if (mydata.getXmls() != null)
                        datas.append(mydata.getXmls());

                    mydata.clear();

                    // stop --build unit xml
                }
                // start --build unit xml
                datas.append("</objects>");
                File file0 = new File(catalogue + sheetName + "_data.xml");
                if (file0.exists())
                    file0.delete();
                file0.createNewFile();
                FileOutputStream out0 = new FileOutputStream(file0);
                out0.write(datas.toString().getBytes("utf-8"));
                out0.close();
                sessionLock.unlock();
                // stop --build unit xml
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static boolean isStart0(String cell) {
        return cell.contains("P0");
    }

    public static void main(String args[]) {
        // xlsReader("C:\\Users\\CDI\\Desktop\\test1.xls");
        // xlsReader("0118-Findthecat(Betty).xls", "someone", "kid");
        xlsReader("world_magiland_hospital_1.xlsx", "someone", "world");
        System.out.println("Done!!!");
    }
}
