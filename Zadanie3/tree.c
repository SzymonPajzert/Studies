#include <malloc.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "tree.h"

// ---------------------------- FACTORY FUNCTIONS ----------------------------

tree_ptr alloc_tree() {
    tree_ptr result  = (tree_ptr) malloc(sizeof(struct tree));
    result->parents = NULL;
    result->parent_number = 0;
    result->id = -1;
    return result;
}

tree_ptr create_variable(int var_num, tree_ptr value) {
    var_ptr variable = (var_ptr) malloc(sizeof(struct variable));
    variable->value = value;
    variable->first_parent = 0;
    variable->var_num = var_num;
    tree_ptr result = alloc_tree();
    result->node.variable = variable;
    result->type = VARIABLE;
    return result;
}

tree_ptr create_number(int number) {
    num_ptr number_ptr = (num_ptr) malloc(sizeof(struct number));
    number_ptr->value = number;
    tree_ptr result = alloc_tree();
    result->node.number = number_ptr;
    result->type = NUMBER;
    return result;
}

tree_ptr create_operation(char operand, tree_ptr left, tree_ptr right) {
    if(left == NULL || right == NULL) {
        fprintf(stderr, "Creating operation with empty subtree: %s %s",
                left == NULL ? "left" : " ",
                right == NULL ? "right" : " ");
        return NULL;
    } else {
        op_ptr operation = (op_ptr) malloc(sizeof(struct operation));
        operation->left_first_parent = 0;
        operation->left = left;
        operation->right_first_parent = 0;
        operation->right = right;
        operation->operand = operand;
        tree_ptr result = alloc_tree();
        result->node.operation = operation;
        result->type = OPERATION;
        return result;
    }
}

// ---------------------------- DESTRUCTORS ----------------------------

void remove_tree(tree_ptr tree) {
    free(tree->parents);
    switch(tree->type) {
        case OPERATION:
            if(tree->node.operation->left_first_parent) remove_tree(tree->node.operation->left);
            if(tree->node.operation->right_first_parent) remove_tree(tree->node.operation->right);
            free(tree->node.operation);
            break;
        case VARIABLE:
            if(tree->node.variable->first_parent) remove_tree(tree->node.variable->value);
            free(tree->node.variable);
            break;
        case NUMBER:
            free(tree->node.number);
            break;
    }
    free(tree);
}

// ---------------------------- TRAVERSALS ----------------------------

/** Sets ids for elements of the tree.
 *
 * @param tree Tree to be labeled.
 * @param first_id First id to start from.
 * @return First non used value of id.
 */
int set_ids(tree_ptr tree, int first_id) {
    switch (tree->type) {
        case OPERATION:
            if(tree->node.operation->left->id == -1) tree->node.operation->left_first_parent = 1;
            first_id = set_ids(tree->node.operation->left, first_id);
            if(tree->node.operation->right->id == -1) tree->node.operation->right_first_parent = 1;
            first_id = set_ids(tree->node.operation->right, first_id);
            break;
        case VARIABLE:
            if(tree->node.variable->value) {
                if(tree->node.variable->value->id == -1) tree->node.variable->first_parent = 1;
                first_id = set_ids(tree->node.variable->value, first_id);
            }
            break;
        case NUMBER: break;
    }
    tree->id = first_id++;
    return first_id;
}

/// Notify children about their parents existence
void notify_children(tree_ptr tree) {
    switch (tree->type) {
        case OPERATION:
            tree->node.operation->left->parent_number++;
            tree->node.operation->right->parent_number++;
            notify_children(tree->node.operation->right);
            notify_children(tree->node.operation->left);
            break;
        case VARIABLE:
            if(tree->node.variable->value) {
                tree->node.variable->value->parent_number++;
                notify_children(tree->node.variable->value);
            }
            break;
        case NUMBER: break;
    }
}

/// allocate parent to sizeof(int) * parent_num, set parent_num to 0;
void allocate_parent_space(tree_ptr tree) {
    if(tree->parent_number > 0) {
        tree->parents = malloc(sizeof(int) * tree->parent_number);
    }
    tree->parent_number = 0;

    switch (tree->type) {
        case OPERATION:
            allocate_parent_space(tree->node.operation->right);
            allocate_parent_space(tree->node.operation->left);
            break;
        case VARIABLE:
            if(tree->node.variable->value) {
                allocate_parent_space(tree->node.variable->value);
            }
            break;
        case NUMBER: break;
    }
}

void set_parent(tree_ptr tree, int parent_id) {
    tree->parents[tree->parent_number++] = parent_id;
}

/// set your id to parent_num++ of child in parents
void insert_parents(tree_ptr tree) {
    switch (tree->type) {
        case OPERATION:
            set_parent(tree->node.operation->right, tree->id);
            set_parent(tree->node.operation->left, tree->id);
            insert_parents(tree->node.operation->right);
            insert_parents(tree->node.operation->left);
            break;
        case VARIABLE:
            if(tree->node.variable->value) {
                set_parent(tree->node.variable->value, tree->id);
                insert_parents(tree->node.variable->value);
            }
            break;
        case NUMBER: break;
    }
}

// ---------------------------- parsing ----------------------------

char peek(char **expr) {
    while(isspace(**expr)) (*expr)++;
    return **expr;
}

char get(char **expr) {
    char result = *((*expr)++);
    while(isspace(**expr)) (*expr)++;
    return result;
}

