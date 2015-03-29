# -*- coding: utf-8 -*-
#Autors l.nies s4136748, g.zuidhof s4160703

from __future__ import division
from bitstring import BitArray
import random
from itertools import izip
import matplotlib.pyplot as plt
import time
import numpy as np

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
    
    return [best, worst, mean]
    
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
    
def select_parents(population, method="roulette"):
    if method == "roulette":
        return select_parents_roulette(population)
    else:
        return select_parents_tournament(population)
    
    
# Roulette wheel selection
def select_parents_roulette(population):
    size = roulette_size(population)
    
    # Cumulative probability distribution
    # Example: [0.1, 0.3, 0.35, 0.5, 0.7, 1.0]
    cumulative = []
    
    total = 0    
    for individual in population:
        probability = fitness(individual) / size
        total += probability
        cumulative.append(total)
    
    selected = []
    amount = len(population)

    for i in xrange(amount):
        selected.append( select_from_cumulative_distribution(cumulative, population))
    
    return selected
    
# Randomly sample from cumulative distribution
# (Used in roulette wheel selection)
def select_from_cumulative_distribution(distribution, population):
    # Naïve O(N)
    r = random.random()
    

    for i, prob in enumerate(distribution):
        if (prob > r):
            return population[i]
            
        
    return -1

# Tournament selection
def select_parents_tournament(population, k=2):
    selected = []
    amount = len(population)
    
    #Not enough selected yet
    while len(selected) < amount:
        
        highest = -1
        winner = None
        
        # Run tournament of size k
        for _ in xrange(k):
            individual = random.choice(population)
            if fitness(individual) > highest:
                highest = fitness(individual)
                winner = individual
        
        selected.append(winner)
        
    return selected
    
    
# Generate offspring for selected individuals
def recombine(selected):
    new_generation = []
    
    for parent1, parent2 in pairwise(selected):
        kid1, kid2 = offspring(parent1, parent2)
    
        new_generation.append(kid1)
        new_generation.append(kid2)
    
    return new_generation    
   
def plot(stats, iterations=100,string_length = 25, selection_mode="unspecified"):
    best = []
    mean = []
    worst = []
    for gen_stats in stats:
        best.append(gen_stats[0])
        mean.append(gen_stats[2])
        worst.append(gen_stats[1])
    
    
    plt.title("L={0}, {1} selection".format(string_length, selection_mode))
    
    plt.xlabel("generation")
    plt.ylabel("fitness")
    plt.axis([0, iterations, 0, string_length])
    plt.plot(best, 'b', mean, 'g', worst, 'r')
    plt.savefig("plot" + str(string_length) + selection_mode + ".png")
    
def ea(iterations=100,  string_length=25, make_plot=True, selection_mode="roulette"):
   
   # Initialization
    pop = create_population(population_size=100, string_length=string_length)

    mut_probability = 1/string_length   
    
    #List of best, mean and worst data
    stats = []
    
    stats.append(fitness_stats(pop))
    
    
    for generation in range(iterations):   
        
        #Optimum found? 
        # Note that stats[t][0] contains the best individual's fitness
        if stats[generation][0] == string_length:
            break;
        
        #Select parents for recombination
        parents = select_parents(pop, selection_mode)
        
        #Replace current generation by offspring
        pop = recombine(parents)
        
        #Mutate
        for individual in pop:
            mutate(individual, mut_probability)
    
        stat = fitness_stats(pop)
        stats.append(stat)
        
    return generation
    
    
    if make_plot:
        plot(stats, iterations, string_length, selection_mode)
    
#runs the ea ten times
def testRun(string_length, selection_mode):
    
    durations = []
    generations = []
    for i in range(10):
        start = time.time()
        generations.append(ea(string_length = string_length, make_plot=False, selection_mode = selection_mode))
        end = time.time()
        durations.append(end-start)
        
    mean = np.mean(durations)
    std = np.std(durations)    
    print mean, std
    meanGen = np.mean(generations)
    stdGen = np.std(generations)
    print meanGen, stdGen

    
if __name__ == '__main__':
    testRun(string_length = 75, selection_mode = "roulette")
    #ea(string_length = 75)
    
    #ea(string_length=25, selection_mode="roulette", iterations=100)