#!/usr/bin/env python
from sys import argv
from pylab import *
import numpy as np

X = np.loadtxt(argv[1])
scatter(X[0],X[1])

show()

