#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#define NUMBER_OF_THREADS 5

void *print_hello_world(void *tid)
{
    printf("Hello from thread %d\n", tid);
    pthread_exit(NULL);
}

int main(int argc, char *argv[])
{
   pthread_t threads[NUMBER_OF_THREADS];
   int status, i;
   for (i = 0; i < NUMBER_OF_THREADS; i++){
     printf("Creating thread %d\n", i);
     status = pthread_create(&threads[i], NULL, print_hello_world, (void *)i);
     if (status != 0){
        printf("error %d\n", status);
        exit(-1);
     }
   }
   for (i = 0; i < NUMBER_OF_THREADS; i++)
     printf("thread %d terminated with error %d\n", i, pthread_join(threads[i], NULL));
   exit(0);
}