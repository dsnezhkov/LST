#include <stdio.h>
#include <stdlib.h>
#include <errno.h>

extern int errno;
extern FILE *stdout;

int main(int argc, char** argv)
{
  errno = 0;

  int err = printf("Hello World\n");

  if (err < 0) {
    return EXIT_FAILURE;
  }

  err = fflush(stdout);

  if (err < 0 || errno != 0) {
    return EXIT_FAILURE;
  } else {
    return EXIT_SUCCESS;
  }
}
