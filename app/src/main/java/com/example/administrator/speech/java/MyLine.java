package com.example.administrator.speech.java;

import java.util.ArrayList;

public class MyLine {
    private float x;
    private float y;
    private String notion;
    private String sn;
    private String role;
    private String choices;
    private ArrayList<String> keywordsList = new ArrayList<String>();
    private ArrayList<String> defaultList = new ArrayList<String>();
    private String flag;
    private String object;
    private String input;
    private String action;
    //private String mDefault;
    private String hint;
    private String help;

    private boolean isLine = true;

    MyLine() {
    }

    MyLine(String iSn, String iRole, String iChoices, String iKeywords,
           String iFlag, String iObject, String iInput, String iAction,
           String iDefault, String iHint, String iHelp) {
        sn = iSn;
        role = iRole;
        choices = iChoices;

        String[] roleKeywordList = iKeywords.split(":");

        for (int i = 0; i < roleKeywordList.length; i++) {
            //System.out.println(roleKeywordList[i]);

            keywordsList.add(roleKeywordList[i]);
        }

        int pos = iDefault.indexOf(":");

        if (pos == -1 || iDefault.charAt(pos - 1) == 'F')
            defaultList.add(iDefault);
        else {
            String[] roleDefaultList = iDefault.split(":");

            for (int i = 0; i < roleDefaultList.length; i++) {
                //System.out.println(roleDefaultList[i]);

                defaultList.add(roleDefaultList[i]);
            }
        }

        flag = iFlag;
        object = iObject;
        input = iInput;
        action = iAction;
        //mDefault = iDefault;
        hint = iHint;
        help = iHelp;

        if (!role.equals("A") && x == 0 && y == 0) {
            WXExcel2Script.NODE_CNT++;

            x = WXExcel2Script.STARTING_X + ((WXExcel2Script.NODE_CNT - 1) % WXExcel2Script.ROW_NODE_NUM) * WXExcel2Script.NODE_W * 2;
            y = WXExcel2Script.STARTING_Y + ((WXExcel2Script.NODE_CNT - 1) / WXExcel2Script.ROW_NODE_NUM) * WXExcel2Script.NODE_H * (float) (1.5);
        }
    }

    MyLine(String line) {
        String[] tokens = line.split("} ");

        if (tokens.length < 11) {
            isLine = false;
            return;
        }

        for (int i = 0; i < tokens.length; i++) {
            int index = tokens[i].indexOf("{");

            tokens[i] = tokens[i].substring(index + 1);

            if (i == tokens.length - 1)
                tokens[i] = tokens[i].substring(0, tokens[i].length() - 2);
        }

        int start = -1;
        if (tokens.length == 11)
            start = 0;
        else if (tokens.length == 12)
            start = 1;
        else if (tokens.length == 13)
            start = 2;
        else {
            isLine = false;
            return;
        }

        sn = tokens[start];

        if (isSNReserved(sn)) {
            isLine = false;
            return;
        }

        sn = ajustStrForFunc(sn);

        notion = tokens[start - 1];
        role = tokens[start + 1];
        keywordsList = getList(keywordsList, tokens[start + 2]);
        choices = tokens[start + 3];
        flag = tokens[start + 4];
        object = tokens[start + 5];
        input = tokens[start + 6];
        action = ajustStrForFunc(tokens[start + 7]);
        defaultList = getList(defaultList, tokens[start + 8]);
        hint = tokens[start + 9];
        help = tokens[start + 10];
    }

