#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sched.h>
#include <unistd.h>

void dump_attr(char *title){
   pthread_t self;
   pthread_attr_t attr;
   void *stack_addr;
   size_t stack_size;
   struct sched_param param;
   int policy;

   self = pthread_self();
   pthread_getattr_np(self, &attr);
   pthread_attr_getstack(&attr, &stack_addr, &stack_size);
   pthread_attr_getschedpolicy(&attr, &policy);
   pthread_attr_getschedparam(&attr, &param);
   printf("%s: stack addr = %p, stack size = %d\naddr near top of stack = %p\n", 
     title, stack_addr, stack_size, &stack_size);
   switch (policy){
     case SCHED_FIFO: printf("SCHED_FIFO "); break;
     case SCHED_RR: printf("SCHED_RR "); break;
     case SCHED_OTHER: printf("SCHED_OTHER "); break;
     default: ;
   }
   printf("priority = %d\n", param.sched_priority);
   
}

void *thread_entry(void *arg){
   dump_attr("working thread");
   return NULL;
}

int main(int argc, char *argv[]){
   pthread_t pthread;
   
   dump_attr("main thread");
   pthread_create(&pthread, NULL, thread_entry, NULL);
   pthread_join(pthread, NULL);
   return 0;
}
