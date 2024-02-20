//GraSS Compression Code
package SSG;
import java.io.*;
import java.util.*;
class Compress{	
	static final int vec_size1 = 6107460; 
	static final int vec_size2 = 695172;
	static final int vec_size3 = 89664;
	public static int[] seqLowVecBeg; //Lower vector begin
	public static int[] seqLowVecLen; //Lower vector length
	public static String[] seqId; //Store sequence IDs, array for multi-FASTA file
	public static int[] seqLineLen; //Store FASTA file first line length
	public static int[] seqBlockLen; //Store FASTA file  block length
	public static int[] seqSpecialIndex; //Store FASTA file  other char index
	public static byte[] seqSpecialChar; //Store FASTA file  other char
	static int iden = 0; //Store number of id = line length = sequence block length
	static int lowVecLen = 0;
	static int seqCodeLen = 0;
	static int seqSpecialLen = 0;
	static int charLen = 0; //Stores total number of chraracters in a file
	static String info, info1;
	static StringBuffer sb; 
	static int flag = 0, flagT = 0, flagU = 0; //flagT, flagU checks the presence of T and U
	
	public static void compress(String path) { 
		try{
			File f = new File("F1.SSG");
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);		
			seqExtraction(path);
			saveIdName(bw);
			rleForLineLen(bw);
			saveSeqBlockLen(bw);
			saveLowerVec(bw);
			saveSpecialChar(bw);
			satSubsGramModel();
		}catch(Exception e){
			System.out.println("e1... =  "+e);
		}
	}
	
	public static void seqExtraction(String path) {
        seqLowVecBeg = new int[vec_size1];  
        seqLowVecLen = new int[vec_size1];
		seqId = new String[vec_size2];  
		seqLineLen = new int[vec_size2]; 
		seqBlockLen = new int[vec_size2];
		seqSpecialIndex = new int[vec_size3];
		seqSpecialChar = new byte[vec_size3];
		BufferedReader br = null;
		File file = new File(path);

		Boolean flag = true;
		int lettersLen = 0, mark=0, count=0;
		char ch;
        try {
			File f = new File("temp.fa");
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
            br = new BufferedReader(new FileReader(file));
			int index = 0; 
			while ((info = br.readLine()) != null) {
				if(info.charAt(0) == '>'){ //Sequence Id start by '>' symbol
					seqId[iden] = info;
					mark = 1;
					seqBlockLen[iden] = seqCodeLen; 
					seqCodeLen = 0;
				}
				else{
					if(mark == 1){
						seqLineLen[iden++] = info.length();
						mark = 0;
					}
					for (int i = 0; i < info.length(); i++) {
						charLen++;
						ch = info.charAt(i);
						if (Character.isLowerCase(ch)) {
							if (flag) {
								flag = false;
								seqLowVecBeg[lowVecLen] = lettersLen;
								lettersLen = 0;
							}
							ch = Character.toUpperCase(ch);
						} else {
							if (!flag) {
								flag = true;
								seqLowVecLen[lowVecLen++] = lettersLen;
								lettersLen = 0;
							}
						}
						lettersLen++;
						if (ch == 'A' || ch == 'C' || ch == 'G' || ch == 'T' || ch == 'U'||ch=='N') {
							bw.write(ch);
							count++;
							if(count == (0.66*(Integer.MAX_VALUE-2))){ 
								bw.write("\n");
								count = 0;
							}
						}
						else{//If FASTA file contain special characters except A,C,G,T/U,N
							seqSpecialIndex[seqSpecialLen] = (charLen-1)-index; 
							seqSpecialChar[seqSpecialLen] = (byte)(ch-65);
							seqSpecialLen++;
							index = charLen;
						}
						seqCodeLen++;
					}
				}
			}
			if (!flag) {
                seqLowVecLen[lowVecLen++] = lettersLen;
            }
			seqBlockLen[iden] = seqCodeLen;
			for(int i = 2;i <= iden;i++){ //Modified Delta Coding
				seqBlockLen[i] = seqBlockLen[i]-seqBlockLen[1];
			}
			br.close();
			bw.flush();
			//System.out.println(iden+" "+lowVecLen+" "+seqSpecialLen);
		}catch(Exception e){
			System.out.println("e2 =  "+e);
		}
	}
	
	public static void saveIdName(BufferedWriter bw) {
        try {
			bw.write(iden+"");
            for (int i = 0; i < iden; i++) {
                bw.write(seqId[i]);
            }
			bw.write("\n");
			seqId = null;
        } catch (IOException e) {
            System.out.println("e3 =  "+e);
        }
    }
	
	public static void rleForLineLen(BufferedWriter bw) { 
        List<Integer> rleCode = new ArrayList<>(2); 
		int count = 1,codeLen; 
        if (iden > 0) { 
            rleCode.add(seqLineLen[0]);   
            for (int i = 1; i < iden; i++) { 
                if (seqLineLen[i] == seqLineLen[i - 1]) { 
                    count++;
                } else {
                    rleCode.add(count);
                    rleCode.add(seqLineLen[i]);
                    count = 1;
                }
            }
            rleCode.add(count);
			seqLineLen = null;
        }
        codeLen = rleCode.size(); 
        try {
            bw.write(codeLen + " "); 
            for (int i = 0; i < codeLen; i++) { 
                bw.write(rleCode.get(i) + " "); 
            }
			bw.write("\n");
        } catch (IOException e) {
            System.out.println("e4 =  "+e);
        }
    }
	
	public static void saveSeqBlockLen(BufferedWriter bw) {
		try {
			//'iden' store number of blocks
            for (int i = 1; i <= iden; i++) {
                bw.write(seqBlockLen[i] + " ");
            }
			bw.write("\n");
			seqBlockLen = null;
        } catch (IOException e) {
            System.out.println("e5 =  "+e);
        }
    }
	
	public static void saveLowerVec(BufferedWriter bw) {
        try {
			bw.write(lowVecLen+" ");
            for (int i = 0; i < lowVecLen; i++) {
                bw.write(seqLowVecBeg[i]+" "+seqLowVecLen[i] + " ");
            }
			bw.write("\n");
			seqLowVecBeg = seqLowVecLen = null;
        } catch (IOException e) {
            System.out.println("e6 =  "+e);
        }
    }
	
	//Save Special Characters Information
	public static void saveSpecialChar(BufferedWriter bw) {
		try {
            for (int i = 0; i < seqSpecialLen; i++) {
                bw.write(seqSpecialIndex[i] + " ");
				bw.write(seqSpecialChar[i] + " ");
            }
			bw.flush();
			seqSpecialIndex = null;
			seqSpecialChar = null;
        } catch (IOException e) {
            System.out.println("e7 =  "+e);
        }
	}
	
	//Statistical, Substitutional and Grammar Model 
	public static void satSubsGramModel()throws IOException{
		grammar1Rule();
		
		satSubsModel();
	
		grammar2Rule();	
		
		bscCompress();	//BSC Compression
	}
	
	//Count frequency of symbols and apply grammar rule 1
	public static void grammar1Rule(){
		int freqArr[] = {0,0,0,0,0}, n = 0; //We consider symbols {A, C, G, T/U, N}
		int L, i; 
		int m1, m2, min1, min2;
		char ch;
		try {
			File f = new File("temp.fa");
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			//Calculate the frequency of characters {A, C, G, T/U, N} if any from file temp.fa
			while ((info = br.readLine()) != null) { 
				L = info.length();
				for (i = 0; i < L; i++) {
					if(info.charAt(i) == 'A')
						freqArr[0]++; 
					else if(info.charAt(i) == 'C')
						freqArr[1]++;
					else if(info.charAt(i) == 'G')
						freqArr[2]++;
					else if(info.charAt(i) == 'T'){//For DNA
						freqArr[3]++;
						flagT = 1;
					}
					else if(info.charAt(i) == 'U'){ //For RNA
						freqArr[3]++; 
						flagU = 1;
					}
					else if(info.charAt(i) == 'N')
						freqArr[4]++;
				}
			}
			br.close();
			
			//Calculating actual number of characetrs: Either 4 or 5
			for(i = 0; i < 5; i++){
				if(freqArr[i] != 0)
					n++;
			}			
			
			//Find the smallest, second and third smallest frequency characters, Assuming all sequence will have atleast four characters among A, C, G, T/U, N
			int firstMin = Integer.MAX_VALUE, firstMinPos = -1;
            int secMin = Integer.MAX_VALUE, secMinPos = -1;
            int thirdMin = Integer.MAX_VALUE, thirdMinPos = -1;
			for (i = 0; i < n; i++){ 
                if (freqArr[i] < firstMin){
                    thirdMin = secMin; 
					thirdMinPos = secMinPos; 
                    secMin = firstMin;
					secMinPos = firstMinPos;
                    firstMin = freqArr[i];
					firstMinPos = i; 
                }
                else if (freqArr[i] < secMin){
                    thirdMin = secMin;
					thirdMinPos = secMinPos;
                    secMin = freqArr[i];
					secMinPos = i;
                }
                else if (freqArr[i] < thirdMin){
                    thirdMin = freqArr[i];
					thirdMinPos = i;
				}
            }
			
			//Applying Grammar Rule 1
			File f1 = new File("temp1.fa");
			FileWriter fw = new FileWriter(f1);
			BufferedWriter bw = new BufferedWriter(fw);
			f = new File("temp.fa");
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			int rule = 0;
			while ((info = br.readLine()) != null) {				
				if(n == 5){ 				
					if((secMinPos == 0 && thirdMinPos == 1) || (secMinPos == 1 && thirdMinPos == 0)){
						if(flagT == 1){
							info1 = info.replaceAll("N","ZZ").replaceAll("A","ZT").replaceAll("C","ZG");
						}else{ 
							info1 = info.replaceAll("N","ZZ").replaceAll("A","ZU").replaceAll("C","ZG");
						}
						if(rule == 0){ //As the information is required to write for the first time
							if(flagT == 1)
								bw.write("AZTCZG\n"); //Storing this information once as first line, required during decompression
							else 
								bw.write("AZUCZG\n");
							rule = 1;
						}
						flag = 1;
					}
					else if((secMinPos == 1 && thirdMinPos == 2) || (secMinPos == 2 && thirdMinPos == 1)){
						if(flagT == 1){
							info1 = info.replaceAll("N","ZZ").replaceAll("C","ZA").replaceAll("G","ZT");
						}else{
							info1 = info.replaceAll("N","ZZ").replaceAll("C","ZA").replaceAll("G","ZU");
						}
						if(rule == 0){
							if(flagT == 1)
								bw.write("CZAGZT\n");
							else
								bw.write("CZAGZU\n");
							rule = 1;
						}
						flag=2;
					}
					else if((secMinPos == 2 && thirdMinPos == 3) || (secMinPos == 3 && thirdMinPos == 2)){
						if(flagT == 1){
							info1 = info.replaceAll("N","ZZ").replaceAll("G","ZC").replaceAll("T","ZA");
						}else{
							info1 = info.replaceAll("N","ZZ").replaceAll("G","ZC").replaceAll("U","ZA");
						}
						if(rule == 0){
							if(flagT == 1)
								bw.write("GZCTZA\n");
							else
								bw.write("GZCUZA\n");
							rule = 1;
						}
						flag = 3;
					}
					else if((secMinPos == 0 && thirdMinPos == 2) || (secMinPos == 2 && thirdMinPos == 0)){
						if(flagT == 1)
							info1 = info.replaceAll("N","ZZ").replaceAll("A","ZC").replaceAll("G","ZT");
						else
							info1 = info.replaceAll("N","ZZ").replaceAll("A","ZC").replaceAll("G","ZU");
						if(rule == 0){
							if(flagT == 1)
								bw.write("AZCGZT\n");
							else
								bw.write("AZCGZU\n");
							rule = 1;
						}
						flag = 4;
					}
					else if((secMinPos == 0 && thirdMinPos == 3) || (secMinPos == 3 && thirdMinPos == 0)){
						if(flagT == 1)
							info1 = info.replaceAll("N","ZZ").replaceAll("A","ZC").replaceAll("T","ZG");
						else
							info1 = info.replaceAll("N","ZZ").replaceAll("A","ZC").replaceAll("U","ZG");
						if(rule == 0){
							if(flagT == 1)
								bw.write("AZCTZG\n");
							else
								bw.write("AZCUZG\n");
							rule = 1;
						}
						flag = 5;
					}
					else if((secMinPos == 1 && thirdMinPos == 3) || (secMinPos == 3 && thirdMinPos == 1)){
						if(flagT == 1)
							info1 = info.replaceAll("N","ZZ").replaceAll("C","ZA").replaceAll("T","ZG");
						else
							info1 = info.replaceAll("N","ZZ").replaceAll("C","ZA").replaceAll("U","ZG");
						if(rule == 0){
							if(flagT == 1)
								bw.write("CZATZG\n");
							else
								bw.write("CZAUZG\n");
							rule = 1;
						}
						flag = 6;
					}
				}
				else if(n == 4 && freqArr[4] == 0){ //Assuming only four character A, C, G, T/U				
					if((firstMinPos == 0 && secMinPos == 1) || (secMinPos == 0 && firstMinPos == 1)){
						if(flagT == 1)
							info1 = info.replaceAll("A","ZT").replaceAll("C","ZG");
						else 
							info1 = info.replaceAll("A","ZU").replaceAll("C","ZG");
						if(rule == 0){ //As the information is required to write for the first time
							if(flagT == 1)
								bw.write("AZTCZG\n"); //Storing this information once as first line, required during decompression
							else 
								bw.write("AZUCZG\n");
							rule = 1;
						}
						flag = 1;
					}
					else if((firstMinPos == 1 && secMinPos == 2) || (secMinPos == 1 && firstMinPos == 2)){
						if(flagT == 1)
							info1 = info.replaceAll("C","ZA").replaceAll("G","ZT");
						else
							info1 = info.replaceAll("C","ZA").replaceAll("G","ZU");
						if(rule == 0){
							if(flagT == 1)
								bw.write("CZAGZT\n");
							else
								bw.write("CZAGZU\n");
							rule = 1;
						}
						flag=2;
					}
					else if((firstMinPos == 2 && secMinPos == 3) || (secMinPos == 2 && firstMinPos == 3)){
						if(flagT == 1)
							info1 = info.replaceAll("G","ZC").replaceAll("T","ZA");
						else
							info1 = info.replaceAll("G","ZC").replaceAll("U","ZA");
						if(rule == 0){
							if(flagT == 1)
								bw.write("GZCTZA\n");
							else
								bw.write("GZCUZA\n");
							rule = 1;
						}
						flag = 3;
					}
					else if((firstMinPos == 0 && secMinPos == 2) || (secMinPos == 0 && firstMinPos == 2)){
						if(flagT == 1)
							info1 = info.replaceAll("A","ZC").replaceAll("G","ZT");
						else
							info1 = info.replaceAll("A","ZC").replaceAll("G","ZU");
						if(rule == 0){
							if(flagT == 1)
								bw.write("AZCGZT\n");
							else
								bw.write("AZCGZU\n");
							rule = 1;
						}
						flag = 4;
					}
					else if((firstMinPos == 0 && secMinPos == 3) || (secMinPos == 0 && firstMinPos == 3)){
						if(flagT == 1)
							info1 = info.replaceAll("A","ZC").replaceAll("T","ZG");
						else
							info1 = info.replaceAll("A","ZC").replaceAll("U","ZG");
						if(rule == 0){
							if(flagT == 1)
								bw.write("AZCTZG\n");
							else
								bw.write("AZCUZG\n");
							rule = 1;
						}
						flag = 5;
					}
					else if((firstMinPos == 1 && secMinPos == 3) || (secMinPos == 1 && firstMinPos == 3)){
						if(flagT == 1)
							info1 = info.replaceAll("C","ZA").replaceAll("T","ZG");
						else
							info1 = info.replaceAll("C","ZA").replaceAll("U","ZG");
						if(rule == 0){
							if(flagT == 1)
								bw.write("CZATZG\n");
							else
								bw.write("CZAUZG\n");
							rule = 1;
						}
						flag = 6;
					}
				}
				bw.write(info1);
				bw.write("\n");
			}
			br.close();
			bw.flush();
			info = info1 = null;
			delFile(f);
		} catch (IOException e) {
            System.out.println(e);
        }	
	}
	
	//Character to Decimal Coding {0, 1, 2}
	//File need not to store this information as we are assinging decimal code according to ASCII values
	//Substitution Rule
	public static void satSubsModel(){	
		int rule = 0;
		try{
			File f = new File("temp1.fa");
			BufferedReader br = new BufferedReader(new FileReader(f));
			File f1 = new File("temp2.fa");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
			info = br.readLine();
			bw.write(info);
			bw.write("\n");
			while ((info = br.readLine()) != null) {
				sb = new StringBuffer(info);
				if(flag == 1){
					for(int i=0; i<sb.length(); i++){
						if(sb.charAt(i) == 'G')
							sb.setCharAt(i,'0');
						else if(sb.charAt(i) == 'T')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'U')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'Z')
							sb.setCharAt(i,'2');
					}
					if(rule == 0 && flagT == 1){ //For first time write
						bw.write("G0T1\n"); //Required during decompression
						rule = 1;
					}
					else if(rule == 0 && flagU == 1){
						bw.write("G0U1\n"); //Required during decompression
						rule = 1;
					}
				}
				else if(flag == 2){
					for(int i=0; i<sb.length(); i++){
						if(sb.charAt(i) == 'A')
							sb.setCharAt(i,'0');
						else if(sb.charAt(i) == 'T')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'U')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'Z')
							sb.setCharAt(i,'2');
					}
					if(rule == 0 && flagT == 1){ //For first time write
						bw.write("A0T1\n"); //Required during decompression
						rule = 1;
					}
					else if(rule == 0 && flagU == 1){
						bw.write("A0U1\n"); //Required during decompression
						rule = 1;
					}
				}
				else if(flag == 3){
					for(int i=0; i<sb.length(); i++){
						if(sb.charAt(i) == 'A')
							sb.setCharAt(i,'0');
						else if(sb.charAt(i) == 'C')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'Z')
							sb.setCharAt(i,'2');
					}
					if(rule == 0){
						bw.write("A0C1\n");
						rule = 1;
					}
				}
				else if(flag == 4){
					for(int i=0; i<sb.length(); i++){
						if(sb.charAt(i) == 'C')
							sb.setCharAt(i,'0');
						else if(sb.charAt(i) == 'T')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'U')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'Z')
							sb.setCharAt(i,'2');
					}
					if(rule == 0 && flagT == 1){ //For first time write
						bw.write("C0T1\n"); //Required during decompression
						rule = 1;
					}
					else if(rule == 0 && flagU == 1){
						bw.write("C0U1\n"); //Required during decompression
						rule = 1;
					}
				}
				else if(flag == 5){
					for(int i=0; i<sb.length(); i++){
						if(sb.charAt(i) == 'C')
							sb.setCharAt(i,'0');
						else if(sb.charAt(i) == 'G')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'Z')
							sb.setCharAt(i,'2');
					}
					if(rule == 0){
						bw.write("C0G1\n");
						rule = 1;
					}
				}
				else if(flag == 6){
					for(int i=0; i<sb.length(); i++){
						if(sb.charAt(i) == 'A')
							sb.setCharAt(i,'0');
						else if(sb.charAt(i) == 'G')
							sb.setCharAt(i,'1');
						else if(sb.charAt(i) == 'Z')
							sb.setCharAt(i,'2');
					}
					if(rule == 0){
						bw.write("A0G1\n");
						rule = 1;
					}
				}
				info1 = sb.toString();
				bw.write(info1);
				bw.write("\n");
			}
			br.close();
			bw.flush();
			sb = null;
			info = info1 = null;
			delFile(f);
		} catch (IOException e) {
            System.out.println(e);
        }	
	}
	
	//Replace two consecutive numbers by the following rule and store to file F2.SSG
	/*Grammar Rule 2:
		00 => P, 01 => Q, 10 => R, 02 => S, 20 => T, 11 => V, 12 => W, 21 => X, 22 => Y
	*/
	public static void grammar2Rule() {
		try{
			File f = new File("temp2.fa");
			BufferedReader br = new BufferedReader(new FileReader(f));
			File f1 = new File("F2.SSG");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
			int tar_seq_len = 0, L, i;
			char temp_ch;
			info = br.readLine();
			bw.write(info);
			bw.write("\n");
			info = br.readLine();
			bw.write(info);
			bw.write("\n");
			while ((info = br.readLine()) != null) {
				L = info.length();
				for (i = 0; i < L; i += 2) {
					if(L-i != 1){
						info1 = info.substring(i,i+2);
						if(info1.equals("00")) bw.write("P");
						else if(info1.equals("01")) bw.write("Q");
						else if(info1.equals("10")) bw.write("R");
						else if(info1.equals("02")) bw.write("S");
						else if(info1.equals("20")) bw.write("U");
						else if(info1.equals("11")) bw.write("V");
						else if(info1.equals("12")) bw.write("W");
						else if(info1.equals("21")) bw.write("X");
						else if(info1.equals("22")) bw.write("Y");
					}
					else{ 
						info1 = info.substring(i,i+1);
						bw.write(info1);
					}
				}
				bw.write("\n");
			}
			br.close();
			bw.flush();
			info = info1= null;
			delFile(f);
		} catch (IOException e) {
            System.out.println(e);
        }	
	}
	
	public static boolean delFile(File f) {
        if (!f.exists()) {
            return false;
        }
		if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (File f1 : fs) 
                delFile(f1);
        }
        return f.delete();
    }
	
	//BSC Compression
	public static void bscCompress() { 
		try {
            String tarCommand = "tar -cf " + "FinalTar.tar "+ "F1.SSG " + "F2.SSG";
			Process p1 = Runtime.getRuntime().exec(tarCommand);
            p1.waitFor();
            String bscCommand = "./bsc e " + "FinalTar.tar " + "FinalBsc.bsc -e2";
            Process p2 = Runtime.getRuntime().exec(bscCommand);
            p2.waitFor();

            delFile(new File("FinalTar.tar"));
            delFile(new File("F1.SSG"));
            delFile(new File("F2.SSG"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
