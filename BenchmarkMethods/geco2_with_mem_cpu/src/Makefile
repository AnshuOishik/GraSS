#============================================================================#
#          GeCo2 & GeDe2 2014-2019 IEETA/DETI, UNIVERSITY OF AVEIRO          #
#============================================================================#
BIN    = .
CC     = gcc
CPLP   =
#-----------------------------------------------------------------------------
CFLAGS = -O3 -Wall $(CPLP)
#-----------------------------------------------------------------------------
LIBS   = -lm -pthread
DEPS   = defs.h
PROGS  = $(BIN)/GeCo2 $(BIN)/GeDe2
OBJS   = mem.o msg.o common.o buffer.o levels.o pmodels.o context.o bitio.o \
         arith.o arith_aux.o
#-----------------------------------------------------------------------------
all:
	$(MAKE) progs
progs: $(PROGS)
$(BIN)/GeCo2: geco2.c $(DEPS) $(OBJS)
	$(CC) $(CFLAGS) -o $(BIN)/GeCo2 geco2.c cpuUsage.c $(OBJS) $(LIBS)
$(BIN)/GeDe2: gede2.c $(DEPS) $(OBJS)
	$(CC) $(CFLAGS) -o $(BIN)/GeDe2 gede2.c cpuUsage.c $(OBJS) $(LIBS)
mem.o: mem.c mem.h $(DEPS)
	$(CC) -c $(CFLAGS) mem.c
msg.o: msg.c msg.h $(DEPS)
	$(CC) -c $(CFLAGS) msg.c
buffer.o: buffer.c buffer.h $(DEPS)
	$(CC) -c $(CFLAGS) buffer.c
common.o: common.c common.h $(DEPS)
	$(CC) -c $(CFLAGS) common.c
levels.o: levels.c levels.h $(DEPS)
	$(CC) -c $(CFLAGS) levels.c
pmodels.o: pmodels.c pmodels.h $(DEPS)
	$(CC) -c $(CFLAGS) pmodels.c
context.o: context.c context.h $(DEPS)
	$(CC) -c $(CFLAGS) context.c
bitio.o: bitio.c bitio.h
	$(CC) -c $(CFLAGS) bitio.c
arith.o: arith.c arith.h
	$(CC) -c $(CFLAGS) arith.c
arith_aux.o: arith_aux.c arith_aux.h
	$(CC) -c $(CFLAGS) arith_aux.c

clean:
	/bin/rm -f *.o
cleanall:
	/bin/rm -f *.o $(PROGS)
#-----------------------------------------------------------------------------
