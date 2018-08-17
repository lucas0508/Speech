/*
 * Author: Sam Zhu
 * Date: 11/08/2016
 * Description:
 *
 */

package com.example.administrator.speech.java;

import com.example.administrator.speech.java.Reader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyRow {
    private String sheet;
    private String serial;
    private String object = "";
    private String aniNum;
    private String keywords;
    private String input;
    private String hint;
    private String help;
    private String funcName;

    private String role = "";
    private String choice = "";
    private String defaultStr = "";

    MyRow(String sheet, String serialNum, String animation, String hint, String sentence, String help) {
        this.sheet = sheet;
        serial = serialNum;
        aniNum = animation;
        this.hint = hint;
        keywords = sentence;
        this.help = help;
        funcName = getFuncName(serial);

        //System.out.println("sn="+serial+":");
        setAll();
    }

    private String getFuncName(String sn) {
        if (sn.startsWith("FUNC_"))
            return sn;

        return null;
    }

    private String strip1(String inStr) {
        int cur = inStr.indexOf("<");
        if (cur != -1)
            inStr = inStr.substring(cur + 1).trim();

        cur = inStr.indexOf(">");
        if (cur != -1)
            inStr = inStr.substring(0, cur).trim();

        return inStr;
    }

    private void setFuncChoiceAni(String prevFuncName) {
        if (aniNum.equals("") || aniNum.equals("0"))
            aniNum = "0";
        else {
            String[] tokens = aniNum.split("&");

            aniNum = "";
            for (int i = 0; i < tokens.length; i++) {
                //aniNum += prevFuncName + "@" + tokens[i];
                aniNum += tokens[i];

                if (i < tokens.length - 1)
                    aniNum += ":";

                System.out.println("aniNum=" + aniNum + ", token=" + tokens[i]);
            }
        }

        //if (choice.contains("OUT-"))
        //return;

        String[] tokens = choice.split("/");
        choice = "";
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("DF"))
                choice += "DF" + prevFuncName + "@" + tokens[i].substring(2);
            else
                choice += prevFuncName + "@" + tokens[i];

            if (i != tokens.length - 1)
                choice += "/";
        }
    }

    private void setAll() {
        serial = strip1(serial);

        String roleStr = "";
        role = "?";

        //System.out.println("1. key=" + keywords);

        for (int i = 0; i < Reader.ROLES.length; i++) {
            for (int j = 0; j < Reader.ROLES_SUFFIXS.length; j++) {
                String tmp = Reader.ROLES[i] + Reader.ROLES_SUFFIXS[j];
                //if (keywords.toLowerCase().contains(tmp.toLowerCase())) {
                if (keywords.toLowerCase().trim().startsWith(tmp.toLowerCase())) {
                    roleStr = Reader.ROLES[i];

                    if (Reader.ROLES_SUFFIXS[j].charAt(0) == ' ')
                        roleStr += " ";

                    if (Reader.ROLES[i].equals("Alex"))
                        role = "H";
                    else if (Reader.ROLES[i].equals("Dr.X"))
                        role = "X";
                    else if (Reader.ROLES[i].equals("User"))
                        role = "A";
                    else if (Reader.ROLES[i].equals("NPC"))
                        role = "B";
                    else if (Reader.ROLES[i].startsWith("X_"))
                        role = "X_B";
		  /*
		  else if (Reader.ROLES[i].equals("Erica"))
			  role = "Erica";
		  else if (Reader.ROLES[i].equals("Toby"))
			  role = "Toby";
		  else if (Reader.ROLES[i].equals("Raco"))
			  role = "Raco";
		  */
                    else
                        role = roleStr;

                    break;
                }
            }
        }

        int pos = keywords.indexOf(roleStr);

        if (pos != -1) {
            keywords = keywords.substring(pos + roleStr.length() + 1).trim();
            //System.out.println("2. key=" + keywords);
            try {
                if (keywords.charAt(0) == '(' || keywords.charAt(0) == '（'
                        || keywords.charAt(1) == '(' || keywords.charAt(1) == '（') {
                    int pos2 = keywords.indexOf(")");

                    if (pos2 == -1) {
                        pos2 = keywords.indexOf("）");
                    }

                    pos = pos2 + 1;
                    keywords = keywords.substring(pos);
                    //System.out.println("3. key=" + keywords);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!sheet.contains("phonics")) {

                if (keywords.contains("[") && keywords.contains("]")) {
                    Pattern pattern = Pattern.compile("(\\[)([^<]+?)(\\])");
                    Matcher matcher = pattern.matcher(keywords);

                    while (matcher.find()) {
                        keywords = keywords.replace("[" + matcher.group(2) + "]", "");
                    }
                }
            }

        }

        // choice
        //
        String choiceMark = "--->";
        pos = keywords.indexOf(choiceMark);
        if (pos == -1) {
            choice = "";
        } else {
            if (pos != 0 && (keywords.charAt(pos - 1) == '(' || keywords.charAt(pos - 1) == '（')) {
                pos--;
                choice = keywords.substring(pos + choiceMark.length() + 1);
                choice = choice.substring(0, choice.length() - 1).trim();
            } else
                choice = keywords.substring(pos + choiceMark.length()).trim();

            keywords = keywords.substring(0, pos).trim();

            pos = choice.indexOf("(");
            if (pos == -1)
                pos = choice.indexOf("（");

            if (pos != -1) {
                String tmp = choice.substring(pos).trim();
                choice = choice.substring(0, pos).trim();

                if (choice.equals("TBD")) {
                    object = tmp.substring(1, tmp.length() - 1);
                    //System.out.println("object="+object);
                }
            }
        }

        // remove spaces in choice
        //
        if (choice.contains(" ")) {
            System.out.println("&&&&&&&b4, choice=" + choice);
            choice = choice.replaceAll(" ", "");
            System.out.println("&&&&&&&after, choice=" + choice);
        }

        String prevFuncName = Reader.getPrevFuncName();
        System.out.println("prevFuncName=" + prevFuncName + ", funcName=" + funcName);

        if (prevFuncName != null) {
            if (funcName == null) {
                serial = prevFuncName + "@" + serial;

                setFuncChoiceAni(prevFuncName);
            } else {
                if (!funcName.equals("FUNC_END")) {
                    Reader.setPrevFuncName(funcName);

                    setFuncChoiceAni(funcName);
                } else {
                    prevFuncName = null;
                }
            }
        } else {
            if (funcName != null && !funcName.equals("FUNC_END")) {
                Reader.setPrevFuncName(funcName);

                setFuncChoiceAni(funcName);
            }
        }

        System.out.println("--sn=" + serial);
        System.out.println("--choice=" + choice + " key=" + keywords);

        if (serial.contains("OUT-")) {
            choice = "";
        }

        // default
        //

        if (!role.equals("A")) {
            pos = keywords.indexOf("(");
            if (pos == -1)
                pos = keywords.indexOf("（");

            int pos1 = keywords.indexOf(")");
            if (pos1 == -1)
                pos1 = keywords.indexOf("）");

            if (pos != -1 && pos1 != -1) {
                defaultStr = keywords.substring(pos + 1, pos1).trim();

                //if (!defaultStr.contains("/")) {
                keywords = keywords.substring(0, pos).trim();

                String defMark = "Default:";
                pos = defaultStr.indexOf(defMark);
                if (pos == -1)
                    pos = defaultStr.indexOf("Default：");

                if (pos != -1)
                    defaultStr = defaultStr.substring(pos + defMark.length());
                //} else
                //defaultStr = "";
            } else
                defaultStr = "";
        }

        // replace some : with ：
        //
        for (int i = 0; i < Reader.ROLES.length; i++) {
            if (keywords.contains(Reader.ROLES[i] + ":"))
                keywords = keywords.replaceAll(Reader.ROLES[i] + ":", Reader.ROLES[i] + "：");
        }

        // remove line feed in an excel cell
        //
        int index = keywords.indexOf(10);
        while (index > 0) {
            keywords = keywords.substring(0, index) + keywords.substring(index + 1);
            index = keywords.indexOf(10);
        }

        //System.out.println("defaultStr=" + defaultStr + " key=" + keywords);

        if (defaultStr.equals(""))
            input = "0";
        else
            input = "1";
    }

    public String getSerial() {
        return serial;
    }

    public String getChoice() {
        return choice;
    }

    public String getRole() {
        return role;
    }

    public String getAni() {
        return aniNum;
    }

    public String getDefault() {
        return defaultStr;
    }

    public String getHint() {
        return hint;
    }

    public String getHelp() {
        return help;
    }

    public void setChoice(String c) {
        choice = c;
    }

    public void setHint(String ht) {
        hint = ht;
    }

    public void setHelp(String hp) {
        help = hp;
    }

    public String getLine() {
        String line = "";

        line += "S{" + serial + "} ";
        line += "R{" + role + "} ";
        line += "K{" + keywords + "} ";
        line += "C{" + choice + "} ";
        line += "O{0} T{" + object + "} ";
        line += "I{" + getInput() + "} ";
        line += "A{" + getAnimations() + "} ";
        line += "D{" + defaultStr + "} ";
        line += "HT{" + hint + "} ";
        line += "HP{" + help + "}";

//	System.out.println("key="+keywords);
//	System.out.println("choice="+choice);
//	System.out.println("default="+defaultStr);
//	System.out.println("line="+line);
        return line;
    }

    public void setAnimations(String aniStr) {
        aniNum = aniStr;
    }

    private String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    private String getAnimations() {
        if (aniNum.equals(""))
            return "0";

        return aniNum;
    }

    public static void main(String args[]) {
        String[] t1 = "t1:t2::".split(":", 4);
        String[] t2 = "t1:t2::".split(":");
        String[] t3 = "t1:t2".split(":", 4);

        System.out.println("num=" + t1.length);
        System.out.println("num=" + t2.length);
        System.out.println("num=" + t3.length);
    }
}
