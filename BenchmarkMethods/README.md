# Clone all repository from github:
# GeCo3: https://github.com/cobilab/geco3
# GeCo2: https://github.com/cobilab/geco2 
# GeCo: https://github.com/cobilab/geco
# Jarvis: https://github.com/cobilab/jarvis
# LQFC: https://github.com/mariusmni/lfqc
# fqzcomp: https://sourceforge.net/projects/fqzcomp/
# DSRC 2: https://github.com/lrog/dsrc
# minicom: https://github.com/yuansliu/minicom

# Executing the state-of-the-art compressors GeCo, GeCo2, geCo3 and Jarvis:

# Install miniconda (https://docs.anaconda.com/miniconda/#quick-command-line-install) then run the following commands
> conda install -y -c bioconda geco2
> conda install -y -c bioconda geco3
> conda install -y -c bioconda jarvis

# Modify geco and jarvis (modify garbage in defs.h and common.c)

# Add cpuUsage.c to to repository for Memory & CPU usage

# Modify geco(1,2,3).c and gede(1,2,3).c and Jarvis.c for Memory & CPU usage

# Make
> cp Makefile.linux Makefile2.linux
> mv Makefile2.linux Makefile
> make

# To clean make
> make clean
 
# To run
# GeCo
> ./GeCo -tm 1:1:0:0/0 -tm 3:1:0:0/0 -tm 6:1:0:0/0 -tm 9:10:0:0/0 -tm 11:10:0:0/0 -tm 13:50:1:0/0 -tm 18:100:1:3/10 -c 30 -g 0.9 CompressedFileName
> ./GeDe -v DecompressedFileName

# GeCo2
> ./GeCo2 -v -l <level(or mode)> CompressedFileName
> ./GeDe2 -v DecompressedFileName

# GeCo3
> ./GeCo3 -l <level> -lr <learning rate> -hs <hidden nodes> CompressedFileName
> ./GeDe3 DecompressedFileName

# Jarvis
> ./JARVIS -v -l <level> CompressedFileName
> ./JARVIS -v -d DecompressedFileName

# We calculate CPU usage over a period of time and then give avg usage, same for Memory usage.

# To run other compressor please use the readme file provided in the corresponding compressor folder

# To change the file format, please use the code supplied in the following link:
https://github.com/KirillKryukov/scb/tree/master/seq-tools-c.
