#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main(int argc, char *argv[])
{
    int pid; 
    int status;
    if ((pid = fork()) != 0){
      printf("child pid = %i\n", pid);
      printf("my process id = %i\n", getpid());
      wait(&status);
      printf("status = %i\n", status);
    }else{
      printf("parent pid = %i\n", getppid());
      printf("my process id = %i\n", getpid());
    }
    exit(1);
}


