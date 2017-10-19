#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

struct ThreadArgs { int tid; };

void *ThreadMain(void *threadArgs){
    pthread_detach(pthread_self());
    int tid = ((struct ThreadArgs *) threadArgs)->tid;
    free(threadArgs);
    sleep(tid);
    printf("\nthis is the end of thread %d\n", tid);
    return (NULL);
}

int main(int argc, char *argv[]){
    int number, i;
    for (; ;){
      printf("Enter a small number and a thread will run that long in secs: ");
      scanf("%d", &number);
      struct ThreadArgs *threadArgs = (struct ThreadArgs *)malloc(sizeof(struct ThreadArgs));
      threadArgs->tid = number;
      pthread_t threadID;
      pthread_create(&threadID, NULL, ThreadMain, threadArgs);
    }
}