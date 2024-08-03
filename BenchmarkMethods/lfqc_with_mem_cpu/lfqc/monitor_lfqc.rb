require 'sys/proctable'
require 'open3'

include Sys

def get_process_info(pid)
  process = ProcTable.ps.select { |p| p.pid == pid }.first
  return nil unless process

  cpu_usage = `ps -p #{pid} -o %cpu`.split.last.to_f
  memory_usage = process.rss / 1024.0 # Convert from KB to MB

  { cpu: cpu_usage, memory: memory_usage }
end

def monitor_lfqc(command, output_file)
  Open3.popen3(command) do |stdin, stdout, stderr, wait_thr|
    pid = wait_thr.pid
    puts "Started process with PID: #{pid}"

    File.open(output_file, 'w') do |file|
      file.puts("Time, CPU Usage (%), Memory Usage (MB)")

      while wait_thr.alive?
        info = get_process_info(pid)
        if info
          time = Time.now.strftime("%Y-%m-%d %H:%M:%S")
          file.puts("#{time}, #{info[:cpu]}, #{info[:memory]}")
          puts "Logged at #{time}: CPU #{info[:cpu]}%, Memory #{info[:memory]} MB"
        else
          puts "No process info found for PID #{pid}"
        end
        sleep 1 # Monitor every second
      end
    end

    puts "Process finished with status: #{wait_thr.value}"
  end
end

# Replace 'your_lfqc_command_here' with the actual command to run lfqc
# To compress
lfqc_command = "ruby lfqc.rb ../file.fastq"
# To decompress
lfqc_command = "ruby lfqcd.rb ../file.fastq.lfqc"
output_file = "lfqc_usage.csv"

monitor_lfqc(lfqc_command, output_file)

