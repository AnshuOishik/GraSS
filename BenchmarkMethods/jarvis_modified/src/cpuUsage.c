#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/sysinfo.h>
#include <stdbool.h>

#include "mem.h"
#include "defs.h"
#include "buffer.h"
#include "common.h"
#include "bitio.h"
#include "arith.h"
#include "arith_aux.h"

extern uint64_t cpu_avg;
extern uint64_t ram_avg;
extern volatile bool keep_running;

uint64_t arr[60];
uint64_t arr2[60];
uint64_t i,count=0, count2=0;
uint64_t num_cpus;

void iniArr(){
    for(i=0;i<60;i++){
        arr[i]=0;
        arr2[i]=0;
    }
}


void setUsage(){
    if(count == 0){
        cpu_avg = 0;
        ram_avg = 0;
        return;
    }
    uint64_t sum=0, sum2=0;
    for(i=0;i<60;i++){
        sum+=arr[i];
        sum2+=arr2[i];
    }
    cpu_avg = sum/count;
    ram_avg = sum2/count;
}

void* get_cpu_usage(void* arg) {
    uint64_t main_pid = *(uint64_t*)arg;
    char cmd[128];
    char buffer[128];
    FILE* fp;
    char pid[16];
    iniArr();

    sprintf(pid, "%d", (int)main_pid);
    num_cpus = sysconf(_SC_NPROCESSORS_ONLN); // Get the number of CPUs

    do {
        // Step 2: Find the CPU usage of the process using the PID  //////////////////////// CPU
        snprintf(cmd, sizeof(cmd), "ps -p %s -o %%cpu", pid);

        fp = popen(cmd, "r");
        if (fp == NULL) {
            perror("popen failed");
            return NULL;
        }
        
        // Skip the header line
        if (fgets(buffer, sizeof(buffer), fp) == NULL) {
            fprintf(stdout , "Failed to retrieve CPU usage\n");
            pclose(fp);
            return NULL;
        }

        // Get the CPU usage
        if (fgets(buffer, sizeof(buffer), fp) != NULL) {
            //printf("CPU usage of the process: %s%%\n", buffer);
            arr[count] = atoi(buffer);
        } 
        else {
            fprintf(stdout , "Failed to retrieve CPU usage\n");
        }
        pclose(fp);

        // Step 3: Find the RAM usage of the process using the PID  /////////////////////// RAM
        snprintf(cmd, sizeof(cmd), "ps -p %s -o %%mem", pid);

        fp = popen(cmd, "r");
        if (fp == NULL) {
            perror("popen failed");
            return NULL;
        }
        
        // Skip the header line
        if (fgets(buffer, sizeof(buffer), fp) == NULL) {
            fprintf(stdout , "Failed to retrieve RAM usage\n");
            pclose(fp);
            return NULL;
        }

        // Get the RAM usage
        if (fgets(buffer, sizeof(buffer), fp) != NULL) {
            arr2[count++] = atoi(buffer);
        } 
        else {
            fprintf(stdout ,"Failed to retrieve RAM usage\n");
        }
        
        pclose(fp);

        sleep(1);
    }while(count < 60 && keep_running);
    setUsage();
    return NULL;
}