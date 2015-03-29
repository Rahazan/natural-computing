# -*- coding: utf-8 -*-

import numpy as np
from random import randint

def create_world(file=''):
    if file == '':
        return np.zeros((9,9,9))
    else:
        print "Not implemented yet"

def update_next_steps(step_taken, visitable=None):
    if visitable == None:
        visitable = np.ones((9,9,9))
    
    step_x, step_y, step_z = step_taken
    
    # Vertical line
    visitable[step_x,:,step_z] = 0
    
    # Horizontal line
    visitable[:,step_y,step_z] = 0
    
    # Box 3x3
    x_start = (step_x/3) * 3
    y_start = (step_y/3) * 3
    x_end = x_start + 3
    y_end = y_start + 3
    
    visitable[x_start:x_end, y_start:y_end, step_z] = 0
    
    # Single space
    visitable[step_x,step_y,:] = 0
    
    return visitable
    



def print_world(visitable):
    print np.sum(visitable, axis=2)
    
vis =  update_next_steps((3,3,1))
print_world(vis)

world = create_world()
where_to = vis * world
print_world(where_to)

def create_ants(n=1):
    ants = []
    poss = [] #Visitable places per ant, possibilities
    for _ in xrange(n):
        pos = (randint(0,9), randint(0,9), randint(0,9))
        ants.append(pos)
        poss.append(update_next_steps(pos))
        
    return ants, poss
    
    
