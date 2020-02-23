#include "logger.h"

std::mutex _logger<true>::mutex;
std::string _logger<true>::spaces;