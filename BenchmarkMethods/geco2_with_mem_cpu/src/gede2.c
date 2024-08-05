#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <float.h>
#include <ctype.h>
#include <time.h>
#include <pthread.h>
#include <stdbool.h>

#include "mem.h"
#include "msg.h"
#include "defs.h"
#include "buffer.h"
#include "common.h"
#include "pmodels.h"
#include "context.h"
#include "bitio.h"
#include "arith.h"
#include "arith_aux.h"

///////////////////////////////////////////////////////////
////////// RAM USAGE //////////////////////////////////////

char statement[100];
volatile bool keep_running = true;

uint64_t mem_total, mem_free_beg, mem_free_end, mem_used;
uint64_t cpu_avg, ram_avg, ram_total;

extern void* get_cpu_usage(void* arg);

void get_memory_usage(uint64_t* total, uint64_t* free) {
    FILE* file = fopen("/proc/meminfo", "r");
    if (!file) {
        perror("fopen");
        exit(EXIT_FAILURE);
    }

    char buffer[256];
    while (fgets(buffer, sizeof(buffer), file)) {
        if (sscanf(buffer, "MemTotal: %ld kB", total) == 1 ||
            sscanf(buffer, "MemFree: %ld kB", free) == 1) {
            // Do nothing, just parsing
        }
    }

    fclose(file);
}

//////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
// - - - - - - - - - - - - - - D E C O M P R E S S O R - - - - - - - - - - - -

