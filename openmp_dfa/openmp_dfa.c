#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <omp.h>
#include <time.h>
#include <sys/time.h>

enum STATE {START = 0,  TOP = 1, BOTTOM = 2, RIGHT = 3, FINAL = 4}; 
enum STATE states[50000][5];
char* results[50000][5];
char dictionary[12] = {'0','1','2','3','4','5','6','7','8','9','.','a'};

int randomIndex(){
	return (int)12*((double)rand()/(double)RAND_MAX);
}

char* generateString(int size){
	char* string = (char*) malloc(sizeof(char)*size);
	int count = 0;
	while (count < size-1){
		// keeps selecting a random alphabet
		string[count] = dictionary[randomIndex()];
		count++;
	}
	string[size-1] = '\0';
	return string;
}

char* processChar(enum STATE a, char* b, char* c, int d, int e){
	// transition from start to top
	if (a == START && b[0] == '0'){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		strcpy(cc, c);
		cc[len] = b[0];
		cc[len+1] = '\0';
		// points to the new string
		c = cc;
		a = TOP;
		// points to next 
		b++;
		return processChar(a,b,c,d,e);
	}
	// transition from start to bottom
	else if (a == START && b[0] >= '1' && b[0] <= '9'){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		strcpy(cc, c);
		cc[len] = b[0];
		cc[len+1] = '\0';
		c = cc;
		a = BOTTOM;
		b++;
		return processChar(a,b,c,d,e);
	}
	// end of a portion of line
	else if (a == START && b[0] == '\0'){
		states[d][e] = a;
		return c;
	}
	// illegal expression
	else if (a == START){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		for (int i = 0; i < len+1; i++){
			cc[i] = ' ';
		}
		cc[len+1] = '\0';
		c = cc;
		a = START;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == TOP && b[0] == '.'){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		strcpy(cc, c);
		cc[len] = b[0];
		cc[len+1] = '\0';
		c = cc;
		a = RIGHT;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == TOP && b[0] == '\0'){
		states[d][e] = a;
		return c;
	}
	else if (a == TOP){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		for (int i = 0; i < len+1; i++){
			cc[i] = ' ';
		}
		cc[len+1] = '\0';
		c = cc;
		a = START;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == BOTTOM && b[0] == '.'){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		strcpy(cc, c);
		cc[len] = b[0];
		cc[len+1] = '\0';
		c = cc;
		a = RIGHT;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == BOTTOM && b[0] >= '0' && b[0] <= '9'){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		strcpy(cc, c);
		cc[len] = b[0];
		cc[len+1] = '\0';
		c = cc;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == BOTTOM && b[0] == '\0'){
		states[d][e] = a;
		return c;
	}
	else if (a == BOTTOM){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		for (int i = 0; i < len+1; i++){
			cc[i] = ' ';
		}
		cc[len+1] = '\0';
		c = cc;
		a = START;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == RIGHT && b[0] >= '0' && b[0] <= '9'){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+2);
		strcpy(cc, c);
		cc[len] = b[0];
		cc[len+1] = '\0';
		c = cc;
		a = FINAL;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == RIGHT && b[0] == '\0'){
		states[d][e] = a;
		return c;
	}
	else if (a == RIGHT){
		size_t len = strlen(c);
		char* cc = (char*) malloc(len+2);
		for (int i = 0; i < len+1; i++){
			cc[i] = ' ';
		}
		cc[len+1] = '\0';
		c = cc;
		a = START;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == FINAL && b[0] >= '0' && b[0] <= '9'){
		size_t len = strlen(c);
		char* cc = (char*) malloc(len+2);
		strcpy(cc, c);
		cc[len] = b[0];
		cc[len+1] = '\0';
		c = cc;
		b++;
		return processChar(a,b,c,d,e);
	}
	else if (a == FINAL && b[0] == '\0'){
		size_t len = strlen(c);
		char *cc = (char*) malloc(len+1);
		strcpy(cc, c);
		cc[len] = '\0';
		c = cc;
		states[d][e] = a;
		return c;
	}
	else if (a == FINAL){
		char* cc = (char*) malloc(2);
		cc[0] = ' ';
		a = START;
		b++;
		return strcat(c, processChar(a,b,cc,d,e));
	}
	return c;
}

int main(int argc, char *argv[]){
	int n = 0;
	if (argc > 1){
		n = atoi(argv[1]);
		printf("Using %d threads\n", n);
	}
	omp_set_num_threads(n+1);
	int size = 25000;
	char* sentence = generateString(size);
	char processed[n+1][5][50000];
	char substrings[n+1][50000];
	int part = size/(n+1)+1;
	if (size % (n+1) == 0){
		part = size/(n+1);
	}
	struct timeval start;
	struct timeval end;
	gettimeofday(&start, NULL);
	// parallel portion
#pragma omp parallel for
	for (int i = 0; i < size; i=i+part){
		if (i + part > size){
			for (int j = 0; j < size-i; j++){
				substrings[i/part][j] = sentence[i+j];
			}
			substrings[i/part][size-i] = '\0';
		}
		else{
			for (int j = 0; j < part; j++){
				substrings[i/part][j] = sentence[i+j];
			}
			substrings[i/part][part] = '\0';
		}
	}
	// parallel portion
#pragma omp parallel
	{
		int t = omp_get_thread_num();
		if (t == 0){
			processed[t][0][0] = '\0';
			results[t][0] = processChar((enum STATE)0, substrings[t], processed[t][0], t, 0);
		}
		else{
			processed[t][0][0] = '\0';
			processed[t][1][0] = '\0';
			processed[t][2][0] = '\0';
			processed[t][3][0] = '\0';
			processed[t][4][0] = '\0';
			results[t][0] = processChar((enum STATE)0, substrings[t], processed[t][0], t, 0);
			results[t][1] = processChar((enum STATE)1, substrings[t], processed[t][1], t, 1);
			results[t][2] = processChar((enum STATE)2, substrings[t], processed[t][2], t, 2);
			results[t][3] = processChar((enum STATE)3, substrings[t], processed[t][3], t, 3);
			results[t][4] = processChar((enum STATE)4, substrings[t], processed[t][4], t, 4);
		}
	}
	char* temp = (char*) malloc(sizeof(char*)*50000);
	strcpy(temp, results[0][0]);
	int state = 0;
	// removes all illegal tails
	for (int i = 0; i < n; i++){
		if (results[i+1][state][0] == ' '){
			for (int j = (int)(strlen(temp))-1; j >= 0; j--){
				if (temp[j] == ' '){
					for (int k = j; k < (int)(strlen(temp)); k++){
						temp[k] = ' ';
					}
					break;
				}
			}
		}
		strcat(temp, results[i+1][state]);
		state = (int)states[i+1][state];
		if (i == n-1 && state != 4){
			for (int j = strlen(temp)-1; j >= 0; j--){
				if (temp[j] == ' '){
					for (int k = j; k < (int)(strlen(temp)); k++){
						temp[k] = ' ';
					}
					break;
				}
			}
		}
	}
	printf("Feasible soluions: %s\n", temp);
	char *token;
	char *max;
	token = strtok(temp, " ");
	max = token;
	// finds the expression with max length
	while (token != NULL){
		if (strlen(token)>strlen(max)){
			max = token;
		}
		token = strtok(NULL, " ");
	}
	gettimeofday(&end, NULL);
	printf("Optimal solution: %s\n", max);
	printf("Time spent: %ld nanoseconds\n", ((1000000 * (end.tv_sec - start.tv_sec)) + end.tv_usec - start.tv_usec));
	return 0;
}
