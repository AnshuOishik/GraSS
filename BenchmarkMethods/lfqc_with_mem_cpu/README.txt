# Colone the compressor
git clone https://github.com/mariusmni/lfqc

# Prerequisites
Linux system with at least 4gb of RAM (preferably 8)
Ruby

# Installation
To measure CPU and memory usage we need a 'sys-proctable' library.
Run the following command, if the 'sys-proctable' is not already installed 

```bash
  gem install sys-proctable
```
    
# To Compress

To execute this program run the following command

```bash
  ruby monitor_lfqc.rb ruby lfqc.rb ../file.fastq

```

# Note:
Here, the monitor_lfqc.rb is the program for cpu utilization monitoring, the file.fastq is the file to be compressed and lfqc.rb is the program for compression using LFQC algorithm.

# To Decompress

```bash
  ruby monitor_lfqc.rb ruby lfqcd.rb ../file.fastq.lfqc

```




