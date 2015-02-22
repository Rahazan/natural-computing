# -*- coding: utf-8 -*-
from __future__ import division
from bitstring import BitArray
import random
from itertools import izip

# From http://stackoverflow.com/questions/5389507/iterating-over-every-two-elements-in-a-list
def pairwise(iterable):
    "s -> (s0,s1), (s2,s3), (s4, s5), ..."
    a = iter(iterable)
    return izip(a, a)


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
def offspring(parent1, parent2, crossover_probability=0.7):
    
    string_length = len(parent1)
    crossover_point = random.randint(1, string_length-1)
    
    if random.random() >= crossover_probability:
        crossover_point = 0
    
    off1_left  = parent1[:crossover_point]
    off1_right = parent2[crossover_point:]

    off2_left  = parent2[:crossover_point]
    off2_right = parent1[crossover_point:]
    
    offspring_1 = BitArray().join([off1_left, off1_right])
    offspring_2 = BitArray().join([off2_left, off2_right])
    
    return (offspring_1, offspring_2)


def mutate(individual, probability):

    bitflip_positions = []
    
    #Fill the list of indices to flip bits at
    for index in xrange(len(individual)):
        if random.random() < probability:
            bitflip_positions.append(index) 
    
    #Flip the bits at the given positions
    individual.invert(bitflip_positions)
    return individual
    
# Roulette wheel selection
def select_parents(population):
    size = roulette_size(population)
    
    #[0.1, 0.3, 0.35, 0.5, 0.7, 1]
    cumulative = []
    
    total = 0    
    for individual in population:
        probability = fitness(individual) / size
        total += probability
        cumulative.append(total)
    
    selected = []
    amount = len(population)

    for i in xrange(int(amount/2)):
        selected.append( select_from_cumulative_distribution(cumulative, population))
    
    return selected
    
def select_from_cumulative_distribution(distribution, population):
    # Naïve O(N)
    r = random.random()

    for i, prob in enumerate(distribution):
        if (prob > r):
            return population[i]
            
        
    return -1
    
    
def recombine(selected):
    new_generation = []
    
    for parent1, parent2 in pairwise(selected):
        kid1, kid2 = offspring(parent1, parent2)
    
        new_generation.append(kid1)
        new_generation.append(kid2)
    
        
    
    return new_generation    
    
    
def ea(iterations=100,  string_length=25):
    # Initialization

    pop = create_population(population_size=100, string_length=string_length)

    mut_probability = 1/string_length    
    
    # Plot
    stats = []
    stats.append(fitness_stats(pop))
    #  
    
    for t in xrange(iterations):
        parents = select_parents(pop)
        pop = recombine(parents)
        
        for individual in pop:
            mutate(individual, mut_probability)
        
        print pop
        stat = fitness_stats(pop)
        print stat
        stats.append(stat)
        
    
    
if __name__ == '__main__':
    
    ea()
    
    
    #pop = create_population(10, string_length=8)
    #for x in pop:
    #    print x.bin   
    
    #print fitness(pop[0])
    #print roulette_size(pop)
    #print fitness_stats(pop)  

    #p1 = BitArray('0b111111')
    #p2 = BitArray('0b000000')
    #print offspring(p1, p2)
    
    #print select_parents(pop)    
    
   # print mutate(p1, 0.25)