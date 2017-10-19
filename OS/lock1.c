#include <sys/types.h>
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>

int main(void){
  int fd; struct stat statbuf; 
  fd = open ("templock", O_RDWR | O_CREAT | O_TRUNC, 0x777);
  write(fd, "abcdefghijklmnop", 16);
  fstat(fd, &statbuf);
  if (fchmod(fd, (statbuf.st_mode & ~S_IXGRP) | S_ISGID) < 0){
    printf("error in fchmod"); exit(1);
  }
}
