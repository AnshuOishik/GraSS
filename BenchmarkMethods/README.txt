# Clone all repositories from github:
# GeCo3: https://github.com/cobilab/geco3
# GeCo2: https://github.com/cobilab/geco2 
# GeCo: https://github.com/cobilab/geco
# Jarvis: https://github.com/cobilab/jarvis
# FQZComp: https://sourceforge.net/projects/fqzcomp/
# DSRC 2: https://github.com/lrog/dsrc
# LQFC: https://github.com/mariusmni/lfqc
# Minicom: https://github.com/yuansliu/minicom
# Zstd: https://github.com/facebook/zstd.git
# Gzip: https://www.gnu.org/software/gzip/
# https://calc.biokirr.com/sequence-compression-benchmark/ (UHT, NUHT, DNA-COMPACT)

######### To use the above compressor, please cite the corresponding papers #########
--------------------------------------------------------------------------------------------------------------------------
# Executing the state-of-the-art compressors GeCo, GeCo2, geCo3 and Jarvis:

# Install miniconda (https://docs.anaconda.com/miniconda/#quick-command-line-install) then run the following commands
# For GeCo2: conda install -y -c bioconda geco2
# For GeCo3: conda install -y -c bioconda geco3
# For Jarvis: conda install -y -c bioconda jarvis

# We have modified and added the following to the original codes to measure memory and CPU usage:

# We have modified geco and jarvis (modify garbage in defs.h and common.c)

# Add cpuUsage.c to the repository for Memory & CPU usage

# Modify geco(1, 2, 3).c and gede(1, 2, 3).c and Jarvis.c for Memory & CPU usage

# For user convenience, we are retaining all of the benchmark compressors' files (the updated file names are listed above).

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

# GeCo2
./GeCo2 -v -l <level(or mode)> CompressedFileName
./GeDe2 -v DecompressedFileName

# GeCo3
./GeCo3 -l <level> -lr <learning rate> -hs <hidden nodes> CompressedFileName
./GeDe3 DecompressedFileName

# Jarvis
./JARVIS -v -l <level> CompressedFileName
./JARVIS -v -d DecompressedFileName

---------------------------------------------------------------------------------------------------------------------------
## Installation of Zstd
requirements (git, linux, cmake)

# clone github reposiroty
git clone https://github.com/facebook/zstd.git

# go to programs and make
cd zstd/programs
make

# creating dictionary for speedup
time ./zstd --train FullPathToTrainingSet/* -o dictionaryName
eg.
time ./zstd --train ../../../DNACorpus/* -o dictionary

# compress with dictionary 
time ./zstd -D dictionaryName FilePath
E.g.
time ./zstd -D dictionary ../../../DNACorpus/AeCa

# compress without dictionary 
time ./zstd -4 --ultra -22 ../../../DNACorpus/AeCa
time ./zstd -4 --ultra -22 ../../../DNA/GCF_001884535.1
time ./zstd -4 --ultra -22 ../../../RNA/SILVA_132_LSURef_tax_silva.fasta

# decompress with dictionary, compresssed fileName is FILE.zst
time ./zstd -D dictionaryName --decompress File.zst
eg.
time ./zstd -D dictionary --decompress ../../../DNACorpus/AeCa.zst

# decompress without dictionary
time ./zstd --decompress ../../../DNACorpus/AeCa.zst
time ./zstd --decompress ../../../DNA/GCF_001884535.1.zst
time ./zstd --decompress ../../../RNA/SILVA_132_LSURef_tax_silva.fasta.zst
---------------------------------------------------------------------------------------------------------------------------
## Installation of Gzip
# Gzip website
https://www.gnu.org/software/gzip/

# Download gzip latest version (gzip-1.13.tar.gz  2023-08-19  20:20  1.2M)
https://ftp.gnu.org/gnu/gzip/gzip-1.13.tar.gz

# Extract files
tar -xzf gzip-1.13.tar.gz

# Open gzip and install
cd gzip-1.13
./configure
make

# If want to make gzip globally available run this command with sudo privilage
sudo make install

# Compress a FILE
time ./gzip -9 ../../DNACorpus/AeCa
time ./gzip -9 ../../RNA/SILVA_132_LSURef_tax_silva.fasta

# decompress a FILE
time ./gzip -d ../../DNACorpus/AeCa.gz
time ./gzip -d ../../RNA/SILVA_132_LSURef_tax_silva.fasta.gz
------------------------------------------------------------------------------------------------------------------------------
# FQZComp
# Compilation
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
---------------------------------------------------------------------------------------------------------------------------
# Minicom
# Please modify decompress.c, minicommain.c, and Makefile. 
# For memory and CPU usage, add cpuUsage.c to the repository.

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
---------------------------------------------------------------------------------------------------------------------------------
#Lfqc
# Prerequisites
Linux system with at least 4GB of RAM (preferably 8)
C++
Ruby

# Installation
To measure CPU and memory usage we need a 'sys-proctable' library.
Run the following command, if the 'sys-proctable' is not installed. 

gem install sys-proctable
    
# To Compress

cd lfqc/lfqc

To execute this program run the following command

ruby monitor_lfqc.rb "ruby lfqc.rb ../DNA/IN.fastq"

# Note:
Here, the monitor_lfqc.rb is the program for cpu utilization monitoring, the IN.fastq is the file to be compressed and lfqc.rb is the program for compression using LFQC algorithm.

# To Decompress

ruby monitor_lfqc.rb "ruby lfqcd.rb ../DNA/IN.fastq.lfqc"

[Note: The decompressed file will be generated in the same location and will overwrite the original file.]

Notice:
If the executable files of the backbone compressors zpaq702 and lpaq8 does not work then, please create executable files for zpaq702 and lpaq8 using the command 'make' [You make need to fix the errors during compilation.].
---------------------------------------------------------------------------------------------------------------------------
# We calculate CPU usage over a period of time and then give average usage; the same is true for memory usage.
---------------------------------------------------------------------------------------------------------------------------
# To change the file format, please use the code supplied in the following link:
https://github.com/KirillKryukov/scb/tree/master/seq-tools-c
or
Supplementary Material of GeCo3