tree_ptr expression(char ** expr, tree_ptr variables[], int V);

/** Parse number
 *
 * @param expr Modifiable pointer to untouched part of the string.
 * @return Parsed number.
 */
int parse_number(char ** expr) {
    int result = get(expr) - '0';
    while (peek(expr) >= '0' && peek(expr) <= '9')
    {
        result = 10*result + get(expr) - '0';
    }
    return result;
}

/** Parse number
 *
 * @param expr Modifiable pointer to untouched part of the string.
 * @return Parsed tree or NULL in case of error.
 */
tree_ptr number(char ** expr)
{
    return create_number(parse_number(expr));
}

/** Parse variable
 *
 * @param expr Modifiable pointer to untouched part of the string.
 * @param variables Array of variable trees.
 * @param V Maximum number of nodes.
 * @return Parsed tree or NULL in case of error.
 */
tree_ptr variable(char ** expr, tree_ptr variables[], int V) {
    get(expr); // x
    get(expr); // [
    int var_num = parse_number(expr);
    get(expr); // ]
    return (var_num >= 0 && var_num < V) ? variables[var_num] : NULL;
}

/** Parse simple factor
 *
 * @param expr Modifiable pointer to untouched part of the string.
 * @param variables Array of variable trees.
 * @param V Maximum number of nodes.
 * @return Parsed tree or NULL in case of error.
 */
tree_ptr factor(char ** expr, tree_ptr variables[], int V)
{
    if (peek(expr) >= '0' && peek(expr) <= '9') {
        return number(expr);
    } else if (peek(expr) == 'x') {
        return variable(expr, variables, V);
    } else if (peek(expr) == '(') {
        get(expr); // '('
        tree_ptr result = expression(expr, variables, V);
        get(expr); // ')'
        return result;
    } else if (peek(expr) == '-') {
        get(expr);
        tree_ptr fact = factor(expr, variables, V);
        return fact != NULL ? create_operation('*', create_number(-1), fact) : NULL;
    }
    return NULL; // error
}

tree_ptr expression(char ** expr, tree_ptr variables[], int V)
{
    tree_ptr left = factor(expr, variables, V);
    tree_ptr result = NULL;
    if (peek(expr) == '+' || peek(expr) == '*') {
        char op = get(expr);
        tree_ptr right = factor(expr, variables, V);
        result = right != NULL ? create_operation(op, left, right) : NULL;
    } else {
        result = left;
    }
    return result;
}

typedef struct {
    int var_num;
    tree_ptr value;
} parse_result;

parse_result parse_line(const char * line, tree_ptr variables[], int V) {
    char * eq_sign = strchr(line, '=');
    char * expr = eq_sign+1;
    *eq_sign = '\0';

    tree_ptr right_hand_expr = expression(&expr, variables, V);

    *strchr(line, ']') = '\0';
    int var_num = atoi(strchr(line, '[')+1);

    parse_result result;
    result.value = right_hand_expr;
    result.var_num = var_num;
    return result;
}

int transform(const char * line, tree_ptr variables[], int V) {
    parse_result result = parse_line(line, variables, V);
    if(variables[result.var_num]->node.variable->value) {
        return 0;
    } else {
        variables[result.var_num]->node.variable->value = result.value;
        return 1;
    }
}

tree_ptr parse_tree(int lines, int V) {
    tree_ptr variables[V];
    for(int i=0; i<V; i++) {
        variables[i] = create_variable(i, NULL);
    }
    tree_ptr result = variables[0];

    for(int i=1; i<=lines; i++) {
        char * line = NULL;
        size_t size;
        getline(&line, &size, stdin);
        char * equation = strchr(line, ' ') + 1;
        if(transform(equation, variables, V)) {
            printf("%d P\n", i);
        } else {
            printf("%d F\n", i);
            free(line);
            remove_tree(result);
            exit(1);
        }
        print_tree(result);
        free(line);
    }

    set_ids(result, 0);
    notify_children(result);
    allocate_parent_space(result);
    insert_parents(result);
    return result;
}

// ---------------------- printing ----------------------

const int indentation_step = 2;
void helper_print_tree(tree_ptr tree, int indentation) {
    // Indentation based on:
    // http://stackoverflow.com/questions/25609437/print-number-of-spaces-using-printf-in-c
    printf("%*s", indentation, "");
    printf("node_id: %d\n", tree->id);

    printf("%*s", indentation, "");
    printf("with %d parents: ", tree->parent_number);
    for(int i=0; i<tree->parent_number; i++) {
        printf("%d, ", tree->parents[i]);
    }
    printf("\n");

    printf("%*s", indentation, "");
    switch (tree->type) {
        case OPERATION:
            printf("as operand: %c\n\n", tree->node.operation->operand);
            helper_print_tree(tree->node.operation->left, indentation+indentation_step);
            helper_print_tree(tree->node.operation->right, indentation+indentation_step);
            break;
        case VARIABLE:
            printf("as variable: x[%d]\n\n", tree->node.variable->var_num);
            if(tree->node.variable->value) {
                helper_print_tree(tree->node.variable->value, indentation+indentation_step);
            }
            break;
        case NUMBER:
            printf("as number: %ld\n\n", tree->node.number->value);
            break;
    }
}

void print_tree(tree_ptr tree) {
    helper_print_tree(tree, 0);
}