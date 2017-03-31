/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.computergodzilla.tfidf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
/**
 *
 * @author Mubin Shrestha
 */
public class IDme {
    
    /**
     * Main method
     * @param args
     * @throws FileNotFoundException
     * @throws IOException 
     */

    /** for Command line easy parser */        
    private final HashMap<String, String> map_lname_cha = new HashMap<>();
    private final HashMap<String, String> opt_with_value = new HashMap<>();
    private final HashMap<String, String> values = new HashMap<>();
    private final HashSet<String> opt_without_value = new HashSet<>();
    private final ArrayList<String> arglist = new ArrayList<>();  
    //tag:change it to your file-folder
    private final String filepath = "/home/ocean/Documents/data/";
    //for testing IDme-ByTfidf
    //private final String filepath = "/home/ocean/Documents/data/tfidf/";


    /**
     * Command line easy parser
     * @param args command line arguments
     */
    private void parse(String[] args) {
        for(int i=0;i<args.length;++i) {
            if (opt_with_value.containsKey(args[i])) {
                String key = opt_with_value.get(args[i]);
                values.put(key, args[i+1]);
                ++i;
            } else if (args[i].startsWith("-")) {
                opt_without_value.add(args[i]);
            } else {
                arglist.add(args[i]);
            }
        }
    }

    private void addOpt(String opt, String key, String value) {
        opt_with_value.put(opt, key);
        values.put(key, value);
    }

    private boolean hasOpt(String opt) {
        return opt_without_value.contains(opt);
    }
//    private void idmebytfidf(String opt, String key, String value) throws IOException {
     private void idmebytfidf() throws IOException {
        DocumentParser dp = new DocumentParser();
        dp.parseFiles(filepath);//tag:search
        dp.tfIdfCalculator(); //calculates tfidf
        dp.getCosBetweenTestandDatabse();//calculated cosine similarity 
        dp.savegrtfilestfidf(filepath);
    }
     
