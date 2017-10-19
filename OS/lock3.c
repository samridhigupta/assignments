#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>

int main(void){
  int fd;  struct flock lock;
  fd = open ("templock", O_RDWR);
  lock.l_type = F_RDLCK;
  lock.l_start = 6;
  lock.l_whence = SEEK_SET;
  lock.l_len = 4;
  if (fcntl(fd, F_SETLK, &lock) < 0){
    printf("setlk error\n"); exit(1);
  }
  sleep(10);
  lock.l_type = F_UNLCK;
  if (fcntl(fd, F_SETLK, &lock) < 0){
    printf("setlk error\n"); exit(1);
  }
}