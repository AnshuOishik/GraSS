//GraSS Decompression Code
package SSG;
import java.io.*;
import java.util.*;
class Decompress{
	static String info,info1;
	public static void decompress() { 
		try{
			satSubsGramModel(); //Convert Compressed sequence to upper case RAW sequence
			finalDecomp();
		}catch(Exception e){System.out.println(e);}
	}
	
	public static void satSubsGramModel()throws IOException{
		//BSC Decompression
		bscDecompress();
		
		grammar2Rule();	
		
		satSubsModel();
		
		grammar1Rule();	
	}
	
	public static void finalDecomp(){
		try{
			File fr1 = new File("F1.SSG");
			BufferedReader br1 = new BufferedReader(new FileReader(fr1));
			File fr2 = new File("dtemp.fa");
			BufferedReader br2 = new BufferedReader(new FileReader(fr2));
			
			File fw0 = new File("dtempSpecial.fa");
			BufferedWriter bw0 = new BufferedWriter(new FileWriter(fw0));
			
			File fw1 = new File("dtempCase.fa");
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(fw1));
			
			File fw2 = new File("Final.SSG");
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(fw2));
			
			String info2[] = new String[5]; //Read five lines fron F1.SSG
			int i = 0, j, k;
			while((info = br1.readLine()) != null){
				info2[i] = info;
				i += 1;
			}

			String id[] = info2[0].split(">"); //Stores no of id & individual ids
			
			String lin_len[] = info2[1].split(" ");
			int line_len[] = new int[lin_len.length];
			for(i = 0; i < lin_len.length; i++) {
				line_len[i] = Integer.parseInt(lin_len[i]);
			}
			
			String blk_len[] = info2[2].split(" ");
			int block_len[] = new int[blk_len.length];
			for(i = 0; i<blk_len.length; i++) {
				block_len[i] = Integer.parseInt(blk_len[i]);
			}
			//Reverse Modified Delta Coding
			for(i = 1; i<blk_len.length; i++) {
				block_len[i] += block_len[0]; 
			}
			
			//lowr_info[] stores both position and length
			String lowr_info[] = info2[3].split(" ");
			int lower_info[] = new int[lowr_info.length];
			for(i = 0; i < lowr_info.length; i++) {
				lower_info[i] = Integer.parseInt(lowr_info[i]);
			}
			
			//Reverse delta coding 
			for(i = 2; i <= lower_info[0]*2 ; i++){
				lower_info[i] = lower_info[i] + lower_info[i-1];
			} 			
			
			//For other characters
			int ol  =0;
			int other_char_index[] = null;
			int other_char[] = null;
			if(info2[4] != null){
				String other_char_info[] = info2[4].split(" ");
				ol = other_char_info.length;
				other_char_index = new int[ol/2];
				other_char = new int[ol/2];
				j = k = 0;
				for(i = 0; i < ol; i++) {
					if(i%2 == 0){
						other_char_index[j] = Integer.parseInt(other_char_info[i]);
						j++;
					}
					else{
						other_char[k] = Integer.parseInt(other_char_info[i]);
						k++;
					}
				}
				//Reverse Modified Delta Coding
				for(i = 1; i<ol/2; i++) {
					other_char_index[i] += other_char_index[i-1]+1; 
				}
			}
			
			//no_of_id stores number of FASTA file in multi-FASTA file
			int no_of_id = Integer.parseInt(id[0]); //same as no_of_block or line_length
						
			StringBuilder target_string;

			//Reading from 'dtemp.fa' and write to 'dtempSpecial.fa'
			while( (info = br2.readLine()) != null){
				j = k = 0;
				target_string = new StringBuilder(info);
				for(i = 0; i<(target_string.length()+ol/2); i++){
					if((j<ol/2) && (i == other_char_index[j])){ 
						bw0.write((char)(other_char[j]+65));
						j++;
					}
					else{
						bw0.write(target_string.charAt(k));
						k++;
					}
				}
				bw0.write("\n");
			}
			bw0.flush();
			br1.close();
			delFile(fr1);
			br2.close();
			delFile(fr2);
			
			//Reading from 'dtempSpecial.fa' and write to 'dtempCase.fa'
			BufferedReader br = new BufferedReader(new FileReader(fw0));
			j = 1;
			while( (info = br.readLine()) != null){
				target_string = new StringBuilder(info);
				for(i = 1; i <= lower_info[0]; i++){
					for (k = lower_info[j]; k < lower_info[j+1]; k++) {
						target_string.setCharAt(k, Character.toLowerCase(target_string.charAt(k)));
					}
					j += 2;
				}
				bw1.write(target_string.toString());
				bw1.write("\n");
			}
			bw1.flush();
			
			br.close();
			delFile(fw0);
			