     /**
     * Method to load the dictionary with the structure : 
     * first column is the special letters and the second column is the type of the language
     * @param filePath : the dictionary file path
     * @throws FileNotFoundException
     * @throws IOException
     * *author : Haixia
     */
     private void loadlanguagechasfile(String filePath) throws FileNotFoundException, IOException{
         //add test file
        String fchar=filePath+"Language_recognition_chart.csv";
        BufferedReader br= new BufferedReader(new FileReader(new File(fchar)));
        StringBuilder sbtest = new StringBuilder();
        String line = null;
        String strlatin = null;
        int nline = 0;
        while ((line=br.readLine())!=null){
            String[] cha_lname = line.split(",");
            //clean charachters that are in latin_letters_list
            if (nline == 0){//locate latin letters
                strlatin = cha_lname[0];                
            }
            String[] strchars = cha_lname[0].split("");
            String strcharscleaned = cha_lname[0];
            for (String strsinglecha : strchars){
                if (strlatin.contains(strsinglecha.replace(" ", "")) && nline > 0){//be careful: since the crawed charachters may have noise, such as it has space: " "                    
                    strcharscleaned.replace(strsinglecha.replace(" ", ""), "");
                    strcharscleaned.replace(" ", "");
                }
            }
            
            //end clean charachters that are in latin_letters_list
            map_lname_cha.put(cha_lname[2], strcharscleaned);
            nline++;
        }   
     }
     /**
     * Method to save the character distribution, for plot
     * @param filePath : source file path
     * @throws FileNotFoundException
     * @throws IOException
     * *author : Haixia
     */
//     public void savedist() throws IOException{                
//        String strOutPutDir = "/home/ocean/NetBeansProjects/computergodzilla/data/";
//        String strOutFile = "dist.csv";
//        String strOutPut = strOutPutDir + "/" + strOutFile;
//        FileWriter fwSentences = new FileWriter(strOutPut);
//        String docname="";
//        Set<String> keyset_map_lname_chatimes = map_lname_chatimes.keySet();        
//        for (String key: keyset_map_lname_chatimes) {
//            
//            fwSentences.write(key.toString()+','+map_lname_chatimes.get(key).toString()+'\n');
//            }
//        fwSentences.flush();
//        fwSentences.close();        
//    }
//     /**
//     * Method to load evaluate the method with accuracy 
//     * @param filePath : source file path
//     * @throws FileNotFoundException
//     * @throws IOException
//     * *author : Haixia
//     */
//     private void evaluation(String filePath) throws FileNotFoundException, IOException{            
//        
//        //todo: loop the test files folder
//        String fchar=filePath+"test.txt";
//        BufferedReader br= new BufferedReader(new FileReader(new File(fchar)));
//        StringBuilder sbtest = new StringBuilder();
//        String line = null;
//        while ((line=br.readLine())!=null){}
//        //calculate accuracy 
//        
//     
//     }
     /**
     * Method to load test file and predict the language
     * @param filePath : source file path
     * @throws FileNotFoundException
     * @throws IOException
     * *author : Haixia
     */
     private double idmebydistribution(String filePath) throws FileNotFoundException, IOException{                     
        //todo: loop the file folder, examin file by file
        File[] files = new File(filePath).listFiles();
        double ntotalfiles = 0.0;
        double ncorrect = 0.0;
        for (File file : files) {
                if(file.getName().endsWith(".txt")){//verify 'if it is .txt file
                String strGtr = file.getName().split("-")[0].toString().toLowerCase();//get ground truth
                BufferedReader br= new BufferedReader(new FileReader(new File(filePath+file.getName())));
                String line = null;
                //***********define variables outside of the loop(line by line within one file)
                //***************************************
                HashMap<String, String> wordspec_lname = new HashMap<>();
                HashMap<String, String> letterspec_lname = new HashMap<>();    
                HashMap<String, Double> map_lname_chatimes = new HashMap<>();
                double dconf = 0.0;
                String lnamewithlowerconf = "";
                String lnamewithhigherconf = "";
                double mincount = 100000000000.0;
                double maxcount = 0.0;
                double dconfmax = 0.0;
                double currentcount = -1.0;
                double ntotaltimesspecialchaoccur = 0.0;
                //****************
                while ((line=br.readLine())!=null){//examin one file, line by line
                    int nwords = line.split(" ").length;
                    if (nwords > 2){//only consider tested text contains more than 2 words
                        
                        String[] strtestwords = line.split(" ");//sep word by word
                        int nstrtestwords = strtestwords.length;
                        for (int i = 0; i < nstrtestwords; i++){
                            String[] strtestchas = strtestwords[i].split("");
                            //sep charachter by charachter
                            int nstrtestchas = strtestchas.length;
                            for (int j = 0; j< nstrtestchas; j++){
                                //examin every character in the test file
                                String strexamin = strtestchas[j];
                                Set<String> strmapkeyset = map_lname_cha.keySet();
                                String strlname = "";
                                for (String keylname : strmapkeyset){
                                    //compare with ALL the possible country-oriented characters
                                    String strchas2compare = map_lname_cha.get(keylname); 
                                    Integer flag = 0;
                                    if (strchas2compare.contains(strexamin)){
                                        //it garentees : if the examined character belongs 
                                        //to multipul languages, every language bin will be 
                                        //updated accordingly
                                        flag = 1;
                                        if (map_lname_chatimes.containsKey(keylname)){ 
                                            Double nnew = map_lname_chatimes.get(keylname) + 1;
                                            map_lname_chatimes.replace(keylname, map_lname_chatimes.get(keylname), nnew);
                                        }
                                        else{
                                            map_lname_chatimes.put(keylname, 1.0);
                                        }
                                    }   
                                    if (flag == 1){
                                    letterspec_lname.put(strexamin, keylname);
                                    wordspec_lname.put(strtestwords[i], keylname);
                                    }                            
                                }
                            }
                        }     
                        //***************************************
                    }

                }//end while ((line=br.readLine())!=null)   
                
                Set<String> keysetlcandidate = map_lname_chatimes.keySet();
                for (String keylname : keysetlcandidate){
                    ntotaltimesspecialchaoccur = ntotaltimesspecialchaoccur + map_lname_chatimes.get(keylname);
                }
//                System.out.print("number of total times special character occur: ");
//                if (keysetlcandidate.isEmpty()){
//                    System.out.println("**********************My final decition: *****************************");                 
//                    System.out.println("It is probably written in English.");                     
//                }
                for (String keylname : keysetlcandidate){
                    //get the infor of the confusing word and language
                    currentcount = map_lname_chatimes.get(keylname);
                    if (currentcount < mincount){
                        mincount = currentcount;
                        lnamewithlowerconf = keylname;
                    }

                    if (currentcount >= maxcount){
                        maxcount = currentcount;
                        lnamewithhigherconf = keylname;
                        dconfmax = (double)(map_lname_chatimes.get(keylname)/  (double) ntotaltimesspecialchaoccur);
                    }

                    System.out.print("There are: ");
                    System.out.print(keylname);
                    System.out.print(" characters, with the percentage: ");
                    dconf = (double)(map_lname_chatimes.get(keylname)/  (double) ntotaltimesspecialchaoccur);
                    System.out.print(dconf);
                    System.out.println(" out of 1.");
                }
                //***************print out***********************************************
                Set<String> keset_letterspec_lname = letterspec_lname.keySet();
                System.out.println("==================special characters are: ==========================");                             
                for (String letter: keset_letterspec_lname){            
                    System.out.print(letter);
                    System.out.print(" which appears in the language: ");
                    System.out.println(letterspec_lname.get(letter));            
                }

                Set<String> keset_word_lname = wordspec_lname.keySet();
                System.out.println("==================special words are: ==========================");                                         
                for (String word: keset_word_lname){System.out.print(word);
                    System.out.print(" which appears in the language: ");
                    System.out.println(wordspec_lname.get(word));            
                }
                if (keysetlcandidate.isEmpty()){
                    System.out.println("**********************My final decition: *****************************");                 
                    System.out.println("It is probably written in English.");  
                    lnamewithhigherconf = "En";
                }
                else {
                    System.out.println("**********************My final decition: *****************************");                 
                    System.out.print("It is written in: ");
                    System.out.println(lnamewithhigherconf);
                    System.out.print("With confidence: ");
                    System.out.println(dconfmax);
                } 
                
                //***************print out***********************************************
                //evaluate:
                System.out.print("detected: "+lnamewithhigherconf+"; ");
                System.out.println("gtr: "+strGtr);
                ntotalfiles++;
                if (lnamewithhigherconf.contains(strGtr)){
                 ncorrect++;
                }                       
            System.out.println("**********************Processing one file is finished**********************"); 
            }//end 'if it is .txt file'
        }//end looping within the folder
        double acc = ncorrect / ntotalfiles;
        System.out.print("Number of correctly detected files: ");
        System.out.println(ncorrect);
        System.out.print(" Total number of files: ");
        System.out.println(ntotalfiles);
        System.out.print("Accuracy : "+ acc);     
        return  ncorrect / ntotalfiles;
     }//end IDmebyDistribution
     
