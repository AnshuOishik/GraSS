# Clone all repositories from github:
# Gzip: https://www.gnu.org/software/gzip/
# Zstd: https://github.com/facebook/zstd.git
# FQZComp: https://sourceforge.net/projects/fqzcomp/
# DSRC 2: https://github.com/lrog/dsrc
# LFQC: https://github.com/mariusmni/lfqc
# DSRC 2, UHT, NUHT, and DNA-COMPACT: https://calc.biokirr.com/sequence-compression-benchmark/ 
# Minicom: https://github.com/yuansliu/minicom
# GeCo: https://github.com/cobilab/geco
# Jarvis: https://github.com/cobilab/jarvis
# GeCo2: https://github.com/cobilab/geco2 
# GeCo3: https://github.com/cobilab/geco3

######### To use the above compressor, please cite the corresponding papers #########
------------------------------------------------------------------------------------------------------------------------------------
## Installation of Gzip
# Please download gzip latest version (gzip-1.13.tar.gz  2023-08-19  20:20  1.2M)
https://ftp.gnu.org/gnu/gzip/gzip-1.13.tar.gz

# Extract files
tar -xzf gzip-1.13.tar.gz

# Open gzip and install
cd gzip-1.13
./configure
make

# If want to make gzip globally available run this command with sudo privilage
sudo make install

# To compress
time ./gzip -9 ../../DNACorpus/AeCa
time ./gzip -9 ../../RNA/SILVA_132_LSURef_tax_silva.fasta

# To decompress
time ./gzip -d ../../DNACorpus/AeCa.gz
time ./gzip -d ../../RNA/SILVA_132_LSURef_tax_silva.fasta.gz
-------------------------------------------------------------------------------------------------------------------------------------
## Installation of Zstd
requirements (git, linux, cmake)

# Please create clone 
git clone https://github.com/facebook/zstd.git

# Go to programs and make
cd zstd/programs
make

