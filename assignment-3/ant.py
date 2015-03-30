# -*- coding: utf-8 -*-

import numpy as np
import operator
import time
import matplotlib.pyplot as plt
import random

def create_world():
    return np.ones((9,9,9))


def update_memory(step_taken, visitable=None):
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


def create_ants(n=1, common_memory=None):
    ants = []
    poss = []

    for i in xrange(n):
        ants.append((-1,-1,-1))
        poss.append( np.copy(common_memory))

    return ants, poss
    
def create_memory(filled_pos):
    
    mem = update_memory(filled_pos[0])
    for position in filled_pos[1:]:
        mem = update_memory(position, mem)
    return mem
                
    
def kill_dead_ants(ants, poss):
    i = 0
    while(i < len(ants)):
        if not ants[i] == None and np.sum(poss[i]) == 0:
            ants[i] = None
            poss[i] = None
        i+=1
    return ants, poss
    
def recycle_ants(ants, poss, common_memory, paths, filled_pos):
    for i, ant in enumerate(ants):
        if np.sum(poss[i]) == 0: #Ant found a dead ant (pun)
            ants[i] = (-1,-1,-1) #Not-spawned-yet-ant
            poss[i] = np.copy(common_memory) #Ant has no memory
            paths[i] = filled_pos[:]

    return ants, poss, paths
    

def make_step(ants, poss, world, p_ignore_pheromones=0.05):
  
    cor = []
    for i in xrange(9*9*9):
        x,y,z = to_3d_index(i)
        cor.append(i)  
  
    for a in xrange(len(ants)):
      
      if random.random() < p_ignore_pheromones:
          probabilities = world * poss[a]
      else:
          probabilities = poss[a]

      weights = np.ravel(probabilities)
      #if a == 13:
         # print weights
      weights = weights / np.sum(weights)
      chosen_step = np.random.choice(cor, p=weights)
      ants[a] = to_3d_index(chosen_step)
    return ants

def update_possibilities(ants, poss):
    
  for i, ant in enumerate(ants):
    poss[i] = update_memory(ant, poss[i])
    
  return poss
                
def to_3d_index(i, yLength = 9, zLength = 9):
  z = i % zLength
  y = (i / zLength) % yLength
  x = i / (yLength * zLength)
  return (x,y,z)

def read_file(filepath = './s10a.txt'):
    filled_pos = []
    if filepath == None:
        return filled_pos
    
    with open(filepath) as f:
        for x, line in  enumerate(f):
            column = line.split(" ")
            for y, value in enumerate(column):
                if value.isdigit() and int(value) > 0:
                    filled_pos.append((x,y,int(value)-1))
    
    return filled_pos
            
def release_pheromones(world, ants, path_lengths, max_fitness = 81):
    for i, ant in enumerate(ants):
        
        if ant == None:
            continue
        
        x,y,z = ant
        phero = float(path_lengths[i]) / float(max_fitness)
        world[x,y,z] += phero
    return world
        
def decay_pheromones(world, decay_rate = 0.01):
    for i in xrange(81):
        x, y, z = to_3d_index(i)
        world[x,y,z] = (1-decay_rate) * world[x,y,z]
    return world
  
def pipeline(n = 200, decay_rate = 0.15):
    filled_pos = read_file()
    
    common_memory = create_memory(filled_pos)
    ants, poss = create_ants(n, common_memory)
    world = create_world()    
    
    
    iteration = 0
    begin = time.time()
    paths = []
    for _ in enumerate(ants):
        paths.append(filled_pos[:])
        
    
    while iteration < 5000:

        for i, ant in enumerate(ants):
            if not ant==(-1,-1,-1):
                paths[i].append(ant)  
                
        path_lengths = map(len, paths)
        max_l = max(path_lengths)
        if max_l == 81:
            break
        #print "max", max_l, "min", min(path_lengths),"avg", float(sum(path_lengths))/float(len(path_lengths))
        
        
        ants, poss, paths = recycle_ants(ants, poss, common_memory, paths, filled_pos)
        ants = make_step(ants, poss, world)
        
        world = release_pheromones(world, ants, path_lengths)
              
        
        poss = update_possibilities(ants, poss)
        
        
        
            
        iteration += 1
        world = decay_pheromones(world, decay_rate)  
        #print iteration
        
        
        
    max_index, max_value = max(enumerate(path_lengths), key=operator.itemgetter(1))
    
    print path_to_sudoku(paths[max_index])
    
    print "Time taken", time.time() - begin
    
    return time.time() - begin, iteration
        
        
def path_to_sudoku(path):
    sudoku = np.zeros((9,9))
    #sudoku.fill(-1)
    for x,y,z in path:
        sudoku[x,y] = z+1
    
    return sudoku
    
<<<<<<< HEAD


if __name__ == '__main__':
    filenames = ['']
=======
def bench():
>>>>>>> 1f0ee8163324f9a3c1f48f3e28560f284332d70f
    comp_times = []
    max_decay = 0.5
    n = 4
    comp_times = []
    for decay_rate in np.arange(0.05, max_decay, 0.05):
        average_time = 0
            
        for i in range(10):
            timing, iterations = pipeline(n, decay_rate)
            average_time += timing
                
        comp_times.append(float(average_time)/float(10))
                
        
<<<<<<< HEAD
    plt.xlabel("Decay Rate")
    plt.ylabel("Computation_time")
#    plt.axis([0, max_decay, 0, max(comp_times)])
    plt.plot(np.arange(0.05, max_decay, 0.05), np.array(comp_times), 'b')
    plt.savefig(str(n) + "ants.png")
            
#    pipeline()
=======
        plt.xlabel("Decay Rate")
        plt.ylabel("Computation_time")
        plt.axis([0, max_decay, 0, max(comp_times)])
        plt.plot(np.arange(0.05, max_decay, 0.05), np.array(comp_times), 'b')
        plt.savefig(str(n) + "ants.png")

def ant_n_test():
    comp_times = []
    iterations = []
    
    

if __name__ == '__main__':
    #bench()
    pipeline(2)
>>>>>>> 1f0ee8163324f9a3c1f48f3e28560f284332d70f
