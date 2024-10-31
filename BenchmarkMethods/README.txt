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

# To use the above compressor, please cite the corresponding papers.
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
---------------------------------------------------------------------------------------------------------------------------
# We calculate CPU usage over a period of time and then give average usage; the same is true for memory usage.
# To run other compressors, please use the readme file provided in the corresponding compressor folder
---------------------------------------------------------------------------------------------------------------------------
# To change the file format, please use the code supplied in the following link:
https://github.com/KirillKryukov/scb/tree/master/seq-tools-c
or
Supplementary Material of GeCo3
