import copy

__author__ = 'samridhi'

import math
import scipy.cluster.hierarchy as sch
import matplotlib.pylab as plt
import pylab as pl
import numpy as np

from sklearn.cluster import DBSCAN
import sklearn.metrics

givenData = [[6, 12], [19, 7], [15, 4], [11, 0], [18, 12], [9, 20], [19, 22], [18, 17],
             [5, 11], [4, 18], [7, 15], [21, 18], [1, 19], [1, 4], [0, 9], [5, 11]]

'''
def distance(p0, p1):
    return math.sqrt((p0[0] - p1[0]) ** 2 + (p0[1] - p1[1]) ** 2)


def centeroid(arr):
    tempx = [seq[0] for seq in arr]
    tempy = [seq[1] for seq in arr]
    return [np.mean(tempx), np.mean(tempy)]


cluster = [[], [], [], []]


def BSAS(data):
    remaining = data
    clusters = []
    cluster = [[], [], [], []]
    for i in range(4):
        cluster[i].append(remaining[0])
        remaining.remove(remaining[0])
        for x in remaining:
            if distance(centeroid(cluster[i]), x) < 12:
                cluster[i].append(x)
                remaining.remove(x)


    for i in range(4):
        print cluster[i]
        clusters.append(cluster[i])

    print "remaining"
    print remaining
    labels = []
    for x in givenData:
        if x in cluster[0]:
            labels.append(0)
        elif x in cluster[1]:
            labels.append(1)
        elif x in cluster[2]:
            labels.append(2)
        elif x in cluster[3]:
            labels.append(3)
    return clusters, labels


print "1a"
BSAS(copy.deepcopy(givenData))
print"1b"
BSAS(copy.deepcopy(givenData[::-1]))

print "1a"
given_order_clusters, correct_labels = BSAS(copy.deepcopy(givenData))
print"1b"
reverse_order_cluster, reverse_labels = BSAS(copy.deepcopy(givenData[::-1]))

print(givenData)
print(correct_labels)
print(reverse_labels)
print("1c ")
#print(sklearn.metrics.adjusted_rand_score(correct_labels, reverse_labels))

i = 0

plt.scatter([item[0] for item in given_order_clusters[0]], [item[1] for item in given_order_clusters[0]], color='#0000FF')
plt.scatter([item[0] for item in given_order_clusters[1]], [item[1] for item in given_order_clusters[1]], color='#ff0000')
plt.scatter([item[0] for item in given_order_clusters[2]], [item[1] for item in given_order_clusters[2]], color='#008000')
plt.scatter([item[0] for item in given_order_clusters[3]], [item[1] for item in given_order_clusters[3]], color='#FFFF00')

plt.show()

plt.scatter([item[0] for item in reverse_order_cluster[0]], [item[1] for item in reverse_order_cluster[0]], color='#0000FF')
plt.scatter([item[0] for item in reverse_order_cluster[1]], [item[1] for item in reverse_order_cluster[1]], color='#ff0000')
plt.scatter([item[0] for item in reverse_order_cluster[2]], [item[1] for item in reverse_order_cluster[2]], color='#008000')
plt.scatter([item[0] for item in reverse_order_cluster[3]], [item[1] for item in reverse_order_cluster[3]], color='#FFFF00')

plt.show()


def sse(cluster):
    xy = centeroid(cluster)
    sumval = 0
    for val in cluster:
        sumval += distance(val, xy)
    print(sumval)


print "2a"


def hierarchical_clustering(method, data):
    tempcluster = []
    k = 3
    d = sch.distance.pdist(data)
    Z = sch.linkage(d, method=method, metric='euclidean')
    T = sch.fcluster(Z, k, 'maxclust')

    print T
    # calculate color threshold
    ct = Z[-(k - 1), 2]

    # plot
    """P = sch.dendrogram(Z, color_threshold=ct)
    plt.show()"""

    cluster = [[], [], []]
    for i, x in enumerate(T):
        cluster[x - 1].append(data[i])

    tempcluster.append(cluster[0])
    tempcluster.append(cluster[1])
    tempcluster.append(cluster[2])
    print("clustered sets are:")
    for i in tempcluster:
        print(i)
    print("sum of squared errors:")
    for i in tempcluster:
        sse(i)
    return tempcluster



a = hierarchical_clustering('single', copy.deepcopy(givenData))
print a
print "2b"
b = hierarchical_clustering("complete", copy.deepcopy(givenData))
print(b)
'''