void Decompress(Parameters *P, CModel **cModels, uint8_t id){
  FILE        *Reader  = Fopen(P->tar[id], "r");
  char        *name    = ReplaceSubStr(P->tar[id], ".co", ".de");
  FILE        *Writter = Fopen(name, "w");
  uint64_t    nSymbols = 0;
  uint32_t    n, k, cModel, totModels;
  int32_t     idxOut = 0;
  uint8_t     *outBuffer, sym = 0, irSym = 0, *pos;
  CBUF        *symbolBuffer;
  PModel      **pModel, *MX;
  FloatPModel *PT;
  CMWeight    *WM;

  #ifdef PROGRESS
  uint64_t    i = 0;
  #endif

  if(P->verbose)
    fprintf(stderr, "Decompressing %"PRIu64" symbols of target %d ...\n",
    P[id].size, id + 1);

  startinputtingbits();
  start_decode(Reader);

  uint64_t garbage;
  P[id].watermark           = ReadNBits(BITS_WATERMARK,   Reader);
  garbage                   = ReadNBits(BITS_CHECKSUM,    Reader);
  P[id].size                = ReadNBits(BITS_SIZE,        Reader);
  P[id].nModels             = ReadNBits(BITS_N_MODELS,    Reader);
  for(k = 0 ; k < P[id].nModels ; ++k)
    {
    P[id].model[k].type     = ReadNBits(BITS_TYPE,        Reader);
    P[id].model[k].ctx      = ReadNBits(BITS_CTX,         Reader);
    P[id].model[k].den      = ReadNBits(BITS_ALPHA_DEN,   Reader);
    P[id].model[k].ir       = ReadNBits(BITS_IR,          Reader);
    P[id].model[k].gamma    = ReadNBits(BITS_GAMMA,       Reader) / 65534.0;
    P[id].model[k].hashSize = ReadNBits(BITS_HASH,        Reader);
    P[id].model[k].edits    = ReadNBits(BITS_EDITS,       Reader);
    if(P[id].model[k].edits != 0)
      {
      P[id].model[k].eDen   = ReadNBits(BITS_E_ALPHA_DEN, Reader);
      P[id].model[k].eGamma = ReadNBits(BITS_E_GAMMA,     Reader) / 65534.0;
      }
    }

  // EXTRA MODELS DERIVED FROM EDITS
  totModels = P[id].nModels;
  for(n = 0 ; n < P[id].nModels ; ++n)
    if(P[id].model[n].edits != 0)
      totModels += 1;

  nSymbols      = P[id].size;
  pModel        = (PModel  **) Calloc(totModels, sizeof(PModel *));
  for(n = 0 ; n < totModels ; ++n)
    pModel[n]   = CreatePModel(ALPHABET_SIZE);
  MX            = CreatePModel(ALPHABET_SIZE);
  PT            = CreateFloatPModel(ALPHABET_SIZE);
  WM            = CreateWeightModel(totModels);

  outBuffer     = (uint8_t  *) Calloc(BUFFER_SIZE, sizeof(uint8_t));
  symbolBuffer  = CreateCBuffer(BUFFER_SIZE, BGUARD);

  for(n = 0 ; n < P[id].nModels ; ++n)
    if(P[id].model[n].type == TARGET)
      cModels[n] = CreateCModel(TARGET, P[id].model[n].ctx, P[id].model[n].den,
      P[id].model[n].ir, P[id].model[n].hashSize, P[id].model[n].gamma,
      P[id].model[n].edits, P[id].model[n].eDen, P[id].model[n].eGamma);

  // GIVE SPECIFIC GAMMA:
  int pIdx = 0;
  for(n = 0 ; n < P[id].nModels ; ++n)
    {
    WM->gamma[pIdx++] = cModels[n]->gamma;
    if(P[id].model[n].edits != 0)
      {
      WM->gamma[pIdx++] = cModels[n]->SUBS.eGamma;
      }
    }

  while(nSymbols--)
    {
    #ifdef PROGRESS
    CalcProgress(P[id].size, ++i);
    #endif

    memset((void *)PT->freqs, 0, ALPHABET_SIZE * sizeof(double));

    n = 0;
    pos = &symbolBuffer->buf[symbolBuffer->idx-1];
    for(cModel = 0 ; cModel < P[id].nModels ; ++cModel)
      {
      CModel *CM = cModels[cModel];
      GetPModelIdx(pos, CM);
      ComputePModel(CM, pModel[n], CM->pModelIdx, CM->alphaDen);
      ComputeWeightedFreqs(WM->weight[n], pModel[n], PT, 4);
      if(CM->edits != 0)
        {
        ++n;
        CM->SUBS.idx = GetPModelIdxCorr(CM->SUBS.seq->buf+
        CM->SUBS.seq->idx-1, CM, CM->SUBS.idx);
        ComputePModel(CM, pModel[n], CM->SUBS.idx, CM->SUBS.eDen);
        ComputeWeightedFreqs(WM->weight[n], pModel[n], PT, 4);
        }
      ++n;
      }

    ComputeMXProbs(PT, MX, 4);

    symbolBuffer->buf[symbolBuffer->idx] = sym = ArithDecodeSymbol(4,
    (int *) MX->freqs, (int) MX->sum, Reader);
    outBuffer[idxOut] = NumToDNASym(sym);

    for(n = 0 ; n < P[id].nModels ; ++n)
      if(cModels[n]->edits != 0)
        cModels[n]->SUBS.seq->buf[cModels[n]->SUBS.seq->idx] = sym;

    CalcDecayment(WM, pModel, sym);

    for(n = 0 ; n < P[id].nModels ; ++n)
      {
      CModel *CM = cModels[n];
      if(P[id].model[n].type == TARGET)
        {
        switch(CM->ir)
          {
          case 0:
          UpdateCModelCounter(CM, sym, CM->pModelIdx);
          break;
          case 1:
          UpdateCModelCounter(CM, sym, CM->pModelIdx);
          irSym = GetPModelIdxIR(symbolBuffer->buf+symbolBuffer->idx, CM);
          UpdateCModelCounter(CM, irSym, CM->pModelIdxIR);
          break;
          case 2:
          irSym = GetPModelIdxIR(symbolBuffer->buf+symbolBuffer->idx, CM);
          UpdateCModelCounter(CM, irSym, CM->pModelIdxIR);
          break;
          default:
          UpdateCModelCounter(CM, sym, CM->pModelIdx);
          break;
          }
        }
      }

    RenormalizeWeights(WM);

    n = 0;
    for(cModel = 0 ; cModel < P[id].nModels ; ++cModel)
      {
      if(cModels[cModel]->edits != 0)      // CORRECT CMODEL CONTEXTS
        CorrectCModelSUBS(cModels[cModel], pModel[++n], sym);
      ++n;
      }

    if(++idxOut == BUFFER_SIZE)
      {
      fwrite(outBuffer, 1, idxOut, Writter);
      idxOut = 0;
      }

    UpdateCBuffer(symbolBuffer);
    }

  if(idxOut != 0)
    fwrite(outBuffer, 1, idxOut, Writter);

  finish_decode();
  doneinputtingbits();

  fclose(Writter);
  Free(MX);
  Free(name);
  for(n = 0 ; n < P[id].nModels ; ++n)
    if(P[id].model[n].type == REFERENCE)
      ResetCModelIdx(cModels[n]);
    else
      FreeCModel(cModels[n]);
  for(n = 0 ; n < totModels ; ++n){
    Free(pModel[n]->freqs);
    Free(pModel[n]);
    }

  RemoveWeightModel(WM);
  RemoveFPModel(PT);
  RemoveCBuffer(symbolBuffer);

  Free(pModel);
  Free(outBuffer);
  fclose(Reader);

  if(P->verbose == 1)
    fprintf(stderr, "Done!                          \n");  // SPACES ARE VALID
  }


