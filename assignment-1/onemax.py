# -*- coding: utf-8 -*-
from __future__ import division
from bitstring import BitArray
import random


def create_population(population_size=100, string_length=25):
    return [create_individual(string_length) 
                for _ in xrange(population_size)]

def create_individual(string_length=25):
    s = random.randint(0, 2**string_length - 1)
    return BitArray(uint=s, length=string_length)

def fitness(individual):
    return individual.count(1)
    

def fitness_stats(population):
    fit = map(fitness, population)
    
    best = max(fit)
    worst = min(fit)
    mean = sum(fit)/len(fit)
    
    return best, worst, mean
    
# Fitness total of all individuals    
def roulette_size(population):
    return sum( map(fitness, population) )


# Generates 2 offspring for parents
def offspring(parent1, parent2):
    
    string_length = len(parent1)
    crossover_point = random.randint(0, string_length)
    
    off1_left  = parent1[:crossover_point]
    off1_right = parent2[crossover_point:]

    off2_left  = parent2[:crossover_point]
    off2_right = parent1[crossover_point:]
    
    offspring_1 = BitArray().join([off1_left, off1_right])
    offspring_2 = BitArray().join([off2_left, off2_right])
    
    return [offspring_1, offspring_2]


def mutate(individual, probability):

    bitflip_positions = []
    
    #Fill the list of indices to flip bits at
    for index in xrange(len(individual)):
        if random.random() <= probability:
            bitflip_positions.append(index) 
    
    #Flip the bits at the given positions
    individual.invert(bitflip_positions)
    return individual
    
if __name__ == '__main__':
    
    pop = create_population(10, string_length=8)
    for x in pop:
        print x.bin   
    
    #print fitness(pop[0])
    #print roulette_size(pop)
    #print fitness_stats(pop)  

    p1 = BitArray('0b111111')
    p2 = BitArray('0b000000')
    print offspring(p1, p2)
    
    print mutate(p1, 0.25)