#include <iostream>
#include <fstream>
#include <locale>
#include <string>
#include <list>
#include <codecvt>
#include <numeric>
#include <vector>
#include <thread>

int grep(std::string filename, std::wstring word) {
    std::locale loc("pl_PL.UTF-8");
    std::wfstream file(filename);
    file.imbue(loc);
    std::wstring line;
    unsigned int count = 0;
    while (getline(file, line)) {
        for (auto pos = line.find(word,0);
             pos != std::string::npos;
             pos = line.find(word, pos+1))
            count++;
    }
    return count;
}

int main() {
    std::ios::sync_with_stdio(false);
    std::locale loc("pl_PL.UTF-8");
    std::wcout.imbue(loc);
    std::wcin.imbue(loc);

    std::wstring word;
    std::getline(std::wcin, word);

    std::wstring s_file_count;
    std::getline(std::wcin, s_file_count);
    int file_count = std::stoi(s_file_count);

    const int thread_num = 5;

    std::vector<std::list<std::string> > filenames{thread_num};
    
    std::wstring_convert<std::codecvt_utf8<wchar_t>, wchar_t> converter;

    for (int file_num = 0; file_num < file_count; file_num++) {
        std::wstring w_filename;
        std::getline(std::wcin, w_filename);
        std::string s_filename = converter.to_bytes(w_filename);
        filenames[file_num % thread_num].push_back(s_filename);
    }

    std::vector<int> results{thread_num};
    std::vector<std::thread> threads{thread_num};
    for(int i=0; i<thread_num; i++) {
        threads[i] = std::thread { [i, filenames, &results[i]]{
                int count = 0;
                for (auto filename : filenames[i]) {
                    count += grep(filename, word);
                }
                results[i] = count;
            }
        };
    }
    
    for(int i=0; i<thread_num; i++) {
        threads[i].join();    
    }

    int count = accumulate(results.begin(), results.end(), 0);
    std::wcout << count << std::endl;
}