//////////////////////////////////////////////////////////////////////////////
// - - - - - - - - - - - - - - - - R E F E R E N C E - - - - - - - - - - - - -

CModel **LoadReference(Parameters *P)
  {
  FILE      *Reader = Fopen(P->ref, "r");
  uint32_t  n, k, idxPos;
  uint64_t  nBases = 0;
  int32_t   idx = 0;
  uint8_t   *readerBuffer, *symbolBuffer, sym, irSym, type = 0, header = 1,
            line = 0, dna = 0;
  CModel    **cModels;
  #ifdef PROGRESS
  uint64_t  i = 0;
  #endif

  if(P->verbose == 1)
    fprintf(stderr, "Building reference model ...\n");

  readerBuffer  = (uint8_t *) Calloc(BUFFER_SIZE + 1, sizeof(uint8_t));
  symbolBuffer  = (uint8_t *) Calloc(BUFFER_SIZE + BGUARD+1, sizeof(uint8_t));
  symbolBuffer += BGUARD;

  cModels = (CModel **) Malloc(P->nModels * sizeof(CModel *));
  for(n = 0 ; n < P->nModels ; ++n)
    if(P->model[n].type == REFERENCE)
      cModels[n] = CreateCModel(REFERENCE, P->model[n].ctx, P->model[n].den,
      P->model[n].ir, P->model[n].hashSize, P->model[n].gamma,
      P->model[n].edits, P->model[n].eDen, P->model[n].eGamma);

  sym = fgetc(Reader);
  switch(sym){
    case '>': type = 1; break;
    case '@': type = 2; break;
    default : type = 0;
    }
  rewind(Reader);

  switch(type){
    case 1:  nBases = NDNASymInFasta(Reader); break;
    case 2:  nBases = NDNASymInFastq(Reader); break;
    default: nBases = NDNASyminFile (Reader); break;
    }

  P->checksum   = 0;
  while((k = fread(readerBuffer, 1, BUFFER_SIZE, Reader)))
    for(idxPos = 0 ; idxPos < k ; ++idxPos)
      {
      sym = readerBuffer[idxPos];
      if(type == 1){  // IS A FAST[A] FILE
        if(sym == '>'){ header = 1; continue; }
        if(sym == '\n' && header == 1){ header = 0; continue; }
        if(sym == '\n') continue;
        if(sym == 'N' ) continue;
        if(header == 1) continue;
        }
      else if(type == 2){ // IS A FAST[Q] FILE
        switch(line){
          case 0: if(sym == '\n'){ line = 1; dna = 1; } break;
          case 1: if(sym == '\n'){ line = 2; dna = 0; } break;
          case 2: if(sym == '\n'){ line = 3; dna = 0; } break;
          case 3: if(sym == '\n'){ line = 0; dna = 0; } break;
          }
        if(dna == 0 || sym == '\n') continue;
        if(dna == 1 && sym == 'N' ) continue;
        }

      // FINAL FILTERING DNA CONTENT
      if(sym != 'A' && sym != 'C' && sym != 'G' && sym != 'T')
        continue;

      symbolBuffer[idx] = sym = DNASymToNum(sym);
      P->checksum = (P->checksum + (uint8_t) sym);

      for(n = 0 ; n < P->nModels ; ++n)
        if(P->model[n].type == REFERENCE){

          GetPModelIdx(symbolBuffer+idx-1, cModels[n]);

          // UPDATE ONLY IF IDX LARGER THAT CONTEXT
          switch(cModels[n]->ir)
            {
            case 0:
            UpdateCModelCounter(cModels[n], sym, cModels[n]->pModelIdx);
            break;
            case 1:
            UpdateCModelCounter(cModels[n], sym, cModels[n]->pModelIdx);
            irSym = GetPModelIdxIR(symbolBuffer+idx, cModels[n]);
            UpdateCModelCounter(cModels[n], irSym, cModels[n]->pModelIdxIR);
            break;
            case 2:
            irSym = GetPModelIdxIR(symbolBuffer+idx, cModels[n]);
            UpdateCModelCounter(cModels[n], irSym, cModels[n]->pModelIdxIR);
            break;
            default:
            UpdateCModelCounter(cModels[n], sym, cModels[n]->pModelIdx);
            break;
            }
          }

      if(++idx == BUFFER_SIZE){
        memcpy(symbolBuffer - BGUARD, symbolBuffer + idx - BGUARD, BGUARD);
        idx = 0;
        }
      #ifdef PROGRESS
      CalcProgress(nBases, ++i);
      #endif
      }

  P->checksum %= CHECKSUMGF;
  for(n = 0 ; n < P->nModels ; ++n)
    if(P->model[n].type == REFERENCE)
      ResetCModelIdx(cModels[n]);
  Free(readerBuffer);
  Free(symbolBuffer-BGUARD);
  fclose(Reader);

  if(P->verbose == 1)
    fprintf(stderr, "Done!                          \n");  // SPACES ARE VALID

  return cModels;
  }

