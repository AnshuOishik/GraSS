require 'open3'
require 'time'

# Function to get process information (CPU usage in % and memory usage in MB)
def get_process_info(pid)
  # Command to fetch CPU and memory (rss in KB) info
  process_info = `ps -p #{pid} -o %cpu,rss`.split("\n").last
  return nil if process_info.nil? || process_info.empty?

  cpu_usage, memory_kb = process_info.split.map(&:to_f)
  memory_mb = memory_kb / 1024.0 # Convert KB to MB
  { cpu: cpu_usage, memory: memory_mb }
end

# Function to monitor CPU and memory usage of a command
def monitor_command(command)
  Open3.popen3(command) do |stdin, stdout, stderr, wait_thr|
    pid = wait_thr.pid
    puts "Started process with PID: #{pid}"

    # Attempt to capture stats immediately after process starts
    while wait_thr.alive?
      info = get_process_info(pid)
      if info
        time = Time.now.strftime("%Y-%m-%d %H:%M:%S")
        puts "[#{time}] CPU Usage: #{info[:cpu]}%, Memory Usage: #{info[:memory]} MB"
      else
        puts "No process info found for PID #{pid}, retrying..."
      end
      sleep 0.5 # Retry every 0.5 seconds to handle short-lived processes
    end

    # Final info after the process finishes
    final_info = get_process_info(pid)
    if final_info
      time = Time.now.strftime("%Y-%m-%d %H:%M:%S")
      puts "[#{time}] Final CPU Usage: #{final_info[:cpu]}%, Final Memory Usage: #{final_info[:memory]} MB"
    end

    puts "Process finished with status: #{wait_thr.value}"
  end
end

# Command-line argument support
if ARGV.length < 1
  puts "Usage: ruby monitor_lfqc.rb <command_to_monitor>"
  exit(1)
end

command = ARGV.join(' ') # Join command and its arguments

# Monitor the command
monitor_command(command)