    public boolean isLine() {
        return isLine;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setNotion(String notion) {
        this.notion = notion;
    }

    public void setChoices(String choices) {
        this.choices = choices;
    }

    private String ajustStrForFunc(String str) {
        // if it is a FUNC_ related string
        //
        //if (str.contains("@"))
        //return str.split("@")[1];

        return str;
    }

    private String ajustStringsForFunc(String strs, boolean isChoice) {
        // if a "/" seperated FUNC_ string
        //
        String retStr = "";
        boolean isDF = false;

        if (strs.startsWith("DF")) {
            isDF = true;
            strs = strs.substring(2);
        }

        if (isChoice && strs.contains(":")) {
            String[] funcTokens = strs.split(":");

            if (funcTokens.length == 3)
                retStr = funcTokens[0] + ":" + funcTokens[1];
        } else {
            String[] tokens = strs.split("/");

            for (int i = 0; i < tokens.length; i++) {
                retStr += ajustStrForFunc(tokens[i]);

                if (i != tokens.length - 1)
                    retStr += "/";
            }
        }

        if (isDF)
            retStr = "DF" + retStr;

        return retStr;
    }

    private boolean isSNReserved(String sn) {
        if (sn.equals("0") || sn.equals("0-0") || sn.equals("0-1") || sn.equals("0-1-0") || sn.equals("0-1-1")
                || sn.equals("TO") || sn.equals("TO-0"))
            return true;

        return false;
    }

    private ArrayList<String> getList(ArrayList<String> theList, String str) {
        String[] tokens = str.split("/");

        for (int i = 0; i < tokens.length; i++) {
            theList.add(tokens[i]);
        }

        return theList;
    }

    private String getListStr(ArrayList<String> theList) {
        String retStr = "";

        for (int i = 0; i < theList.size(); i++) {
            retStr += theList.get(i);

            if (i != theList.size() - 1)
                retStr += "/";
        }

        return retStr;
    }

    public String getExcelStr() {
        String roleStr = role;

        if (role.equals("B"))
            roleStr = "NPC";
        else if (role.equals("A"))
            roleStr = "User";

        //System.out.println("role="+role);

        String dStr = getListStr(defaultList);
        String retStr;

        if (dStr.trim().equals(""))
            retStr = roleStr + ":" + ajustStringsForFunc(getListStr(keywordsList), false) + "--->" + ajustStringsForFunc(choices, true);
        else
            retStr = roleStr + ":" + ajustStringsForFunc(getListStr(keywordsList), false) + "(" + getListStr(defaultList) + ")--->" + ajustStringsForFunc(choices, false);

        return retStr;
    }

    public String toString(int role) {
        if (input.equals("1"))
            return sn + "->" + role + "->" + choices + "->" + keywordsList.get(role)
                    + "->" + flag + "->" + object + "->" + input + "->" + action + "->" + defaultList.get(role)
                    + "->" + hint + "->" + help;
        else
            return sn + "->" + role + "->" + choices + "->" + keywordsList.get(role)
                    + "->" + flag + "->" + object + "->" + input + "->" + action + "->"
                    + "->" + hint + "->" + help;
    }

    public String getLine() {
        String line = "";

        line += "XY{" + x + "," + y + "} ";
        line += "N{" + notion + "} ";
        line += "S{" + sn + "} ";
        line += "R{" + role + "} ";
        line += "K{" + getListStr(keywordsList) + "} ";
        line += "C{" + choices + "} ";
        line += "O{0} T{" + object + "} ";
        line += "I{" + input + "} ";
        line += "A{" + action + "} ";
        line += "D{" + getListStr(defaultList) + "} ";
        line += "HT{" + hint + "} ";
        line += "HP{" + help + "}";

//	System.out.println("key="+keywords);
//	System.out.println("choice="+choice);
//	System.out.println("default="+defaultStr);
//	System.out.println("line="+line);
        return line;
    }

    public String getSN() {
        return sn;
    }

    public void setSN(String sn) {
        this.sn = sn;
    }

    public String getNotion() {
        return notion;
    }

    public String getRole() {
        return role;
    }

    public String getChoices() {
        return choices;
    }

    public String getRoleSuffix(int role) {
        if (role != 0 && this.role.startsWith("X"))
            return "" + role;

        return "";
    }

    // index: = 0, normal; = 1, normal default; = 2, default of "F:"
    //
    public String getSingleAct(int roleNum, int index) {
        String[] scences = action.split(":");
        String result = scences[index];

        if (role.equals("X")) {
            if (roleNum > 0)
                result += "-" + roleNum;
        }

        //System.out.println("MyLine: role="+role + ", roleNum="+roleNum + ",single act="+result);

        return result;
    }

    public int getKeyRoleNum() {
        return keywordsList.size();
    }

    public String getKeywords(int role) {
        String keywords;

        if (this.role.startsWith("X")) {
            if (role >= keywordsList.size())
                keywords = keywordsList.get(0);
            else
                keywords = keywordsList.get(role);
        } else
            keywords = keywordsList.get(0);

        //System.out.println("SN="+sn+",Role="+this.role+",key="+keywords);

        return keywords;
    }

    public String getFlag() {
        return flag;
    }

    public String getObject() {
        return object;
    }

    public String getInput() {
        return input;
    }

    public String getAction() {
        return action;
    }

    public String getDefault(int role) {
        //System.out.println("SN="+sn+",Role="+this.role);

        String defaultStr;

        if (this.role.startsWith("X")) {
            if (role >= defaultList.size())
                defaultStr = defaultList.get(0);
            else
                defaultStr = defaultList.get(role);
        } else
            defaultStr = defaultList.get(0);

        return defaultStr;
    }

    public String getHint() {
        return hint;
    }

    public String getHelp() {
        return help;
    }

    public void resetChoice() {
        choices = "0";
    }
}
