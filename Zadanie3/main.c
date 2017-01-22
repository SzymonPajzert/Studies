#include <stdio.h>
#include "tree.h"

int N, K, V;

int main(int argc, char* argv[]) {
    scanf("%d%d%d", &N, &K, &V);
    tree_ptr tree = parse_tree(K, V);
    print_tree(tree);
    return 0;
}

