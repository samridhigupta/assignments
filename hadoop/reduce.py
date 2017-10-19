#!/usr/bin/env python

import sys
import string

days = {}


for line in sys.stdin:
  (key,val) = line.strip().split('\t',1)
  if key not in days:
      days.setdefault(key, 0)   
  days[key] += int(val);

for key in days:
  print "%s\t%s" % (key,days[key])