package SSG;
import java.io.BufferedReader;
import java.io.InputStreamReader;

class CPUUsage {

    public static void compCpuUsage() {
        try {
            // Step 1: Find the PID of the process 
			String processName = "java SSG.SSG compress HoSa";
            String[] cmd = { "/bin/sh", "-c", "pgrep -f \"" + processName + "\"" };
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String pid = reader.readLine();
            if (pid == null) {
                System.out.println("Process not found");
                return;
            }
            
            // Step 2: Find the CPU usage of the process using the PID
            String[] cpuCmd = { "/bin/sh", "-c", "ps -p " + pid + " -o %cpu" };
            Process cpuProcess = Runtime.getRuntime().exec(cpuCmd);
            BufferedReader cpuReader = new BufferedReader(new InputStreamReader(cpuProcess.getInputStream()));
			
            // Skip the header line
            cpuReader.readLine();
            
            String cpuUsage = cpuReader.readLine();
            if (cpuUsage != null) {
                System.out.println("CPU usage of the process: " + cpuUsage + "%");
            } else {
                System.out.println("Failed to retrieve CPU usage");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public static void decomCpuUsage() {
        try {
            // Step 1: Find the PID of the process 
			String processName = "java SSG.SSG decompress";
            String[] cmd = { "/bin/sh", "-c", "pgrep -f \"" + processName + "\"" };
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String pid = reader.readLine();
            if (pid == null) {
                System.out.println("Process not found");
                return;
            }
            
            //System.out.println("PID of the process: " + pid);
            
            // Step 2: Find the CPU usage of the process using the PID
            String[] cpuCmd = { "/bin/sh", "-c", "ps -p " + pid + " -o %cpu" };
            Process cpuProcess = Runtime.getRuntime().exec(cpuCmd);
            BufferedReader cpuReader = new BufferedReader(new InputStreamReader(cpuProcess.getInputStream()));
			
            //System.out.println(cpuReader);
			
            // Skip the header line
            cpuReader.readLine();
            
            String cpuUsage = cpuReader.readLine();
            if (cpuUsage != null) {
                System.out.println("CPU usage of the process: " + cpuUsage + "%");
            } else {
                System.out.println("Failed to retrieve CPU usage");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//javac CPUUsage.java
//java CPUUsage