# Creating dictionary for speedup
time ./zstd --train FullPathToTrainingSet/* -o dictionaryName
E.g.
time ./zstd --train ../../../DNACorpus/* -o dictionary

# To compress with dictionary 
time ./zstd -D dictionaryName FilePath
E.g.
time ./zstd -D dictionary ../../../DNACorpus/AeCa

# To compress without dictionary 
time ./zstd -4 --ultra -22 ../../../DNACorpus/AeCa
time ./zstd -4 --ultra -22 ../../../DNA/GCF_001884535.1
time ./zstd -4 --ultra -22 ../../../RNA/SILVA_132_LSURef_tax_silva.fasta

# To decompress with dictionary, compresssed fileName is FILE.zst
time ./zstd -D dictionaryName --decompress File.zst
E.g.
time ./zstd -D dictionary --decompress ../../../DNACorpus/AeCa.zst

# To decompress without dictionary
time ./zstd --decompress ../../../DNACorpus/AeCa.zst
time ./zstd --decompress ../../../DNA/GCF_001884535.1.zst
time ./zstd --decompress ../../../RNA/SILVA_132_LSURef_tax_silva.fasta.zst
------------------------------------------------------------------------------------------------------------------------------------
# FQZComp

# Please add cpuUsage.c to the repository for Memory & CPU usage
# Please modified fqz_comp.c

# Make
cp Makefile Makefile2
mv Makefile2 Makefile
make

# Run
# To compress:
time ./fqz_comp -s1 -q3 ../DNA/In.fastq ../DNA/comp.fqz
	
# To decompress:
time ./fqz_comp -d ../DNA/comp.fqz ../DNA/Out.fastq
time ./fqz_comp -d -X ../DNA/comp.fqz ../DNA/Out.fastq [To pass checksum failures use -X in the decompressor]
	
# Options:
-s <level>
    Specifies the size of the sequence context. Increasing this will
    improve compression on large data sets, but each increment in
    level will quadruple the memory used by the sequence compression
    steps. Further more increasing it too high may harm compression on
    small files.

    Specifying a "+" after level will use two context sizes (eg -s5+);
    a small fixed size and the <level> specified. This greatly helps
    compression of small files and also slightly helps compression of
    larger files.

    Defaults to -s3.

-q <level>
    Specifies the degree of quality compression, with a value from -q1
    to -q3.

    -q1
        Uses the previous quality value and the maximum of the two
        previous to that as a context for predicting the next value.
        Combined this adds 12 bits of context.

    -q2
        In addition to -q1, this extends the context by adding a
        single bit to indicate if the 2nd and 3rd previous qualities
        are identical to each other, as well as using 3 bits of
        context for the running-delta. (A measure of how variable a
	string of quality values are.) This is the default level.

     -q3
        As per -q2, but also adds 4 bits worth of context holding the
        position each the sequence.

The caveats and other informations in the original source. 
https://sourceforge.net/projects/fqzcomp/
------------------------------------------------------------------------------------------------------------------------------------
# LFQC
# Prerequisites
Linux system with at least 4GB of RAM (preferably 8)
C++, Ruby

# Installation
To measure CPU and memory usage we need a 'sys-proctable' library. Run the following command, if the 'sys-proctable' is not installed. 
gem install sys-proctable

# For memory and CPU usage, add monitor_lfqc.rb to the repository.

# Please go to the directory lfqc
cd lfqc/lfqc

# To compress
ruby monitor_lfqc.rb "ruby lfqc.rb ../DNA/IN.fastq"

# Note:
Here, the monitor_lfqc.rb is the program for cpu utilization monitoring, the IN.fastq is the file to be compressed and lfqc.rb is the program for compression using LFQC algorithm.

# To decompress
ruby monitor_lfqc.rb "ruby lfqcd.rb ../DNA/IN.fastq.lfqc"

[Note: The decompressed file will be generated in the same location and will overwrite the original file.]

Notice:
If the executable files of the backbone compressors zpaq702 and lpaq8 does not work then, please create executable files for zpaq702 and lpaq8 using the command 'make' [You make need to fix the errors during compilation.].
-------------------------------------------------------------------------------------------------------------------------------------
# Minicom

# For memory and CPU usage, add cpuUsage.c to the repository.
# Please modify decompress.c, minicommain.c, and Makefile. 

# To install
cd minicom
sudo apt-get install libz-dev
sh install.sh

# In the script `install.sh`, it downloads the tools *bsc* and *p7zip*. Please make sure the two tools can be ran on your machine.

# To compress:
./minicom -r ../DNA/IN.fastq 						

# Minicom creates compressed file 'IN_comp.minicom'

# To decompress:
./minicom -d ../DNA/IN_comp.minicom
------------------------------------------------------------------------------------------------------------------------------------
# Executing the state-of-the-art compressors GeCo, Jarvis, GeCo2, and GeCo3:

# Install miniconda (https://docs.anaconda.com/miniconda/#quick-command-line-install) then run the following commands
# For Jarvis: conda install -y -c bioconda jarvis
# For GeCo2: conda install -y -c bioconda geco2
# For GeCo3: conda install -y -c bioconda geco3

# Please modify and add the following to the original codes to measure memory and CPU usage:
1. Please modify files of compressors GeCo and Jarvis (modify garbage in defs.h and common.c)
2. Please add cpuUsage.c to the repository for memory & CPU usage
3. Please modify geco(1, 2, 3).c and gede(1, 2, 3).c and Jarvis.c

# Make (To Compile)
cp Makefile.linux Makefile2.linux
mv Makefile2.linux Makefile
make

# To clean make
make clean
 
# To run
# GeCo
./GeCo -tm 1:1:0:0/0 -tm 3:1:0:0/0 -tm 6:1:0:0/0 -tm 9:10:0:0/0 -tm 11:10:0:0/0 -tm 13:50:1:0/0 -tm 18:100:1:3/10 -c 30 -g 0.9 CompressedFileName
./GeDe -v DecompressedFileName

# Jarvis
./JARVIS -v -l <level> CompressedFileName
./JARVIS -v -d DecompressedFileName

# GeCo2
./GeCo2 -v -l <level(or mode)> CompressedFileName
./GeDe2 -v DecompressedFileName

# GeCo3
./GeCo3 -l <level> -lr <learning rate> -hs <hidden nodes> CompressedFileName
./GeDe3 DecompressedFileName
-------------------------------------------------------------------------------------------------------------------------------------
# For user convenience, we are retaining all of the benchmark compressors' files (the revised and added file names are listed above).
# We have renamed the compressor by the suffix "_with_mem_cpu", so please change the directory names accordingly.
# We calculate CPU usage over a period of time and then give average usage; the same is true for memory usage.
-------------------------------------------------------------------------------------------------------------------------------------
# To change the file format, please use the code supplied in the following link:
https://github.com/KirillKryukov/scb/tree/master/seq-tools-c
or
Supplementary material of GeCo3 
Link: https://academic.oup.com/gigascience/article/9/11/giaa119/5974977#supplementary-data
File name: giaa119_Supplemental_File
-------------------------------------------------------------------------------------------------------------------------------------