data = [[1, 0], [3, 0], [5, 0], [6, 0], [8, 0], [11, 0], [12, 0], [13, 0], [14, 0], [15, 0], [16, 0], [22, 0], [28, 0],
        [32, 0], [33, 0], [34, 0], [35, 0], [36, 0], [37, 0], [42, 0], [58, 0]]


def compute_dbscan(epsilon, X):
    db = DBSCAN(eps=epsilon, min_samples=3, metric='euclidean').fit(X)
    core_samples = db.core_sample_indices_

    labels = db.labels_
    noise = []
    border = []
    core = []
    for i, x in enumerate(db.labels_):
        if x < 0:
            noise.append(X[i][0])
        if i not in core_samples and x > 0:
            border.append(X[i][0])

    for i in core_samples:
        core.append(X[i][0])

    print noise
    print border
    print core
    # Number of clusters in labels, ignoring noise if present.
    n_clusters_ = len(set(labels)) - (1 if -1 in labels else 0)

    print('Estimated number of clusters: %d' % n_clusters_)
    print labels
    unique_labels = set(labels)
    colors = pl.cm.Spectral(np.linspace(0, 1, len(unique_labels)))
    for k, col in zip(unique_labels, colors):
        if k == -1:
            # Black used for noise.
            col = 'k'
            markersize = 4
        class_members = [index[0] for index in np.argwhere(labels == k)]
        for index in class_members:
            x = X[index]
            if index in core_samples and k != -1:
                markersize = 4
            else:
                markersize = 4
            pl.plot(x[0], x[1], 'o', markerfacecolor=col,
                    markeredgecolor='k', markersize=markersize)

    pl.title('Estimated number of clusters: %d' % n_clusters_)
    pl.show()
    return labels


print "3a"
label_one = compute_dbscan(4, copy.deepcopy(data))

print "3b"
label_two = compute_dbscan(6, copy.deepcopy(data))

print "3c"
print sklearn.metrics.adjusted_rand_score(label_one, label_two)

'''
a = [1,6,9,10,11,13,16]
b = [2,3,4,5]
c = [7,8,12]
d = [14,15]
given_order_clusters =[[],[],[],[]]
given_order_clusters[0]= [x for i,x in enumerate(givenData) if i+1 in a]
given_order_clusters[1]= [x for i,x in enumerate(givenData) if i+1 in b]
given_order_clusters[2] = [x for i,x in enumerate(givenData) if i+1 in c]
given_order_clusters[3]= [x for i,x in enumerate(givenData) if i+1 in d]

print("reverse order")
rev = givenData[::-1]
a1 = [1,2,3,4,6,7,8,11,16]
b1 = [5,9,10,12]
c1 = [13,14,15]
reverse_order_cluster = [[],[],[]]
reverse_order_cluster[0] = [x for i,x in enumerate(rev) if i+1 in a1]
reverse_order_cluster[1] = [x for i,x in enumerate(rev) if i+1 in b1]
reverse_order_cluster[2] = [x for i,x in enumerate(rev) if i+1 in c1]

plt.scatter([item[0] for item in given_order_clusters[0]], [item[1] for item in given_order_clusters[0]], color='#00FF00')
plt.scatter([item[0] for item in given_order_clusters[1]], [item[1] for item in given_order_clusters[1]], color='#008000')
plt.scatter([item[0] for item in given_order_clusters[2]], [item[1] for item in given_order_clusters[2]], color='#ff0000')
plt.scatter([item[0] for item in given_order_clusters[3]], [item[1] for item in given_order_clusters[3]], color='#FF00ff')

plt.show()

plt.scatter([item[0] for item in reverse_order_cluster[0]], [item[1] for item in reverse_order_cluster[0]], color='#008000')
plt.scatter([item[0] for item in reverse_order_cluster[1]], [item[1] for item in reverse_order_cluster[1]], color='#ff0000')
plt.scatter([item[0] for item in reverse_order_cluster[2]], [item[1] for item in reverse_order_cluster[2]], color='#0000FF')

plt.show()
'''