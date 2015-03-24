# -*- coding: utf-8 -*-
import numpy as np

w = 0.1

p1 = np.array([5,5])
p2 = np.array([8,3])
p3 = np.array([6,7])

bp1 = np.array([5,5])
bp2 = np.array([7,3])
bp3 = np.array([5,6])

social_best = np.array([5,5])

v1 = np.array([2,2])
v2 = np.array([3,3])
v3 = np.array([4,4])

r1 = r2 = 0.5


positions = [p1,p2,p3]
best_positions = [bp1,bp2,bp3]
velocities = [v1,v2,v3]


new_pos = []

for pos, best, vel in zip(positions, best_positions, velocities):
    updated_pos = w * vel + r1*(best-pos) + r2*(social_best-pos)
    new_pos.append(updated_pos)

print new_pos


