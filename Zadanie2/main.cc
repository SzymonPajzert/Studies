#include <iostream>
#include <fstream>
#include <vector>
#include <set>
#include <atomic>
#include <queue>
#include <thread>
#include <stack>
#include <list>
#include <unordered_map>

#include "logger.h"

using bc_t = std::vector<std::atomic<double> >;
using set_type = std::set<size_t>;

using graph_t = std::vector<std::vector<size_t> >;

graph_t graph;

size_t thread_number;
std::string input_file_name;
std::string output_file_name;

int N;
std::unordered_map<size_t, size_t> representation;
std::vector<size_t> node_ids;

size_t represent(size_t node) {
	size_t result = representation[node] = N++;
	node_ids.push_back(node);
	return result;
}

void read_graph() {
    std::ifstream input;
    input.open(input_file_name, std::ios_base::in);

	std::list<std::pair<size_t, size_t> > edges;
    size_t a, b, prev_a, repr;
    while (input >> a >> b) {
		if ((!edges.empty() && prev_a != a) || edges.empty())  {
			repr = represent(a);
		}
		prev_a = a;
		edges.push_back(std::make_pair(repr, b));
    }
	graph.resize(N);

	size_t b_repr;
	for(const auto & edge : edges) {
		auto query = representation.find(edge.second);
		if(query == representation.end()) {
			b_repr = represent(edge.second);
		} else {
			b_repr = query->second;
		}
		graph[edge.first].push_back(b_repr);
	}
}

void calculate(size_t s, bc_t & BC) {
	logger::print("Calculating for " + std::to_string(s));
    std::stack<size_t > stack;
    std::queue<size_t > queue;
	
    std::vector<std::list<size_t> > P(N, std::list<size_t>());
    std::vector<double> delta(N, 0.0);
    std::vector<int> d(N, -1), sigma(N, 0);

    sigma[s] = 1;
    d[s] = 0;
    queue.push(s);

    while (!queue.empty()) {
        auto v = queue.front();
        queue.pop();

        stack.push(v);

        if(v < graph.size()) {
            for(auto w : graph[v]) {
                if(d[w] < 0) {
                    queue.push(w);
                    d[w] = d[v] + 1;
                }
                if (d[w] == d[v] + 1) {
                    sigma[w] += sigma[v];
                    P[w].push_back(v);
                }
            }
        }
    }

    while (!stack.empty()) {
        auto w = stack.top();
        stack.pop();

        for(auto v : P[w]) {
            delta[v] += ((double)sigma[v] / sigma[w]) * (1 + delta[w]);
        }

        // Increase BC only if node has descendants.
        if (w != s && delta[w] != 0.0 && w < graph.size()) {
            auto current = BC[w].load();
            while (!BC[w].compare_exchange_weak(current, current + delta[w]));
        }
    }
}

void run(bc_t & BC) {
    std::vector<std::thread> threads(thread_number);
    for(size_t mod = 0; mod<thread_number; mod++) {
        threads[mod] = std::thread{[mod, & BC] {
				for(size_t i = mod; i < graph.size(); i += thread_number) {
					calculate(i, BC);
				}
			}
		};
    }

    for(auto & thread : threads) {
        thread.join();
    }
}

void save(bc_t & BC) {
    std::fstream output_file;
    output_file.open(output_file_name, std::ios_base::out);

	using pair_t = std::pair<size_t, double >;
    std::priority_queue<pair_t, std::vector<pair_t>, std::greater<pair_t> > result;

    for(size_t i=0; i<BC.size(); i++) {
        result.push(std::make_pair(node_ids[i], BC[i].load()));
    }

    while(!result.empty()) {
        auto top = result.top();
        result.pop();
        output_file << top.first << " " << top.second << std::endl;
   }

    output_file.close();
}

int main(int argc, char* args[]) {
	N = 0;
	
    logger::print_open("Program started");
    thread_number = std::stoi(args[1]);
    input_file_name = args[2];
    output_file_name = args[3];
    logger::print_end("Input read successfully");

    logger::print_open("read_graph started");
    read_graph();
    logger::print_end("read_graph finished");

	logger::print_open("Calculation start");
    bc_t BC(graph.size());
    for(size_t i = 0; i<BC.size(); i++) {
        BC[i] = 0.0;
    }
	logger::print_end("Calculation end");
	
	run(BC);
	save(BC);

    return 0;
}
