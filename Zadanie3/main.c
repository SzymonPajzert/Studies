#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "tree.h"
#include "parse.h"

int N, K, V;

typedef struct {
    int from_init;
    int calc_num;
    long result;
} calculation;

FILE *logging;
int node_id = 0;
int parent_id;

tree_ptr * vertices;
int * variables;

int ** child_input;

/** Create file descriptors
 *
 * @param n number of file descriptor
 * @return
 */
int ** create_descriptors(int n) {
    int ** array = malloc(sizeof(int*) * n);
    for(int i=0; i<n; i++) {
        array[i] = malloc(sizeof(int) * 2);
        pipe(array[i]);
    }
    return array;
}

void close_descriptors(int ** array, int n) {
    for(int i=0; i<n; i++) {
        free(array[i]);
    }
    free(array);
}

/// Starts logging to file ./logging/thread_<<thread_number>>
void start_logging() {
    char filename[100];
    sprintf(filename, "./logging/thread_%d", node_id);
    logging = fopen(filename, "w");
    fprintf(logging, "Hello I'm %d thread\n", node_id);
}

// --------------------------- parent ---------------------------

void send_line(int line_number, const char * line) {
    int * _line_number_ptr = parse_number(&line);
    int _line_number = *(_line_number_ptr);
    free(_line_number_ptr);
    assert(line_number == _line_number);
    calculation input;
    input.calc_num = line_number;

    input.from_init = 1;
    int * variable_ptr = NULL;
    int * value_ptr = NULL;
    do {
        variable_ptr = parse_variable(&line);
        if(variable_ptr) {
            value_ptr = parse_number(&line);
            input.result = *value_ptr;
            fprintf(logging, "Sending to %d value calc_num: %d of value %ld\n", variables[*variable_ptr], input.calc_num, input.result);
            write(child_input[variables[*variable_ptr]][1], &input, sizeof(calculation));
            free(variable_ptr);
            free(value_ptr);
        }
    } while(variable_ptr);
}

void parent_start_close_descriptors() {
    for(int i=0; i<parent_id; i++) {
        fprintf(logging, "Closing %d %d\n", i, 0);
        close(child_input[i][0]);
    }
}

/// Starts parent process - feeding data to children.
void start_parent() {
    parent_start_close_descriptors();

    for(int line_number = K+1; line_number <= N; line_number++) {
        char * line = NULL;
        size_t size = 0;
        getline(&line, &size, stdin);
        fprintf(logging, "Read %d line number\n", line_number);
        send_line(line_number, line);
        free(line);
    }

    calculation end_of_transmission;
    end_of_transmission.from_init = 1;
    end_of_transmission.calc_num = N+1;
    end_of_transmission.result = -69; // TODO remove
    for(int i=0; i<parent_id; i++) {
        write(child_input[i][1], &end_of_transmission, sizeof(calculation));
        close(child_input[i][1]);
    }
}


// --------------------------- children ------------------------------------

void broadcast_parents(calculation input) {
    fprintf(logging, "Broadcasting started\n");
    tree_ptr node = vertices[node_id];
    for(int p = 0; p < node->parent_number; p++) {
        fprintf(logging, "Sending to %d value calc_num: %d of value %ld\n", node->parents[p], input.calc_num, input.result);
        write(child_input[node->parents[p]][1], &input, sizeof(calculation));
    }
}

void start_number_child() {
    tree_ptr node = vertices[node_id];

    calculation input;
    input.from_init = 0;
    input.result = node->node.number->value;
    for(int l = K+1; l <= N; l++) {
        input.calc_num = l;
        broadcast_parents(input);
    }
}

void start_operation_child() {
    tree_ptr node = vertices[node_id];

    int results[N+1];
    int touched[N+1];           // Contains number of accesses to variable - let us know if it can be send or not.
    for(int i=0; i<=N; i++) {
        touched[i] = 0;
        switch (node->node.operation->operand) {
            case '+': results[i] = 0; break;
            case '*': results[i] = 1; break;
            default: exit(2);
        }
    }

    int my_input = child_input[node_id][0];

    calculation input;
    int read_result;
    while ((read_result = read(my_input, &input, sizeof(calculation)) > 0)) {
        fprintf(logging, "Read calc_num: %d of value %ld\n", input.calc_num, input.result);
        switch (node->node.operation->operand) {
            case '+': results[input.calc_num] += input.result; break;
            case '*': results[input.calc_num] *= input.result; break;
            default: exit(2);
        }

        if(++touched[input.calc_num] == 2) {
            input.from_init = 0;
            input.result = results[input.calc_num];
            broadcast_parents(input);
        }
    }
    if(read_result < 0) {
        fprintf(logging, "Error: %s", strerror(read_result));
    }
}

