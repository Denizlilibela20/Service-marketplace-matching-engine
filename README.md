GigMatch Engine – Service Marketplace Matching Engine (Java)

A command-driven gig marketplace engine implemented in Java, inspired by platforms such as Armut and designed for large-scale inputs under strict time constraints.
The project focuses on algorithmic matching, deterministic composite ranking, and performance-aware data structures, implemented from scratch.

This engine simulates a service marketplace where customers request jobs and freelancers are matched automatically based on a composite score derived from skills, ratings, reliability, and burnout status.

## Features
1) Core Platform Operations (Type-1)

- Register customers and freelancers with globally unique IDs

- Manual employment of freelancers

- Customer-initiated and freelancer-initiated cancellations

- Rating updates using incremental averaging

- Customer and freelancer query operations

- Customer-level freelancer blacklisting

2) Matching & Simulation Mechanics (Type-2)

- Automatic job matching using top-K ranked freelancers

- Deterministic composite ranking with integer scores

- Burnout system with trigger and recovery rules

- Loyalty tier system with platform-subsidized discounts

- Queued service changes applied at month-end

- Platform-level blacklisting for frequent freelancer cancellations

3) Advanced Operations (Type-3)

- Manual skill updates with immediate rank recomputation

- Composite Ranking Logic

- Freelancers are ranked using a deterministic integer composite score in the range [0, 10000].



The composite score is computed using:

Skill matching score:

Weighted dot product of freelancer skills and service requirements

Normalized to the range [0, 1]

Rating score : averageRating / 5

Reliability score:  1 − (cancelledJobs / totalJobs)

Burnout penalty:

Applied if the freelancer is in burnout state

Final score formula:

floor(10000 × (0.55 × skill + 0.25 × rating + 0.20 × reliability − burnoutPenalty))

Tie-breaking rule:

If two freelancers have the same composite score, the lexicographically smaller freelancer ID is ranked higher

This guarantees fully deterministic output.



## Architecture Overview:

The project is structured with a clear separation of concerns.

1) Main.java

Entry point of the application

Handles file-based input/output

Parses commands and delegates execution to the core engine

2) PlatformEngine.java

Central command-processing engine

Implements all platform operations defined in the specification

Maintains global system state

Coordinates ranking, employment, cancellations, and simulations

3) Customer.java

Represents a customer account

Stores total spending, loyalty tier, and employment statistics

Maintains a personal blacklist of freelancers

4) Freelancer.java

Represents a freelancer profile

Stores service type, price, skill vector, rating, and availability

Tracks reliability, burnout state, and platform blacklist status

Stores heap index to support efficient ranking updates

5) FreelancerMaxHeap.java

Custom max-heap implementation for freelancer ranking

One heap per service category

Orders freelancers by composite score with deterministic tie-breaking

Supports insertion, removal, and priority updates in O(log n)

6) UserHashMap.java

Custom hash map implemented using separate chaining

Used for all ID-based lookups

Expected O(1) access time under large workloads

7) UserHashSet.java

Hash-set abstraction built on top of UserHashMap

Used primarily for blacklist membership checks

8) ServiceRequirement.java

Stores predefined skill requirement profiles for each service type

9) ServiceChange.java

Represents queued service change requests

Applied atomically during month-end simulation



## Job Matching Flow: 

When a customer requests a job:

1- Retrieve the heap corresponding to the requested service

2- Evaluate freelancers in descending composite score order

3- Skip ineligible freelancers:

* Platform-blacklisted

* Burned out

* Currently unavailable

* Customer-blacklisted

4- List the top-K eligible freelancers

5- Automatically employ the highest-ranked freelancer

This approach avoids global sorting and ensures efficient matching.


## Monthly Simulation:

The simulate_month command advances the system by one month and applies:

1- Burnout trigger and recovery rules

2- Reset of monthly counters

3- Loyalty tier updates based on effective spending

4- Application of all queued service changes

5- Composite score recomputation where necessary

Batching these updates prevents unnecessary per-command recalculation.



## Large-Scale Design & Algorithms: 

The system is explicitly designed for large test cases.

* Up to 500,000 users

* Hundreds of thousands of commands

* Frequent job requests and ranking updates

Algorithmic design choices:

* Custom hash tables for expected O(1) lookups

* Per-service max-heaps to avoid global re-sorting

* Indexed heap nodes for O(log n) updates and removals

* Incremental rating and skill updates

* Month-based batching of expensive state transitions

* All major operations run in O(1) or O(log n) time, except month-end simulations which require linear traversal by design.

## Build & Run

Compile:

javac *.java


Run:

java Main <input_file> <output_file>

## Testing

The repository includes self-authored example test cases under: testcases

use test_runner.py to test cases.

## Notes:

All outputs are deterministic

Any state-changing operation triggers appropriate reindexing

Designed to meet strict performance constraints under large inputs

## Disclaimer:

This project was implemented as part of a university-level data structures and algorithms assignment.
All code is original and written from scratch with a focus on algorithmic efficiency and clean architecture.