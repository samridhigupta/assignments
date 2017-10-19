#!/usr/bin/env python

import sys
import string
import json

with open('data.txt','r') as f:
    for line in f:
        try:
              a=json.loads(line)
              if (a['user']['screen_name']):
                  if(a['user']['screen_name'] =='PrezOno'):
                      date = a['created_at'].split()
                      hour = date[3].split(":")
                      print '%s\t%s' % (hour[0], 1)
        except Exception, e:
              continue