/// Sends awaiting results from variable
void send_awaiting(int last_from_main, int new_from_main, long ** child_results, int * sent) {
    for(int i=last_from_main+1; i<new_from_main; i++) {
        if(child_results[i]) {
            if(!sent[i]) {
                calculation result;
                result.from_init = 0;
                result.result = *(child_results[i]);
                result.calc_num = i;
                broadcast_parents(result);
            }
            free(child_results[i]);
            child_results[i]=NULL;
            sent[i] = 1;
        }
    }
}

/// Starts variable process
void start_variable_child() {
    int last_from_main = K-1; // Describes last line from initialization list.
    long** child_results = malloc(sizeof(long*) * (N+1)); // Contains values received from children
    int* sent = malloc(sizeof(int) * (N+1));

    for(int i=0; i<=N; i++) {
        child_results[i] = NULL;
        sent[i] = 0;
    }

    int my_input = child_input[node_id][0];

    calculation input;
    int read_result;
    while ((read_result = read(my_input, &input, sizeof(calculation)) > 0)) {
        fprintf(logging, "Read calc_num: %d of value %ld\n", input.calc_num, input.result);

        if(input.from_init) {
            send_awaiting(last_from_main, input.calc_num, child_results, sent);

            if(input.calc_num <= N) { // Normal message.
                input.from_init = 0;
                broadcast_parents(input);
            } // Otherwise it's end of message from parent.

            last_from_main = input.calc_num;
        } else {
            if(input.calc_num < last_from_main) {
                broadcast_parents(input);
            } else if(input.calc_num > last_from_main){
                child_results[input.calc_num] = malloc(sizeof(long));
                *(child_results[input.calc_num]) = input.result;
            }
        }
    }
    if(read_result < 0) {
        fprintf(logging, "Error: %s", strerror(read_result));
    }

    free(child_results);
    free(sent);
}

void child_start_close_descriptors() {
    tree_ptr tree = vertices[node_id];
    int p = tree->parent_number-1;
    for(int i=0; i<parent_id; i++) {
        // Closing reading pipes
        if(i != node_id) {
            fprintf(logging, "Closing %d %d\n", i, 0);
            close(child_input[i][0]);
        }

        // Closing writing pipes
        // We use fact that ids in parents are sorted in descending order
        if(p < 0 || tree->parents[p] != i) {
            fprintf(logging, "Closing %d %d\n", i, 1);
            close(child_input[i][1]);
        } else {
            p--;
        }
    }
}

void close_parent_descriptors() {
    tree_ptr tree = vertices[node_id];
    for(int i=0; i<tree->parent_number; i++) {
        fprintf(logging, "Closing %d %d\n", tree->parents[i], 1);
        close(child_input[tree->parents[i]][1]);
    }
}

/// Starts child process
void start_child() {
    fprintf(logging, "I'm a child\n");

    child_start_close_descriptors();
    int my_input = child_input[node_id][0];

    switch (vertices[node_id]->type) {
        case OPERATION:
            start_operation_child();
            break;
        case VARIABLE:
            start_variable_child();
            break;
        case NUMBER:
            start_number_child();
            break;
    }

    close(my_input);
    close_parent_descriptors();
    fprintf(logging, "Finished reading\n");
}

int main(int argc, char* argv[]) {
    (void)argc;
    (void)argv;

    scanf("%d %d %d\n", &N, &K, &V);
    const tree_ptr const tree = parse_tree(K, V);
    parent_id = tree->id + 1;

    vertices = malloc(sizeof(tree_ptr) * parent_id);
    get_vertices(tree, vertices);

    variables = malloc(sizeof(int) * V);
    get_variables(tree, variables);
    print_tree(tree);

    child_input = create_descriptors(parent_id);

    int am_groot = 1;
    while(node_id<parent_id && am_groot > 0) {
        if((am_groot = fork())) node_id++; // exit loop if you're child, otherwise increase counter
    }

    start_logging();

    if(node_id == parent_id) {
        start_parent();
    } else {
        start_child();
    }

    fprintf(logging, "-----------------\n\n");
    fclose(logging);
    remove_tree(tree);

    close_descriptors(child_input, parent_id);
    free(vertices);
    free(variables);
    return 0;
}

