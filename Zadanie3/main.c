#include <stdio.h>
#include "tree.h"

int N, K, V;

int main(int argc, char* argv[]) {
    (void)argc;
    (void)argv;

    scanf("%d %d %d\n", &N, &K, &V);
    const tree_ptr const tree = parse_tree(K, V);
    print_tree(tree);
    return 0;
}