     private Double idmebyonehot(String filePath) throws FileNotFoundException, IOException{        
        
        //todo: loop the file folder, examin file by file
        File[] files = new File(filePath).listFiles();
        double ntotalfiles = 0.0;
        double ncorrect = 0.0;
        for (File file : files) {
            if(file.getName().endsWith(".txt")){//verify 'if it is .txt file
            String strGtr = file.getName().split("-")[0];//get ground truth
            BufferedReader br= new BufferedReader(new FileReader(new File(filePath+file.getName())));
            String line = null;
            //***********define variables outside of the loop(line by line within one file)
            //***************************************
            List<String> list_language_type_candidate = new ArrayList<String>();
            HashMap<String, String> wordspec_lname = new HashMap<>();
            HashMap<String, String> letterspec_lname = new HashMap<>();    
            HashMap<String, Double> map_lname_chatimes = new HashMap<>();
            Double dconf = 0.0;
            String lnamewithlowerconf = "";
            String lnamewithhigherconf = "";
            Double mincount = 100000000000.0;
            Double maxcount = 0.0;
            Double dconfmax = 0.0;
            double currentcount = -1.0;
            double ntotaltimesspecialchaoccur = 0.0;
            //****************
        while ((line=br.readLine())!=null){
            String[] strtestwords = line.split(" ");//sep word by word
        int nstrtestwords = strtestwords.length;
        for (int i = 0; i < nstrtestwords; i++){
            String[] strtestchas = strtestwords[i].split("");//sep charachter by charachter
            int nstrtestchas = strtestchas.length;
            for (int j = 0; j< nstrtestchas; j++){
                String strexamin = strtestchas[j];
                Set<String> strmapkeyset = map_lname_cha.keySet();
                int nlanguagescontainthestrtest = 0;
                String strlname = "";
                String strletterspecial = "";
                for (String keylname : strmapkeyset){
                    String strchas2compare = map_lname_cha.get(keylname); 
                    if (strchas2compare.contains(strexamin)){ 
                        nlanguagescontainthestrtest++;
                        //this is the key of this algorithm                        
                        strlname = keylname;
                    } 
                }   
                if (nlanguagescontainthestrtest == 1 && !strlname.contains("Latin alphabet")){
                    //if nlanguagescontainthestrtest>1, indicates that the examined letter is not dishdinguashable
                    list_language_type_candidate.add(strlname); 
                    letterspec_lname.put(strexamin, strlname);
                    wordspec_lname.put(strtestwords[i], strlname);
                }
//                //to identify english
//                if (nlanguagescontainthestrtest == 1 && strlname.contains("Latin alphabet")){
//                    list_language_type_candidate.add(strlname); 
//                }

                for (String lname: list_language_type_candidate){
                    if (map_lname_chatimes.containsKey(lname)){ 
                        map_lname_chatimes.replace(lname, map_lname_chatimes.get(lname), map_lname_chatimes.get(lname) +1);
                    }
                    else{
                        map_lname_chatimes.put(lname, 1.0);
                    }
                }                
            }
        }  
            
        } //end while ((line=br.readLine())!=null)
        Set<String> keysetlcandidate = map_lname_chatimes.keySet();
        for (String keylname : keysetlcandidate){
            ntotaltimesspecialchaoccur = ntotaltimesspecialchaoccur + map_lname_chatimes.get(keylname);
        }
//        System.out.print("number of total times special character occur: ");
//        System.out.println(ntotaltimesspecialchaoccur);
            
        if (keysetlcandidate.isEmpty()){
            System.out.println("It is probably English.");            
        }
        for (String keylname : keysetlcandidate){//todo: haixia check
            //get the infor of the confusing word and language
            currentcount = map_lname_chatimes.get(keylname);
//            if (currentcount < mincount){
//                mincount = currentcount;
//                lnamewithlowerconf = keylname;
//            }
            
            if (currentcount > maxcount){
                maxcount = currentcount;
                lnamewithhigherconf = keylname;
                dconfmax = (double)(currentcount/(double) ntotaltimesspecialchaoccur);
            }
            
            System.out.print("There are: ");
            System.out.print(keylname);
            System.out.print(" characters, with the percentage: ");
            dconf = (double)(currentcount/(double) ntotaltimesspecialchaoccur);
            System.out.print(dconf);
            System.out.println(" out of 1.");
            System.out.println("==================================================");
        }
        Set<String> keset_letterspec_lname = letterspec_lname.keySet();
        System.out.println("==================unique characters are: ==========================");                             
        for (String letter: keset_letterspec_lname){
            System.out.print(letter);
            System.out.print(" which appears in the language: ");
            System.out.println(letterspec_lname.get(letter));            
        }
        
        Set<String> keset_word_lname = wordspec_lname.keySet();
        System.out.println("==================unique words are: ==========================");                                         
        for (String word: keset_word_lname){System.out.print(word);
            System.out.print(" which appears in the language: ");
            System.out.println(wordspec_lname.get(word));            
        }
        if (keysetlcandidate.size() > 0){
            System.out.println("**********************My final decition: *****************************");                 
            System.out.print("It is written in: ");
            System.out.println(lnamewithhigherconf);
            System.out.print("With confidence: ");
            System.out.println(dconfmax);
        }
        //***************print out***********************************************
                //evaluate:
                System.out.print("detected: "+lnamewithhigherconf+"; ");
                System.out.println("gtr: "+strGtr);
                ntotalfiles++;
                if (lnamewithhigherconf.contains(strGtr)){
                 ncorrect++;
                }
            }//end 'if it is .txt file'
        }//end looping within the folder
        double acc = ncorrect / ntotalfiles;
        System.out.print("Number of correctly detected files: ");
        System.out.println(ncorrect);
        System.out.print(" Total number of files: ");
        System.out.println(ntotalfiles);
        System.out.print("Accuracy : ");
        System.out.print(acc);
        System.out.println(" out of 1.");
        return  acc;
     }//end onehot
      /**
     * Method to load test file and predict the language
     * @param strtest : a piece of text from test file (a line)
     * This method would be called once the map_lname_cha is filled
     * This method would be called while looping the test text line by line
     * Output of this method: map_lname_chatimes, where it records the special letter that can identify the language, 
     * as well as how many time the letter occurs in the test file
     * *author : Haixia
     * This method is used when user input texts manually, instead of loading texts from files
     */
    private void idmebycharachter(String strtest){ 
        HashMap<String, String> wordspec_lname = new HashMap<>();
        HashMap<String, String> letterspec_lname = new HashMap<>();
        HashMap<String, String> map_lname_cha = new HashMap<>();    
        HashMap<String, Double> map_lname_chatimes = new HashMap<>();
        String[] strtestwords = strtest.split(" ");//sep word by word
        int nstrtestwords = strtestwords.length;
        for (int i = 0; i < nstrtestwords; i++){
            List<String> list_language_type_candidate = new ArrayList<String>();
            String[] strtestchas = strtestwords[i].split("");//sep charachter by charachter
            int nstrtestchas = strtestchas.length;
            for (int j = 0; j< nstrtestchas; j++){
                String strexamin = strtestchas[j];
                Set<String> strmapkeyset = map_lname_cha.keySet();
                int nlanguagescontainthestrtest = 0;
                String strlname = "";
                String strletterspecial = "";
                for (String keylname : strmapkeyset){
                    String strchas2compare = map_lname_cha.get(keylname); 
                    if (strchas2compare.contains(strexamin)){ 
                        nlanguagescontainthestrtest++;
                        //this is the key of this algorithm                        
                        strlname = keylname;
                    } 
                }   
                if (nlanguagescontainthestrtest == 1 && !strlname.contains("Latin alphabet")){
                    //if nlanguagescontainthestrtest>1, indicates that the examined letter is not dishdinguashable
                    list_language_type_candidate.add(strlname); 
                    letterspec_lname.put(strexamin, strlname);
                    wordspec_lname.put(strtestwords[i], strlname);
                }
//                //to identify english
//                if (nlanguagescontainthestrtest == 1 && strlname.contains("Latin alphabet")){
//                    list_language_type_candidate.add(strlname); 
//                }

                for (String lname: list_language_type_candidate){
                    if (map_lname_chatimes.containsKey(lname)){  
                        map_lname_chatimes.replace(lname, map_lname_chatimes.get(lname), map_lname_chatimes.get(lname)+1);
                    }
                    else{
                        map_lname_chatimes.put(lname, 1.0);
                    }
                }                
            }
        }        
     }
    /**
     * Command Line Interface
     * @param args command line arguments: no arguments are taken into account at the moment
     */
    public static void main(String[] args) throws IOException {
        IDme command = new IDme();
        command.loadlanguagechasfile(command.filepath); 
        //*********IDme-byTfIdf*****************
        //command.idmebytfidf();
        //*********IDme-byLRC1*****************
        double strresultdist = command.idmebydistribution(command.filepath);
        //*********IDme-byLRC2*****************
        //double strresultonehot = command.idmebyonehot(command.filepath);  
        
        //the following codes are not in use at the moment
        //command.savedist();
        //command.idmebyonehot(command.filepath);   
        //command.addOpt("-d", "directory", "./");
        //command.addOpt("-a", "alpha", "" + DEFAULT_ALPHA);
        //command.addOpt("-s", "seed", null);
        //command.addOpt("-l", "lang", null);
        //command.parse(args);
        ////--detectlang -d '/home/ocean/NetBeansProjects/ld/profiles.sm' '/home/ocean/NetBeansProjects/ld/data/nobel-chemistry-sorted.csv'
        //if (command.hasOpt("--detectlang")) {
        //    command.idmebytfidf();
        //} else if (command.hasOpt("--test")) {
        //    System.out.print("hello");
        //}
    } 
    

}
