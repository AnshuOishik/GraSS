		     ****************************** GraSS ****************************
			GraSS: Grammatical, Statistical, and Substitution rule based
					https://github.com/AnshuOishik/GraSS
						Copyright (C) 2024 
=============================================================================================================================
Introduction
To utilize the code, please use the Notepad++ editor.
Java has been utilized by us in the implementation.
Presently available in the Linux environment.
=============================================================================================================================
# Compilation Command:
> javac -d . *.java

# Execution Command:
Compression:
> java SSG.SSG compress <Input File Path>

Decompression:
> java SSG.SSG decompress

Notice:
# "compress" is the argument for compression, and "decompress" for decompression
# FinalBsc.bsc is the final compressed file that the BSC compressor produces.
# The decompressed file name is Final.SSG
# Please place the executable "bsc" in the main class file's directory.
# Kindly set "chmod 0777" for "bsc" mode.
=============================================================================================================================
Commands for platform dependent "bsc" executable file generation from available code at https://github.com/IlyaGrebnov/libbsc
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
=======================================================================================================================================
### Contacts 
Please send an email to <subhankar.roy07@gmail.com> if you experience any issues.
