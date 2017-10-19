#include <fcntl.h>
#include <stdio.h>
#include <sys/types.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/mman.h>

int update(int *ptr){ return (*ptr)++; }

int main(int argc, char *argv[])
{
   int i, counter;
   pid_t pid;
   void *area;

   area = mmap(0, sizeof(int), PROT_READ | PROT_WRITE, MAP_ANON | MAP_SHARED, -1, 0);
   if (area == MAP_FAILED) exit(1);
   pid = fork();
   for (i = 0; i < 10; i += 2){
        counter = update((int *)area);
	if(pid>0)
        printf("Parent: %d %d %d\n", pid, i, counter);
	else
	printf("Child: %d %d %d\n", pid, i, counter);
   }
}