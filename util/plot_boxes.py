"""
Plots 2D Boxes.
"""
from sys import argv
import matplotlib.pyplot as plt
from matplotlib.path import Path
import matplotlib.patches as patches
import numpy as np

# boxes = [[[0.3535533905932738,0.8838834764831844],[0.0,10.0]],[[1.2374368670764582,10.0],[0.3535533905932738,10.0]],[[0.0,0.0],[0.3535533905932738,0.8838834764831844]],[[0.3535533905932738,0.8838834764831844],[0.0,0.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.3535533905932738,0.8838834764831844],[0.0,0.8838834764831844]],[[0.3535533905932738,10.0],[0.0,0.0]],[[0.0,0.0],[0.3535533905932738,0.8838834764831844]],[[1.2374368670764582,10.0],[0.3535533905932738,0.8838834764831844]],[[0.3535533905932738,0.8838834764831844],[1.2374368670764582,10.0]],[[0.3535533905932738,10.0],[1.2374368670764582,10.0]],[[1.2374368670764582,10.0],[1.2374368670764582,10.0]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.0,0.8838834764831844],[0.3535533905932738,0.8838834764831844]],[[0.0,10.0],[0.3535533905932738,0.8838834764831844]],[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,10.0]],[[1.2374368670764582,10.0],[1.2374368670764582,10.0]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.3535533905932738,0.8838834764831844],[0.3535533905932738,10.0]],[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.8838834764831844]],[[0.0,0.0],[0.0,0.0]],[[1.2374368670764582,10.0],[0.0,10.0]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.3535533905932738,0.8838834764831844],[0.3535533905932738,0.8838834764831844]],[[1.2374368670764582,10.0],[0.0,0.8838834764831844]],[[0.3535533905932738,0.8838834764831844],[1.2374368670764582,10.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.3535533905932738,10.0],[0.3535533905932738,0.8838834764831844]],[[1.2374368670764582,10.0],[1.2374368670764582,10.0]],[[0.0,0.8838834764831844],[0.0,0.0]],[[0.0,10.0],[0.0,0.0]],[[1.2374368670764582,10.0],[0.3535533905932738,0.8838834764831844]],[[0.0,0.8838834764831844],[1.2374368670764582,10.0]],[[0.0,10.0],[1.2374368670764582,10.0]],[[0.3535533905932738,0.8838834764831844],[0.0,0.0]],[[0.0,0.0],[0.3535533905932738,10.0]]]

boxes = [[[0.3535533905932738,10.0],[0.0,10.0]],[[0.0,0.3535533905932738],[0.3535533905932738,0.8838834764831844]],[[0.0,0.3535533905932738],[1.2374368670764582,10.0]],[[1.2374368670764582,10.0],[0.3535533905932738,10.0]],[[0.0,0.0],[0.3535533905932738,0.8838834764831844]],[[0.3535533905932738,0.8838834764831844],[0.0,0.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.3535533905932738,10.0],[0.0,0.0]],[[0.0,0.0],[0.3535533905932738,0.8838834764831844]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,10.0]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.8838834764831844]],[[0.0,0.0],[0.0,0.0]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.0,0.8838834764831844],[0.0,0.0]],[[0.0,10.0],[0.0,0.0]],[[0.3535533905932738,0.8838834764831844],[0.0,0.0]],[[0.0,0.0],[0.3535533905932738,10.0]]]

boxes = [[[0.3535533905932738,0.8838834764831844],[0.0,10.0]],[[0.8838834764831844,10.0],[1.2374368670764582,10.0]],[[1.2374368670764582,10.0],[0.0,1.2374368670764582]],[[0.8838834764831844,1.2374368670764582],[0.3535533905932738,0.8838834764831844]],[[0.0,0.3535533905932738],[1.2374368670764582,10.0]],[[0.0,0.3535533905932738],[0.3535533905932738,0.8838834764831844]],[[1.2374368670764582,10.0],[0.3535533905932738,10.0]],[[0.0,0.0],[0.3535533905932738,0.8838834764831844]],[[0.3535533905932738,0.8838834764831844],[0.0,0.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.3535533905932738,10.0],[0.0,0.0]],[[0.0,0.0],[0.3535533905932738,0.8838834764831844]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,10.0]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.0]],[[0.0,0.0],[0.0,0.8838834764831844]],[[0.0,0.0],[0.0,0.0]],[[1.2374368670764582,10.0],[0.0,0.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.0,0.0],[1.2374368670764582,10.0]],[[0.0,0.8838834764831844],[0.0,0.0]],[[0.0,10.0],[0.0,0.0]],[[0.3535533905932738,0.8838834764831844],[0.0,0.0]],[[0.0,0.0],[0.3535533905932738,10.0]]]

boxes = [[[0.3535533905932738,0.8838834764831844],[0.0,10.0]],[[0.8838834764831844,10.0],[1.2374368670764582,10.0]],[[0.8838834764831844,10.0],[0.3535533905932738,0.8838834764831844]],[[1.2374368670764582,10.0],[0.0,0.3535533905932738]],[[1.2374368670764582,10.0],[0.8838834764831844,1.2374368670764582]],[[0.0,0.3535533905932738],[1.2374368670764582,10.0]],[[0.0,0.3535533905932738],[0.3535533905932738,0.8838834764831844]],[[1.2374368670764582,10.0],[0.3535533905932738,10.0]]]

def chunks(l, n):
    """ Yield successive n-sized chunks from l.
    """
    for i in xrange(0, len(l), n):
        yield l[i:i+n]

# X = np.loadtxt(argv[1])
# n_samples = np.shape(X)[0]
# n_vars = np.shape(X)[1]

codes = [Path.MOVETO,
         Path.LINETO,
         Path.LINETO,
         Path.LINETO,
         Path.CLOSEPOLY,
         ]

# fig = plt.figure()

fig = plt.figure()
ax = fig.add_subplot(111)
for box in boxes:
    # ax = fig.add_subplot(111)
    # for sqr in chunks(X[i,:],3):
    xlow = box[0][0]
    xhigh = box[0][1]
    ylow = box[1][0]
    yhigh = box[1][1]
    verts = [
        (xlow, ylow), # left, bottom
        (xlow, yhigh), # left, top
        (xhigh, yhigh), # right, top
        (xhigh, ylow), # right, bottom
        (xlow, ylow), # ignored
        ]
    path = Path(verts, codes)
    patch = patches.PathPatch(path, facecolor='red', alpha=0.3, lw=2)
    ax.add_patch(patch)

ax.set_xlim(-10,20)
ax.set_ylim(-10,20)
plt.show()
