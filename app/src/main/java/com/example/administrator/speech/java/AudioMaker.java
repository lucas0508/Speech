package com.example.administrator.speech.java;

import com.example.administrator.speech.gen.DBuserinfo;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AudioMaker {
    private final String theme;

    private static List<MyLine> lineList = new ArrayList<MyLine>();
    private String scriptName;

    private String[] keys1 = {"K"};
    private String[] keys2 = {"K", "D"};

    private int cnt = 0;
    private int cnt2 = 0;

   // private MyFlyTts myTts;
    private Object lock = new Object();

    private static String catalogue;
    private boolean forCourse, cAudioText;
    private int create;
    private List<DBuserinfo> users = new ArrayList<>();
    private String name;

    public AudioMaker(String name, boolean forCourse, int create, boolean cAudioText) {
        scriptName = name;
        this.forCourse = forCourse;
        this.create = create;
        this.cAudioText = cAudioText;
        getFilename(name);

        theme = catalogue + getFilename(name).split(".txt")[0];


        File file = new File(catalogue);

        try {
            System.out.println("making " + catalogue);

            if (!file.exists()) {
                System.out.println("made dir=" + catalogue);
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        File[] fileList = file.listFiles();
//        for (File f : fileList) {
//            if (f.getName().endsWith(".wav") || f.getName().endsWith(".mp3") || f.getName().endsWith(".ogg"))
//                f.delete();
//        }
        lineList.clear();

      //  myTts = new MyFlyTts(lock);

        if (!forCourse)
            init();
    }

    public static String getFilename(String file) {
        File files = new File(file);

        System.out.println("file=" + file);
        //catalogue = file.substring(0,file.indexOf(files.getName()));

        catalogue = file.substring(0, file.indexOf(".txt")) + "\\";

        return files.getName();
    }

    private boolean init() {
        String pattern_state1 = "S{";
        String pattern_state2 = "} R";

        String pattern_role1 = "R{";
        String pattern_role2 = "} K";

        String pattern_keywords1 = "K{";
        String pattern_keywords2 = "} C";

        String pattern_choices1 = "C{";
        String pattern_choices2 = "} O";

        String pattern_obtain_flag1 = "O{";
        String pattern_obtain_flag2 = "} T";

        String pattern_obtain_object1 = "T{";
        String pattern_obtain_object2 = "} I";

        String pattern_input1 = "I{";
        String pattern_input2 = "} A";

        String pattern_action1 = "A{";
        String pattern_action2 = "} D";

        String pattern_default1 = "D{";
        String pattern_default2 = "} HT";

        String pattern_hint1 = "HT{";
        String pattern_hint2 = "} HP";

        String pattern_help1 = "HP{";
        String pattern_help2 = "};";

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(scriptName), "UTF-8"));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
               // System.out.println("Read script line=" + line);

                if (line.indexOf(pattern_state1) != -1) {
                    String temp1 = line.substring(line.indexOf(pattern_state1) + pattern_state1.length(), line.indexOf(pattern_state2));
                    String temp2 = line.substring(line.indexOf(pattern_role1) + pattern_role1.length(), line.indexOf(pattern_role2));
                    String temp3 = line.substring(line.indexOf(pattern_choices1) + pattern_choices1.length(), line.indexOf(pattern_choices2));
                    String temp4 = line.substring(line.indexOf(pattern_keywords1) + pattern_keywords1.length(), line.indexOf(pattern_keywords2));
                    String temp5 = line.substring(line.indexOf(pattern_obtain_flag1) + pattern_obtain_flag1.length(), line.indexOf(pattern_obtain_flag2));
                    String temp6 = line.substring(line.indexOf(pattern_obtain_object1) + pattern_obtain_object1.length(), line.indexOf(pattern_obtain_object2));
                    String temp7 = line.substring(line.indexOf(pattern_input1) + pattern_input1.length(), line.indexOf(pattern_input2));
                    String temp8 = line.substring(line.indexOf(pattern_action1) + pattern_action1.length(), line.indexOf(pattern_action2));
                    String temp9 = line.substring(line.indexOf(pattern_default1) + pattern_default1.length(), line.indexOf(pattern_default2));
                    String temp10 = line.substring(line.indexOf(pattern_hint1) + pattern_hint1.length(), line.indexOf(pattern_hint2));
                    String temp11 = line.substring(line.indexOf(pattern_help1) + pattern_help1.length(), line.indexOf(pattern_help2));

                    MyLine newLine = new MyLine(temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp8, temp9, temp10, temp11);
//                    System.out.println(newLine.toString());
                    lineList.add(newLine);
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("MyScript::init(): " + e);
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int getCnt() {
        return cnt;
    }

    public int getCnt2() {
        return cnt2;
    }

    private void playnRecord(String text, String targetWave, String voice) {
//        myTts.setVoice(voice);
//        myTts.setTarget(catalogue + "/" + targetWave);
//
//        myTts.startSpeaking(text);


        users.add(new DBuserinfo(null,0,targetWave, text));

       // if (cAudioText) createExcle(theme, users);
//        synchronized (lock) {
//            try {
//                System.out.println("Wait for TTS and convert");
//                lock.wait();
//                System.out.println("Done TTS and convert");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
       // wavCastMp3OrOgg.change(targetWave, create);


        File wavFile = new File(catalogue + targetWave);

        if (wavFile.exists()) {
            if (wavFile.delete())
                System.out.println("Successfully deleted " + targetWave);
            else
                System.out.println("Failed to delete " + targetWave);
        } else {
            System.out.println(catalogue + targetWave + " not exist");
        }
    }

    private void generateAudios(MyLine line)//String role, String sn, String input, int roleNum, String text)
    {
        String[] keys = {};
        String input = line.getInput();
        String sn = line.getSN();
        String role = line.getRole();
        int roleNum = line.getKeyRoleNum();
        String text = "";

        System.out.println(line.toString(0));
        //	System.out.println(line.toString(1));

        if (input.equals("0"))
            keys = keys1;
        else if (input.equals("1"))
            keys = keys2;

        for (int i = 0; i < keys.length; i++) {
            //File fMp3 = new File(keys[i].toLowerCase() + "_base.mp3");
            //File fOgg = new File(keys[i].toLowerCase() + "_base.ogg");

            String audioName = "";

            if (keys[i].equals("K"))
                text = line.getKeywords(0);
            else
                text = line.getDefault(0);

            String[] tokens = text.split("/");

            for (int j = 0; j < tokens.length; j++) {
                // if text for speaking is "", do not record
                //
                if (tokens[j].equals(""))
                    continue;

                audioName = sn + "-" + keys[i] + "-B-" + j + ".wav";

                cnt++;

                if (role.equals("Toby"))
                    playnRecord(tokens[j], audioName, "vixx");
                else if (role.equals("Raco") || role.equals("����үү") || role.equals("Dylan"))
                    playnRecord(tokens[j], audioName, "henry");
                else if (role.equals("Erica"))
                    playnRecord(tokens[j], audioName, "vinn");
                else if (role.equals("H"))
                    playnRecord(tokens[j], audioName, "xiaoyu");
                else if (role.equals("X") || role.equals("Alicia"))
                    playnRecord(tokens[j], audioName, "vimary");
                else // henry for alex_custom, vils for alex_findingX (guard)
                    playnRecord(tokens[j], audioName, "vils");//"vimary");//"henry");//vils");
            }

            //copy(fMp3, "tmp/" + audioName + ".mp3");
            //copy(fOgg, "tmp/" + audioName + ".ogg");

            if (role.equals("X") || roleNum == 2) {
                if (keys[i].equals("K"))
                    text = line.getKeywords(1);
                else
                    text = line.getDefault(1);

                tokens = text.split("/");

                for (int j = 0; j < tokens.length; j++) {
                    // if text for speaking is "", do not record
                    //
                    if (tokens[j].equals(""))
                        continue;

                    audioName = sn + "-" + keys[i] + "-B1-" + j + ".wav";
                    System.out.println("role1:" + audioName);
                    cnt++;
                    cnt2++;
                    System.out.println("role 1: " + tokens[j]);

                    playnRecord(tokens[j], audioName, "henry");
                }

                //copy(fMp3, "tmp/" + audioName + ".mp3");
                //copy(fOgg, "tmp/" + audioName + ".ogg");
            }
        }
    }

    private void generateAudios2(String text, String audioName, String voice) {
      //  playnRecord(text, audioName, voice);

        //copy(fMp3, "tmp/" + audioName + ".mp3");
        //copy(fOgg, "tmp/" + audioName + ".ogg");
    }

    public void makeAudios() {
        // for course module to generate audios, we have a special rule
        //
        if (forCourse) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(scriptName), "UTF-8"));

                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    if (line.indexOf(":") == -1)
                        continue;

                    //System.out.println("line="+line);
                    String[] tokens = line.split(":");
                    String text = tokens[0];

                    generateAudios2(text, text + "_1.wav", "mary");
                    generateAudios2(text + "," + text + "," + text, text + "_3.wav", "mary");
                    generateAudios2(tokens[1], text + "_ch.wav", "xiaoyan");
                    cnt += 3;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("\n\n\n success1: wav convert to mp3 and ogg");

           // if (cAudioText) createExcle(theme, users);
            return;
        }

        for (int i = 0; i < lineList.size(); i++) {
            MyLine line = lineList.get(i);

            if (line.getRole().equals("A") || line.getSN().equals("0") || line.getSN().equals("0-0")
                    || line.getSN().equals("0-1-0") || line.getSN().equals("0-1-1")
                    || line.getSN().equals("TO") || line.getSN().equals("TO-0")) {
                continue;
            }

            if (line.getInput().equals("0") && !line.getDefault(0).equals(""))
                System.out.println("            !!!!!!!!!!! " + line.getSN());

            if (line.getInput().equals("1") && line.getDefault(0).equals(""))
                System.out.println("           2. !!!!!!!!!!! " + line.getSN());

            if (line.getKeywords(0).equals("#"))
                continue;

            generateAudios(line);//line.getRole(), line.getSN(), line.getInput(), line.getKeyRoleNum());
        }
       // if (cAudioText) createExcle(theme, users);
        System.out.println("\n\n\n success2: wav convert to mp3 and ogg");
    }

    public static void copy(File oldfile, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;

            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldfile);
                FileOutputStream fs = new FileOutputStream(newPath);

                byte[] buffer = new byte[1280];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    //System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        } catch (Exception e) {
            System.out.println("error  ");
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        //File f1 = new File("k_base.mp3");
        //File f2 = new File("d_base.ogg");

        AudioMaker maker = new AudioMaker("E:\\abc\\abc\\javatxt\\test\\Airport20180706-I.txt", false, 1, true);

        maker.makeAudios();



        System.out.println("total=" + maker.getCnt());
        System.out.println("total2=" + maker.getCnt2());
    }

    public List<DBuserinfo> getUsers() {
        return users;
    }

    public String getTheme() {
        return theme;
    }

    public class User {
        public User(String audio, String audioText) {
            this.audio = audio;
            this.audioText = audioText;
        }

        private String audio;

        private String audioText;

        public String getAudio() {

            return audio.split(".wav")[0];
        }

        public String getAudioText() {
            return audioText;
        }
    }

    public static void createExcle(String path, List<User> users) {
        //第一步，创建一个workbook对应一个excel文件
        HSSFWorkbook workbook = new HSSFWorkbook();
        //第二部，在workbook中创建一个sheet对应excel中的sheet
        HSSFSheet sheet = workbook.createSheet("用户表一");
        sheet.setColumnWidth(0, 50 * 256);
        sheet.setColumnWidth(1, 50 * 256);
        sheet.setColumnWidth(2, 50 * 256);
        //第三部，在sheet表中添加表头第0行，老版本的poi对sheet的行列有限制
        HSSFRow row = sheet.createRow(0);
        //第四步，创建单元格，设置表头
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("voice");
        cell = row.createCell(1);
        cell.setCellValue("npc");
//        cell = row.createCell(2);
//        cell.setCellValue("voice_num(mp3)");
//        String pathVoliceNum = "C:\\Users\\Administrator\\Desktop\\temp\\Airport\\";

        //第五步，写入实体数据，实际应用中这些数据从数据库得到,对象封装数据，集合包对象。对象的属性值对应表的每行的值
        for (int i = 0; i < users.size(); i++) {
            HSSFRow row1 = sheet.createRow(i + 1);
            // User user = map.get(i);
            //创建单元格设值
            row1.createCell(0).setCellValue(users.get(i).getAudio());
            row1.createCell(1).setCellValue(users.get(i).getAudioText());
//            try {
//                File file = new File(pathVoliceNum);
//                for (File sss : file.listFiles()) {
//                    String saa = sss.getPath();
//                    name = sss.getName();
//                    ReadMp3 read = new ReadMp3(saa);
//                    byte[] buffer = new byte[128];
//                    read.ran.seek(read.ran.length() - 128);
//                    read.ran.read(buffer);
//                    info = new SongInfo(buffer);
//                    if (info.getSongName() != null && info.getSongName().length()>5){
//                        if (users.get(i).getAudioText().startsWith(info.getSongName().substring(0, 5))){
//                            row1.createCell(2).setCellValue(name);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        //将文件保存到指定的位置
        try {
            FileOutputStream fos = new FileOutputStream(path + ".xls");
            workbook.write(fos);
            System.out.println("写入成功");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
