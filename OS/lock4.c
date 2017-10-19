#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>

int main(void){
  int fd;  struct flock lock;
  fd = open ("templock", O_RDWR);
  lock.l_type = F_WRLCK;
  lock.l_start = 6;
  lock.l_whence = SEEK_SET;
  lock.l_len = 2;
  if (fcntl(fd, F_SETLK, &lock) < 0){
    printf("setlk error\n"); exit(1);
  }
  lseek(fd, 6, SEEK_SET);
  write(fd, "GH", 2);
  lock.l_type = F_UNLCK;
  if (fcntl(fd, F_SETLK, &lock) < 0){
    printf("setlk error\n"); exit(1);
  }
}