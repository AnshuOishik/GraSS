                         ****************************** GraSS ****************************
							              GraSS: Grammatical, Statistical, and Substitution rule based
							                          https://github.com/AnshuOishik/GraSS
								                                Copyright (C) 2023 
=============================================================================================================================
Introduction
To utilize the code, please use the Notepad++ editor.
Java has been utilized by us in the implementation.
Please use Linux as your operating system.
Please confirm that the physical memory on your computer is larger than 10 GB.
=============================================================================================================================
GraSS: Grammatical, Statistical, and Substitution rule based

Compilation Command:
> javac -d . *.java

Execution Command:
Compression:
> java -Xms10240m rgcok.RGCOK chr.fa comp 8
Decompression:
> java -Xms10240m rgcok.RGCOK chr.fa decomp 8

Notice:
# The list of target file directories and the reference file path (the first line) are both found in chr.fa
# "decomp" is the argument for decompression, and "comp" for compression
# The final compressed file created by the BSC compressor is called "BscC.bsc".
# The number of threads is eight (4, by default, is the optional value)
# -Xms10240m is the initial allocation of memory (MB)
4. Please place the executable "bsc" in the main class file's directory.
5. Kindly set "chmod 0777" for "bsc" mode.
=============================================================================================================================
Commands for "bsc" executable file generation from available code at https://github.com/IlyaGrebnov/libbsc
Compilation commands:
> g++ -c libbsc/adler32/adler32.cpp
> g++ -c libbsc/bwt/libsais/libsais.c
> g++ -c libbsc/bwt/bwt.cpp
> g++ -c libbsc/coder/coder.cpp
> g++ -c libbsc/coder/qlfc/qlfc.cpp
> g++ -c libbsc/coder/qlfc/qlfc_model.cpp
> g++ -c libbsc/filters/detectors.cpp
> g++ -c libbsc/filters/preprocessing.cpp
> g++ -c libbsc/libbsc/libbsc.cpp
> g++ -c libbsc/lzp/lzp.cpp
> g++ -c libbsc/platform/platform.cpp
> g++ -c libbsc/st/st.cpp
> g++ -c bsc.cpp

Linking command:
> g++ -o bsc bsc.o adler32.o bwt.o coder.o detectors.o libbsc.o libsais.o lzp.o platform.o preprocessing.o qlfc.o qlfc_model.o st.o
=============================================================================================================================
### Contacts 
Please send an email to <subhankar.roy07@gmail.com> if you experience any issues.