			//Reading from 'dtempCase.fa' file and write to 'Final.SSG' file
			br = new BufferedReader(new FileReader(fw1));
			info = br.readLine();  
			int line_start = 0, block_start = 0, c1 = -1, c2;
			for(i = 0; i < info.length(); i += block_len[c1]){ //no_of_block
				c2 = 0;
				bw2.write(">"+id[c1+2]+"\n"); //Write Identifier
				for(j = block_start; j < (i+block_len[c1+1]); j += line_len[1]){
					if((block_len[c1+1] - c2*line_len[1]) >= line_len[1]){
						for(k = line_start; k < line_len[1]; k++){
							bw2.write(info.charAt(j+k)+"");
						}
					}
					else{
						for(k = line_start; k < (block_len[c1+1]%line_len[1]); k++){
							bw2.write(info.charAt(j+k)+"");
						}
					}
					c2++;
					bw2.write("\n");
				}
				c1++;
				block_start += block_len[c1];
			}
			bw2.flush();
			br.close();
			delFile(fw1);
		}catch(Exception e){System.out.println(e);}
	}
	
	public static void bscDecompress() {
        try {
            String bscCommand = "./bsc d " + "FinalBsc.bsc " + "FinalTar.tar";
            Process p1 = Runtime.getRuntime().exec(bscCommand);
            p1.waitFor();
            String tarCommand = "tar -xf " + "FinalTar.tar";
            Process p2 = Runtime.getRuntime().exec(tarCommand);
            p2.waitFor();
			delFile(new File("FinalTar.tar"));	
        } catch (Exception e) {
            System.out.println(e);
        }
    }
	
	public static void grammar2Rule() {
		try{
			File f = new File("F2.SSG");
			BufferedReader br = new BufferedReader(new FileReader(f));
			File f1 = new File("dtemp2.fa");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
			int L, i;
			info = br.readLine();
			bw.write(info);
			bw.write("\n");
			info = br.readLine();
			bw.write(info);
			bw.write("\n");
			while ((info = br.readLine()) != null) {
				L = info.length();
				for (i = 0; i < L; i++) {
					if(info.charAt(i)== 'P') bw.write("00");
					else if(info.charAt(i)== 'Q') bw.write("01");
					else if(info.charAt(i)== 'R') bw.write("10");
					else if(info.charAt(i)== 'S') bw.write("02");
					else if(info.charAt(i)== 'U') bw.write("20");
					else if(info.charAt(i)== 'V') bw.write("11");
					else if(info.charAt(i)== 'W') bw.write("12");
					else if(info.charAt(i)== 'X') bw.write("21");
					else if(info.charAt(i)== 'Y') bw.write("22");
					else bw.write(info.charAt(i)+"");
				}
				bw.write("\n");
			}
			bw.flush();
			br.close();
			info = null;
			delFile(f);
		} catch (IOException e) {
            System.out.println(e);
        }	
	}
	
	public static void satSubsModel(){	
		char r1 ,r2 ,r3 ,r4;
		try{
			File f = new File("dtemp2.fa");
			BufferedReader br = new BufferedReader(new FileReader(f));
			File f1 = new File("dtemp1.fa");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
			info = br.readLine();
			bw.write(info);
			bw.write("\n");
			info = br.readLine();
			r1 = info.charAt(0); 
			r2 = info.charAt(1); 
			r3 = info.charAt(2);
			r4 = info.charAt(3);
			StringBuffer sb;
			while ((info = br.readLine()) != null) {
				sb = new StringBuffer(info);
				for(int i=0; i<sb.length(); i++){
					if(sb.charAt(i) == r2)
						sb.setCharAt(i,r1);
					else if(sb.charAt(i) == r4)
						sb.setCharAt(i,r3);
					else
						sb.setCharAt(i,'Z');
				}
				info1 = sb.toString();
				bw.write(info1);
				bw.write("\n");
			}
			bw.flush();
			br.close();
			info = info1 = null;
			sb = null;
			delFile(f);
		} catch (IOException e) {
            System.out.println(e);
        }	
	}
	
	public static void grammar1Rule(){
		String r1 = "", r2 = "", r3 = "", r4 = "";
		try{
			File f = new File("dtemp1.fa");
			BufferedReader br = new BufferedReader(new FileReader(f));
			File f1 = new File("dtemp.fa");
			BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
			info = br.readLine();
			r1 = info.charAt(0)+"";
			r2 = info.substring(1,3);
			r3 = info.charAt(3)+"";
			r4 = info.substring(4,6);
			while ((info = br.readLine()) != null) {
				info1 = info.replaceAll("ZZ","N").replaceAll(r2,r1).replaceAll(r4,r3);
				bw.write(info1);
				bw.write("\n");
			}
			bw.flush();
			br.close();
			info = info1 = null;
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
}