//////////////////////////////////////////////////////////////////////////////
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - M A I N - - - - - - - - - - - - - - - - -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

int32_t main(int argc, char *argv[]){
  char        **p = *&argv;
  CModel      **refModels;
  uint32_t    n, k, *checksum, refNModels = 0;
  Parameters  *P;
  FILE        *Reader = NULL;
  uint8_t     help, verbose, force, nTar = 1;
  clock_t     stop = 0, start = clock();

  ////////////////////////////////////////////////
  /////////// CPU AND MEM USAGE //////////////////

  pthread_t monitor_thread;
  uint64_t pid = (uint64_t)getpid();

  // Create a thread to monitor CPU usage
  pthread_create(&monitor_thread, NULL, get_cpu_usage, &pid);

  //////////////////////////////////////////
  /////////   MEM USAGE CALCULATE //////////

  get_memory_usage(&mem_total, &mem_free_beg);

  /////////////////////////////////////////

  if((help = ArgsState(DEFAULT_HELP, p, argc, "-h", "--help")) == 1
  || argc < 2){
    PrintMenuDecompression();
    return EXIT_SUCCESS;
    }

  if(ArgsState(DEF_VERSION, p, argc, "-V", "--version")){
    PrintVersion();
    return EXIT_SUCCESS;
    }

  force    = ArgsState  (DEFAULT_FORCE,   p, argc, "-F", "--force");
  verbose  = ArgsState  (DEFAULT_VERBOSE, p, argc, "-v", "--verbose");

  for(n = 0 ; n != strlen(argv[argc-1]) ; ++n)
    if(argv[argc-1][n] == ':')
      ++nTar;

  P        = (Parameters *) Malloc(nTar * sizeof(Parameters));
  checksum = (uint32_t   *) Calloc(nTar , sizeof(uint32_t));

  P[0].force   = force;
  P[0].verbose = verbose;
  P[0].nTar    = ReadFNames (P, argv[argc-1]);
  P[0].ref     = ArgsString (NULL, p, argc, "-r", "--reference");
  for(n = 0 ; n < nTar ; ++n){
    Reader = Fopen(P[0].tar[n], "r");
    startinputtingbits();
    start_decode(Reader);

    refNModels = 0;
    P[n].watermark           = ReadNBits(BITS_WATERMARK,   Reader);
    if(P[n].watermark != WATERMARK)
      {
      fprintf(stderr, "Error: Invalid compressed file to decompress!\n");
      return 1;
      }
    checksum[n]              = ReadNBits(BITS_CHECKSUM,    Reader);
    P[n].size                = ReadNBits(BITS_SIZE,        Reader);
    P[n].nModels             = ReadNBits(BITS_N_MODELS,    Reader);
    P[n].model = (ModelPar *) Calloc(P[n].nModels, sizeof(ModelPar));
    for(k = 0 ; k < P[n].nModels ; ++k)
      {
      P[n].model[k].type     = ReadNBits( BITS_TYPE,       Reader);
      if(P[n].model[k].type == REFERENCE)
        ++refNModels;
      P[n].model[k].ctx      = ReadNBits(BITS_CTX,         Reader);
      P[n].model[k].den      = ReadNBits(BITS_ALPHA_DEN,   Reader);
      P[n].model[k].ir       = ReadNBits(BITS_IR,          Reader);
      P[n].model[k].gamma    = ReadNBits(BITS_GAMMA,       Reader) / 65534.0;
      P[n].model[k].hashSize = ReadNBits(BITS_HASH,        Reader);
      P[n].model[k].edits    = ReadNBits(BITS_EDITS,       Reader);
      if(P[n].model[k].edits != 0)
        {
        P[n].model[k].eDen   = ReadNBits(BITS_E_ALPHA_DEN, Reader);
        P[n].model[k].eGamma = ReadNBits(BITS_E_GAMMA,     Reader) / 65534.0;
        }
      }

    finish_decode();
    doneinputtingbits();
    fclose(Reader);
    }

  if(P->verbose)
    PrintArgs(P);

  if(refNModels > 0 && P[0].ref == NULL){
    fprintf(stderr, "Error: using reference model(s) in nonexistent "
    "reference sequence!\n");
    exit(1);
    }

  if(refNModels != 0)
    refModels = LoadReference(P);
  else
    refModels = (CModel **) Malloc(P->nModels * sizeof(CModel *));

  if(P->verbose && refNModels != 0)
    fprintf(stderr, "Checksum: %"PRIu64"\n", P->checksum);

  for(n = 0 ; n < nTar ; ++n){
    if(refNModels != 0){
      if(CmpCheckSum(checksum[n], P[0].checksum) == 0)
        Decompress(P, refModels, n);
      }
    else
      Decompress(P, refModels, n);
    }

  Free(checksum);
  Free(refModels);
  for(n = 0 ; n < nTar ; ++n){
    Free(P[n].model);
  }
  Free(P->tar);
  Free(P);

  stop = clock();
  fprintf(stderr, "Spent %g sec.\n", ((double)(stop-start))/CLOCKS_PER_SEC);


  ////////////////////////////////////////////////
  /////////// CPU AND MEM USAGE //////////////////
  keep_running = false;

  // Wait for the monitoring thread to finish
  pthread_join(monitor_thread, NULL);


  get_memory_usage(&mem_total, &mem_free_end);
  if(mem_free_beg > mem_free_end)
    mem_used = mem_free_beg - mem_free_end;
  ram_total = (int)(mem_total / 1000);
  //printf("\nMemory used: %ld out of %ld kb", mem_used, mem_total);
  //printf("\nCPU usage: %ld%%\n", cpu_avg);
  //printf("\nRAM usage: %ld mb out of %ld mb\n", ram_avg*ram_total/100, ram_total);

	fprintf(stdout,"Memory used: %"PRIu64" kb out of %"PRIu64" kb \n", mem_used, mem_total);
	fprintf(stdout,"CPU usage: %"PRIu64" %%\n", cpu_avg);
	//fprintf(stdout,"RAM usage: %"PRIu64" mb out of %"PRIu64" mb\n", ram_avg*ram_total/100, ram_total);

  ////////////////////////////////////////////////
  return EXIT_SUCCESS;
  }

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
