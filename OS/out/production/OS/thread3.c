#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

#define MAX 10
pthread_mutex_t the_mutex;
pthread_cond_t condc, condp;
int buffer = 0;

void *producer(void *ptr){
   int i;
   for (i = 1; i <= MAX; i++){
     pthread_mutex_lock(&the_mutex);
     if (buffer != 0) pthread_cond_wait(&condp, &the_mutex);
     buffer = i;
     pthread_cond_signal(&condc);
     pthread_mutex_unlock(&the_mutex);
     printf("producing item %d\n", i);
     sleep(rand() % 10);
   }
   pthread_exit(0);
}

void *consumer(void *ptr){
   int i;  int j;
   for (i = 1; i <= MAX; i++){
     pthread_mutex_lock(&the_mutex);
     if (buffer == 0) pthread_cond_wait(&condc, &the_mutex);
     j = buffer;
     buffer = 0;
     pthread_cond_signal(&condp);
     pthread_mutex_unlock(&the_mutex);
     printf("consuming item %d\n", j);
     sleep(rand() % 10);
   }
   pthread_exit(0);
}

int main(int argc, char *argv[]){
   pthread_t pro, con;
   pthread_mutex_init(&the_mutex, 0);
   pthread_cond_init(&condc, 0);
   pthread_cond_init(&condp, 0);
   pthread_create(&con, 0, consumer, 0);
   pthread_create(&pro, 0, producer, 0);
   pthread_join(pro, 0);
   pthread_join(con, 0);
   pthread_cond_destroy(&condc);
   pthread_cond_destroy(&condp);
   pthread_mutex_destroy(&the_mutex);
   

}