# Clone the compressor using the following command
# git clone https://github.com/yuansliu/minicom.git
# Please modify decompress.c, minicommain.c, and Makefile. 
# For memory and CPU usage, add cpuUsage.c to the repository.

# Install
# cd minicom
# sudo apt-get install libz-dev
# sh install.sh

# In the script `install.sh`, it downloads the tools *bsc* and *p7zip*. Please make sure the two tools can be ran on your machine.

# To compress:
# ./minicom -r IN.fastq 					
# ./minicom -r IN.fastq -p 				            #order-preserving mode
# ./minicom -1 IN_1.fastq -2 IN_2.fastq 			#preserving paired-end information		

# Minicom creates compressed file 'IN_comp.minicom', 'IN_comp_order.minicom' and 'IN_comp_pe.minicom' respectively.

# To decompress:
# ./minicom -d IN.minicom [-t number of threads, default 24]
